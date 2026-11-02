package com.iie.android.data.repository

import androidx.room.withTransaction
import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.domain.valuation.DistributionClaimDecodeResult
import com.iie.android.domain.valuation.VerifiedDistributionClaimPaymentCandidate
import java.security.MessageDigest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Persists protocol-specific claim/payment evidence immutably and quarantined. */
class DistributionClaimObservationStore(
    private val database: IIEDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun persist(result: DistributionClaimDecodeResult): DistributionClaimStoreResult {
        val candidate = when (result) {
            is DistributionClaimDecodeResult.Candidate -> result.evidence
            is DistributionClaimDecodeResult.Abstained ->
                return DistributionClaimStoreResult.Abstained(result.reasonCodes)
        }
        val now = clock()
        if (now <= 0L || candidate.blockTimestampEpochMs <= 0L ||
            candidate.blockTimestampEpochMs > now
        ) return DistributionClaimStoreResult.Abstained(listOf("CLAIM_TIMESTAMP_INVALID"))
        if (candidate.assetId.isBlank() || candidate.protocolId.isBlank() ||
            candidate.chainId <= 0L || candidate.blockNumber < 0L ||
            candidate.claimEventLogIndex < 0L || candidate.paymentTransferLogIndex < 0L ||
            candidate.rawAmount.toBigIntegerOrNull()?.signum() != 1
        ) return DistributionClaimStoreResult.Abstained(listOf("CLAIM_EVIDENCE_INVALID"))

        return try {
            database.withTransaction {
                if (database.assetDao().getAsset(candidate.assetId) == null) {
                    return@withTransaction DistributionClaimStoreResult.Abstained(
                        listOf("CANONICAL_ASSET_NOT_FOUND")
                    )
                }
                val deployment = database.assetDeploymentDao()
                    .getDeploymentForAssetAndContract(candidate.assetId, candidate.tokenContract)
                    ?: return@withTransaction DistributionClaimStoreResult.Abstained(
                        listOf("DISTRIBUTED_TOKEN_NOT_REGISTERED_FOR_CANONICAL_ASSET")
                    )
                val network = database.networkDao().getNetwork(deployment.networkId)
                    ?: return@withTransaction DistributionClaimStoreResult.Abstained(
                        listOf("TOKEN_DEPLOYMENT_NETWORK_NOT_REGISTERED")
                    )
                if (network.chainId != candidate.chainId.toString() ||
                    !deployment.reference.equals(candidate.tokenContract, ignoreCase = true)
                ) return@withTransaction DistributionClaimStoreResult.Abstained(
                    listOf("DISTRIBUTION_TOKEN_DEPLOYMENT_MISMATCH")
                )

                val sourceDao = database.dataSourceDao()
                if (sourceDao.getSource(SOURCE_ID) == null) {
                    sourceDao.insertSource(
                        DataSourceEntity(
                            sourceId = SOURCE_ID,
                            sourceKey = SOURCE_ID,
                            displayName = "Uniswap UNI Merkle Distributor",
                            providerType = "ONCHAIN_PROTOCOL_DISTRIBUTION",
                            homepageUrl = "https://uniswap.org/",
                            termsUrl = null,
                            attributionText = "On-chain evidence: Uniswap UNI Merkle Distributor on Ethereum",
                            permittedUseStatus = "REVIEW_REQUIRED",
                            registeredAtEpochMs = now
                        )
                    )
                }
                val row = candidate.toEntity(deployment.deploymentId, now)
                val inserted = database.assetObservationDao()
                    .insertHistoricalObservationsIfAbsent(listOf(row)).single()
                DistributionClaimStoreResult.Persisted(
                    inserted = if (inserted == -1L) 0 else 1,
                    duplicates = if (inserted == -1L) 1 else 0,
                    sourceUseStatus = "REVIEW_REQUIRED"
                )
            }
        } catch (error: Exception) {
            DistributionClaimStoreResult.Abstained(
                listOf("PERSISTENCE_FAILED:${error.javaClass.simpleName}")
            )
        }
    }

    private fun VerifiedDistributionClaimPaymentCandidate.toEntity(
        deploymentId: String,
        retrievedAt: Long
    ): AssetObservationEntity {
        val payload = buildJsonObject {
            put("schema", "iie.protocol-distribution-claim-payment.v1")
            put("protocolId", protocolId)
            put("assetId", assetId)
            put("chainId", chainId)
            put("transactionHash", transactionHash)
            put("blockNumber", blockNumber)
            put("blockHash", blockHash)
            put("blockTimestampEpochMs", blockTimestampEpochMs)
            put("finalityStatus", finalityStatus)
            safeBlockNumber?.let { put("safeBlockNumber", it) }
            finalizedBlockNumber?.let { put("finalizedBlockNumber", it) }
            put("distributorAddress", distributorAddress)
            put("beneficiaryAddress", beneficiaryAddress)
            put("tokenContract", tokenContract)
            put("rawAmount", rawAmount)
            put("claimEventLogIndex", claimEventLogIndex)
            put("paymentTransferLogIndex", paymentTransferLogIndex)
            put("evidenceStatus", evidenceStatus)
            put("abiEvidenceUrl", "https://etherscan.io/address/0x090d4613473dee047c3f2706764f49e0821d256e#code")
            put("officialProtocolReference", "https://developers.uniswap.org/docs/ecosystem/governance/technical-reference")
        }.toString()
        return AssetObservationEntity(
            observationId = sha256(
                "$assetId|$SOURCE_ID|$chainId|$transactionHash|$claimEventLogIndex|$paymentTransferLogIndex"
            ),
            assetId = assetId,
            deploymentId = deploymentId,
            sourceId = SOURCE_ID,
            observationType = OBSERVATION_TYPE,
            observedAtEpochMs = blockTimestampEpochMs,
            retrievedAtEpochMs = retrievedAt,
            status = "QUARANTINED",
            quality = "UNVERIFIED",
            valueSchemaVersion = 1,
            valueJson = payload,
            payloadSha256 = sha256(payload),
            licenseReference = null
        )
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    companion object {
        const val SOURCE_ID = "uniswap-uni-merkle-distributor"
        const val OBSERVATION_TYPE = "ONCHAIN_DISTRIBUTION_CLAIM_PAYMENT"
    }
}

sealed class DistributionClaimStoreResult {
    data class Persisted(
        val inserted: Int,
        val duplicates: Int,
        val sourceUseStatus: String
    ) : DistributionClaimStoreResult()

    data class Abstained(val blockers: List<String>) : DistributionClaimStoreResult()
}
