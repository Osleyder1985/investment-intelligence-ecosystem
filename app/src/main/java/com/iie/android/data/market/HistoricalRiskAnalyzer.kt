package com.iie.android.data.market

import kotlin.math.sqrt

/**
 * Risk statistics derived from observed daily price snapshots.
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

/**
 * A canonical historical series is always sourced from exactly one provider.
 * The priority list is an explicit policy; observations from different sources
 * are never averaged or interleaved to manufacture a price path.
 */
data class CanonicalHistoricalSeries(
    val sourceId: String,
    val points: List<CoinPaprikaHistoricalPoint>
)

class HistoricalRiskAnalyzer(
    private val minimumVolatilityReturns: Int = 7,
    private val minimumTailReturns: Int = 30
) {
    init {
        require(minimumVolatilityReturns >= 2)
        require(minimumTailReturns >= minimumVolatilityReturns)
    }

    /**
     * Analyze one provider's observations. Mixing sources is a caller error:
     * use analyzeBySource() or selectCanonicalSeries() first.
     */
    fun analyze(points: List<CoinPaprikaHistoricalPoint>): AssetRiskMetrics {
        val sourceIds = points.map { it.sourceId }.distinct()
        require(sourceIds.size <= 1 && sourceIds.none { it.isBlank() }) {
            "Historical risk analysis requires one explicit data source; separate providers before analysis"
        }
        // A timestamp with conflicting prices inside this provider is ambiguous:
        // discard the entire bucket
        // rather than letting input order decide which observation survives.
        val ordered = points.asSequence()
            .filter { it.sourceId.isNotBlank() && it.priceUsd.isFinite() && it.priceUsd > 0.0 && it.timestampEpochMs > 0L }
            .groupBy { it.timestampEpochMs }
            .values
            .mapNotNull { sameTimestamp ->
                val first = sameTimestamp.first()
                first.takeIf { sameTimestamp.all { point -> point.priceUsd == first.priceUsd } }
            }
            .sortedBy { it.timestampEpochMs }

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
            returns.filter { it <= threshold }.map { -it }
                .takeIf { it.isNotEmpty() }?.average()?.times(100.0)
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

    /** Preserve and analyze each provider independently, including same-time price divergences. */
    fun analyzeBySource(points: List<CoinPaprikaHistoricalPoint>): Map<String, AssetRiskMetrics> =
        points.asSequence()
            .filter { it.sourceId.isNotBlank() }
            .groupBy { it.sourceId }
            .toSortedMap()
            .mapValues { (_, sourcePoints) -> analyze(sourcePoints) }

    /**
     * Select one complete provider series using caller-supplied priority.
     * No implicit averaging, timestamp-level cross-provider deduplication, or fallback
     * to an unlisted provider is permitted.
     */
    fun selectCanonicalSeries(
        points: List<CoinPaprikaHistoricalPoint>,
        sourcePriority: List<String>
    ): CanonicalHistoricalSeries? {
        require(sourcePriority.none { it.isBlank() } && sourcePriority.distinct().size == sourcePriority.size) {
            "Source priority must contain unique, non-blank source IDs"
        }
        val grouped = points.filter { it.sourceId.isNotBlank() }.groupBy { it.sourceId }
        for (sourceId in sourcePriority) {
            val candidate = grouped[sourceId] ?: continue
            val valid = candidate.filter {
                it.timestampEpochMs > 0L && it.priceUsd.isFinite() && it.priceUsd > 0.0
            }.sortedBy { it.timestampEpochMs }
            if (valid.size >= 2) return CanonicalHistoricalSeries(sourceId, valid)
        }
        return null
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
