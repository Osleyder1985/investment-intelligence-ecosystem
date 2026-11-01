package com.iie.android.domain.quality

import com.iie.android.domain.freshness.FreshnessEvaluator
import com.iie.android.domain.model.AssetObservation
import com.iie.android.domain.model.FreshnessEvaluation
import com.iie.android.domain.model.ObservationEvidenceEvaluation
import com.iie.android.domain.model.ObservationQualityEvaluation
import com.iie.android.domain.model.ObservationQualityInput

/**
 * Coordinates independent payload, metadata-quality, and freshness checks.
 * It intentionally does not collapse these dimensions into a confidence score
 * or a single approval decision.
 */
class ObservationEvidenceEvaluator(
    private val payloadValidator: ObservationPayloadValidator,
    private val qualityEvaluator: ObservationQualityEvaluator,
    private val freshnessEvaluator: FreshnessEvaluator
) {
    fun evaluate(
        observation: AssetObservation,
        sourcePermittedUseStatus: String,
        intendedUse: String,
        evaluatedAtEpochMs: Long
    ): ObservationEvidenceEvaluation {
        val payload = payloadValidator.validate(
            observation.observationType,
            observation.valueSchemaVersion,
            observation.valueJson
        )
        val quality = qualityEvaluator.evaluate(
            ObservationQualityInput(
                observation = observation,
                sourcePermittedUseStatus = sourcePermittedUseStatus,
                intendedUse = intendedUse,
                evaluatedAtEpochMs = evaluatedAtEpochMs
            )
        )
        val freshness = freshnessEvaluator.evaluate(
            sourceId = observation.sourceId,
            observationType = observation.observationType,
            intendedUse = intendedUse,
            observedAtEpochMs = observation.observedAtEpochMs,
            retrievedAtEpochMs = observation.retrievedAtEpochMs,
            evaluatedAtEpochMs = evaluatedAtEpochMs
        )

        return ObservationEvidenceEvaluation(
            observationId = observation.observationId,
            payload = payload,
            quality = quality,
            freshness = freshness
        )
    }
}
