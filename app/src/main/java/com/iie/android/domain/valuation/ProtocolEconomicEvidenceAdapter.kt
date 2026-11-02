package com.iie.android.domain.valuation

import com.iie.android.domain.model.FundamentalResearchSection
import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchEvidenceQualityGate
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchLicenseStatus

/**
 * Normalizes field-level protocol economics disclosures while preserving their meaning.
 * Protocol revenue, fees, holder distributions, buyback spend and burned token quantities
 * are distinct measures and are never added together or treated as holder cash flow.
 */
enum class ProtocolEconomicMetric(val fieldKey: String) {
    PROTOCOL_REVENUE_USD("protocol_revenue_usd"),
    PROTOCOL_FEES_USD("protocol_fees_usd"),
    HOLDER_DISTRIBUTIONS_USD("holder_distributions_usd"),
    TOKEN_BUYBACK_SPEND_USD("token_buyback_spend_usd"),
    TOKENS_BURNED("tokens_burned")
}

data class VerifiedProtocolEconomicMetric(
    val metric: ProtocolEconomicMetric,
    val value: Double,
    val sourceId: String,
    val sourceName: String,
    val sourceUrl: String,
    val observedAtEpochMs: Long,
    val retrievedAtEpochMs: Long,
    val evidenceReference: String
)

data class ProtocolEconomicEvidenceSnapshot(
    val assetId: String,
    val metrics: Map<ProtocolEconomicMetric, VerifiedProtocolEconomicMetric>,
    val qualitativeRightsDisclosures: Map<String, String>,
    val evidence: EvidenceReferences,
    val limitations: List<String>
)

sealed class ProtocolEconomicEvidenceResult {
    data class Ready(val snapshot: ProtocolEconomicEvidenceSnapshot) : ProtocolEconomicEvidenceResult()
    data class Abstained(val blockers: List<String>) : ProtocolEconomicEvidenceResult()
}

object ProtocolEconomicEvidenceAdapter {
    private val rightsDisclosureKeys = setOf(
        "holder_cashflow_right",
        "token_value_capture_mechanism",
        "distribution_policy",
        "buyback_policy",
        "burn_policy"
    )

    fun build(
        assetId: String,
        section: FundamentalResearchSection,
        licenseStatuses: Map<String, ResearchLicenseStatus>,
        nowEpochMs: Long,
        maximumObservationSkewMs: Long = 24L * 60L * 60L * 1000L
    ): ProtocolEconomicEvidenceResult {
        require(assetId.isNotBlank())
        require(nowEpochMs > 0L)
        require(maximumObservationSkewMs >= 0L)

        val blockers = mutableListOf<String>()
        val accepted = linkedMapOf<ProtocolEconomicMetric, VerifiedProtocolEconomicMetric>()
        val relevant = section.metrics.filter { evidence ->
            ProtocolEconomicMetric.entries.any { it.fieldKey == evidence.fieldKey }
        }

        relevant.forEach { evidence ->
            val metric = ProtocolEconomicMetric.entries.first { it.fieldKey == evidence.fieldKey }
            val license = evidence.sourceId?.let(licenseStatuses::get) ?: ResearchLicenseStatus.UNKNOWN
            val quality = ResearchEvidenceQualityGate.evaluate(evidence, nowEpochMs, license)
            if (!quality.accepted) {
                blockers += quality.reasons.map { "${metric.name}_${it}" }
                return@forEach
            }
            val value = evidence.value
            if (value == null || !value.isFinite() || value < 0.0) {
                blockers += "${metric.name}_VALUE_INVALID"
                return@forEach
            }
            val sourceId = evidence.sourceId!!
            val observed = evidence.observedAtEpochMs!!
            val retrieved = evidence.retrievedAtEpochMs!!
            accepted[metric] = VerifiedProtocolEconomicMetric(
                metric = metric,
                value = value,
                sourceId = sourceId,
                sourceName = evidence.sourceName!!,
                sourceUrl = evidence.sourceUrl!!,
                observedAtEpochMs = observed,
                retrievedAtEpochMs = retrieved,
                evidenceReference = "$sourceId:${evidence.fieldKey}"
            )
        }

        if (blockers.isNotEmpty()) return ProtocolEconomicEvidenceResult.Abstained(blockers.distinct())
        if (accepted.isEmpty()) {
            return ProtocolEconomicEvidenceResult.Abstained(listOf("NO_VERIFIABLE_PROTOCOL_ECONOMIC_METRICS"))
        }

        val observations = accepted.values.map { it.observedAtEpochMs }
        if (observations.maxOrNull()!! - observations.minOrNull()!! > maximumObservationSkewMs) {
            return ProtocolEconomicEvidenceResult.Abstained(listOf("PROTOCOL_ECONOMIC_METRICS_NOT_TIME_COMPARABLE"))
        }

        val rights = section.qualitativeFacts
            .filter { it.fieldKey in rightsDisclosureKeys }
            .mapNotNull { evidence ->
                val license = evidence.sourceId?.let(licenseStatuses::get) ?: ResearchLicenseStatus.UNKNOWN
                val quality = ResearchEvidenceQualityGate.evaluate(evidence, nowEpochMs, license)
                if (!quality.accepted) {
                    blockers += quality.reasons.map { "${evidence.fieldKey.uppercase()}_${it}" }
                    null
                } else {
                    evidence.fieldKey to evidence.value!!.takeIf { it.isNotBlank() }
                }
            }.filter { it.second != null }.associate { it.first to it.second!! }

        if (blockers.isNotEmpty()) return ProtocolEconomicEvidenceResult.Abstained(blockers.distinct())

        val references = accepted.values.map { it.evidenceReference } +
            section.qualitativeFacts.filter { it.fieldKey in rightsDisclosureKeys && it.fieldKey in rights }
                .mapNotNull { item -> item.sourceId?.let { "$it:${item.fieldKey}" } }
        val asOf = accepted.values.maxOf { it.observedAtEpochMs }

        return ProtocolEconomicEvidenceResult.Ready(
            ProtocolEconomicEvidenceSnapshot(
                assetId = assetId,
                metrics = accepted,
                qualitativeRightsDisclosures = rights,
                evidence = EvidenceReferences(references.distinct(), asOf),
                limitations = listOf(
                    "Protocol revenue and protocol fees are protocol-level activity, not automatically token-holder cash flows.",
                    "Holder distributions are reported separately from buyback spend and token burns.",
                    "Buyback expenditure does not establish that every token holder receives an enforceable claim or benefit.",
                    "A burned-token quantity is not a USD cash flow and must not be summed with revenue, fees or distributions.",
                    "Rights disclosures are descriptive evidence; legal enforceability and holder eligibility require separate verification."
                )
            )
        )
    }
}
