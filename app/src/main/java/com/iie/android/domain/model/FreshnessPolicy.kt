package com.iie.android.domain.model

/**
 * Contextual freshness policy. Thresholds must be approved for a specific
 * source, observation type, and intended use; there are no global defaults.
 */
data class FreshnessPolicy(
    val policyId: String,
    val version: Int,
    val sourceId: String,
    val observationType: String,
    val intendedUse: String,
    val timestampBasis: TimestampBasis,
    val maxAgeMs: Long,
    val approachingExpiryWindowMs: Long
) {
    init {
        require(policyId.isNotBlank())
        require(version > 0)
        require(sourceId.isNotBlank())
        require(observationType.isNotBlank())
        require(intendedUse.isNotBlank())
        require(maxAgeMs > 0)
        require(approachingExpiryWindowMs in 0..maxAgeMs)
    }
}

enum class TimestampBasis {
    OBSERVED_AT,
    RETRIEVED_AT
}

enum class FreshnessStatus {
    FRESH,
    APPROACHING_EXPIRY,
    STALE,
    NOT_APPLICABLE
}

data class FreshnessAssessment(
    val status: FreshnessStatus,
    val policyId: String,
    val policyVersion: Int,
    val intendedUse: String,
    val evaluatedAtEpochMs: Long,
    val referenceTimestampEpochMs: Long,
    val ageMs: Long
)

sealed interface FreshnessEvaluation {
    data class Evaluated(val assessment: FreshnessAssessment) : FreshnessEvaluation
    data class NotEvaluated(val reason: Reason) : FreshnessEvaluation

    enum class Reason {
        NO_MATCHING_POLICY,
        REQUIRED_TIMESTAMP_MISSING,
        INVALID_TIME_ORDER
    }
}
