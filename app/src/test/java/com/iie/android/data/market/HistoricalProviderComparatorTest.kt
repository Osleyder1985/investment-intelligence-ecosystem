package com.iie.android.data.market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoricalProviderComparatorTest {
    private val comparator = HistoricalProviderComparator()
    private val timestamp = 1_800_000_000_000L
    private val day = 86_400_000L

    private fun point(source: String, time: Long, price: Double) =
        CoinPaprikaHistoricalPoint("provider-asset", time, price, null, null, time, source)

    @Test fun comparesOnlyIdenticalUtcTimestampsAndCalculatesSymmetricDivergence() {
        val result = comparator.compare(
            listOf(point("source:coinpaprika", timestamp, 100.0)),
            listOf(point("source:coinmarketcap", timestamp, 101.0))
        )
        assertEquals(1, result.matchedObservations)
        assertEquals(timestamp, result.latestTimestampEpochMs ?: -1L)
        assertEquals(100.0, result.coinPaprikaPriceUsd!!, 0.0)
        assertEquals(101.0, result.coinMarketCapPriceUsd!!, 0.0)
        assertEquals(200.0 / 201.0, result.latestSymmetricDivergencePercent!!, 0.000001)
    }

    @Test fun doesNotMatchDifferentInstantsEvenWhenTheyFallOnTheSameUtcDate() {
        val result = comparator.compare(
            listOf(point("source:coinpaprika", timestamp, 100.0)),
            listOf(point("source:coinmarketcap", timestamp + 1_000L, 101.0))
        )
        assertEquals(0, result.matchedObservations)
        assertNull(result.latestTimestampEpochMs)
        assertNull(result.latestSymmetricDivergencePercent)
    }

    @Test fun excludesConflictingDuplicateTimestampsButKeepsIdenticalDuplicates() {
        val result = comparator.compare(
            listOf(
                point("source:coinpaprika", timestamp, 100.0),
                point("source:coinpaprika", timestamp, 100.0),
                point("source:coinpaprika", timestamp + day, 110.0),
                point("source:coinpaprika", timestamp + day, 111.0)
            ),
            listOf(
                point("source:coinmarketcap", timestamp, 101.0),
                point("source:coinmarketcap", timestamp + day, 112.0)
            )
        )
        assertEquals(1, result.matchedObservations)
        assertEquals(timestamp, result.latestTimestampEpochMs)
    }

    @Test fun rejectsWrongSourcesInvalidPricesAndMissingObservations() {
        val result = comparator.compare(
            listOf(
                point("source:coinmarketcap", timestamp, 100.0),
                point("source:coinpaprika", timestamp + day, Double.NaN),
                point("source:coinpaprika", timestamp + 2 * day, -1.0)
            ),
            listOf(point("source:coinpaprika", timestamp, 101.0))
        )
        assertEquals(0, result.matchedObservations)
        assertNull(result.latestSymmetricDivergencePercent)
    }

    @Test fun comparesLatestOfSeveralExactTimestampPairs() {
        val result = comparator.compare(
            listOf(
                point("source:coinpaprika", timestamp, 100.0),
                point("source:coinpaprika", timestamp + day, 110.0)
            ),
            listOf(
                point("source:coinmarketcap", timestamp, 101.0),
                point("source:coinmarketcap", timestamp + day, 111.0)
            )
        )
        assertEquals(2, result.matchedObservations)
        assertEquals(timestamp + day, result.latestTimestampEpochMs ?: -1L)
        assertEquals(110.0, result.coinPaprikaPriceUsd!!, 0.0)
        assertEquals(111.0, result.coinMarketCapPriceUsd!!, 0.0)
    }
    @Test fun reportsPerProviderCoverageInvalidRowsConflictsAndRetrievalFreshness() {
        val result = comparator.quality(
            listOf(
                point("source:coinpaprika", timestamp, 100.0),
                point("source:coinpaprika", timestamp + day, Double.NaN),
                point("source:coinpaprika", timestamp + 2 * day, 120.0),
                point("source:coinpaprika", timestamp + 2 * day, 121.0),
                point("source:coinmarketcap", timestamp, 101.0)
            ),
            "source:coinpaprika"
        )
        assertEquals(4, result.receivedObservations)
        assertEquals(3, result.validObservations)
        assertEquals(1, result.invalidObservations)
        assertEquals(1, result.conflictingTimestamps)
        assertEquals(1, result.usableTimestamps)
        assertEquals(timestamp, result.latestTimestampEpochMs ?: -1L)
        assertEquals(timestamp, result.latestRetrievedAtEpochMs ?: -1L)
    }

}
