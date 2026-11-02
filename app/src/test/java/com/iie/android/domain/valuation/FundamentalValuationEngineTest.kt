package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FundamentalValuationEngineTest {
    private fun input(
        price: Double = 70.0,
        lower: Double = 100.0,
        upper: Double = 130.0,
        reference: ValuationPriceReference = ValuationPriceReference.EXECUTABLE_BUY,
        risk: ValuationRiskGate = ValuationRiskGate.NO_VETO_IDENTIFIED
    ) = FundamentalValuationInput(
        assetId = "asset:example",
        valuationObject = "native token",
        valuationUnit = "token",
        currency = "USD",
        methodId = "relative-demand-v1",
        applicability = ValuationApplicability.APPLICABLE,
        conditionalRequirementsSatisfied = true,
        evidenceStatus = ValuationEvidenceStatus.SUFFICIENT,
        identityResolved = true,
        fairValueLower = lower,
        fairValueUpper = upper,
        rangeKind = FairValueRangeKind.MODEL_UNCERTAINTY,
        marketPrice = price,
        priceReference = reference,
        valuationTimestampEpochMs = 10_000L,
        marketTimestampEpochMs = 10_000L,
        maximumTimestampSkewMs = 0L,
        sameEconomicObjectAndUnit = true,
        liquidityAndDepthReviewed = true,
        executionCostsIncluded = true,
        sensitivityRobust = true,
        alternativeMethodsReviewed = true,
        riskGate = risk
    )

    @Test fun robustDiscountCanBeModelSupportedButIsNotAnExpectedReturn() {
        val result = FundamentalValuationEngine.evaluate(input())
        assertEquals(ValuationClassification.MODEL_SUPPORTED_UNDERVALUATION, result.classification)
        assertEquals(30.0, result.conservativeGapPercent!!, 0.000001)
        assertTrue(result.isDecisionEligible)
        assertTrue(result.reasons.contains("MARKET_PRICE_BELOW_FAIR_VALUE_LOWER_BOUND"))
    }

    @Test fun lastTradeCannotPassExecutableEntryGate() {
        val result = FundamentalValuationEngine.evaluate(input(reference = ValuationPriceReference.LAST_TRADE))
        assertEquals(ValuationClassification.POTENTIAL_UNDERVALUATION, result.classification)
        assertFalse(result.isDecisionEligible)
        assertTrue(result.reasons.contains("EXECUTABLE_BUY_PRICE_REQUIRED"))
    }

    @Test fun criticalRiskVetoBlocksSupportedClassification() {
        val result = FundamentalValuationEngine.evaluate(input(risk = ValuationRiskGate.VETO_ACTIVE))
        assertEquals(ValuationClassification.POTENTIAL_UNDERVALUATION, result.classification)
        assertFalse(result.isDecisionEligible)
        assertTrue(result.reasons.contains("CRITICAL_RISK_VETO_ACTIVE"))
    }

    @Test fun priceInsideRangeIsNotUndervalued() {
        val result = FundamentalValuationEngine.evaluate(input(price = 110.0))
        assertEquals(ValuationClassification.WITHIN_FAIR_VALUE_RANGE, result.classification)
        assertFalse(result.isDecisionEligible)
        assertEquals(-10.0, result.conservativeGapPercent!!, 0.000001)
    }

    @Test fun priceAboveRangeIsModelOvervalued() {
        val result = FundamentalValuationEngine.evaluate(input(price = 140.0))
        assertEquals(ValuationClassification.OVERVALUED_BY_MODEL, result.classification)
        assertEquals(-40.0, result.conservativeGapPercent!!, 0.000001)
    }

    @Test fun unresolvedIdentityAbstainsBeforeCalculatingGap() {
        val result = FundamentalValuationEngine.evaluate(input().copy(identityResolved = false))
        assertEquals(ValuationClassification.INSUFFICIENT_EVIDENCE, result.classification)
        assertNull(result.conservativeGapPercent)
        assertTrue(result.reasons.contains("ASSET_IDENTITY_NOT_RESOLVED"))
    }

    @Test fun conditionalMethodWithoutRightsEvidenceCannotValueToken() {
        val result = FundamentalValuationEngine.evaluate(
            input().copy(applicability = ValuationApplicability.CONDITIONAL, conditionalRequirementsSatisfied = false)
        )
        assertEquals(ValuationClassification.NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE, result.classification)
        assertTrue(result.reasons.contains("CONDITIONAL_METHOD_REQUIREMENTS_NOT_MET"))
        assertNull(result.conservativeGapPercent)
    }

    @Test fun conflictedEvidenceCannotProduceDiscountClassification() {
        val result = FundamentalValuationEngine.evaluate(
            input().copy(evidenceStatus = ValuationEvidenceStatus.CONFLICTED)
        )
        assertEquals(ValuationClassification.INSUFFICIENT_EVIDENCE, result.classification)
        assertNull(result.conservativeGapPercent)
    }

    @Test fun extremeUndervaluationIsNeverInferredFromLargeGap() {
        val result = FundamentalValuationEngine.evaluate(input(price = 1.0))
        assertEquals(ValuationClassification.MODEL_SUPPORTED_UNDERVALUATION, result.classification)
        assertTrue(result.reasons.contains("MARKET_PRICE_BELOW_FAIR_VALUE_LOWER_BOUND"))
        assertFalse(result.reasons.any { it.contains("EXTREME") })
    }

    @Test fun invalidRangeAbstains() {
        val result = FundamentalValuationEngine.evaluate(input(lower = 0.0, upper = 130.0))
        assertEquals(ValuationClassification.NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE, result.classification)
        assertNull(result.conservativeGapPercent)
    }

    @Test fun differentMarketAndValuationTimestampsAreNotComparedWithoutTolerance() {
        val result = FundamentalValuationEngine.evaluate(input().copy(marketTimestampEpochMs = 20_000L))
        assertEquals(ValuationClassification.INSUFFICIENT_EVIDENCE, result.classification)
        assertNull(result.conservativeGapPercent)
    }

    @Test fun explicitTimestampToleranceAllowsDocumentedTemporalAlignment() {
        val result = FundamentalValuationEngine.evaluate(
            input().copy(marketTimestampEpochMs = 10_030L, maximumTimestampSkewMs = 60_000L)
        )
        assertEquals(ValuationClassification.MODEL_SUPPORTED_UNDERVALUATION, result.classification)
    }
}
