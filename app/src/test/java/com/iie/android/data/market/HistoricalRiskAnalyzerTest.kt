package com.iie.android.data.market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HistoricalRiskAnalyzerTest {
    private val analyzer = HistoricalRiskAnalyzer()
    private val day = 86_400_000L

    private fun points(prices: List<Double>) = prices.mapIndexed { index, price ->
        CoinPaprikaHistoricalPoint("btc-bitcoin", 1_700_000_000_000L + index * day, price, null, null, 1_700_000_000_000L)
    }

    @Test fun calculatesAnnualizedVolatilityFromConsecutiveDailyReturns() {
        val result = analyzer.analyze(points(listOf(100.0, 101.0, 100.0, 102.0, 101.0, 103.0, 102.0, 104.0, 103.0)))
        assertEquals(8, result.dailyReturns)
        assertNotNull(result.annualizedVolatilityPercent)
    }

    @Test fun calculatesPeakToTroughMaximumDrawdown() {
        val result = analyzer.analyze(points(listOf(100.0, 120.0, 90.0, 95.0)))
        assertEquals(25.0, result.maximumDrawdownPercent!!, 0.000001)
    }

    @Test fun requiresThirtyDailyReturnsForHistoricalTailRisk() {
        val short = analyzer.analyze(points((0..20).map { 100.0 + it }))
        assertNull(short.historicalVar95Percent)
        assertNull(short.historicalExpectedShortfall95Percent)
        val enough = analyzer.analyze(points((0..31).map { 100.0 + it * 0.2 + if (it == 20) -5.0 else 0.0 }))
        assertNotNull(enough.historicalVar95Percent)
        assertNotNull(enough.historicalExpectedShortfall95Percent)
    }

    @Test fun ignoresGapsAndDuplicateTimestampsForReturnCalculations() {
        val all = points(listOf(100.0, 110.0, 120.0, 130.0, 140.0, 150.0, 160.0, 170.0, 180.0))
        val withGap = all.mapIndexed { index, point ->
            if (index >= 4) point.copy(timestampEpochMs = point.timestampEpochMs + day * 4) else point
        } + all.first()
        val result = analyzer.analyze(withGap)
        // The five-day gap is excluded, while the seven remaining daily intervals are retained.
        assertEquals(7, result.dailyReturns)
        assertNotNull(result.annualizedVolatilityPercent)
    }

    @Test fun rejectsInvalidPricesAndReturnsUnknownRiskForInsufficientHistory() {
        val result = analyzer.analyze(listOf(
            CoinPaprikaHistoricalPoint("btc-bitcoin", 1L, -1.0, null, null, 1L),
            CoinPaprikaHistoricalPoint("btc-bitcoin", 2L, Double.NaN, null, null, 2L)
        ))
        assertEquals(0, result.observations)
        assertNull(result.maximumDrawdownPercent)
        assertNull(result.annualizedVolatilityPercent)
    }
}
