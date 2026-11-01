package com.iie.android.domain.model

/**
 * Independent evaluation dimensions for one observation.
 * No aggregate pass/fail is provided: callers must inspect each dimension.
 */
data class ObservationEvidenceEvaluation(
    val observationId: String,
    val payload: ObservationPayloadValidation,
    val quality: ObservationQualityEvaluation,
    val freshness: FreshnessEvaluation
)
