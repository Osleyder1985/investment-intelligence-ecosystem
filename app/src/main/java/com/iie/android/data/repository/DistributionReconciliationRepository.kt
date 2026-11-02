package com.iie.android.data.repository

import androidx.room.withTransaction
import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.domain.valuation.DistributionMovementKind
import com.iie.android.domain.valuation.DistributionReconciliationEngine
import com.iie.android.domain.valuation.DistributionReconciliationInput
import com.iie.android.domain.valuation.DistributionReconciliationReport
import com.iie.android.domain.valuation.DistributionReconciliationResult
import java.security.MessageDigest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.put

/**
 * Room adapter for the reconciliation engine.
 *
 * Only observations explicitly promoted to ACCEPTED/VERIFIED and whose source
 * is APPROVED may enter the calculation. Decoder candidates remain quarantined
 * and therefore cannot silently become financial inputs.
 */
class DistributionReconciliationRepository(
    private val database: IIEDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) {
    /**
     * Builds movement inputs directly from Room observations that have already passed
     * the explicit review gate. Quarantined candidates in the requested deployment/window
     * cause abstention rather than being silently treated as complete coverage.
     */
    suspend fun reconcileFromReviewedObservations(
        assetId: String,
        deploymentId: String,
        windowStartEpochMs: Long,
        windowEndEpochMs: Long,
        circulatingSupplyRaw: String,
        sellThrough: com.iie.android.domain.valuation.SellThroughAssumption,
        topBeneficiaryLimit: Int = 5
    ): DistributionReconciliationPersistenceResult {
        if (assetId.isBlank() || deploymentId.isBlank() || windowStartEpochMs <= 0L || windowEndEpochMs < windowStartEpochMs) {
            return DistributionReconciliationPersistenceResult.Abstained(listOf("REVIEWED_RECONCILIATION_INPUT_INVALID"))
        }
        return try {
            val relevantTypes = setOf(CLAIM_OBSERVATION_TYPE, VESTING_OBSERVATION_TYPE, BATCH_OBSERVATION_TYPE)
            val rows = database.assetObservationDao().observeForAsset(assetId).first()
                .filter { row -> row.deploymentId == deploymentId && row.observationType in relevantTypes &&
                    (row.observedAtEpochMs ?: 0L) in windowStartEpochMs..windowEndEpochMs }
                .sortedWith(compareBy<AssetObservationEntity> { it.observedAtEpochMs }.thenBy { it.observationId })
            if (rows.isEmpty()) return DistributionReconciliationPersistenceResult.Abstained(listOf("NO_DISTRIBUTION_OBSERVATIONS_IN_WINDOW"))
            val movements = mutableListOf<com.iie.android.domain.valuation.ObservedDistributionMovement>()
            val blockers = mutableListOf<String>()
            rows.forEach { row ->
                val source = database.dataSourceDao().getSource(row.sourceId)
                if (row.status != "ACCEPTED" || row.quality != "VERIFIED" || source?.permittedUseStatus != "APPROVED") {
                    blockers += "DISTRIBUTION_OBSERVATION_NOT_REVIEWED:" + row.observationId
                    return@forEach
                }
                if (row.payloadSha256.isNullOrBlank() || !row.payloadSha256.equals(sha256(row.valueJson), ignoreCase = true)) {
                    blockers += "DISTRIBUTION_OBSERVATION_HASH_MISMATCH:" + row.observationId
                    return@forEach
                }
                try {
                    val payload = Json.parseToJsonElement(row.valueJson).jsonObject
                    val payloadAssetId = payload["assetId"]?.jsonPrimitive?.content
                    val transactionHash = payload["transactionHash"]?.jsonPrimitive?.content
                    val timestamp = payload["blockTimestampEpochMs"]?.jsonPrimitive?.content?.toLongOrNull()
                    if (payloadAssetId != assetId || transactionHash.isNullOrBlank() || timestamp != row.observedAtEpochMs) {
                        blockers += "DISTRIBUTION_OBSERVATION_PAYLOAD_IDENTITY_MISMATCH:" + row.observationId
                        return@forEach
                    }
                    when (row.observationType) {
                        CLAIM_OBSERVATION_TYPE, VESTING_OBSERVATION_TYPE -> {
                            val beneficiary = if (row.observationType == CLAIM_OBSERVATION_TYPE) payload["beneficiaryAddress"]?.jsonPrimitive?.content
                                else payload["recipientAddress"]?.jsonPrimitive?.content
                            val amount = payload["rawAmount"]?.jsonPrimitive?.content
                            if (beneficiary.isNullOrBlank() || amount.isNullOrBlank()) {
                                blockers += "DISTRIBUTION_OBSERVATION_FIELDS_MISSING:" + row.observationId
                            } else {
                                movements += com.iie.android.domain.valuation.ObservedDistributionMovement(
                                    movementId = "movement:" + row.observationId, beneficiary = beneficiary,
                                    observedAtEpochMs = timestamp, rawAmount = amount,
                                    kind = DistributionMovementKind.CLAIMED_AND_TRANSFERRED,
                                    evidenceId = row.observationId, transactionHash = transactionHash, verified = true
                                )
                            }
                        }
                        BATCH_OBSERVATION_TYPE -> {
                            val payments = payload["payments"]?.jsonArray
                            if (payments == null || payments.isEmpty()) {
                                blockers += "BATCH_DISTRIBUTION_PAYMENTS_MISSING:" + row.observationId
                            } else payments.forEachIndexed { index, element ->
                                val payment = element.jsonObject
                                val beneficiary = payment["beneficiaryAddress"]?.jsonPrimitive?.content
                                val amount = payment["rawAmount"]?.jsonPrimitive?.content
                                val logIndex = payment["transferLogIndex"]?.jsonPrimitive?.content
                                if (beneficiary.isNullOrBlank() || amount.isNullOrBlank() || logIndex.isNullOrBlank()) {
                                    blockers += "BATCH_DISTRIBUTION_PAYMENT_INVALID:" + row.observationId + ":" + index
                                } else {
                                    movements += com.iie.android.domain.valuation.ObservedDistributionMovement(
                                        movementId = "movement:" + row.observationId + ":" + logIndex,
                                        beneficiary = beneficiary, observedAtEpochMs = timestamp, rawAmount = amount,
                                        kind = DistributionMovementKind.TRANSFERRED, evidenceId = row.observationId,
                                        transactionHash = transactionHash, verified = true
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) { blockers += "DISTRIBUTION_OBSERVATION_PAYLOAD_UNREADABLE:" + row.observationId }
            }
            if (blockers.isNotEmpty()) return DistributionReconciliationPersistenceResult.Abstained(blockers.distinct())
            if (movements.isEmpty()) return DistributionReconciliationPersistenceResult.Abstained(listOf("NO_REVIEWED_DISTRIBUTION_MOVEMENTS_IN_WINDOW"))
            reconcileAndPersist(assetId, DistributionReconciliationInput(
                windowStartEpochMs = windowStartEpochMs, windowEndEpochMs = windowEndEpochMs,
                circulatingSupplyRaw = circulatingSupplyRaw, schedules = emptyList(), movements = movements,
                sellThrough = sellThrough, topBeneficiaryLimit = topBeneficiaryLimit
            ))
        } catch (error: Exception) {
            DistributionReconciliationPersistenceResult.Abstained(listOf("REVIEWED_EVIDENCE_LOAD_FAILED:" + error.javaClass.simpleName))
        }
    }
    suspend fun reconcileAndPersist(
        assetId: String,
        input: DistributionReconciliationInput
    ): DistributionReconciliationPersistenceResult {
        if (assetId.isBlank()) {
            return DistributionReconciliationPersistenceResult.Abstained(listOf("ASSET_ID_MISSING"))
        }
        if (input.schedules.isNotEmpty()) {
            return DistributionReconciliationPersistenceResult.Abstained(
                listOf("REVIEWED_SCHEDULE_OBSERVATIONS_NOT_YET_SUPPORTED")
            )
        }
        if (input.movements.isEmpty()) {
            return DistributionReconciliationPersistenceResult.Abstained(listOf("DISTRIBUTION_EVIDENCE_EMPTY"))
        }

        return try {
            database.withTransaction {
                val deploymentIds = mutableSetOf<String>()
                val evidenceRows = mutableMapOf<String, AssetObservationEntity>()
                val blockers = mutableListOf<String>()

                input.movements.forEach { movement ->
                    val row = database.assetObservationDao().getObservationById(movement.evidenceId)
                    if (row == null) {
                        blockers += "REVIEWED_EVIDENCE_NOT_FOUND:${movement.evidenceId}"
                        return@forEach
                    }
                    evidenceRows[movement.evidenceId] = row
                    if (row.assetId != assetId) blockers += "EVIDENCE_ASSET_MISMATCH:${movement.evidenceId}"
                    if (row.status != "ACCEPTED" || row.quality != "VERIFIED") {
                        blockers += "EVIDENCE_NOT_ACCEPTED_AND_VERIFIED:${movement.evidenceId}"
                    }
                    val source = database.dataSourceDao().getSource(row.sourceId)
                    if (source?.permittedUseStatus != "APPROVED") {
                        blockers += "EVIDENCE_SOURCE_NOT_APPROVED:${movement.evidenceId}"
                    }
                    if (row.payloadSha256.isNullOrBlank() ||
                        !row.payloadSha256.equals(sha256(row.valueJson), ignoreCase = true)
                    ) blockers += "EVIDENCE_PAYLOAD_HASH_MISMATCH:${movement.evidenceId}"
                    if (row.deploymentId.isNullOrBlank()) {
                        blockers += "EVIDENCE_DEPLOYMENT_MISSING:${movement.evidenceId}"
                    } else {
                        deploymentIds += row.deploymentId
                    }
                    blockers += validateMovementAgainstPayload(movement, row)
                }

                if (deploymentIds.size != 1) blockers += "EVIDENCE_DEPLOYMENT_NOT_UNIQUE"
                if (blockers.isNotEmpty()) {
                    return@withTransaction DistributionReconciliationPersistenceResult.Abstained(blockers.distinct())
                }

                val engineResult = DistributionReconciliationEngine.reconcile(input)
                if (engineResult is DistributionReconciliationResult.Abstained) {
                    return@withTransaction DistributionReconciliationPersistenceResult.Abstained(engineResult.blockers)
                }
                val report = (engineResult as DistributionReconciliationResult.Ready).report
                val now = clock()
                if (now <= 0L || input.windowEndEpochMs > now) {
                    return@withTransaction DistributionReconciliationPersistenceResult.Abstained(
                        listOf("RECONCILIATION_CLOCK_INVALID")
                    )
                }

                val sourceDao = database.dataSourceDao()
                if (sourceDao.getSource(SOURCE_ID) == null) {
                    sourceDao.insertSource(
                        DataSourceEntity(
                            sourceId = SOURCE_ID,
                            sourceKey = SOURCE_ID,
                            displayName = "IIE Distribution Reconciliation Engine",
                            providerType = "INTERNAL_DERIVED_ANALYSIS",
                            homepageUrl = "https://github.com/Osleyder1985/investment-intelligence-ecosystem",
                            termsUrl = null,
                            attributionText = "Derived only from accepted, verified and approved on-chain observations",
                            permittedUseStatus = "INTERNAL_ONLY",
                            registeredAtEpochMs = now
                        )
                    )
                }

                val payload = report.toJson(assetId, deploymentIds.single(), input, evidenceRows.keys.toList()).toString()
                val identity = sha256(
                    "iie.distribution-reconciliation.v1|$assetId|${deploymentIds.single()}|" +
                        "${input.windowStartEpochMs}|${input.windowEndEpochMs}|$payload"
                )
                val row = AssetObservationEntity(
                    observationId = identity,
                    assetId = assetId,
                    deploymentId = deploymentIds.single(),
                    sourceId = SOURCE_ID,
                    observationType = OBSERVATION_TYPE,
                    observedAtEpochMs = input.windowEndEpochMs,
                    retrievedAtEpochMs = now,
                    status = "ACCEPTED",
                    quality = "DERIVED",
                    valueSchemaVersion = 1,
                    valueJson = payload,
                    payloadSha256 = sha256(payload),
                    licenseReference = null
                )
                val inserted = database.assetObservationDao()
                    .insertHistoricalObservationsIfAbsent(listOf(row)).single()
                DistributionReconciliationPersistenceResult.Persisted(
                    report = report,
                    observationId = identity,
                    inserted = inserted != -1L,
                    evidenceCount = evidenceRows.size
                )
            }
        } catch (error: Exception) {
            DistributionReconciliationPersistenceResult.Abstained(
                listOf("RECONCILIATION_PERSISTENCE_FAILED:${error.javaClass.simpleName}")
            )
        }
    }

    private fun validateMovementAgainstPayload(
        movement: com.iie.android.domain.valuation.ObservedDistributionMovement,
        row: AssetObservationEntity
    ): List<String> {
        val errors = mutableListOf<String>()
        try {
            val payload = Json.parseToJsonElement(row.valueJson).jsonObject
            fun field(name: String): String? = payload[name]?.jsonPrimitive?.content
            val transactionHash = field("transactionHash")
            val timestamp = field("blockTimestampEpochMs")?.toLongOrNull()
            val beneficiary = field("beneficiaryAddress") ?: field("recipientAddress")

            if (transactionHash == null || !transactionHash.equals(movement.transactionHash, true)) {
                errors += "MOVEMENT_TRANSACTION_MISMATCH:${movement.evidenceId}"
            }
            if (timestamp == null || timestamp != movement.observedAtEpochMs) {
                errors += "MOVEMENT_TIMESTAMP_MISMATCH:${movement.evidenceId}"
            }

            when (row.observationType) {
                CLAIM_OBSERVATION_TYPE, VESTING_OBSERVATION_TYPE -> {
                    if (movement.kind != DistributionMovementKind.CLAIMED_AND_TRANSFERRED) {
                        errors += "MOVEMENT_KIND_NOT_SUPPORTED_BY_CLAIM_EVIDENCE:${movement.evidenceId}"
                    }
                    if (field("rawAmount") != movement.rawAmount) {
                        errors += "MOVEMENT_AMOUNT_MISMATCH:${movement.evidenceId}"
                    }
                    if (beneficiary == null || !beneficiary.equals(movement.beneficiary, true)) {
                        errors += "MOVEMENT_BENEFICIARY_MISMATCH:${movement.evidenceId}"
                    }
                }
                BATCH_OBSERVATION_TYPE -> {
                    if (movement.kind != DistributionMovementKind.TRANSFERRED) {
                        errors += "BATCH_EVIDENCE_ONLY_SUPPORTS_TRANSFERRED_KIND:${movement.evidenceId}"
                    }
                    val payments = payload["payments"]?.jsonArray.orEmpty()
                    val matchingPayment = payments.any { item ->
                        val payment = item.jsonObject
                        payment["beneficiaryAddress"]?.jsonPrimitive?.content
                            ?.equals(movement.beneficiary, true) == true &&
                            payment["rawAmount"]?.jsonPrimitive?.content == movement.rawAmount
                    }
                    if (!matchingPayment) errors += "BATCH_PAYMENT_NOT_FOUND_IN_EVIDENCE:${movement.evidenceId}"
                }
                else -> errors += "OBSERVATION_TYPE_NOT_ADMISSIBLE_FOR_RECONCILIATION:${movement.evidenceId}"
            }
        } catch (_: Exception) {
            errors += "EVIDENCE_PAYLOAD_UNREADABLE:${movement.evidenceId}"
        }
        return errors
    }

    private fun DistributionReconciliationReport.toJson(
        assetId: String,
        deploymentId: String,
        input: DistributionReconciliationInput,
        reviewedEvidenceIds: List<String>
    ) = buildJsonObject {
        put("schema", "iie.distribution-reconciliation-report.v1")
        put("assetId", assetId)
        put("deploymentId", deploymentId)
        put("windowStartEpochMs", windowStartEpochMs)
        put("windowEndEpochMs", windowEndEpochMs)
        put("scheduledUnlockRaw", scheduledUnlockRaw)
        put("observedUnlockedRaw", observedUnlockedRaw)
        put("claimedRaw", claimedRaw)
        put("transferredToBeneficiariesRaw", transferredToBeneficiariesRaw)
        put("scheduleVsObservedClaimDeltaRaw", scheduleVsObservedClaimDeltaRaw)
        put("transferredAsCirculatingSupplyPercent", transferredAsCirculatingSupplyPercent.toPlainString())
        put("topBeneficiaries", buildJsonArray {
            topBeneficiaries.forEach { item ->
                add(buildJsonObject {
                    put("beneficiary", item.beneficiary)
                    put("rawTransferredAmount", item.rawTransferredAmount)
                    put("shareOfObservedTransfersPercent", item.shareOfObservedTransfersPercent.toPlainString())
                })
            }
        })
        put("topBeneficiariesSharePercent", topBeneficiariesSharePercent.toPlainString())
        put("estimatedSellPressureLowRaw", estimatedSellPressureLowRaw)
        put("estimatedSellPressureHighRaw", estimatedSellPressureHighRaw)
        put("estimatedSellPressureLowOfCirculatingPercent", estimatedSellPressureLowOfCirculatingPercent.toPlainString())
        put("estimatedSellPressureHighOfCirculatingPercent", estimatedSellPressureHighOfCirculatingPercent.toPlainString())
        put("sellThroughLowBasisPoints", sellThroughAssumption.lowBasisPoints)
        put("sellThroughHighBasisPoints", sellThroughAssumption.highBasisPoints)
        put("sellThroughRationale", sellThroughAssumption.rationale)
        put("sellThroughEvidenceId", sellThroughAssumption.evidenceId)
        put("circulatingSupplyRaw", input.circulatingSupplyRaw)
        put("reviewedEvidenceIds", buildJsonArray { reviewedEvidenceIds.sorted().forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
        put("evidenceIds", buildJsonArray { evidenceIds.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
        put("caveats", buildJsonArray { caveats.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    companion object {
        const val SOURCE_ID = "iie-distribution-reconciliation-engine"
        const val OBSERVATION_TYPE = "DISTRIBUTION_RECONCILIATION_REPORT"
        private const val CLAIM_OBSERVATION_TYPE = "ONCHAIN_DISTRIBUTION_CLAIM_PAYMENT"
        private const val VESTING_OBSERVATION_TYPE = "ONCHAIN_VESTING_WITHDRAWAL"
        private const val BATCH_OBSERVATION_TYPE = "ONCHAIN_BATCH_DISTRIBUTION_CANDIDATE"
    }
}

sealed class DistributionReconciliationPersistenceResult {
    data class Persisted(
        val report: DistributionReconciliationReport,
        val observationId: String,
        val inserted: Boolean,
        val evidenceCount: Int
    ) : DistributionReconciliationPersistenceResult()

    data class Abstained(val blockers: List<String>) : DistributionReconciliationPersistenceResult()
}
