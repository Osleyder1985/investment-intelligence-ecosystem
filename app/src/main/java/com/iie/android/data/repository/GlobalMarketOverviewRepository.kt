package com.iie.android.data.repository

import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.GlobalMarketSnapshotEntity
import com.iie.android.data.market.AlternativeMeFearGreedClient
import com.iie.android.data.market.CoinPaprikaGlobalMarketClient
import com.iie.android.data.market.FearGreedIndex
import com.iie.android.data.market.GlobalMarketMetrics
import java.security.MessageDigest
import java.util.UUID
import org.json.JSONObject

data class GlobalMarketDashboardData(
    val globalMetrics: GlobalMarketMetrics?,
    val fearGreed: FearGreedIndex?
)

data class GlobalMarketRefreshResult(
    val globalUpdated: Boolean,
    val sentimentUpdated: Boolean,
    val globalError: String? = null,
    val sentimentError: String? = null
)

/**
 * Stores provider snapshots separately from asset observations because global
 * market statistics do not belong to any single canonical asset.
 */
class GlobalMarketOverviewRepository(
    private val database: IIEDatabase,
    private val globalClient: CoinPaprikaGlobalMarketClient = CoinPaprikaGlobalMarketClient(),
    private val fearGreedClient: AlternativeMeFearGreedClient = AlternativeMeFearGreedClient()
) {
    suspend fun loadLatest(): GlobalMarketDashboardData = GlobalMarketDashboardData(
        globalMetrics = loadGlobalSnapshot(),
        fearGreed = loadFearGreedSnapshot()
    )

    suspend fun refresh(): GlobalMarketRefreshResult {
        val globalResult = runCatching {
            val source = database.dataSourceDao().getSource(COINPAPRIKA_SOURCE_ID)
                ?: error("CoinPaprika global source is not registered")
            check(source.permittedUseStatus == "PERSONAL_USE_ONLY") {
                "CoinPaprika global data is outside the configured personal-use scope"
            }
            val metrics = globalClient.fetch()
            persist(
                sourceId = COINPAPRIKA_SOURCE_ID,
                snapshotType = GLOBAL_MARKET_TYPE,
                observedAtEpochMs = metrics.providerUpdatedAtEpochMs,
                retrievedAtEpochMs = metrics.retrievedAtEpochMs,
                payload = JSONObject()
                    .put("marketCapUsd", metrics.marketCapUsd)
                    .put("volume24hUsd", metrics.volume24hUsd)
                    .put("bitcoinDominancePercent", metrics.bitcoinDominancePercent)
                    .put("activeCryptocurrencies", metrics.activeCryptocurrencies)
                    .put("marketCapChange24hPercent", metrics.marketCapChange24hPercent ?: JSONObject.NULL)
                    .put("volumeChange24hPercent", metrics.volumeChange24hPercent ?: JSONObject.NULL)
                    .put("providerUpdatedAtEpochMs", metrics.providerUpdatedAtEpochMs)
                    .put("retrievedAtEpochMs", metrics.retrievedAtEpochMs)
            )
        }
        val sentimentResult = runCatching {
            val source = database.dataSourceDao().getSource(ALTERNATIVE_ME_SOURCE_ID)
                ?: error("Alternative.me source is not registered")
            check(source.permittedUseStatus == "ATTRIBUTION_REQUIRED") {
                "Alternative.me attribution policy is not registered"
            }
            val index = fearGreedClient.fetch()
            persist(
                sourceId = ALTERNATIVE_ME_SOURCE_ID,
                snapshotType = FEAR_GREED_TYPE,
                observedAtEpochMs = index.providerUpdatedAtEpochMs,
                retrievedAtEpochMs = index.retrievedAtEpochMs,
                payload = JSONObject()
                    .put("value", index.value)
                    .put("classification", index.classification)
                    .put("providerUpdatedAtEpochMs", index.providerUpdatedAtEpochMs)
                    .put("retrievedAtEpochMs", index.retrievedAtEpochMs)
            )
        }
        return GlobalMarketRefreshResult(
            globalUpdated = globalResult.isSuccess,
            sentimentUpdated = sentimentResult.isSuccess,
            globalError = globalResult.exceptionOrNull()?.message,
            sentimentError = sentimentResult.exceptionOrNull()?.message
        )
    }

    private suspend fun persist(
        sourceId: String,
        snapshotType: String,
        observedAtEpochMs: Long,
        retrievedAtEpochMs: Long,
        payload: JSONObject
    ) {
        val json = payload.toString()
        database.globalMarketSnapshotDao().insert(
            GlobalMarketSnapshotEntity(
                snapshotId = "global-snapshot:$sourceId:$snapshotType:${UUID.randomUUID()}",
                sourceId = sourceId,
                snapshotType = snapshotType,
                observedAtEpochMs = observedAtEpochMs,
                retrievedAtEpochMs = retrievedAtEpochMs,
                quality = "PROVIDER_SCHEMA_VALIDATED",
                payloadJson = json,
                payloadSha256 = sha256(json)
            )
        )
    }

    private suspend fun loadGlobalSnapshot(): GlobalMarketMetrics? {
        val row = database.globalMarketSnapshotDao().getLatest(COINPAPRIKA_SOURCE_ID, GLOBAL_MARKET_TYPE)
            ?: return null
        if (!isTrusted(row, COINPAPRIKA_SOURCE_ID, GLOBAL_MARKET_TYPE)) return null
        val json = JSONObject(row.payloadJson)
        return GlobalMarketMetrics(
            marketCapUsd = json.getDouble("marketCapUsd"),
            volume24hUsd = json.getDouble("volume24hUsd"),
            bitcoinDominancePercent = json.getDouble("bitcoinDominancePercent"),
            activeCryptocurrencies = json.getInt("activeCryptocurrencies"),
            marketCapChange24hPercent = nullableDouble(json, "marketCapChange24hPercent"),
            volumeChange24hPercent = nullableDouble(json, "volumeChange24hPercent"),
            providerUpdatedAtEpochMs = row.observedAtEpochMs,
            retrievedAtEpochMs = row.retrievedAtEpochMs
        ).takeIf {
            it.marketCapUsd > 0.0 && it.volume24hUsd >= 0.0 &&
                it.bitcoinDominancePercent in 0.0..100.0 &&
                it.activeCryptocurrencies > 0
        }
    }

    private suspend fun loadFearGreedSnapshot(): FearGreedIndex? {
        val row = database.globalMarketSnapshotDao().getLatest(ALTERNATIVE_ME_SOURCE_ID, FEAR_GREED_TYPE)
            ?: return null
        if (!isTrusted(row, ALTERNATIVE_ME_SOURCE_ID, FEAR_GREED_TYPE)) return null
        val json = JSONObject(row.payloadJson)
        val value = json.getInt("value")
        val classification = json.getString("classification")
        return FearGreedIndex(
            value = value,
            classification = classification,
            providerUpdatedAtEpochMs = row.observedAtEpochMs,
            retrievedAtEpochMs = row.retrievedAtEpochMs
        ).takeIf { it.value in 0..100 && it.classification.isNotBlank() }
    }

    private fun isTrusted(row: GlobalMarketSnapshotEntity, sourceId: String, snapshotType: String): Boolean =
        row.sourceId == sourceId &&
            row.snapshotType == snapshotType &&
            row.quality == "PROVIDER_SCHEMA_VALIDATED" &&
            row.observedAtEpochMs > 0L &&
            row.retrievedAtEpochMs >= row.observedAtEpochMs &&
            row.payloadSha256 == sha256(row.payloadJson)

    private fun nullableDouble(json: JSONObject, key: String): Double? {
        if (!json.has(key) || json.isNull(key)) return null
        return json.getDouble(key).takeIf { it.isFinite() }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val COINPAPRIKA_SOURCE_ID = "source:coinpaprika"
        const val ALTERNATIVE_ME_SOURCE_ID = "source:alternative-me"
        const val GLOBAL_MARKET_TYPE = "GLOBAL_MARKET"
        const val FEAR_GREED_TYPE = "FEAR_GREED"
    }
}
