package com.iie.android.data.market

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CoinMarketCapKeylessClientTest {
    private val client = CoinMarketCapKeylessClient()
    private val retrievedAt = 1_798_000_000_000L

    private fun payload(item: String, key: String = "1") = """
        {
          "status": {"error_code": 0, "timestamp": "2026-11-01T12:00:00.000Z"},
          "data": {"$key": $item}
        }
    """.trimIndent()

    private fun quote(
        id: Int = 1,
        name: String = "Bitcoin",
        symbol: String = "BTC",
        supplies: String = "\"circulating_supply\": 19000000, \"total_supply\": 19000000, \"max_supply\": 21000000,",
        marketValues: String = "\"market_cap\": 1230000000000, \"fully_diluted_market_cap\": 1365000000000,"
    ) = """
        {
          "id": $id,
          "name": "$name",
          "symbol": "$symbol",
          $supplies
          "last_updated": "2026-11-01T11:59:00.000Z",
          "quote": {
            "USD": {
              "price": 65000.25,
              $marketValues
              "last_updated": "2026-11-01T11:59:00.000Z"
            }
          }
        }
    """.trimIndent()

    @Test fun parsesQuoteAndFundamentalMetricsUsingCanonicalProviderId() {
        val result = client.parse(payload(quote()), 1L, retrievedAt)
        assertEquals(1L, result.providerId)
        assertEquals("BTC", result.symbol)
        assertEquals(65_000.25, result.priceUsd, 0.0)
        assertEquals(19_000_000.0, result.circulatingSupply!!, 0.0)
        assertEquals(21_000_000.0, result.maxSupply!!, 0.0)
        assertEquals(1_230_000_000_000.0, result.marketCapUsd!!, 0.0)
        assertEquals(1_365_000_000_000.0, result.fullyDilutedMarketCapUsd!!, 0.0)
        assertEquals(retrievedAt, result.retrievedAtEpochMs)
    }

    @Test fun acceptsArrayDataAndUsdQuoteArrayShapes() {
        val item = quote().replace(
            "\"quote\": {\n            \"USD\": {",
            "\"quote\": [{\n            \"symbol\": \"USD\","
        ).replace(
            "\n          }\n        }",
            "\n          }]\n        }"
        )
        val arrayPayload = """
            {"status":{"error_code":"0"},"data":[$item]}
        """.trimIndent()
        val result = client.parse(arrayPayload, 1L, retrievedAt)
        assertEquals(65_000.25, result.priceUsd, 0.0)
    }

    @Test fun absentSupplyAndValuationRemainNullRatherThanBecomingZero() {
        val result = client.parse(
            payload(quote(supplies = "", marketValues = "")), 1L, retrievedAt
        )
        assertNull(result.circulatingSupply)
        assertNull(result.totalSupply)
        assertNull(result.maxSupply)
        assertNull(result.marketCapUsd)
        assertNull(result.fullyDilutedMarketCapUsd)
    }

    @Test fun rejectsAResponseForTheWrongProviderId() {
        assertThrows(IOException::class.java) {
            client.parse(payload(quote(id = 1027)), 1L, retrievedAt)
        }
    }

    @Test fun rejectsProviderApiErrors() {
        val error = """{"status":{"error_code":1001},"data":{}}"""
        assertThrows(IOException::class.java) {
            client.parse(error, 1L, retrievedAt)
        }
    }

    @Test fun rejectsNonFiniteAndNegativeSupply() {
        val invalid = quote(supplies = "\"circulating_supply\": -1,")
        assertThrows(IOException::class.java) {
            client.parse(payload(invalid), 1L, retrievedAt)
        }
    }

    @Test fun rejectsMissingPriceAndInvalidTimestamp() {
        val noPrice = quote().replace("\"price\": 65000.25,", "")
        assertThrows(IOException::class.java) {
            client.parse(payload(noPrice), 1L, retrievedAt)
        }
        val badTimestamp = quote().replace("2026-11-01T11:59:00.000Z", "not-a-time")
        assertThrows(IOException::class.java) {
            client.parse(payload(badTimestamp), 1L, retrievedAt)
        }
    }
}
