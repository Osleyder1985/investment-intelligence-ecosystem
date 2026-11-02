package com.iie.android.domain.valuation

import org.junit.Assert.*
import org.junit.Test

class DefiLlamaEconomicEvidenceAdapterTest {
    private fun series(metric: DefiLlamaMetric) = DefiLlamaMetricSeries(
        metric = metric, protocolSlug = "aave", protocolName = "Aave",
        total24hUsd = 100.0, total48hTo24hUsd = null, total7dUsd = 700.0,
        totalAllTimeUsd = null,
        dailyPoints = listOf(DefiLlamaDailyPoint(1761955200000L, 100.0)),
        sourceUrl = "https://api.llama.fi/summary/fees/aave?dataType=${metric.dataType}",
        retrievedAtEpochMs = 1762200000000L
    )

    @Test fun preservesMetricsAsDistinctProviderObservations() {
        val result = DefiLlamaEconomicEvidenceAdapter.adapt(
            "asset:aave", "aave", listOf(
                series(DefiLlamaMetric.PROTOCOL_FEES),
                series(DefiLlamaMetric.PROTOCOL_REVENUE),
                series(DefiLlamaMetric.HOLDER_REVENUE)
            )
        )
        val observations = (result as DefiLlamaEconomicEvidenceResult.Ready).observations
        assertEquals(9, observations.size)
        assertEquals(3, observations.map { it.providerMetric }.distinct().size)
        assertTrue(observations.all {
            it.evidenceStatus == "PROVIDER_AGGREGATED_PENDING_INDEPENDENT_ONCHAIN_VERIFICATION"
        })
        assertTrue(observations.none { it.providerMetric == DefiLlamaMetric.PROTOCOL_REVENUE &&
            it.period == "DAILY" && it.limitations.isEmpty() })
    }

    @Test fun refusesDuplicateMetricSeries() {
        val result = DefiLlamaEconomicEvidenceAdapter.adapt(
            "asset:aave", "aave", listOf(series(DefiLlamaMetric.PROTOCOL_FEES), series(DefiLlamaMetric.PROTOCOL_FEES))
        )
        assertEquals(
            listOf("DUPLICATE_PROVIDER_METRIC"),
            (result as DefiLlamaEconomicEvidenceResult.Abstained).blockers
        )
    }

    @Test fun doesNotAcceptProtocolSlugAsAssetIdentity() {
        val result = DefiLlamaEconomicEvidenceAdapter.adapt("", "aave", listOf(series(DefiLlamaMetric.PROTOCOL_FEES)))
        assertEquals(listOf("ASSET_ID_MISSING"), (result as DefiLlamaEconomicEvidenceResult.Abstained).blockers)
    }
}
