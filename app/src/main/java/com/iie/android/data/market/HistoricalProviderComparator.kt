package com.iie.android.data.market

import kotlin.math.abs

/**
 * Compares independently sourced USD daily observations only at identical UTC instants.
 * The caller must supply observations for the same canonical IIE asset.
 */
data class HistoricalProviderComparisonResult(
    val matchedObservations: Int,
    val latestTimestampEpochMs: Long?,
    val coinPaprikaPriceUsd: Double?,
    val coinMarketCapPriceUsd: Double?,
    val latestSymmetricDivergencePercent: Double?,
    val coinPaprikaRetrievedAtEpochMs: Long?,
    val coinMarketCapRetrievedAtEpochMs: Long?
)

data class HistoricalProviderSeriesQuality(
    val sourceId: String,
    val receivedObservations: Int,
    val validObservations: Int,
    val invalidObservations: Int,
    val conflictingTimestamps: Int,
    val usableTimestamps: Int,
    val latestTimestampEpochMs: Long?,
    val latestRetrievedAtEpochMs: Long?
)

class HistoricalProviderComparator {
    fun quality(
        points: List<CoinPaprikaHistoricalPoint>,
        expectedSourceId: String
    ): HistoricalProviderSeriesQuality {
        require(expectedSourceId == "source:coinpaprika" || expectedSourceId == "source:coinmarketcap")
        val received = points.filter { it.sourceId == expectedSourceId }
        val valid = received.filter {
            it.providerId.isNotBlank() &&
                it.timestampEpochMs > 0L &&
                it.priceUsd.isFinite() && it.priceUsd > 0.0 &&
                it.retrievedAtEpochMs >= it.timestampEpochMs
        }
        val groups = valid.groupBy { it.timestampEpochMs }
        val conflicts = groups.values.count { sameTime ->
            sameTime.map { it.priceUsd }.distinct().size > 1
        }
        val usable = groups.filterValues { sameTime ->
            sameTime.map { it.priceUsd }.distinct().size == 1
        }
        val latest = usable.keys.maxOrNull()
        return HistoricalProviderSeriesQuality(
            sourceId = expectedSourceId,
            receivedObservations = received.size,
            validObservations = valid.size,
            invalidObservations = received.size - valid.size,
            conflictingTimestamps = conflicts,
            usableTimestamps = usable.size,
            latestTimestampEpochMs = latest,
            latestRetrievedAtEpochMs = latest?.let { timestamp ->
                usable.getValue(timestamp).maxOf { it.retrievedAtEpochMs }
            }
        )
    }
    fun compare(
        coinPaprika: List<CoinPaprikaHistoricalPoint>,
        coinMarketCap: List<CoinPaprikaHistoricalPoint>
    ): HistoricalProviderComparisonResult {
        val paprika = unambiguousByTimestamp(coinPaprika, "source:coinpaprika")
        val cmc = unambiguousByTimestamp(coinMarketCap, "source:coinmarketcap")
        val commonTimestamps = paprika.keys.intersect(cmc.keys).sorted()
        val latestTimestamp = commonTimestamps.lastOrNull()
        val paprikaPrice = latestTimestamp?.let(paprika::get)
        val cmcPrice = latestTimestamp?.let(cmc::get)
        val divergence = if (paprikaPrice != null && cmcPrice != null) {
            (200.0 * abs(paprikaPrice - cmcPrice) / (paprikaPrice + cmcPrice))
                .takeIf { it.isFinite() }
        } else null
        return HistoricalProviderComparisonResult(
            matchedObservations = commonTimestamps.size,
            latestTimestampEpochMs = latestTimestamp,
            coinPaprikaPriceUsd = paprikaPrice,
            coinMarketCapPriceUsd = cmcPrice,
            latestSymmetricDivergencePercent = divergence,
            coinPaprikaRetrievedAtEpochMs = latestTimestamp?.let {
                latestRetrievedAt(coinPaprika, "source:coinpaprika", it, paprikaPrice)
            },
            coinMarketCapRetrievedAtEpochMs = latestTimestamp?.let {
                latestRetrievedAt(coinMarketCap, "source:coinmarketcap", it, cmcPrice)
            }
        )
    }

    private fun latestRetrievedAt(
        points: List<CoinPaprikaHistoricalPoint>,
        sourceId: String,
        timestamp: Long,
        price: Double?
    ): Long? = points.asSequence()
        .filter {
            it.sourceId == sourceId &&
                it.timestampEpochMs == timestamp &&
                price != null && it.priceUsd == price &&
                it.retrievedAtEpochMs >= timestamp
        }
        .maxOfOrNull { it.retrievedAtEpochMs }

    private fun unambiguousByTimestamp(
        points: List<CoinPaprikaHistoricalPoint>,
        expectedSourceId: String
    ): Map<Long, Double> = points.asSequence()
        .filter {
            it.sourceId == expectedSourceId &&
                it.providerId.isNotBlank() &&
                it.timestampEpochMs > 0L &&
                it.priceUsd.isFinite() &&
                it.priceUsd > 0.0 &&
                it.retrievedAtEpochMs >= it.timestampEpochMs
        }
        .groupBy { it.timestampEpochMs }
        .mapNotNull { (timestamp, sameTimestamp) ->
            val price = sameTimestamp.first().priceUsd
            if (sameTimestamp.all { it.priceUsd == price }) timestamp to price else null
        }
        .toMap()
}
