package com.iie.android.domain.quality

import com.iie.android.domain.model.FreshnessEvaluation
import com.iie.android.domain.model.ObservationPayloadValidation
import com.iie.android.domain.model.ObservationQualityEvaluation
import com.iie.android.domain.model.ObservationQualityOutcome
import com.iie.android.domain.model.AssetObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDemoEvidenceEvaluatorTest {
    private val observation = AssetObservation(
        observationId = "observation:demo:asset:bitcoin",
        assetId = "asset:bitcoin",
        deploymentId = null,
        sourceId = "source:iie-demo-fixture",
        observationType = "IDENTITY_FIXTURE",
        observedAtEpochMs = 1_000L,
        retrievedAtEpochMs = 1_000L,
        status = "DEMO",
        quality = "UNVERIFIED",
        valueSchemaVersion = 1,
        valueJson = """{"recordType":"synthetic_demo_fixture","marketData":false}""",
        payloadSha256 = null,
        licenseReference = null
    )

    @Test
    fun demoFixtureIsStructurallyValidAndLimitedToLocalDisplay() {
        val result = LocalDemoEvidenceEvaluator.create().evaluate(
            observation = observation,
            sourcePermittedUseStatus = "INTERNAL_DEMO_ONLY",
            intendedUse = LocalDemoEvidenceEvaluator.INTENDED_USE,
            evaluatedAtEpochMs = 2_000L
        )

        assertTrue(result.payload is ObservationPayloadValidation.Valid)
        val quality = result.quality as ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.PASS_WITH_LIMITATIONS, quality.assessment.outcome)
        assertTrue(result.freshness is FreshnessEvaluation.NotEvaluated)
    }

    @Test
    fun demoFixtureIsNotEvaluatedForMarketAnalysis() {
        val result = LocalDemoEvidenceEvaluator.create().evaluate(
            observation = observation,
            sourcePermittedUseStatus = "INTERNAL_DEMO_ONLY",
            intendedUse = "MARKET_ANALYSIS",
            evaluatedAtEpochMs = 2_000L
        )

        assertTrue(result.quality is ObservationQualityEvaluation.NotEvaluated)
    }
}
