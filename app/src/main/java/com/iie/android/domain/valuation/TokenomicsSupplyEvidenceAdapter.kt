package com.iie.android.domain.valuation

import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchLicenseStatus
import com.iie.android.domain.model.ResearchEvidenceQualityGate
import com.iie.android.domain.model.TokenomicsResearchSection
import kotlin.math.abs

/**
 * Converts source-attributed token supply observations into a validated input envelope.
 *
 * This adapter does not infer monetary demand, fair value, or investment merit. Circulating
 * and total supply remain separate facts; the resulting interval is a supply-sensitivity
 * envelope only and must not be mislabeled as effective or economically available supply.
 */
data class VerifiedTokenomicsSupply(
    val circulatingSupply: Double,
    val totalSupply: Double,
    val maxSupply: Double?,
    val observedAtEpochMs: Long,
    val evidence: EvidenceReferences,
    val notes: List<String>
)

sealed class TokenomicsSupplyEvidenceResult {
    data class Ready(val supply: VerifiedTokenomicsSupply) : TokenomicsSupplyEvidenceResult()
    data class Abstained(val blockers: List<String>) : TokenomicsSupplyEvidenceResult()
}

object TokenomicsSupplyEvidenceAdapter {
    fun build(
        section: TokenomicsResearchSection,
        licenseStatuses: Map<String, ResearchLicenseStatus>,
        nowEpochMs: Long,
        maximumObservationSkewMs: Long = 24L * 60L * 60L * 1000L
    ): TokenomicsSupplyEvidenceResult {
        require(nowEpochMs > 0L)
        require(maximumObservationSkewMs >= 0L)

        val required = listOf(
            "circulatingSupply" to section.circulatingSupply,
            "totalSupply" to section.totalSupply
        )
        val optionalMax = section.maxSupply.takeIf { it.status == ResearchFieldStatus.KNOWN && it.value != null }
        val evidence = required + listOfNotNull(optionalMax?.let { "maxSupply" to it })
        val blockers = mutableListOf<String>()
        val accepted = mutableListOf<Pair<String, ResearchEvidence<Double>>>()

        evidence.forEach { (key, item) ->
            val license = item.sourceId?.let(licenseStatuses::get) ?: ResearchLicenseStatus.UNKNOWN
            val quality = ResearchEvidenceQualityGate.evaluate(item, nowEpochMs, license)
            if (!quality.accepted) {
                blockers += quality.reasons.map { "${key.uppercase()}_${it}" }
            } else {
                accepted += key to item
            }
        }
        if (accepted.size != evidence.size) {
            if (section.circulatingSupply.status != ResearchFieldStatus.KNOWN ||
                section.circulatingSupply.value == null
            ) blockers += "CIRCULATING_SUPPLY_NOT_VERIFIED"
            if (section.totalSupply.status != ResearchFieldStatus.KNOWN ||
                section.totalSupply.value == null
            ) blockers += "TOTAL_SUPPLY_NOT_VERIFIED"
            return TokenomicsSupplyEvidenceResult.Abstained(blockers.distinct())
        }

        val circulating = section.circulatingSupply.value!!
        val total = section.totalSupply.value!!
        val max = optionalMax?.value
        blockers += ResearchEvidenceQualityGate.validateSupplyRelations(circulating, total, max)
        if (blockers.isNotEmpty()) return TokenomicsSupplyEvidenceResult.Abstained(blockers.distinct())

        val observations = accepted.mapNotNull { it.second.observedAtEpochMs }
        if (observations.size != accepted.size || observations.any { it <= 0L }) {
            return TokenomicsSupplyEvidenceResult.Abstained(listOf("SUPPLY_OBSERVATION_TIMESTAMP_MISSING"))
        }
        val earliest = observations.minOrNull()!!
        val latest = observations.maxOrNull()!!
        if (abs(latest - earliest) > maximumObservationSkewMs) {
            return TokenomicsSupplyEvidenceResult.Abstained(listOf("SUPPLY_OBSERVATIONS_NOT_TIME_COMPARABLE"))
        }
        val ids = accepted.mapNotNull { (key, item) ->
            item.sourceId?.let { "${it}:${item.fieldKey.ifBlank { key }}" }
        }.distinct()
        if (ids.size != accepted.size) {
            return TokenomicsSupplyEvidenceResult.Abstained(listOf("SUPPLY_EVIDENCE_REFERENCE_MISSING"))
        }

        return TokenomicsSupplyEvidenceResult.Ready(
            VerifiedTokenomicsSupply(
                circulatingSupply = circulating,
                totalSupply = total,
                maxSupply = max,
                observedAtEpochMs = latest,
                evidence = EvidenceReferences(ids, latest),
                notes = listOf(
                    "Circulating supply and total supply are reported independently.",
                    "The interval is a supply-sensitivity envelope, not proof of effective or freely tradable supply.",
                    "Market capitalization and fully diluted valuation are not used as intrinsic monetary-demand estimates."
                )
            )
        )
    }
}
