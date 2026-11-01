package com.iie.android.data.market

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CoinMarketCapAuthenticatedClientTest {
    private val client = CoinMarketCapAuthenticatedClient()

    private fun payload(
        data: String,
        errorCode: Int = 0
    ) = """{"status":{"error_code":$errorCode},"data":[$data]}"""

    private fun bitcoin(
        price: String = "67000.25",
        timestamp: String = "2026-11-01T11:59:00.000Z"
    ) = """
        {
          "id": 1,
          "name": "Bitcoin",
          "symbol": "BTC",
          "circulating_supply": 19500000,
          "total_supply": 19500000,
          "max_supply": 21000000,
          "quote": [{
            "symbol": "USD",
            "price": $price,
            "market_cap": 1300000000000,
            "fully_diluted_market_cap": 1400000000000,
            "last_updated": "$timestamp"
          }]
        }
    """.trimIndent()

    @Test
    fun parsesAuthenticatedV3QuoteResponse() {
        val result = client.parse(
            payload(bitcoin()),
            "BTC",
            1_798_000_000_000L
        )
        assertEquals(1L, result.providerId)
        assertEquals("Bitcoin", result.name)
        assertEquals("BTC", result.symbol)
        assertEquals(67_000.25, result.priceUsd, 0.0)
        assertEquals(19_500_000.0, result.circulatingSupply!!, 0.0)
        assertEquals(19_500_000.0, result.totalSupply!!, 0.0)
        assertEquals(21_000_000.0, result.maxSupply!!, 0.0)
        assertEquals(1_300_000_000_000.0, result.marketCapUsd!!, 0.0)
        assertEquals(1_400_000_000_000.0, result.fullyDilutedMarketCapUsd!!, 0.0)
        assertEquals(1_798_000_000_000L, result.retrievedAtEpochMs)
    }

    @Test
    fun missingSupplyFieldsRemainNull() {
        val noSupply = bitcoin()
            .replace("\"circulating_supply\": 19500000,", "")
            .replace("\"total_supply\": 19500000,", "")
            .replace("\"max_supply\": 21000000,", "")
        val result = client.parse(payload(noSupply), "BTC", 1_798_000_000_000L)
        org.junit.Assert.assertNull(result.circulatingSupply)
        org.junit.Assert.assertNull(result.totalSupply)
        org.junit.Assert.assertNull(result.maxSupply)
    }

    @Test
    fun rejectsNegativeSupplyValues() {
        val invalid = bitcoin().replace("\"circulating_supply\": 19500000", "\"circulating_supply\": -1")
        assertThrows(IOException::class.java) {
            client.parse(payload(invalid), "BTC", 1_798_000_000_000L)
        }
    }

    @Test
    fun rejectsNumericProviderIdMismatch() {
        assertThrows(IOException::class.java) {
            client.parse(payload(bitcoin()), "BTC", 1_798_000_000_000L, expectedProviderId = 1027L)
        }
    }

    @Test
    fun rejectsMissingSymbolInsteadOfGuessing() {
        assertThrows(IOException::class.java) {
            client.parse(payload(bitcoin().replace("\"BTC\"", "\"OTHER\"")), "BTC", 1_798_000_000_000L)
        }
    }

    @Test
    fun rejectsAmbiguousSymbol() {
        assertThrows(IOException::class.java) {
            client.parse(payload(bitcoin() + "," + bitcoin().replace("\"Bitcoin\"", "\"Wrapped Bitcoin\"")), "BTC", 1_798_000_000_000L)
        }
    }

    @Test
    fun rejectsProviderApiErrors() {
        assertThrows(IOException::class.java) {
            client.parse(payload(bitcoin(), errorCode = 1001), "BTC", 1_798_000_000_000L)
        }
    }

    @Test
    fun rejectsNonFiniteOrNonPositivePrices() {
        assertThrows(IOException::class.java) {
            client.parse(payload(bitcoin(price = "0")), "BTC", 1_798_000_000_000L)
        }
    }

    @Test
    fun rejectsFutureTimestamps() {
        assertThrows(IOException::class.java) {
            client.parse(
                payload(bitcoin(timestamp = "2027-11-01T11:59:00.000Z")),
                "BTC",
                1_798_000_000_000L
            )
        }
    }
}
