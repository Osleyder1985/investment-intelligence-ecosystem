package com.iie.android.data.market

import kotlin.math.sqrt
import kotlin.math.ln

/**
 * Risk statistics derived from observed daily close prices.
 * Historical VaR/expected shortfall are unavailable until a minimum sample exists.
 */
data class AssetRiskMetrics(
    val observations: Int,
    val dailyReturns: Int,
    val annualizedVolatilityPercent: Double?,
    val maximumDrawdownPercent: Double?,
    val historicalVar95Percent: Double?,
    val historicalExpectedShortfall95Percent: Double?
)

class HistoricalRiskAnalyzer(
    private val minimumVolatilityReturns: Int = 7,
    private val minimumTailReturns: Int = 30
) {
    init {
        require(minimumVolatilityReturns >= 2)
        require(minimumTailReturns >= minimumVolatilityReturns)
    }

    fun analyze(points: List<CoinPaprikaHistoricalPoint>): AssetRiskMetrics {
        val ordered = points.asSequence()
            .filter { it.priceUsd.isFinite() && it.priceUsd > 0.0 && it.timestampEpochMs > 0L }
            .distinctBy { it.timestampEpochMs }
            .sortedBy { it.timestampEpochMs }
            .toList()

        val dailyReturns = ordered.zipWithNext().mapNotNull { (previous, current) ->
            val elapsed = current.timestampEpochMs - previous.timestampEpochMs
            if (elapsed !in 20L * HOUR_MS..28L * HOUR_MS) return@mapNotNull null
            (current.priceUsd / previous.priceUsd) - 1.0
        }.filter { it.isFinite() }

        val volatility = dailyReturns.takeIf { it.size >= minimumVolatilityReturns }?.let { returns ->
            val mean = returns.average()
            val variance = returns.sumOf { (it - mean) * (it - mean) } / (returns.size - 1)
            sqrt(variance) * sqrt(365.0) * 100.0
        }

        var peak = Double.NEGATIVE_INFINITY
        var maxDrawdown = 0.0
        ordered.forEach { point ->
            peak = maxOf(peak, point.priceUsd)
            if (peak > 0.0) maxDrawdown = maxOf(maxDrawdown, (peak - point.priceUsd) / peak)
        }

        val tail = dailyReturns.sorted().takeIf { it.size >= minimumTailReturns }
        val var95 = tail?.let { percentile(it, 0.05) }?.let { maxOf(0.0, -it * 100.0) }
        val expectedShortfall = tail?.let { returns ->
            val threshold = percentile(returns, 0.05)
            returns.filter { it <= threshold }.takeIf { it.isNotEmpty() }
                ?.map { -it }.average().takeIf { it.isFinite() }?.times(100.0)
        }

        return AssetRiskMetrics(
            observations = ordered.size,
            dailyReturns = dailyReturns.size,
            annualizedVolatilityPercent = volatility?.takeIf { it.isFinite() },
            maximumDrawdownPercent = (maxDrawdown * 100.0).takeIf { ordered.size >= 2 },
            historicalVar95Percent = var95?.takeIf { it.isFinite() },
            historicalExpectedShortfall95Percent = expectedShortfall?.takeIf { it.isFinite() }
        )
    }

    private fun percentile(sorted: List<Double>, probability: Double): Double {
        val position = (sorted.size - 1) * probability
        val lower = position.toInt()
        val upper = (lower + 1).coerceAtMost(sorted.lastIndex)
        val fraction = position - lower
        return sorted[lower] + (sorted[upper] - sorted[lower]) * fraction
    }

    private companion object {
        const val HOUR_MS = 60L * 60L * 1000L
    }
}
