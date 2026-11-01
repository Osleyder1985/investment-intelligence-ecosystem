package com.iie.android.data.market

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CoinMarketCapHistoricalQuotesClientTest {
    private val client = CoinMarketCapHistoricalQuotesClient()
    private val start = LocalDate.parse("2026-10-01")
    private val end = LocalDate.parse("2026-10-02")
    private val retrieved = 1_798_900_000_000L
    private val payload = """{
      "status":{"error_code":0},
      "data":{"id":1,"quotes":[
        {"timestamp":"2026-10-02T00:00:00.000Z","quote":{"USD":{"price":2.0,"volume_24h":200.0,"market_cap":2000.0}}},
        {"timestamp":"2026-10-01T00:00:00.000Z","quote":{"USD":{"price":1.0,"volume_24h":100.0,"market_cap":1000.0}}}
      ]}
    }"""


    @Test fun historicalEndpointProbeUsesSevenCompletedUtcDays() {
        val retrievedAt = java.time.Instant.parse("2026-11-01T15:49:00Z").toEpochMilli()
        val (start, end) = client.historicalProbeWindow(retrievedAt)
        assertEquals(LocalDate.parse("2026-10-25"), start)
        assertEquals(LocalDate.parse("2026-10-31"), end)
        assertEquals(7L, java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1L)
    }

    @Test fun parsesKeyInfoUsageWithoutExposingCredentialMaterial() {
        val validator = CoinMarketCapApiKeyValidator()
        val result = validator.parseKeyInfo(
            """{
              "status":{"error_code":0},
              "data":{
                "plan":{"credit_limit_monthly":15000,"rate_limit_minute":50},
                "usage":{
                  "current_month":{"credits_left":14980},
                  "current_day":{"credits_left":3900},
                  "current_minute":{"requests_left":47}
                }
              }
            }"""
        )
        assertEquals(15_000L, result.monthlyCreditLimit)
        assertEquals(14_980L, result.monthlyCreditsLeft)
        assertEquals(3_900L, result.dailyCreditsLeft)
        assertEquals(50L, result.minuteRateLimit)
        assertEquals(47L, result.minuteRequestsLeft)
    }

    @Test fun rejectsInvalidKeyInfoAndMalformedQuotaMetadata() {
        val validator = CoinMarketCapApiKeyValidator()
        assertThrows(java.io.IOException::class.java) {
            validator.parseKeyInfo("""{"status":{"error_code":1001},"data":{}}""")
        }
        assertThrows(java.io.IOException::class.java) {
            validator.parseKeyInfo(
                """{"status":{"error_code":0},"data":{"plan":{"credit_limit_monthly":"unknown"}}}"""
            )
        }
    }

    @Test fun parsesDailyQuotesAndPreservesCmcSourceIdentity() {
        val points = client.parse(payload, 1L, retrieved, start, end)
        assertEquals(2, points.size)
        assertEquals("1", points.first().providerId)
        assertEquals(CoinMarketCapHistoricalQuotesClient.SOURCE_ID, points.first().sourceId)
        assertEquals(1.0, points.first().priceUsd, 0.000001)
        assertEquals(100.0, points.first().volume24hUsd!!, 0.000001)
        assertEquals(retrieved, points.first().retrievedAtEpochMs)
    }

    @Test fun preservesMissingOptionalMetricsAsNull() {
        val points = client.parse(
            """{"status":{"error_code":0},"data":{"id":1,"quotes":[
              {"timestamp":"2026-10-01T00:00:00Z","quote":{"USD":{"price":1.0}}}
            ]}}""", 1L, retrieved, start, end
        )
        assertNull(points.single().volume24hUsd)
        assertNull(points.single().marketCapUsd)
    }

    @Test fun rejectsProviderErrorsWrongAssetAndDuplicateTimestamps() {
        assertThrows(java.io.IOException::class.java) {
            client.parse("""{"status":{"error_code":1002},"data":{}}""", 1L, retrieved, start, end)
        }
        assertThrows(java.io.IOException::class.java) {
            client.parse(payload.replace("\"id\":1", "\"id\":2"), 1L, retrieved, start, end)
        }
        val duplicate = payload.replace(
            "\"timestamp\":\"2026-10-01T00:00:00.000Z\"",
            "\"timestamp\":\"2026-10-02T00:00:00.000Z\""
        )
        assertThrows(java.io.IOException::class.java) {
            client.parse(duplicate, 1L, retrieved, start, end)
        }
    }

    @Test fun rejectsInvalidPricesAndOutOfWindowPoints() {
        val invalid = payload.replace("\"price\":1.0", "\"price\":0.0")
        assertThrows(java.io.IOException::class.java) {
            client.parse(invalid, 1L, retrieved, start, end)
        }
        val outside = payload.replace("2026-10-01T00:00:00.000Z", "2026-09-30T00:00:00.000Z")
        assertThrows(java.io.IOException::class.java) {
            client.parse(outside, 1L, retrieved, start, end)
        }
    }
}
