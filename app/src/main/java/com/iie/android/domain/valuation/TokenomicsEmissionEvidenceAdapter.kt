package com.iie.android.domain.valuation

import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchEvidenceQualityGate
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchLicenseStatus
import com.iie.android.domain.model.TokenomicsResearchSection

/**
 * Validates token issuance and unlock disclosures without inventing a numeric unlock curve.
 * Textual schedules remain disclosures until a source supplies structured amounts, dates,
 * beneficiary classes and circulating-supply treatment.
 */
data class VerifiedTokenomicsEmissionEvidence(
    val annualInflationPercent: Double,
    val emissionScheduleDisclosure: String,
    val unlockDisclosures: List<String>,
    val observedAtEpochMs: Long,
    val evidence: EvidenceReferences,
    val limitations: List<String>
)

sealed class TokenomicsEmissionEvidenceResult {
    data class Ready(val evidence: VerifiedTokenomicsEmissionEvidence) : TokenomicsEmissionEvidenceResult()
    data class Abstained(val blockers: List<String>) : TokenomicsEmissionEvidenceResult()
}

object TokenomicsEmissionEvidenceAdapter {
    fun build(
        section: TokenomicsResearchSection,
        licenseStatuses: Map<String, ResearchLicenseStatus>,
        nowEpochMs: Long,
        maximumObservationSkewMs: Long = 7L * 24L * 60L * 60L * 1000L
    ): TokenomicsEmissionEvidenceResult {
        require(nowEpochMs > 0L)
        require(maximumObservationSkewMs >= 0L)

        val blockers = mutableListOf<String>()
        val accepted = mutableListOf<Pair<String, ResearchEvidence<*>>>()

        fun <T> check(key: String, item: ResearchEvidence<T>, required: Boolean) {
            if (!required && item.status == ResearchFieldStatus.UNKNOWN && item.value == null) return
            val license = item.sourceId?.let(licenseStatuses::get) ?: ResearchLicenseStatus.UNKNOWN
            val result = ResearchEvidenceQualityGate.evaluate(item, nowEpochMs, license)
            if (result.accepted) accepted += key to item
            else blockers += result.reasons.map { "${key.uppercase()}_${it}" }
        }

        check("annualInflation", section.annualInflationPercent, required = true)
        check("emissionSchedule", section.emissionSchedule, required = true)
        section.unlockEvents.forEachIndexed { index, item ->
            check("unlockEvent$index", item, required = false)
        }

        if (blockers.isNotEmpty()) return TokenomicsEmissionEvidenceResult.Abstained(blockers.distinct())

        val inflation = section.annualInflationPercent.value
        if (inflation == null || !inflation.isFinite() || inflation < -100.0) {
            return TokenomicsEmissionEvidenceResult.Abstained(listOf("ANNUAL_INFLATION_VALUE_INVALID"))
        }
        val schedule = section.emissionSchedule.value
        if (schedule.isNullOrBlank()) {
            return TokenomicsEmissionEvidenceResult.Abstained(listOf("EMISSION_SCHEDULE_DISCLOSURE_MISSING"))
        }

        val observations = accepted.mapNotNull { it.second.observedAtEpochMs }
        if (observations.size != accepted.size || observations.any { it <= 0L }) {
            return TokenomicsEmissionEvidenceResult.Abstained(listOf("EMISSION_OBSERVATION_TIMESTAMP_MISSING"))
        }
        val earliest = observations.minOrNull()!!
        val latest = observations.maxOrNull()!!
        if (latest - earliest > maximumObservationSkewMs) {
            return TokenomicsEmissionEvidenceResult.Abstained(listOf("EMISSION_EVIDENCE_NOT_TIME_COMPARABLE"))
        }

        val refs = accepted.mapNotNull { (key, item) ->
            item.sourceId?.let { source -> "${source}:${item.fieldKey.ifBlank { key }}" }
        }.distinct()
        if (refs.size != accepted.size) {
            return TokenomicsEmissionEvidenceResult.Abstained(listOf("EMISSION_EVIDENCE_REFERENCE_MISSING"))
        }
        val unlocks = section.unlockEvents.mapNotNull { item ->
            item.value?.takeIf { item.status == ResearchFieldStatus.KNOWN && it.isNotBlank() }
        }

        return TokenomicsEmissionEvidenceResult.Ready(
            VerifiedTokenomicsEmissionEvidence(
                annualInflationPercent = inflation,
                emissionScheduleDisclosure = schedule,
                unlockDisclosures = unlocks,
                observedAtEpochMs = latest,
                evidence = EvidenceReferences(refs, latest),
                limitations = listOf(
                    "Annual inflation is accepted as a reported metric, not independently forecast issuance.",
                    "Unlock disclosures are qualitative text; no unlock amount, date curve or beneficiary-adjusted float is inferred.",
                    "A quantitative dilution forecast requires structured, dated unlock and issuance events with independently verified amounts."
                )
            )
        )
    }
}
