package com.iie.android.domain.valuation

/**
 * Converts provider output into auditable observations without upgrading the provider's
 * aggregation into independently verified on-chain evidence.
 */
data class DefiLlamaEconomicObservation(
    val assetId: String,
    val providerMetric: DefiLlamaMetric,
    val protocolSlug: String,
    val protocolName: String?,
    val period: String,
    val valueUsd: Double,
    val observedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long,
    val sourceId: String,
    val sourceUrl: String,
    val evidenceStatus: String,
    val limitations: List<String>
)

sealed class DefiLlamaEconomicEvidenceResult {
    data class Ready(val observations: List<DefiLlamaEconomicObservation>) : DefiLlamaEconomicEvidenceResult()
    data class Abstained(val blockers: List<String>) : DefiLlamaEconomicEvidenceResult()
}

object DefiLlamaEconomicEvidenceAdapter {
    const val SOURCE_ID = "defillama-public-api"

    fun adapt(
        assetId: String,
        protocolSlug: String,
        series: List<DefiLlamaMetricSeries>
    ): DefiLlamaEconomicEvidenceResult {
        if (assetId.isBlank()) return DefiLlamaEconomicEvidenceResult.Abstained(listOf("ASSET_ID_MISSING"))
        if (!protocolSlug.matches(Regex("[a-z0-9][a-z0-9-]{0,79}"))) {
            return DefiLlamaEconomicEvidenceResult.Abstained(listOf("PROTOCOL_SLUG_INVALID"))
        }
        if (series.isEmpty()) return DefiLlamaEconomicEvidenceResult.Abstained(listOf("NO_PROVIDER_SERIES"))
        if (series.any { it.protocolSlug != protocolSlug || it.retrievedAtEpochMs <= 0L }) {
            return DefiLlamaEconomicEvidenceResult.Abstained(listOf("PROVIDER_SERIES_IDENTITY_OR_TIMESTAMP_INVALID"))
        }
        if (series.map { it.metric }.distinct().size != series.size) {
            return DefiLlamaEconomicEvidenceResult.Abstained(listOf("DUPLICATE_PROVIDER_METRIC"))
        }

        val observations = series.flatMap { item ->
            buildList {
                item.total24hUsd?.let { add(observation(assetId, item, "ROLLING_24H", it, null)) }
                item.total48hTo24hUsd?.let { add(observation(assetId, item, "PREVIOUS_24H", it, null)) }
                item.total7dUsd?.let { add(observation(assetId, item, "ROLLING_7D", it, null)) }
                item.totalAllTimeUsd?.let { add(observation(assetId, item, "ALL_TIME", it, null)) }
                item.dailyPoints.forEach { point ->
                    add(observation(assetId, item, "DAILY", point.valueUsd, point.timestampEpochMs))
                }
            }
        }
        if (observations.isEmpty()) {
            return DefiLlamaEconomicEvidenceResult.Abstained(listOf("PROVIDER_RETURNED_NO_USABLE_METRICS"))
        }
        return DefiLlamaEconomicEvidenceResult.Ready(observations)
    }

    private fun observation(
        assetId: String,
        series: DefiLlamaMetricSeries,
        period: String,
        value: Double,
        observedAt: Long?
    ) = DefiLlamaEconomicObservation(
        assetId = assetId,
        providerMetric = series.metric,
        protocolSlug = series.protocolSlug,
        protocolName = series.protocolName,
        period = period,
        valueUsd = value,
        observedAtEpochMs = observedAt,
        retrievedAtEpochMs = series.retrievedAtEpochMs,
        sourceId = SOURCE_ID,
        sourceUrl = series.sourceUrl,
        evidenceStatus = "PROVIDER_AGGREGATED_PENDING_INDEPENDENT_ONCHAIN_VERIFICATION",
        limitations = listOf(
            "Protocol-level fees and revenue are not automatically attributable to the token.",
            "Holder revenue is a provider-defined metric and does not by itself prove enforceable holder rights.",
            "The provider aggregates reported/collected protocol data; this observation is not a raw blockchain event.",
            "Buybacks and burns are not inferred from fees, revenue or holder-revenue values.",
            "Provider methodology and applicable data-use terms require review before production valuation use."
        )
    )
}
