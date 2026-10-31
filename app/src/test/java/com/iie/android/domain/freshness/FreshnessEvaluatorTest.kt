package com.iie.android.domain.freshness

import com.iie.android.domain.model.FreshnessEvaluation
import com.iie.android.domain.model.FreshnessPolicy
import com.iie.android.domain.model.FreshnessStatus
import com.iie.android.domain.model.TimestampBasis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FreshnessEvaluatorTest {
    private val policy = FreshnessPolicy(
        policyId = "test-market-policy",
        version = 1,
        sourceId = "source:market-test",
        observationType = "MARKET_PRICE",
        intendedUse = "PAPER_TRADING",
        timestampBasis = TimestampBasis.OBSERVED_AT,
        maxAgeMs = 60_000L,
        approachingExpiryWindowMs = 10_000L
    )

    private val evaluator = FreshnessEvaluator(listOf(policy))

    @Test
    fun doesNotInventFreshnessWhenPolicyIsMissing() {
        val result = evaluator.evaluate(
            "source:unknown", "MARKET_PRICE", "PAPER_TRADING",
            100L, 100L, 200L
        )
        assertEquals(
            FreshnessEvaluation.Reason.NO_MATCHING_POLICY,
            (result as FreshnessEvaluation.NotEvaluated).reason
        )
    }

    @Test
    fun requiresTimestampSelectedByPolicy() {
        val result = evaluator.evaluate(
            "source:market-test", "MARKET_PRICE", "PAPER_TRADING",
            null, 100L, 200L
        )
        assertEquals(
            FreshnessEvaluation.Reason.REQUIRED_TIMESTAMP_MISSING,
            (result as FreshnessEvaluation.NotEvaluated).reason
        )
    }

    @Test
    fun identifiesFreshObservation() {
        val result = evaluator.evaluate(
            "source:market-test", "MARKET_PRICE", "PAPER_TRADING",
            150_000L, 155_000L, 200_000L
        ) as FreshnessEvaluation.Evaluated
        assertEquals(FreshnessStatus.FRESH, result.assessment.status)
        assertEquals(50_000L, result.assessment.ageMs)
        assertEquals("test-market-policy", result.assessment.policyId)
    }

    @Test
    fun identifiesApproachingExpiryAtConfiguredBoundary() {
        val result = evaluator.evaluate(
            "source:market-test", "MARKET_PRICE", "PAPER_TRADING",
            150_000L, 155_000L, 200_000L
        ) as FreshnessEvaluation.Evaluated
        assertEquals(FreshnessStatus.FRESH, result.assessment.status)

        val nearExpiry = evaluator.evaluate(
            "source:market-test", "MARKET_PRICE", "PAPER_TRADING",
            145_000L, 155_000L, 200_000L
        ) as FreshnessEvaluation.Evaluated
        assertEquals(FreshnessStatus.APPROACHING_EXPIRY, nearExpiry.assessment.status)
    }

    @Test
    fun identifiesStaleObservationOnlyAfterMaximumAge() {
        val result = evaluator.evaluate(
            "source:market-test", "MARKET_PRICE", "PAPER_TRADING",
            139_999L, 155_000L, 200_000L
        ) as FreshnessEvaluation.Evaluated
        assertEquals(FreshnessStatus.STALE, result.assessment.status)
    }

    @Test
    fun rejectsFutureOrOutOfOrderTimestampsAsNotEvaluated() {
        val result = evaluator.evaluate(
            "source:market-test", "MARKET_PRICE", "PAPER_TRADING",
            210L, 200L, 205L
        )
        assertEquals(
            FreshnessEvaluation.Reason.INVALID_TIME_ORDER,
            (result as FreshnessEvaluation.NotEvaluated).reason
        )
    }

    @Test
    fun duplicatePoliciesForSameScopeAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            FreshnessEvaluator(listOf(policy, policy.copy(version = 2)))
        }
    }
}
