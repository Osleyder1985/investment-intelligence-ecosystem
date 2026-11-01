package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FundamentalValuationModelsTest {
    private val evidence = EvidenceReferences(listOf("obs:primary", "obs:tokenomics"), 1000L)

    @Test fun navRequiresVerifiedTokenHolderClaimAndDeductsLiabilities() {
        val input = NavModelInput(1000.0, 1200.0, 100.0, 150.0, 10.0, 20.0, 10.0, 12.0, true, evidence)
        val result = FundamentalValuationModels.nav(input) as ValuationModelResult.Estimated
        assertEquals(69.1666667, result.estimate.lowerPerUnit, 0.000001)
        assertEquals(109.0, result.estimate.upperPerUnit, 0.000001)
    }

    @Test fun navAbstainsWhenTokenDoesNotOwnTheReserves() {
        val input = NavModelInput(1000.0, 1200.0, 100.0, 150.0, 10.0, 20.0, 10.0, 12.0, false, evidence)
        val result = FundamentalValuationModels.nav(input) as ValuationModelResult.Abstained
        assertEquals("TOKEN_HOLDER_CLAIM_NOT_VERIFIED", result.reason)
    }

    @Test fun dcfUsesOnlyVerifiedAttributableFlowsAndNoHiddenTerminalValue() {
        val input = DcfModelInput(
            annualCashFlows = listOf(DcfCashFlowRange(100.0, 120.0), DcfCashFlowRange(100.0, 120.0)),
            discountRateLow = 0.10, discountRateHigh = 0.20,
            eligibleUnitsLow = 10.0, eligibleUnitsHigh = 12.0,
            cashFlowsAttributableToTokenVerified = true, evidence = evidence
        )
        val result = FundamentalValuationModels.dcf(input) as ValuationModelResult.Estimated
        assertEquals((100.0 / 1.2 + 100.0 / 1.44) / 12.0, result.estimate.lowerPerUnit, 0.000001)
        assertEquals((120.0 / 1.1 + 120.0 / 1.21) / 10.0, result.estimate.upperPerUnit, 0.000001)
        assertTrue(result.estimate.assumptions.any { it.contains("No terminal value") })
    }

    @Test fun comparableAbstainsWithoutTokenCaptureMechanism() {
        val input = ComparableModelInput(100.0, 120.0, 2.0, 3.0, 0.5, 0.8, 10.0, 12.0, false, evidence)
        val result = FundamentalValuationModels.comparable(input) as ValuationModelResult.Abstained
        assertEquals("TOKEN_CAPTURE_MECHANISM_NOT_VERIFIED", result.reason)
    }

    @Test fun comparableUsesCaptureFractionAndEligibleSupply() {
        val input = ComparableModelInput(100.0, 120.0, 2.0, 3.0, 0.5, 0.8, 10.0, 12.0, true, evidence)
        val result = FundamentalValuationModels.comparable(input) as ValuationModelResult.Estimated
        assertEquals(100.0 * 2.0 * 0.5 / 12.0, result.estimate.lowerPerUnit, 0.000001)
        assertEquals(120.0 * 3.0 * 0.8 / 10.0, result.estimate.upperPerUnit, 0.000001)
    }

    @Test fun monetaryScenarioIsExplicitlyAnUncalibratedScenarioEnvelope() {
        val input = MonetaryScenarioInput(1_000_000.0, 2_000_000.0, 100_000.0, 150_000.0,
            listOf("Adoption assumption documented"), evidence)
        val result = FundamentalValuationModels.monetaryScenario(input) as ValuationModelResult.Estimated
        assertEquals(1_000_000.0 / 150_000.0, result.estimate.lowerPerUnit, 0.000001)
        assertEquals(FairValueRangeKind.SCENARIO_ENVELOPE, result.estimate.rangeKind)
    }

    @Test fun redemptionRequiresHolderSpecificAccess() {
        val input = RedemptionModelInput(1.0, 1.0, 0.01, 0.02, false, evidence)
        val result = FundamentalValuationModels.redemption(input) as ValuationModelResult.Abstained
        assertEquals("HOLDER_REDEMPTION_NOT_ACCESSIBLE_OR_VERIFIED", result.reason)
    }

    @Test fun invalidOrMissingEvidenceNeverProducesAValue() {
        val input = MonetaryScenarioInput(1.0, 2.0, 1.0, 2.0, listOf("assumption"),
            EvidenceReferences(emptyList(), 1000L))
        val result = FundamentalValuationModels.monetaryScenario(input) as ValuationModelResult.Abstained
        assertEquals("MONETARY_SCENARIO_EVIDENCE_REFERENCES_MISSING", result.reason)
    }
}
