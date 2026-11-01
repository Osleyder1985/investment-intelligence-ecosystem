package com.iie.android.domain.model

/** Explicit, source- and use-scoped rules for evaluating observation quality. */
data class ObservationQualityPolicy(
    val policyId: String,
    val version: Int,
    val sourceId: String,
    val observationType: String,
    val intendedUse: String,
    val expectedValueSchemaVersion: Int,
    val allowedSourcePermittedUseStatuses: Set<String>,
    val acceptedQualityValues: Set<String>,
    val requireVerifiedEvidence: Boolean
) {
    init {
        require(policyId.isNotBlank())
        require(version > 0)
        require(sourceId.isNotBlank())
        require(observationType.isNotBlank())
        require(intendedUse.isNotBlank())
        require(expectedValueSchemaVersion > 0)
        require(allowedSourcePermittedUseStatuses.isNotEmpty())
        require(allowedSourcePermittedUseStatuses.none { it.isBlank() })
        require(acceptedQualityValues.isNotEmpty())
        require(acceptedQualityValues.none { it.isBlank() })
        require(!requireVerifiedEvidence || "VERIFIED" in acceptedQualityValues) {
            "A policy requiring verified evidence must recognize VERIFIED"
        }
    }
}

data class ObservationQualityInput(
    val observation: AssetObservation,
    val sourcePermittedUseStatus: String,
    val intendedUse: String,
    val evaluatedAtEpochMs: Long
)

enum class ObservationQualityOutcome {
    BLOCKED,
    PASS_WITH_LIMITATIONS,
    PASS
}

enum class ObservationQualitySeverity {
    BLOCKING,
    WARNING
}

enum class ObservationQualityFindingCode {
    MISSING_REQUIRED_METADATA,
    INVALID_TIMESTAMP,
    OBSERVATION_AFTER_RETRIEVAL,
    UNEXPECTED_VALUE_SCHEMA_VERSION,
    EMPTY_VALUE_PAYLOAD,
    SOURCE_USE_NOT_PERMITTED,
    UNKNOWN_QUALITY_VALUE,
    EVIDENCE_UNVERIFIED,
    EVIDENCE_VERIFICATION_REQUIRED,
    INVALID_PAYLOAD_HASH,
    BLANK_LICENSE_REFERENCE
}

data class ObservationQualityFinding(
    val code: ObservationQualityFindingCode,
    val severity: ObservationQualitySeverity,
    val detail: String
)

data class ObservationQualityAssessment(
    val policyId: String,
    val policyVersion: Int,
    val intendedUse: String,
    val evaluatedAtEpochMs: Long,
    val outcome: ObservationQualityOutcome,
    val findings: List<ObservationQualityFinding>
)

sealed interface ObservationQualityEvaluation {
    data class Evaluated(
        val assessment: ObservationQualityAssessment
    ) : ObservationQualityEvaluation

    data class NotEvaluated(
        val reason: Reason
    ) : ObservationQualityEvaluation

    enum class Reason {
        NO_MATCHING_POLICY
    }
}
