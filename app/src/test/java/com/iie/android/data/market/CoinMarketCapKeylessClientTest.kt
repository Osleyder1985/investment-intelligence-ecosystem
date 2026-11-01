package com.iie.android.data.market

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CoinMarketCapKeylessClientTest {
    private val client = CoinMarketCapKeylessClient()

    private fun payload(data: String) = """
        {
          "status": {"error_code": 0, "timestamp": "2026-11-01T12:00:00.000Z"},
          "data": [$data]
        }
    """.trimIndent()

    private fun listing(
        id: Int = 1,
        name: String = "Bitcoin",
        symbol: String = "BTC",
        supply: String = "\"circulating_supply\": 19000000, \"total_supply\": 19000000, \"max_supply\": 21000000,"
    ) = """
        {
          "id": $id,
          "name": "$name",
          "symbol": "$symbol",
          $supply
          "last_updated": "2026-11-01T11:59:00.000Z",
          "quote": {"USD": {"price": 65000.25}}
        }
    """.trimIndent()

    @Test fun parsesPriceAndSupplyWithProviderTimestamps() {
        val result = client.parse(
            payload(listing()), setOf("BTC"), 1_798_000_000_000L
        ).single()
        assertEquals(1L, result.providerId)
        assertEquals("BTC", result.symbol)
        assertEquals(65_000.25, result.priceUsd, 0.0)
        assertEquals(19_000_000.0, result.circulatingSupply!!, 0.0)
        assertEquals(21_000_000.0, result.maxSupply!!, 0.0)
        assertEquals(1_798_000_000_000L, result.retrievedAtEpochMs)
    }

    @Test fun parsesDocumentedV3QuoteArray() {
        val arrayListing = listing().replace(
            "\"quote\": {\"USD\": {\"price\": 65000.25}}",
            "\"quote\": [{\"symbol\": \"USD\", \"price\": 65000.25}]"
        )
        val result = client.parse(
            payload(arrayListing), setOf("BTC"), 1_798_000_000_000L
        ).single()
        assertEquals(65_000.25, result.priceUsd, 0.0)
    }

    @Test fun absentSupplyRemainsNullRatherThanBecomingZero() {
        val result = client.parse(
            payload(listing(supply="")), setOf("BTC"), 1_798_000_000_000L
        ).single()
        assertNull(result.circulatingSupply)
        assertNull(result.totalSupply)
        assertNull(result.maxSupply)
    }

    @Test fun ignoresUnrequestedAssets() {
        val result = client.parse(
            payload(listing() + "," + listing(1027, "Ethereum", "ETH")),
            setOf("ETH"),
            1_798_000_000_000L
        )
        assertEquals(listOf("ETH"), result.map { it.symbol })
    }

    @Test fun rejectsAmbiguousSymbolsInsteadOfGuessingIdentity() {
        val duplicate = listing() + "," + listing(999, "Wrapped Bitcoin", "BTC")
        assertThrows(IOException::class.java) {
            client.parse(payload(duplicate), setOf("BTC"), 1_798_000_000_000L)
        }
    }

    @Test fun rejectsProviderApiErrors() {
        val error = """{"status":{"error_code":1001},"data":[]}"""
        assertThrows(IOException::class.java) {
            client.parse(error, setOf("BTC"), 1_798_000_000_000L)
        }
    }

    @Test fun rejectsNonFiniteAndNegativeSupply() {
        val invalid = listing(supply = "\"circulating_supply\": -1,")
        assertThrows(IOException::class.java) {
            client.parse(payload(invalid), setOf("BTC"), 1_798_000_000_000L)
        }
    }
}
