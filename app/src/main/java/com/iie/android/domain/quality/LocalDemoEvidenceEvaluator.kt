package com.iie.android.domain.quality

import com.iie.android.domain.freshness.FreshnessEvaluator
import com.iie.android.domain.model.ObservationQualityPolicy

/**
 * Explicitly limits the synthetic fixture policy to local catalogue display.
 * This policy does not authorize market analysis or investment decisions.
 */
object LocalDemoEvidenceEvaluator {
    const val INTENDED_USE = "LOCAL_CATALOG_DISPLAY"

    fun create(): ObservationEvidenceEvaluator = ObservationEvidenceEvaluator(
        payloadValidator = ObservationPayloadValidator(ObservationPayloadContracts.initial()),
        qualityEvaluator = ObservationQualityEvaluator(
            listOf(
                ObservationQualityPolicy(
                    policyId = "iie-local-demo-display-quality",
                    version = 1,
                    sourceId = "source:iie-demo-fixture",
                    observationType = "IDENTITY_FIXTURE",
                    intendedUse = INTENDED_USE,
                    expectedValueSchemaVersion = 1,
                    allowedSourcePermittedUseStatuses = setOf("INTERNAL_DEMO_ONLY"),
                    acceptedQualityValues = setOf("UNVERIFIED"),
                    requireVerifiedEvidence = false
                )
            )
        ),
        // No freshness policy is configured for a synthetic identity fixture.
        freshnessEvaluator = FreshnessEvaluator(emptyList())
    )
}
