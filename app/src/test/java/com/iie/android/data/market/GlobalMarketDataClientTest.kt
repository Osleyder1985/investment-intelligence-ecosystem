package com.iie.android.data.market

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GlobalMarketDataClientTest {
    private val retrievedAt = 1_800_000_000_000L

    @Test
    fun parsesGlobalMarketMetricsAndOptionalDailyChanges() {
        val payload = """{
            "market_cap_usd": 2738777362409,
            "volume_24h_usd": 70971745210,
            "bitcoin_dominance_percentage": 57.17,
            "cryptocurrencies_number": 11441,
            "market_cap_change_24h": 0.16,
            "volume_24h_change_24h": -7.77,
            "last_updated": 1793557044
        }"""
        val result = CoinPaprikaGlobalMarketClient().parse(payload, retrievedAt)

        assertEquals(2_738_777_362_409.0, result.marketCapUsd, 0.0)
        assertEquals(70_971_745_210.0, result.volume24hUsd, 0.0)
        assertEquals(57.17, result.bitcoinDominancePercent, 0.0)
        assertEquals(11_441, result.activeCryptocurrencies)
        assertEquals(0.16, result.marketCapChange24hPercent!!, 0.0)
        assertEquals(-7.77, result.volumeChange24hPercent!!, 0.0)
        assertEquals(1_793_557_044_000L, result.providerUpdatedAtEpochMs)
        assertEquals(retrievedAt, result.retrievedAtEpochMs)
    }

    @Test
    fun acceptsMissingOptionalGlobalChangeFieldsAsUnknown() {
        val payload = """{
            "market_cap_usd": 1000,
            "volume_24h_usd": 100,
            "bitcoin_dominance_percentage": 50,
            "cryptocurrencies_number": 10,
            "last_updated": 1793557044
        }"""
        val result = CoinPaprikaGlobalMarketClient().parse(payload, retrievedAt)

        assertNull(result.marketCapChange24hPercent)
        assertNull(result.volumeChange24hPercent)
    }

    @Test
    fun rejectsGlobalMetricsOutsideAcceptedBounds() {
        val payload = """{
            "market_cap_usd": 1000,
            "volume_24h_usd": 100,
            "bitcoin_dominance_percentage": 150,
            "cryptocurrencies_number": 10,
            "last_updated": 1793557044
        }"""
        assertThrows(IOException::class.java) {
            CoinPaprikaGlobalMarketClient().parse(payload, retrievedAt)
        }
    }

    @Test
    fun rejectsGlobalTimestampInFuture() {
        val payload = """{
            "market_cap_usd": 1000,
            "volume_24h_usd": 100,
            "bitcoin_dominance_percentage": 50,
            "cryptocurrencies_number": 10,
            "last_updated": 1800001000
        }"""
        assertThrows(IOException::class.java) {
            CoinPaprikaGlobalMarketClient().parse(payload, retrievedAt)
        }
    }

    @Test
    fun parsesFearGreedValueAndClassification() {
        val payload = """{
            "name": "Fear and Greed Index",
            "data": [{
                "value": "42",
                "value_classification": "Fear",
                "timestamp": "1793557044"
            }],
            "metadata": { "error": null }
        }"""
        val result = AlternativeMeFearGreedClient().parse(payload, retrievedAt)

        assertEquals(42, result.value)
        assertEquals("Fear", result.classification)
        assertEquals(1_793_557_044_000L, result.providerUpdatedAtEpochMs)
        assertEquals(retrievedAt, result.retrievedAtEpochMs)
    }

    @Test
    fun rejectsFearGreedValuesOutsideZeroToOneHundred() {
        val payload = """{
            "data": [{
                "value": "101",
                "value_classification": "Extreme Greed",
                "timestamp": "1793557044"
            }],
            "metadata": { "error": null }
        }"""
        assertThrows(IOException::class.java) {
            AlternativeMeFearGreedClient().parse(payload, retrievedAt)
        }
    }

    @Test
    fun rejectsAlternativeMeApiErrors() {
        val payload = """{
            "data": [{
                "value": "50",
                "value_classification": "Neutral",
                "timestamp": "1793557044"
            }],
            "metadata": { "error": "rate limit" }
        }"""
        assertThrows(IOException::class.java) {
            AlternativeMeFearGreedClient().parse(payload, retrievedAt)
        }
    }
}
