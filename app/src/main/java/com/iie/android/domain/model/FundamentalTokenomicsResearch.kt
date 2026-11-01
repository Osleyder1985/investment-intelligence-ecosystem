package com.iie.android.domain.model

/**
 * Field-level evidence envelope. A value is never considered KNOWN without
 * a source, an observation timestamp, a retrieval timestamp and an explicit
 * usage/licence decision.
 */
data class ResearchEvidence<T>(
    val fieldKey: String,
    val value: T?,
    val status: ResearchFieldStatus,
    val sourceId: String?,
    val sourceName: String?,
    val sourceUrl: String?,
    val licenseReference: String?,
    val permittedUseStatus: String,
    val observedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long?,
    val qualityStatus: String,
    val notes: List<String> = emptyList()
) {
    init {
        require(fieldKey.isNotBlank())
        require(permittedUseStatus.isNotBlank())
        require(qualityStatus.isNotBlank())
    }
}

enum class ResearchLicenseStatus {
    APPROVED_FOR_INTENDED_USE,
    ATTRIBUTION_REQUIRED,
    REVIEW_REQUIRED,
    PROHIBITED,
    UNKNOWN
}

/** Fundamental metrics retain their own provenance; no asset-wide source assumption. */
data class FundamentalResearchSection(
    val metrics: List<ResearchEvidence<Double>>,
    val qualitativeFacts: List<ResearchEvidence<String>> = emptyList()
)

/** Supply, issuance and vesting facts are represented independently. */
data class TokenomicsResearchSection(
    val circulatingSupply: ResearchEvidence<Double>,
    val totalSupply: ResearchEvidence<Double>,
    val maxSupply: ResearchEvidence<Double>,
    val annualInflationPercent: ResearchEvidence<Double>,
    val emissionSchedule: ResearchEvidence<String>,
    val unlockEvents: List<ResearchEvidence<String>>
)

/** Result of deterministic validation; callers must preserve rejected evidence for audit. */
data class EvidenceQualityResult(
    val accepted: Boolean,
    val status: ResearchFieldStatus,
    val reasons: List<String>
)

/**
 * Conservative structural checks only. This does not prove that a provider's
 * statement is true; it verifies that evidence is attributable and internally usable.
 */
object ResearchEvidenceQualityGate {
    fun <T> evaluate(
        evidence: ResearchEvidence<T>,
        nowEpochMs: Long,
        licenseStatus: ResearchLicenseStatus
    ): EvidenceQualityResult {
        require(nowEpochMs > 0L)
        val reasons = mutableListOf<String>()

        if (evidence.status != ResearchFieldStatus.KNOWN || evidence.value == null) {
            reasons += "VALUE_NOT_KNOWN"
        }
        if (evidence.sourceId.isNullOrBlank()) reasons += "SOURCE_ID_MISSING"
        if (evidence.sourceName.isNullOrBlank()) reasons += "SOURCE_NAME_MISSING"
        if (evidence.sourceUrl.isNullOrBlank()) reasons += "SOURCE_URL_MISSING"
        if (evidence.licenseReference.isNullOrBlank()) reasons += "LICENSE_REFERENCE_MISSING"
        if (evidence.observedAtEpochMs == null || evidence.observedAtEpochMs <= 0L) {
            reasons += "OBSERVATION_TIMESTAMP_MISSING"
        }
        if (evidence.retrievedAtEpochMs == null ||
            evidence.retrievedAtEpochMs <= 0L ||
            evidence.retrievedAtEpochMs > nowEpochMs
        ) {
            reasons += "RETRIEVAL_TIMESTAMP_INVALID"
        }
        if (evidence.observedAtEpochMs != null &&
            evidence.retrievedAtEpochMs != null &&
            evidence.observedAtEpochMs > evidence.retrievedAtEpochMs
        ) {
            reasons += "OBSERVATION_AFTER_RETRIEVAL"
        }
        if (evidence.permittedUseStatus == "PROHIBITED" ||
            licenseStatus == ResearchLicenseStatus.PROHIBITED
        ) {
            reasons += "LICENSE_PROHIBITS_USE"
        }
        if (licenseStatus == ResearchLicenseStatus.UNKNOWN ||
            licenseStatus == ResearchLicenseStatus.REVIEW_REQUIRED
        ) {
            reasons += "LICENSE_REQUIRES_REVIEW"
        }

        val accepted = reasons.isEmpty() &&
            (licenseStatus == ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE ||
                licenseStatus == ResearchLicenseStatus.ATTRIBUTION_REQUIRED)
        return EvidenceQualityResult(
            accepted = accepted,
            status = if (accepted) ResearchFieldStatus.KNOWN else ResearchFieldStatus.UNKNOWN,
            reasons = reasons
        )
    }

    fun validateSupplyRelations(
        circulatingSupply: Double?,
        totalSupply: Double?,
        maxSupply: Double?
    ): List<String> {
        val reasons = mutableListOf<String>()
        if (circulatingSupply != null && (!circulatingSupply.isFinite() || circulatingSupply < 0.0)) {
            reasons += "CIRCULATING_SUPPLY_INVALID"
        }
        if (totalSupply != null && (!totalSupply.isFinite() || totalSupply < 0.0)) {
            reasons += "TOTAL_SUPPLY_INVALID"
        }
        if (maxSupply != null && (!maxSupply.isFinite() || maxSupply < 0.0)) {
            reasons += "MAX_SUPPLY_INVALID"
        }
        if (circulatingSupply != null && totalSupply != null && circulatingSupply > totalSupply) {
            reasons += "CIRCULATING_EXCEEDS_TOTAL"
        }
        if (totalSupply != null && maxSupply != null && totalSupply > maxSupply) {
            reasons += "TOTAL_EXCEEDS_MAX"
        }
        return reasons
    }
}
