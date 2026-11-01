package com.iie.android.data.market

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CoinPaprikaHistoricalTickerClientTest {
    private val client = CoinPaprikaHistoricalTickerClient()
    private val start = LocalDate.parse("2026-10-01")
    private val retrieved = 1_798_900_000_000L
    private val valid = """[
      {"timestamp":"2026-10-02T00:00:00Z","price":1.001,"volume_24h":1200.5,"market_cap":3000000},
      {"timestamp":"2026-10-01T00:00:00Z","price":0.999,"volume_24h":1100.0,"market_cap":2900000}
    ]"""

    @Test fun parsesAndSortsDailyPointsAndPreservesAcquisitionTime() {
        val points = client.parse(valid, "usdc-usd-coin", retrieved, start, LocalDate.parse("2026-10-02"))
        assertEquals(2, points.size)
        assertEquals("usdc-usd-coin", points.first().providerId)
        assertEquals(CoinPaprikaHistoricalTickerClient.SOURCE_ID, points.first().sourceId)
        assertEquals(0.999, points.first().priceUsd, 0.000001)
        assertEquals(retrieved, points.first().retrievedAtEpochMs)
        assertEquals(1_200.5, points.last().volume24hUsd!!, 0.000001)
    }

    @Test fun allowsMissingOptionalVolumeAndMarketCap() {
        val points = client.parse(
            """[{"timestamp":"2026-10-01T00:00:00Z","price":1.0}]""",
            "usdt-tether", retrieved, start
        )
        assertNull(points.single().volume24hUsd)
        assertNull(points.single().marketCapUsd)
    }

    @Test fun rejectsInvalidPriceAndNonNumericOptionalMetrics() {
        listOf(
            """[{"timestamp":"2026-10-01T00:00:00Z","price":0}]""",
            """[{"timestamp":"2026-10-01T00:00:00Z","price":1,"volume_24h":"10"}]"""
        ).forEach { payload ->
            assertThrows(java.io.IOException::class.java) {
                client.parse(payload, "usdc-usd-coin", retrieved, start)
            }
        }
    }

    @Test fun rejectsDuplicateTimestamps() {
        val duplicate = """[
          {"timestamp":"2026-10-01T00:00:00Z","price":1.0},
          {"timestamp":"2026-10-01T00:00:00Z","price":1.1}
        ]"""
        assertThrows(java.io.IOException::class.java) {
            client.parse(duplicate, "usdc-usd-coin", retrieved, start)
        }
    }

    @Test fun rejectsPointsOutsideRequestedWindow() {
        val outside = """[{"timestamp":"2026-09-30T00:00:00Z","price":1.0}]"""
        assertThrows(java.io.IOException::class.java) {
            client.parse(outside, "usdc-usd-coin", retrieved, start)
        }
    }

    @Test fun rejectsMalformedJsonAndOversizedPointCount() {
        assertThrows(java.io.IOException::class.java) {
            client.parse("{", "usdc-usd-coin", retrieved, start)
        }
        val tooMany = (0..500).joinToString(",", "[", "]") {
            """{"timestamp":"2026-10-01T00:00:00Z","price":1.0}"""
        }
        assertThrows(java.io.IOException::class.java) {
            client.parse(tooMany, "usdc-usd-coin", retrieved, start)
        }
    }
}
