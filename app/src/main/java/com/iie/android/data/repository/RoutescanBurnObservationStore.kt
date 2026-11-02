package com.iie.android.data.repository

import androidx.room.withTransaction
import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.domain.valuation.VerifiedErc20BurnEvent
import java.math.BigDecimal
import java.security.MessageDigest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Stores indexed burn events immutably; evidence remains quarantined until RPC cross-check. */
class RoutescanBurnObservationStore(
    private val database: IIEDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun persist(assetId: String, events: List<VerifiedErc20BurnEvent>): RoutescanBurnStoreResult {
        if (assetId.isBlank() || events.any { it.assetId != assetId }) {
            return RoutescanBurnStoreResult.Abstained(listOf("ASSET_ID_MISMATCH"))
        }
        if (events.isEmpty()) return RoutescanBurnStoreResult.Persisted(0, 0, 0)
        val now = clock()
        if (now <= 0L || events.any { it.observedAtEpochMs <= 0L || it.observedAtEpochMs > now }) {
            return RoutescanBurnStoreResult.Abstained(listOf("EVENT_TIMESTAMP_INVALID"))
        }
        return try {
            database.withTransaction {
                if (database.assetDao().getAsset(assetId) == null) {
                    return@withTransaction RoutescanBurnStoreResult.Abstained(listOf("CANONICAL_ASSET_NOT_FOUND"))
                }
                val dao = database.dataSourceDao()
                if (dao.getSource(SOURCE_ID) == null) {
                    dao.insertSource(
                        DataSourceEntity(
                            sourceId = SOURCE_ID,
                            sourceKey = SOURCE_ID,
                            displayName = "Routescan EVM Explorer",
                            providerType = "ONCHAIN_EVENT_INDEXER",
                            homepageUrl = "https://routescan.io/",
                            termsUrl = null,
                            attributionText = "Powered by Routescan.io APIs",
                            permittedUseStatus = "ATTRIBUTION_REQUIRED",
                            registeredAtEpochMs = now
                        )
                    )
                }
                val rows = events.map { event ->
                    val deployment = database.assetDeploymentDao()
                        .getDeploymentForAssetAndContract(assetId, event.tokenContract)
                        ?: return@withTransaction RoutescanBurnStoreResult.Abstained(
                            listOf("TOKEN_CONTRACT_NOT_REGISTERED_FOR_CANONICAL_ASSET")
                        )
                    val network = database.networkDao().getNetwork(deployment.networkId)
                        ?: return@withTransaction RoutescanBurnStoreResult.Abstained(
                            listOf("DEPLOYMENT_NETWORK_NOT_REGISTERED")
                        )
                    if (network.chainId != event.chainId.toString() ||
                        deployment.decimals != event.tokenDecimals ||
                        !deployment.reference.equals(event.tokenContract, ignoreCase = true)
                    ) {
                        return@withTransaction RoutescanBurnStoreResult.Abstained(
                            listOf("ONCHAIN_EVENT_DEPLOYMENT_MISMATCH")
                        )
                    }
                    event.toEntity(deployment.deploymentId)
                }
                val results = database.assetObservationDao().insertHistoricalObservationsIfAbsent(rows)
                RoutescanBurnStoreResult.Persisted(
                    attempted = rows.size,
                    inserted = results.count { it != -1L },
                    duplicates = results.count { it == -1L }
                )
            }
        } catch (error: Exception) {
            RoutescanBurnStoreResult.Abstained(listOf("PERSISTENCE_FAILED:${error.javaClass.simpleName}"))
        }
    }

    private fun VerifiedErc20BurnEvent.toEntity(deploymentId: String): AssetObservationEntity {
        val payload = buildJsonObject {
            put("schema", "iie.erc20-burn-event.v1")
            put("assetId", assetId)
            put("chainId", chainId)
            put("tokenContract", tokenContract)
            put("transactionHash", transactionHash)
            put("blockNumber", blockNumber)
            put("logIndex", logIndex)
            put("rawAmount", rawAmount.toString())
            put("tokenAmount", tokenAmount.toPlainString())
            put("tokenDecimals", tokenDecimals)
            put("observedAtEpochMs", observedAtEpochMs)
            put("sourceUrl", sourceUrl)
            put("evidenceStatus", evidenceStatus)
        }.toString()
        return AssetObservationEntity(
            observationId = sha256("$assetId|$SOURCE_ID|$chainId|$tokenContract|$transactionHash|$logIndex"),
            assetId = assetId,
            deploymentId = deploymentId,
            sourceId = SOURCE_ID,
            observationType = "ONCHAIN_ERC20_BURN_EVENT",
            observedAtEpochMs = observedAtEpochMs,
            retrievedAtEpochMs = clock(),
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

    companion object { const val SOURCE_ID = "routescan-evm-explorer" }
}

sealed class RoutescanBurnStoreResult {
    data class Persisted(val attempted: Int, val inserted: Int, val duplicates: Int) : RoutescanBurnStoreResult()
    data class Abstained(val blockers: List<String>) : RoutescanBurnStoreResult()
}
