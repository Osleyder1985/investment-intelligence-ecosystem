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
    val latestSymmetricDivergencePercent: Double?
)

class HistoricalProviderComparator {
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
            latestSymmetricDivergencePercent = divergence
        )
    }

    private fun unambiguousByTimestamp(
        points: List<CoinPaprikaHistoricalPoint>,
        expectedSourceId: String
    ): Map<Long, Double> = points.asSequence()
        .filter {
            it.sourceId == expectedSourceId &&
                it.timestampEpochMs > 0L &&
                it.priceUsd.isFinite() &&
                it.priceUsd > 0.0
        }
        .groupBy { it.timestampEpochMs }
        .mapNotNull { (timestamp, sameTimestamp) ->
            val price = sameTimestamp.first().priceUsd
            if (sameTimestamp.all { it.priceUsd == price }) timestamp to price else null
        }
        .toMap()
}
