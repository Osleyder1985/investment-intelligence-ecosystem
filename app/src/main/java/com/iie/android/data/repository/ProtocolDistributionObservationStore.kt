package com.iie.android.data.repository

import androidx.room.withTransaction
import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.domain.valuation.VestingWithdrawalCandidate
import com.iie.android.domain.valuation.VerifiedBatchDistributionCandidate
import java.security.MessageDigest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Persists vesting and batch transfer candidates as immutable quarantined evidence. */
class ProtocolDistributionObservationStore(
    private val database: IIEDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun persistVesting(assetId: String, evidence: VestingWithdrawalCandidate): ProtocolDistributionStoreResult {
        if (assetId.isBlank()) return ProtocolDistributionStoreResult.Abstained(listOf("ASSET_ID_MISSING"))
        if (evidence.chainId != 1L || !evidence.vestingContractAddress.matches(ADDRESS) ||
            !evidence.recipientAddress.matches(ADDRESS) || !evidence.transactionHash.matches(HASH) ||
            !evidence.blockHash.matches(HASH) || evidence.blockNumber < 0L ||
            evidence.rawAmount.toBigIntegerOrNull()?.signum() != 1 || evidence.quartersPaid <= 0L ||
            evidence.withdrawalLogIndex < 0L || evidence.transferLogIndex < 0L
        ) return ProtocolDistributionStoreResult.Abstained(listOf("VESTING_EVIDENCE_INVALID"))
        val payload = buildJsonObject {
            put("schema", "iie.protocol-vesting-withdrawal.v1")
            put("protocolId", evidence.protocolId)
            put("assetId", assetId)
            put("chainId", evidence.chainId)
            put("vestingContractAddress", evidence.vestingContractAddress)
            put("recipientAddress", evidence.recipientAddress)
            put("tokenContract", evidence.tokenContract)
            put("rawAmount", evidence.rawAmount)
            put("quartersPaid", evidence.quartersPaid)
            put("transactionHash", evidence.transactionHash)
            put("blockNumber", evidence.blockNumber)
            put("blockHash", evidence.blockHash)
            put("blockTimestampEpochMs", evidence.blockTimestampEpochMs)
            put("withdrawalLogIndex", evidence.withdrawalLogIndex)
            put("transferLogIndex", evidence.transferLogIndex)
            put("evidenceStatus", evidence.evidenceStatus)
            put("abiEvidenceUrl", "https://etherscan.io/address/0xca046a83edb78f74ae338bb5a291bf6fdac9e1d2#code")
        }.toString()
        val identity = assetId + "|" + SOURCE_ID + "|VESTING|" + evidence.transactionHash + "|" + evidence.withdrawalLogIndex + "|" + evidence.transferLogIndex
        return persist(assetId, evidence.chainId, evidence.tokenContract, VESTING_OBSERVATION_TYPE,
            evidence.blockTimestampEpochMs, identity, payload)
    }

    suspend fun persistBatch(evidence: VerifiedBatchDistributionCandidate): ProtocolDistributionStoreResult {
        if (evidence.assetId.isBlank() || evidence.chainId != 1L ||
            !evidence.distributorAddress.matches(ADDRESS) || !evidence.transactionHash.matches(HASH) ||
            !evidence.blockHash.matches(HASH) || evidence.blockNumber < 0L ||
            evidence.payments.size < 2 ||
            evidence.payments.any {
                !it.beneficiaryAddress.matches(ADDRESS) ||
                    it.rawAmount.toBigIntegerOrNull()?.signum() != 1 || it.transferLogIndex < 0L
            } ||
            evidence.payments.map { it.beneficiaryAddress.lowercase() }.distinct().size != evidence.payments.size
        ) return ProtocolDistributionStoreResult.Abstained(listOf("BATCH_EVIDENCE_INVALID"))
        val payload = buildJsonObject {
            put("schema", "iie.protocol-batch-distribution.v1")
            put("protocolId", evidence.protocolId)
            put("assetId", evidence.assetId)
            put("chainId", evidence.chainId)
            put("distributorAddress", evidence.distributorAddress)
            put("tokenContract", evidence.tokenContract)
            put("transactionHash", evidence.transactionHash)
            put("blockNumber", evidence.blockNumber)
            put("blockHash", evidence.blockHash)
            put("blockTimestampEpochMs", evidence.blockTimestampEpochMs)
            put("payments", kotlinx.serialization.json.buildJsonArray {
                evidence.payments.forEach { payment ->
                    add(buildJsonObject {
                        put("beneficiaryAddress", payment.beneficiaryAddress)
                        put("rawAmount", payment.rawAmount)
                        put("transferLogIndex", payment.transferLogIndex)
                    })
                }
            })
            put("evidenceStatus", evidence.evidenceStatus)
        }.toString()
        val logIndexes = evidence.payments.joinToString(",") { it.transferLogIndex.toString() }
        val identity = SOURCE_ID + "|BATCH|" + evidence.chainId + "|" + evidence.transactionHash + "|" + logIndexes
        return persist(evidence.assetId, evidence.chainId, evidence.tokenContract, BATCH_OBSERVATION_TYPE,
            evidence.blockTimestampEpochMs, identity, payload)
    }

    private suspend fun persist(
        assetId: String, chainId: Long, tokenContract: String, observationType: String,
        observedAtEpochMs: Long, identity: String, payload: String
    ): ProtocolDistributionStoreResult {
        val now = clock()
        if (now <= 0L || observedAtEpochMs <= 0L || observedAtEpochMs > now ||
            chainId <= 0L || !tokenContract.matches(ADDRESS)
        ) return ProtocolDistributionStoreResult.Abstained(listOf("DISTRIBUTION_OBSERVATION_INVALID"))
        return try {
            database.withTransaction {
                if (database.assetDao().getAsset(assetId) == null) {
                    return@withTransaction ProtocolDistributionStoreResult.Abstained(listOf("CANONICAL_ASSET_NOT_FOUND"))
                }
                val deployment = database.assetDeploymentDao().getDeploymentForAssetAndContract(assetId, tokenContract)
                    ?: return@withTransaction ProtocolDistributionStoreResult.Abstained(
                        listOf("DISTRIBUTION_TOKEN_NOT_REGISTERED_FOR_CANONICAL_ASSET")
                    )
                val network = database.networkDao().getNetwork(deployment.networkId)
                    ?: return@withTransaction ProtocolDistributionStoreResult.Abstained(
                        listOf("DISTRIBUTION_TOKEN_NETWORK_NOT_REGISTERED")
                    )
                if (network.chainId != chainId.toString() || !deployment.reference.equals(tokenContract, true)) {
                    return@withTransaction ProtocolDistributionStoreResult.Abstained(
                        listOf("DISTRIBUTION_TOKEN_DEPLOYMENT_MISMATCH")
                    )
                }
                val sourceDao = database.dataSourceDao()
                if (sourceDao.getSource(SOURCE_ID) == null) {
                    sourceDao.insertSource(
                        DataSourceEntity(
                            sourceId = SOURCE_ID, sourceKey = SOURCE_ID,
                            displayName = "Uniswap UNI Vesting and Distribution Evidence",
                            providerType = "ONCHAIN_PROTOCOL_DISTRIBUTION",
                            homepageUrl = "https://uniswap.org/", termsUrl = null,
                            attributionText = "On-chain evidence from verified Uniswap UNI contracts",
                            permittedUseStatus = "REVIEW_REQUIRED", registeredAtEpochMs = now
                        )
                    )
                }
                val row = AssetObservationEntity(
                    observationId = sha256(identity), assetId = assetId, deploymentId = deployment.deploymentId,
                    sourceId = SOURCE_ID, observationType = observationType, observedAtEpochMs = observedAtEpochMs,
                    retrievedAtEpochMs = now, status = "QUARANTINED", quality = "UNVERIFIED",
                    valueSchemaVersion = 1, valueJson = payload, payloadSha256 = sha256(payload),
                    licenseReference = null
                )
                val inserted = database.assetObservationDao().insertHistoricalObservationsIfAbsent(listOf(row)).single()
                ProtocolDistributionStoreResult.Persisted(
                    inserted = if (inserted == -1L) 0 else 1,
                    duplicates = if (inserted == -1L) 1 else 0,
                    sourceUseStatus = "REVIEW_REQUIRED"
                )
            }
        } catch (error: Exception) {
            ProtocolDistributionStoreResult.Abstained(
                listOf("PERSISTENCE_FAILED:" + error.javaClass.simpleName)
            )
        }
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    companion object {
        const val SOURCE_ID = "uniswap-uni-vesting-batch-evidence"
        const val VESTING_OBSERVATION_TYPE = "ONCHAIN_VESTING_WITHDRAWAL"
        const val BATCH_OBSERVATION_TYPE = "ONCHAIN_BATCH_DISTRIBUTION_CANDIDATE"
        private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")
        private val HASH = Regex("0x[a-fA-F0-9]{64}")
    }
}

sealed class ProtocolDistributionStoreResult {
    data class Persisted(val inserted: Int, val duplicates: Int, val sourceUseStatus: String) : ProtocolDistributionStoreResult()
    data class Abstained(val blockers: List<String>) : ProtocolDistributionStoreResult()
}
