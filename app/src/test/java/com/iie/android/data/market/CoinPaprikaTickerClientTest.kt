package com.iie.android.data.market

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CoinPaprikaTickerClientTest {
    private val client = CoinPaprikaTickerClient()
    private val timestamp = "2026-10-31T18:00:00Z"
    private val retrievedAt = 1_793_474_400_000L

    private fun payload(
        id: String = "usdc-usd-coin",
        symbol: String = "USDC",
        price: String = "1.0001",
        updated: String = timestamp
    ) = """{"id":"$id","symbol":"$symbol","last_updated":"$updated","quotes":{"USD":{"price":$price}}}"""

    @Test fun parsesValidTickerAndKeepsBothTimestamps() {
        val quote = client.parse(payload(), "usdc-usd-coin", "USDC", retrievedAt)
        assertEquals("usdc-usd-coin", quote.providerId)
        assertEquals("USDC", quote.symbol)
        assertEquals(1.0001, quote.priceUsd, 0.0000001)
        assertEquals(retrievedAt, quote.retrievedAtEpochMs)
    }

    @Test fun rejectsMismatchedProviderId() {
        assertThrows(IOException::class.java) {
            client.parse(payload(id = "usdt-tether"), "usdc-usd-coin", "USDC", retrievedAt)
        }
    }

    @Test fun rejectsWrongSymbol() {
        assertThrows(IOException::class.java) {
            client.parse(payload(symbol = "USDT"), "usdc-usd-coin", "USDC", retrievedAt)
        }
    }

    @Test fun rejectsStringPrice() {
        assertThrows(IOException::class.java) {
            client.parse(payload(price = "\"1.0\""), "usdc-usd-coin", "USDC", retrievedAt)
        }
    }

    @Test fun rejectsNonPositiveAndNonFinitePrices() {
        listOf("0", "-1", "1e999").forEach { price ->
            assertThrows(IOException::class.java) {
                client.parse(payload(price = price), "usdc-usd-coin", "USDC", retrievedAt)
            }
        }
    }

    @Test fun rejectsMissingTimestampAndMalformedTimestamp() {
        assertThrows(IOException::class.java) {
            client.parse("""{"id":"usdc-usd-coin","symbol":"USDC","quotes":{"USD":{"price":1}}}""",
                "usdc-usd-coin", "USDC", retrievedAt)
        }
        assertThrows(IOException::class.java) {
            client.parse(payload(updated = "not-a-date"), "usdc-usd-coin", "USDC", retrievedAt)
        }
    }

    @Test fun rejectsMalformedJson() {
        assertThrows(IOException::class.java) {
            client.parse("{", "usdc-usd-coin", "USDC", retrievedAt)
        }
    }
}
