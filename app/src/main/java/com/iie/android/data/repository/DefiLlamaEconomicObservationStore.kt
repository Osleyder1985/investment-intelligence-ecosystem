package com.iie.android.data.repository

import androidx.room.withTransaction
import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.domain.valuation.DefiLlamaEconomicEvidenceResult
import com.iie.android.domain.valuation.DefiLlamaEconomicObservation
import java.security.MessageDigest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Persists provider observations as immutable, quarantined evidence.
 * REVIEW_REQUIRED licensing status deliberately prevents these rows being treated as
 * approved production valuation inputs until the provider terms have been reviewed.
 */
class DefiLlamaEconomicObservationStore(
    private val database: IIEDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun persist(
        assetId: String,
        result: DefiLlamaEconomicEvidenceResult
    ): DefiLlamaStoreResult {
        val observations = when (result) {
            is DefiLlamaEconomicEvidenceResult.Ready -> result.observations
            is DefiLlamaEconomicEvidenceResult.Abstained ->
                return DefiLlamaStoreResult.Abstained(result.blockers)
        }
        if (assetId.isBlank() || observations.any { it.assetId != assetId }) {
            return DefiLlamaStoreResult.Abstained(listOf("ASSET_ID_MISMATCH"))
        }
        if (observations.isEmpty()) return DefiLlamaStoreResult.Abstained(listOf("NO_OBSERVATIONS"))
        val retrievedAt = clock()
        if (retrievedAt <= 0L || observations.any { it.retrievedAtEpochMs > retrievedAt }) {
            return DefiLlamaStoreResult.Abstained(listOf("RETRIEVAL_TIMESTAMP_INVALID"))
        }

        val entities = observations.map { observation -> observation.toEntity() }
        return try {
            database.withTransaction {
                if (database.assetDao().getAsset(assetId) == null) {
                    return@withTransaction DefiLlamaStoreResult.Abstained(listOf("CANONICAL_ASSET_NOT_FOUND"))
                }
                val sourceDao = database.dataSourceDao()
                if (sourceDao.getSource(SOURCE_ID) == null) {
                    sourceDao.insertSource(
                        DataSourceEntity(
                            sourceId = SOURCE_ID,
                            sourceKey = SOURCE_KEY,
                            displayName = "DefiLlama Public API",
                            providerType = "AGGREGATED_PROTOCOL_ECONOMICS",
                            homepageUrl = "https://defillama.com/",
                            termsUrl = null,
                            attributionText = "Data source: DefiLlama",
                            permittedUseStatus = "REVIEW_REQUIRED",
                            registeredAtEpochMs = retrievedAt
                        )
                    )
                }
                val inserted = database.assetObservationDao().insertHistoricalObservationsIfAbsent(entities)
                DefiLlamaStoreResult.Persisted(
                    attempted = entities.size,
                    inserted = inserted.count { it != -1L },
                    duplicates = inserted.count { it == -1L },
                    sourceUseStatus = "REVIEW_REQUIRED"
                )
            }
        } catch (error: Exception) {
            DefiLlamaStoreResult.Abstained(listOf("PERSISTENCE_FAILED:${error.javaClass.simpleName}"))
        }
    }

    private fun DefiLlamaEconomicObservation.toEntity(): AssetObservationEntity {
        val payload = buildJsonObject {
            put("schema", "iie.protocol-economic-observation.v1")
            put("assetId", assetId)
            put("protocolSlug", protocolSlug)
            put("protocolName", protocolName ?: "")
            put("providerMetric", providerMetric.name)
            put("period", period)
            put("valueUsd", valueUsd)
            put("observedAtEpochMs", observedAtEpochMs ?: 0L)
            put("retrievedAtEpochMs", retrievedAtEpochMs)
            put("sourceUrl", sourceUrl)
            put("evidenceStatus", evidenceStatus)
            put("limitations", kotlinx.serialization.json.buildJsonArray {
                limitations.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
            })
        }.toString()
        val digest = sha256(payload)
        val identity = listOf(
            assetId, SOURCE_ID, protocolSlug, providerMetric.name, period,
            observedAtEpochMs?.toString() ?: "snapshot", retrievedAtEpochMs.toString()
        ).joinToString("|")
        return AssetObservationEntity(
            observationId = sha256(identity),
            assetId = assetId,
            deploymentId = null,
            sourceId = SOURCE_ID,
            observationType = OBSERVATION_TYPE,
            observedAtEpochMs = observedAtEpochMs,
            retrievedAtEpochMs = retrievedAtEpochMs,
            status = "QUARANTINED",
            quality = "UNVERIFIED",
            valueSchemaVersion = 1,
            valueJson = payload,
            payloadSha256 = digest,
            licenseReference = null
        )
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    companion object {
        const val SOURCE_ID = "defillama-public-api"
        const val SOURCE_KEY = "defillama-public-api"
        const val OBSERVATION_TYPE = "PROTOCOL_ECONOMIC_METRIC"
    }
}

sealed class DefiLlamaStoreResult {
    data class Persisted(
        val attempted: Int,
        val inserted: Int,
        val duplicates: Int,
        val sourceUseStatus: String
    ) : DefiLlamaStoreResult()

    data class Abstained(val blockers: List<String>) : DefiLlamaStoreResult()
}
