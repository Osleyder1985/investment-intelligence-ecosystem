package com.iie.android.domain.quality

import com.iie.android.domain.model.AssetObservation
import com.iie.android.domain.model.ObservationQualityEvaluation
import com.iie.android.domain.model.ObservationQualityInput
import com.iie.android.domain.model.ObservationQualityOutcome
import com.iie.android.domain.model.ObservationQualityPolicy
import com.iie.android.domain.model.ObservationQualityFindingCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationQualityEvaluatorTest {
    private val policy = ObservationQualityPolicy(
        policyId = "test-quality-policy",
        version = 1,
        sourceId = "source:market-test",
        observationType = "MARKET_PRICE",
        intendedUse = "PAPER_TRADING",
        expectedValueSchemaVersion = 1,
        allowedSourcePermittedUseStatuses = setOf("APPROVED_FOR_MARKET_ANALYSIS"),
        acceptedQualityValues = setOf("VERIFIED", "UNVERIFIED"),
        requireVerifiedEvidence = false
    )

    private val observation = AssetObservation(
        observationId = "observation:1",
        assetId = "asset:bitcoin",
        deploymentId = null,
        sourceId = "source:market-test",
        observationType = "MARKET_PRICE",
        observedAtEpochMs = 1_000L,
        retrievedAtEpochMs = 1_100L,
        status = "AVAILABLE",
        quality = "VERIFIED",
        valueSchemaVersion = 1,
        valueJson = """{"price":65000.0}""",
        payloadSha256 = "a".repeat(64),
        licenseReference = "license:market-test"
    )

    private fun evaluate(
        value: AssetObservation = observation,
        sourceUse: String = "APPROVED_FOR_MARKET_ANALYSIS",
        at: Long = 1_200L,
        activePolicy: ObservationQualityPolicy = policy
    ) = ObservationQualityEvaluator(listOf(activePolicy)).evaluate(
        ObservationQualityInput(
            observation = value,
            sourcePermittedUseStatus = sourceUse,
            intendedUse = activePolicy.intendedUse,
            evaluatedAtEpochMs = at
        )
    )

    @Test
    fun passesWhenAllConfiguredMetadataChecksPass() {
        val result = evaluate() as ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.PASS, result.assessment.outcome)
        assertTrue(result.assessment.findings.isEmpty())
    }

    @Test
    fun blocksDemoOnlySourceForMarketAnalysis() {
        val result = evaluate(sourceUse = "INTERNAL_DEMO_ONLY") as ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.BLOCKED, result.assessment.outcome)
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.SOURCE_USE_NOT_PERMITTED
        })
    }

    @Test
    fun unverifiedEvidencePassesOnlyWithExplicitLimitationWhenPolicyAllowsIt() {
        val result = evaluate(value = observation.copy(quality = "UNVERIFIED")) as
            ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.PASS_WITH_LIMITATIONS, result.assessment.outcome)
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.EVIDENCE_UNVERIFIED
        })
    }

    @Test
    fun policyCanRequireVerifiedEvidence() {
        val strictPolicy = policy.copy(
            policyId = "strict-quality-policy",
            requireVerifiedEvidence = true
        )
        val result = evaluate(
            value = observation.copy(quality = "UNVERIFIED"),
            activePolicy = strictPolicy
        ) as ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.BLOCKED, result.assessment.outcome)
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.EVIDENCE_VERIFICATION_REQUIRED
        })
    }

    @Test
    fun blocksUnknownQualityValue() {
        val result = evaluate(value = observation.copy(quality = "SUSPECT")) as
            ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.BLOCKED, result.assessment.outcome)
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.UNKNOWN_QUALITY_VALUE
        })
    }

    @Test
    fun blocksUnexpectedSchemaVersionAndEmptyPayload() {
        val result = evaluate(
            value = observation.copy(valueSchemaVersion = 2, valueJson = " ")
        ) as ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.BLOCKED, result.assessment.outcome)
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.UNEXPECTED_VALUE_SCHEMA_VERSION
        })
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.EMPTY_VALUE_PAYLOAD
        })
    }

    @Test
    fun blocksImpossibleTimestampOrder() {
        val result = evaluate(
            value = observation.copy(observedAtEpochMs = 1_200L),
            at = 1_300L
        ) as ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.BLOCKED, result.assessment.outcome)
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.OBSERVATION_AFTER_RETRIEVAL
        })
    }

    @Test
    fun blocksMalformedOptionalPayloadHash() {
        val result = evaluate(value = observation.copy(payloadSha256 = "not-a-hash")) as
            ObservationQualityEvaluation.Evaluated
        assertEquals(ObservationQualityOutcome.BLOCKED, result.assessment.outcome)
        assertTrue(result.assessment.findings.any {
            it.code == ObservationQualityFindingCode.INVALID_PAYLOAD_HASH
        })
    }

    @Test
    fun doesNotEvaluateWithoutMatchingPolicy() {
        val result = ObservationQualityEvaluator(emptyList()).evaluate(
            ObservationQualityInput(
                observation = observation,
                sourcePermittedUseStatus = "APPROVED_FOR_MARKET_ANALYSIS",
                intendedUse = "PAPER_TRADING",
                evaluatedAtEpochMs = 1_200L
            )
        )
        assertEquals(
            ObservationQualityEvaluation.Reason.NO_MATCHING_POLICY,
            (result as ObservationQualityEvaluation.NotEvaluated).reason
        )
    }

    @Test
    fun rejectsDuplicatePolicyScope() {
        assertThrows(IllegalArgumentException::class.java) {
            ObservationQualityEvaluator(listOf(policy, policy.copy(version = 2)))
        }
    }
}
