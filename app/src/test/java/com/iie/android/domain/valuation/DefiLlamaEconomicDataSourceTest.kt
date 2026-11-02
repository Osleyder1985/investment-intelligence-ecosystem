package com.iie.android.domain.valuation

import org.junit.Assert.*
import org.junit.Test

class DefiLlamaEconomicDataSourceTest {
    private val fixture = """
        {
          "name":"Example Protocol",
          "total24h":1200.5,
          "total48hto24h":1100.0,
          "total7d":8000.0,
          "totalAllTime":900000.0,
          "totalDataChart":[[1761868800,100.0],[1761955200,125.5],[1761955200,125.5],[1762041600,-2],[1762128000,"bad"]]
        }
    """.trimIndent()

    @Test fun parsesAndNormalizesDailyObservations() {
        val parsed = DefiLlamaEconomicDataSource.parse(
            fixture, DefiLlamaMetric.PROTOCOL_FEES, "example-protocol",
            "https://api.llama.fi/summary/fees/example-protocol?dataType=dailyFees", 1762200000000L
        )
        assertEquals("Example Protocol", parsed.protocolName)
        assertEquals(1200.5, parsed.total24hUsd!!, 0.0)
        assertEquals(8000.0, parsed.total7dUsd!!, 0.0)
        assertEquals(2, parsed.dailyPoints.size)
        assertEquals(1761955200000L, parsed.dailyPoints[1].timestampEpochMs)
    }

    @Test fun rejectsInvalidProtocolSlugBeforeNetworkCall() {
        var called = false
        val source = DefiLlamaEconomicDataSource(DefiLlamaHttpTransport { called = true; fixture })
        val result = source.fetchProtocol("../aave")
        assertFalse(called)
        assertEquals("PROTOCOL_SLUG_INVALID", (result as DefiLlamaFetchResult.Failure).reasonCode)
    }

    @Test fun fetchesFeesRevenueAndHolderRevenueSeparately() {
        val urls = mutableListOf<String>()
        val source = DefiLlamaEconomicDataSource(
            DefiLlamaHttpTransport { url -> urls += url; fixture },
            clock = { 1762200000000L }
        )
        val result = source.fetchProtocol("example-protocol")
        assertEquals(3, (result as DefiLlamaFetchResult.Success).series.size)
        assertTrue(urls.any { it.endsWith("dataType=dailyFees") })
        assertTrue(urls.any { it.endsWith("dataType=dailyRevenue") })
        assertTrue(urls.any { it.endsWith("dataType=dailyHoldersRevenue") })
    }
}
