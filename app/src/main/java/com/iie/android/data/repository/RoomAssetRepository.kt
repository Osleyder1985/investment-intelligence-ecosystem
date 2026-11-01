package com.iie.android.data.repository

import androidx.room.withTransaction
import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.WatchlistEntryEntity
import com.iie.android.data.market.CoinPaprikaTickerClient
import com.iie.android.data.market.CoinMarketCapAuthenticatedClient
import com.iie.android.data.market.CoinPaprikaQuote
import com.iie.android.data.market.CoinPaprikaHistoricalPoint
import com.iie.android.data.market.CoinPaprikaDailyHistoryFetcher
import java.time.LocalDate
import java.time.ZoneOffset
import java.security.MessageDigest
import java.util.UUID
import org.json.JSONObject
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.domain.model.AssetObservation
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class CoinPaprikaHistorySyncResult(
    val pointsReceived: Int,
    val pointsInserted: Int,
    val pointsAlreadyPresent: Int
)

class RoomAssetRepository(
    private val database: IIEDatabase,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) : AssetRepository {

    override fun observeAssets(query: String): Flow<List<Asset>> =
        database.assetDao().observeAssets(query.trim()).map { entities ->
            entities.map { entity ->
                Asset(
                    assetId = entity.assetId,
                    canonicalKey = entity.canonicalKey,
                    name = entity.name,
                    symbol = entity.symbol,
                    assetType = entity.assetType,
                    identityStatus = entity.identityStatus,
                    createdAtEpochMs = entity.createdAtEpochMs,
                    updatedAtEpochMs = entity.updatedAtEpochMs
                )
            }
        }

    override fun observeAsset(assetId: String): Flow<Asset?> =
        database.assetDao().observeAsset(assetId).map { entity ->
            entity?.let {
                Asset(
                    assetId = it.assetId,
                    canonicalKey = it.canonicalKey,
                    name = it.name,
                    symbol = it.symbol,
                    assetType = it.assetType,
                    identityStatus = it.identityStatus,
                    createdAtEpochMs = it.createdAtEpochMs,
                    updatedAtEpochMs = it.updatedAtEpochMs
                )
            }
        }

    override fun observeDeployments(assetId: String): Flow<List<AssetDeploymentSummary>> =
        database.assetDeploymentDao().observeSummariesForAsset(assetId).map { rows ->
            rows.map { row ->
                AssetDeploymentSummary(
                    deploymentId = row.deploymentId,
                    networkName = row.networkName,
                    representationKind = row.representationKind,
                    namespace = row.namespace,
                    reference = row.reference,
                    decimals = row.decimals
                )
            }
        }

    override fun observeProvenance(assetId: String): Flow<List<AssetObservationProvenance>> =
        database.assetObservationDao().observeProvenanceForAsset(assetId).map { rows ->
            rows.map { row ->
                AssetObservationProvenance(
                    observationId = row.observationId,
                    sourceName = row.sourceDisplayName,
                    providerType = row.sourceProviderType,
                    permittedUseStatus = row.sourcePermittedUseStatus,
                    observationType = row.observationType,
                    observedAtEpochMs = row.observedAtEpochMs,
                    retrievedAtEpochMs = row.retrievedAtEpochMs,
                    status = row.status,
                    quality = row.quality
                )
            }
        }

    /** Returns the latest typed observation and its registered source-use status. */
    suspend fun getLatestObservationForEvaluation(
        assetId: String,
        observationType: String
    ): Pair<AssetObservation, String>? {
        val entity = database.assetObservationDao().getLatest(assetId, observationType)
            ?: return null
        val source = database.dataSourceDao().getSource(entity.sourceId)
            ?: return null
        return AssetObservation(
            observationId = entity.observationId,
            assetId = entity.assetId,
            deploymentId = entity.deploymentId,
            sourceId = entity.sourceId,
            observationType = entity.observationType,
            observedAtEpochMs = entity.observedAtEpochMs,
            retrievedAtEpochMs = entity.retrievedAtEpochMs,
            status = entity.status,
            quality = entity.quality,
            valueSchemaVersion = entity.valueSchemaVersion,
            valueJson = entity.valueJson,
            payloadSha256 = entity.payloadSha256,
            licenseReference = entity.licenseReference
        ) to source.permittedUseStatus
    }


    /**
     * Fetches by the registered CoinPaprika identifier (never by ticker symbol),
     * then appends an immutable market observation to the existing provenance ledger.
     */
    suspend fun fetchAndPersistCoinPaprikaQuote(
        assetId: String,
        expectedSymbol: String,
        client: CoinPaprikaTickerClient
    ): AssetObservationEntity {
        val sourceId = "source:coinpaprika"
        val source = database.dataSourceDao().getSource(sourceId)
            ?: error("CoinPaprika source is not registered")
        check(source.permittedUseStatus == "PERSONAL_USE_ONLY") {
            "CoinPaprika source is not enabled for this app's personal-use scope"
        }
        val identifier = database.assetIdentifierDao().findForAsset(
            assetId = assetId,
            sourceId = sourceId,
            namespace = "coinpaprika"
        ) ?: error("No CoinPaprika provider identifier is mapped to this asset")
        check(identifier.verificationStatus == "PROVIDER_ID_CONFIRMED") {
            "CoinPaprika provider identifier is not confirmed"
        }

        val cached = database.assetObservationDao().getLatestForSource(
            assetId = assetId,
            observationType = MARKET_QUOTE_OBSERVATION_TYPE,
            sourceId = sourceId
        )
        val now = nowEpochMs()
        if (cached != null && now >= cached.retrievedAtEpochMs &&
            now - cached.retrievedAtEpochMs < QUOTE_CACHE_TTL_MS &&
            cachedMatchesProvider(cached, identifier.identifierValue, expectedSymbol)
        ) {
            return cached
        }

        val quote = client.fetch(
            providerId = identifier.identifierValue,
            expectedSymbol = expectedSymbol
        )
        val payload = quote.toObservationJson()
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val observation = AssetObservationEntity(
            observationId = "observation:coinpaprika:${UUID.randomUUID()}",
            assetId = assetId,
            deploymentId = null,
            sourceId = sourceId,
            observationType = MARKET_QUOTE_OBSERVATION_TYPE,
            observedAtEpochMs = quote.providerUpdatedAtEpochMs,
            retrievedAtEpochMs = quote.retrievedAtEpochMs,
            status = "ACCEPTED",
            quality = "PROVIDER_SCHEMA_VALIDATED",
            valueSchemaVersion = 1,
            valueJson = payload,
            payloadSha256 = digest,
            licenseReference = source.termsUrl
        )
        database.assetObservationDao().insertObservation(observation)
        return observation
    }

    /**
     * Fetches CoinMarketCap by its confirmed numeric provider ID and appends an immutable
     * observation to the same provenance ledger used by CoinPaprika.
     */
    suspend fun fetchAndPersistCoinMarketCapQuote(
        assetId: String,
        expectedSymbol: String,
        apiKey: String,
        client: CoinMarketCapAuthenticatedClient
    ): AssetObservationEntity {
        val sourceId = "source:coinmarketcap"
        val source = database.dataSourceDao().getSource(sourceId)
            ?: error("CoinMarketCap source is not registered")
        check(source.permittedUseStatus == "PERSONAL_USE_ONLY") {
            "CoinMarketCap source is not enabled for this app's personal-use scope"
        }
        val asset = database.assetDao().getAsset(assetId)
            ?: error("Cannot quote an unknown IIE asset")
        require(asset.symbol.equals(expectedSymbol, ignoreCase = true)) {
            "Expected symbol does not match the canonical asset"
        }
        val identifier = database.assetIdentifierDao().findForAsset(
            assetId = assetId,
            sourceId = sourceId,
            namespace = "coinmarketcap"
        ) ?: error("No confirmed CoinMarketCap numeric ID is mapped to this asset")
        check(identifier.verificationStatus == "PROVIDER_ID_CONFIRMED") {
            "CoinMarketCap provider identifier is not confirmed"
        }
        val expectedProviderId = identifier.identifierValue.toLongOrNull()
            ?: error("CoinMarketCap provider ID must be numeric")

        val cached = database.assetObservationDao().getLatestForSource(
            assetId = assetId,
            observationType = MARKET_QUOTE_OBSERVATION_TYPE,
            sourceId = sourceId
        )
        val now = nowEpochMs()
        if (cached != null && now >= cached.retrievedAtEpochMs &&
            now - cached.retrievedAtEpochMs < QUOTE_CACHE_TTL_MS &&
            cachedCoinMarketCapMatches(cached, expectedProviderId, expectedSymbol)
        ) return cached

        val quote = client.fetchLatestQuote(apiKey, expectedSymbol, expectedProviderId = expectedProviderId)
        require(quote.providerId == expectedProviderId) {
            "CoinMarketCap response ID does not match the confirmed asset mapping"
        }
        require(quote.symbol.equals(expectedSymbol, ignoreCase = true)) {
            "CoinMarketCap response symbol does not match the canonical asset"
        }
        require(quote.retrievedAtEpochMs in 1L..nowEpochMs()) {
            "CoinMarketCap retrieval timestamp is outside the accepted bounds"
        }
        val payload = JSONObject()
            .put("schema", "iie.market-quote.v1")
            .put("providerId", quote.providerId.toString())
            .put("symbol", quote.symbol.uppercase())
            .put("quoteCurrency", "USD")
            .put("price", quote.priceUsd)
            .put("providerUpdatedAtEpochMs", quote.providerUpdatedAtEpochMs)
            .put("retrievedAtEpochMs", quote.retrievedAtEpochMs)
            .toString()
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val observation = AssetObservationEntity(
            observationId = "observation:coinmarketcap:${UUID.randomUUID()}",
            assetId = assetId,
            deploymentId = null,
            sourceId = sourceId,
            observationType = MARKET_QUOTE_OBSERVATION_TYPE,
            observedAtEpochMs = quote.providerUpdatedAtEpochMs,
            retrievedAtEpochMs = quote.retrievedAtEpochMs,
            status = "ACCEPTED",
            quality = "PROVIDER_SCHEMA_VALIDATED",
            valueSchemaVersion = 1,
            valueJson = payload,
            payloadSha256 = digest,
            licenseReference = source.termsUrl
        )
        database.assetObservationDao().insertObservation(observation)
        return observation
    }

    suspend fun getLatestCoinMarketCapQuote(assetId: String): Pair<AssetObservationEntity, String>? {
        val sourceId = "source:coinmarketcap"
        val asset = database.assetDao().getAsset(assetId) ?: return null
        val identifier = database.assetIdentifierDao().findForAsset(
            assetId = assetId,
            sourceId = sourceId,
            namespace = "coinmarketcap"
        ) ?: return null
        val providerId = identifier.identifierValue.toLongOrNull() ?: return null
        if (identifier.verificationStatus != "PROVIDER_ID_CONFIRMED") return null
        val observation = database.assetObservationDao().getLatestForSource(
            assetId = assetId,
            observationType = MARKET_QUOTE_OBSERVATION_TYPE,
            sourceId = sourceId
        ) ?: return null
        if (!cachedCoinMarketCapMatches(observation, providerId, asset.symbol)) return null
        val source = database.dataSourceDao().getSource(sourceId) ?: return null
        if (source.permittedUseStatus != "PERSONAL_USE_ONLY") return null
        return observation to (source.attributionText ?: source.displayName)
    }

    private fun cachedCoinMarketCapMatches(
        observation: AssetObservationEntity,
        expectedProviderId: Long,
        expectedSymbol: String
    ): Boolean = try {
        val payload = JSONObject(observation.valueJson)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(observation.valueJson.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val providerTimestamp = payload.optLong("providerUpdatedAtEpochMs", -1L)
        val retrievedTimestamp = payload.optLong("retrievedAtEpochMs", -1L)
        observation.sourceId == "source:coinmarketcap" &&
            observation.observationType == MARKET_QUOTE_OBSERVATION_TYPE &&
            observation.status == "ACCEPTED" &&
            payload.optString("schema") == "iie.market-quote.v1" &&
            payload.optString("providerId") == expectedProviderId.toString() &&
            payload.optString("symbol").equals(expectedSymbol, ignoreCase = true) &&
            payload.optString("quoteCurrency") == "USD" &&
            payload.optDouble("price", Double.NaN).let { it.isFinite() && it > 0.0 } &&
            observation.payloadSha256 == digest &&
            providerTimestamp == observation.observedAtEpochMs &&
            retrievedTimestamp == observation.retrievedAtEpochMs &&
            providerTimestamp > 0L && retrievedTimestamp >= providerTimestamp
    } catch (_: Exception) {
        false
    }

    /**
     * Appends daily CoinPaprika history as immutable observations.
     * Deterministic IDs make repeated imports idempotent; existing evidence is never replaced.
     * Returns the number of newly inserted points.
     */
    suspend fun persistCoinPaprikaDailyHistory(
        assetId: String,
        expectedSymbol: String,
        points: List<CoinPaprikaHistoricalPoint>
    ): Int {
        require(points.size <= 500) { "Historical batch exceeds the configured point limit" }
        if (points.isEmpty()) return 0
        val sourceId = "source:coinpaprika"
        val source = database.dataSourceDao().getSource(sourceId)
            ?: error("CoinPaprika source is not registered")
        check(source.permittedUseStatus == "PERSONAL_USE_ONLY") {
            "CoinPaprika source is not enabled for this app's personal-use scope"
        }
        val asset = database.assetDao().getAsset(assetId)
            ?: error("Cannot persist history for an unknown IIE asset")
        require(asset.symbol.equals(expectedSymbol, ignoreCase = true)) {
            "Expected symbol does not match the canonical asset"
        }
        val identifier = database.assetIdentifierDao().findForAsset(
            assetId = assetId, sourceId = sourceId, namespace = "coinpaprika"
        ) ?: error("No CoinPaprika provider identifier is mapped to this asset")
        check(identifier.verificationStatus == "PROVIDER_ID_CONFIRMED") {
            "CoinPaprika provider identifier is not confirmed"
        }
        val now = nowEpochMs()
        require(now > 0L) { "Invalid local retrieval clock" }
        require(points.map { it.timestampEpochMs }.distinct().size == points.size) {
            "Historical batch contains duplicate timestamps"
        }
        require(points.all { point ->
            point.providerId == identifier.identifierValue &&
                point.retrievedAtEpochMs in 1L..now &&
                point.timestampEpochMs > 0L &&
                point.timestampEpochMs <= point.retrievedAtEpochMs &&
                point.priceUsd.isFinite() && point.priceUsd > 0.0 &&
                (point.volume24hUsd == null || (point.volume24hUsd.isFinite() && point.volume24hUsd >= 0.0)) &&
                (point.marketCapUsd == null || (point.marketCapUsd.isFinite() && point.marketCapUsd >= 0.0))
        }) { "Historical batch contains an invalid or mismatched point" }

        val observations = points.distinctBy { it.timestampEpochMs }.map { point ->
            val payload = JSONObject()
                .put("schema", "iie.market-history-daily.v1")
                .put("providerId", point.providerId)
                .put("symbol", asset.symbol.uppercase())
                .put("interval", "1d")
                .put("quoteCurrency", "USD")
                .put("priceUsd", point.priceUsd)
                .put("volume24hUsd", point.volume24hUsd ?: JSONObject.NULL)
                .put("marketCapUsd", point.marketCapUsd ?: JSONObject.NULL)
                .put("providerTimestampEpochMs", point.timestampEpochMs)
                .put("retrievedAtEpochMs", point.retrievedAtEpochMs)
                .toString()
            val identity = listOf(
                sourceId, assetId, point.providerId, point.timestampEpochMs.toString(),
                "1d", "USD", "iie.market-history-daily.v1"
            ).joinToString("|")
            val idHash = MessageDigest.getInstance("SHA-256")
                .digest(identity.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(payload.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            AssetObservationEntity(
                observationId = "observation:coinpaprika-history:$idHash",
                assetId = assetId,
                deploymentId = null,
                sourceId = sourceId,
                observationType = MARKET_HISTORY_DAILY_OBSERVATION_TYPE,
                observedAtEpochMs = point.timestampEpochMs,
                retrievedAtEpochMs = point.retrievedAtEpochMs,
                status = "ACCEPTED",
                quality = "PROVIDER_SCHEMA_VALIDATED",
                valueSchemaVersion = 1,
                valueJson = payload,
                payloadSha256 = digest,
                licenseReference = source.termsUrl
            )
        }
        return database.withTransaction {
            database.assetObservationDao()
                .insertHistoricalObservationsIfAbsent(observations)
                .count { it != -1L }
        }
    }

    suspend fun downloadAndPersistCoinPaprikaDailyHistory(
        assetId: String,
        expectedSymbol: String,
        start: LocalDate,
        end: LocalDate,
        fetcher: CoinPaprikaDailyHistoryFetcher
    ): CoinPaprikaHistorySyncResult {
        require(!end.isBefore(start)) { "Historical end date precedes start date" }
        val sourceId = "source:coinpaprika"
        val source = database.dataSourceDao().getSource(sourceId)
            ?: error("CoinPaprika source is not registered")
        check(source.permittedUseStatus == "PERSONAL_USE_ONLY") {
            "CoinPaprika source is not enabled for this app's personal-use scope"
        }
        val asset = database.assetDao().getAsset(assetId)
            ?: error("Cannot download history for an unknown IIE asset")
        require(asset.symbol.equals(expectedSymbol, ignoreCase = true)) {
            "Expected symbol does not match the canonical asset"
        }
        val identifier = database.assetIdentifierDao().findForAsset(
            assetId = assetId, sourceId = sourceId, namespace = "coinpaprika"
        ) ?: error("No CoinPaprika provider identifier is mapped to this asset")
        check(identifier.verificationStatus == "PROVIDER_ID_CONFIRMED") {
            "CoinPaprika provider identifier is not confirmed"
        }
        val retrievedAt = nowEpochMs()
        require(retrievedAt > 0L) { "Invalid local retrieval clock" }
        val todayUtc = java.time.Instant.ofEpochMilli(retrievedAt).atZone(ZoneOffset.UTC).toLocalDate()
        require(!end.isAfter(todayUtc)) { "Historical end date cannot be in the future" }
        val earliestAllowedStart = todayUtc.minusYears(1)
        require(!start.isBefore(earliestAllowedStart)) {
            "Free-plan daily history is limited to one year"
        }

        val points = fetcher.fetchDaily(
            providerId = identifier.identifierValue,
            start = start,
            end = end,
            retrievedAtEpochMs = retrievedAt
        )
        val inserted = persistCoinPaprikaDailyHistory(assetId, expectedSymbol, points)
        return CoinPaprikaHistorySyncResult(
            pointsReceived = points.size,
            pointsInserted = inserted,
            pointsAlreadyPresent = points.size - inserted
        )
    }

    suspend fun getLatestCoinPaprikaDailyHistory(
        assetId: String,
        limit: Int = 30
    ): List<CoinPaprikaHistoricalPoint> {
        require(limit in 1..500) { "History query limit must be between 1 and 500" }
        val sourceId = "source:coinpaprika"
        val asset = database.assetDao().getAsset(assetId) ?: return emptyList()
        val source = database.dataSourceDao().getSource(sourceId) ?: return emptyList()
        if (source.permittedUseStatus != "PERSONAL_USE_ONLY") return emptyList()
        val identifier = database.assetIdentifierDao().findForAsset(
            assetId = assetId, sourceId = sourceId, namespace = "coinpaprika"
        ) ?: return emptyList()
        if (identifier.verificationStatus != "PROVIDER_ID_CONFIRMED") return emptyList()
        return database.assetObservationDao().getLatestHistoryForSource(
            assetId = assetId,
            sourceId = sourceId,
            observationType = MARKET_HISTORY_DAILY_OBSERVATION_TYPE,
            limit = limit
        ).mapNotNull { observation ->
            try {
                val payload = JSONObject(observation.valueJson)
                val actualDigest = MessageDigest.getInstance("SHA-256")
                    .digest(observation.valueJson.toByteArray(Charsets.UTF_8))
                    .joinToString("") { "%02x".format(it) }
                if (payload.optString("schema") != "iie.market-history-daily.v1" ||
                    payload.optString("providerId") != identifier.identifierValue ||
                    !payload.optString("symbol").equals(asset.symbol, ignoreCase = true) ||
                    payload.optString("interval") != "1d" ||
                    payload.optString("quoteCurrency") != "USD" ||
                    observation.payloadSha256 != actualDigest
                ) return@mapNotNull null
                val price = payload.optDouble("priceUsd", Double.NaN)
                val timestamp = payload.optLong("providerTimestampEpochMs", -1L)
                val retrieved = payload.optLong("retrievedAtEpochMs", -1L)
                val volume = if (payload.isNull("volume24hUsd")) null else payload.optDouble("volume24hUsd", Double.NaN)
                val marketCap = if (payload.isNull("marketCapUsd")) null else payload.optDouble("marketCapUsd", Double.NaN)
                if (!price.isFinite() || price <= 0.0 || timestamp <= 0L || retrieved < timestamp ||
                    (volume != null && (!volume.isFinite() || volume < 0.0)) ||
                    (marketCap != null && (!marketCap.isFinite() || marketCap < 0.0))
                ) return@mapNotNull null
                CoinPaprikaHistoricalPoint(
                    providerId = identifier.identifierValue,
                    timestampEpochMs = timestamp,
                    priceUsd = price,
                    volume24hUsd = volume,
                    marketCapUsd = marketCap,
                    retrievedAtEpochMs = retrieved
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun getLatestCoinPaprikaQuote(assetId: String): Pair<AssetObservationEntity, String>? {
        val sourceId = "source:coinpaprika"
        val asset = database.assetDao().getAsset(assetId) ?: return null
        val identifier = database.assetIdentifierDao().findForAsset(
            assetId = assetId,
            sourceId = sourceId,
            namespace = "coinpaprika"
        ) ?: return null
        if (identifier.verificationStatus != "PROVIDER_ID_CONFIRMED") return null
        val observation = database.assetObservationDao().getLatestForSource(
            assetId = assetId,
            observationType = MARKET_QUOTE_OBSERVATION_TYPE,
            sourceId = sourceId
        ) ?: return null
        if (!cachedMatchesProvider(observation, identifier.identifierValue, asset.symbol)) return null
        val source = database.dataSourceDao().getSource(sourceId) ?: return null
        if (source.permittedUseStatus != "PERSONAL_USE_ONLY") return null
        return observation to (source.attributionText ?: source.displayName)
    }

    private fun cachedMatchesProvider(
        observation: AssetObservationEntity,
        expectedProviderId: String,
        expectedSymbol: String
    ): Boolean {
        return try {
            if (observation.sourceId != "source:coinpaprika" ||
                observation.observationType != MARKET_QUOTE_OBSERVATION_TYPE ||
                observation.status != "ACCEPTED"
            ) {
                false
            } else {
                val payload = JSONObject(observation.valueJson)
                val actualDigest = MessageDigest.getInstance("SHA-256")
                    .digest(observation.valueJson.toByteArray(Charsets.UTF_8))
                    .joinToString("") { "%02x".format(it) }
                val providerTimestamp = payload.optLong("providerUpdatedAtEpochMs", -1L)
                val retrievedTimestamp = payload.optLong("retrievedAtEpochMs", -1L)
                payload.optString("schema") == "iie.market-quote.v1" &&
                    payload.optString("providerId") == expectedProviderId &&
                    payload.optString("symbol").equals(expectedSymbol, ignoreCase = true) &&
                    payload.optString("quoteCurrency") == "USD" &&
                    payload.optDouble("price", Double.NaN).let { it.isFinite() && it > 0.0 } &&
                    observation.payloadSha256 == actualDigest &&
                    providerTimestamp == observation.observedAtEpochMs &&
                    retrievedTimestamp == observation.retrievedAtEpochMs &&
                    providerTimestamp > 0L &&
                    retrievedTimestamp >= providerTimestamp
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun CoinPaprikaQuote.toObservationJson(): String =
        """{"schema":"iie.market-quote.v1","providerId":"$providerId","symbol":"$symbol","quoteCurrency":"USD","price":$priceUsd,"providerUpdatedAtEpochMs":$providerUpdatedAtEpochMs,"retrievedAtEpochMs":$retrievedAtEpochMs}"""

    companion object {
        const val MARKET_QUOTE_OBSERVATION_TYPE = "MARKET_QUOTE"
        const val MARKET_HISTORY_DAILY_OBSERVATION_TYPE = "MARKET_HISTORY_DAILY"
        const val QUOTE_CACHE_TTL_MS = 10 * 60 * 1000L
    }

    override suspend fun addToWatchlist(assetId: String, note: String?) {
        require(database.assetDao().getAsset(assetId) != null) {
            "Cannot watchlist an asset that does not exist: $assetId"
        }
        database.watchlistDao().add(
            WatchlistEntryEntity(
                assetId = assetId,
                addedAtEpochMs = nowEpochMs(),
                note = note
            )
        )
    }

    override suspend fun toggleWatchlist(assetId: String): Boolean =
        database.withTransaction {
            require(database.assetDao().getAsset(assetId) != null) {
                "Cannot watchlist an asset that does not exist: $assetId"
            }
            if (database.watchlistDao().getEntry(assetId) != null) {
                database.watchlistDao().remove(assetId)
                false
            } else {
                database.watchlistDao().add(
                    WatchlistEntryEntity(
                        assetId = assetId,
                        addedAtEpochMs = nowEpochMs(),
                        note = null
                    )
                )
                true
            }
        }

    override suspend fun removeFromWatchlist(assetId: String) {
        database.watchlistDao().remove(assetId)
    }

    override fun observeWatchlist(): Flow<List<String>> =
        database.watchlistDao().observeEntries().map { entries ->
            entries.map(WatchlistEntryEntity::assetId)
        }
}
