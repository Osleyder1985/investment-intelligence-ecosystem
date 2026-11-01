package com.iie.android.domain.quality

import com.iie.android.domain.freshness.FreshnessEvaluator
import com.iie.android.domain.model.AssetObservation
import com.iie.android.domain.model.FreshnessEvaluation
import com.iie.android.domain.model.FreshnessPolicy
import com.iie.android.domain.model.FreshnessStatus
import com.iie.android.domain.model.ObservationPayloadValidation
import com.iie.android.domain.model.ObservationQualityEvaluation
import com.iie.android.domain.model.ObservationQualityPolicy
import com.iie.android.domain.model.PayloadField
import com.iie.android.domain.model.PayloadFieldType
import com.iie.android.domain.model.TimestampBasis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationEvidenceEvaluatorTest {
    private val observation = AssetObservation(
        observationId = "obs:fixture",
        assetId = "asset:fixture",
        deploymentId = null,
        sourceId = "source:fixture",
        observationType = "IDENTITY_FIXTURE",
        observedAtEpochMs = 1_000L,
        retrievedAtEpochMs = 1_100L,
        status = "DEMO",
        quality = "UNVERIFIED",
        valueSchemaVersion = 1,
        valueJson = """{"recordType":"synthetic_demo_fixture","marketData":false}""",
        payloadSha256 = null,
        licenseReference = null
    )

    private fun evaluator(
        qualityUseStatuses: Set<String> = setOf("INTERNAL_DEMO_ONLY"),
        freshnessPolicies: List<FreshnessPolicy> = emptyList()
    ) = ObservationEvidenceEvaluator(
        payloadValidator = ObservationPayloadValidator(
            listOf(
                com.iie.android.domain.model.ObservationPayloadContract(
                    "IDENTITY_FIXTURE", 1,
                    fields = listOf(
                        PayloadField("recordType", PayloadFieldType.STRING),
                        PayloadField("marketData", PayloadFieldType.BOOLEAN)
                    )
                )
            )
        ),
        qualityEvaluator = ObservationQualityEvaluator(
            listOf(
                ObservationQualityPolicy(
                    "fixture-quality", 1, "source:fixture", "IDENTITY_FIXTURE",
                    "INTERNAL_DEMO", 1, qualityUseStatuses, setOf("UNVERIFIED"), false
                )
            )
        ),
        freshnessEvaluator = FreshnessEvaluator(freshnessPolicies)
    )

    @Test
    fun evaluatesPayloadQualityAndFreshnessAsSeparateDimensions() {
        val policy = FreshnessPolicy(
            "fixture-freshness", 1, "source:fixture", "IDENTITY_FIXTURE",
            "INTERNAL_DEMO", TimestampBasis.OBSERVED_AT, 10_000L, 1_000L
        )
        val result = evaluator(freshnessPolicies = listOf(policy)).evaluate(
            observation, "INTERNAL_DEMO_ONLY", "INTERNAL_DEMO", 2_000L
        )

        assertEquals("obs:fixture", result.observationId)
        assertTrue(result.payload is ObservationPayloadValidation.Valid)
        assertTrue(result.quality is ObservationQualityEvaluation.Evaluated)
        assertEquals(
            FreshnessStatus.FRESH,
            (result.freshness as FreshnessEvaluation.Evaluated).assessment.status
        )
    }

    @Test
    fun invalidPayloadDoesNotGetHiddenByPassingMetadataQuality() {
        val result = evaluator().evaluate(
            observation.copy(valueJson = """{"recordType":7,"marketData":false}"""),
            "INTERNAL_DEMO_ONLY",
            "INTERNAL_DEMO",
            2_000L
        )

        assertTrue(result.payload is ObservationPayloadValidation.Invalid)
        assertTrue(result.quality is ObservationQualityEvaluation.Evaluated)
        assertTrue(result.freshness is FreshnessEvaluation.NotEvaluated)
    }

    @Test
    fun noFreshnessPolicyRemainsExplicitlyNotEvaluated() {
        val result = evaluator().evaluate(
            observation, "INTERNAL_DEMO_ONLY", "INTERNAL_DEMO", 2_000L
        )
        assertEquals(
            FreshnessEvaluation.Reason.NO_MATCHING_POLICY,
            (result.freshness as FreshnessEvaluation.NotEvaluated).reason
        )
    }
}
