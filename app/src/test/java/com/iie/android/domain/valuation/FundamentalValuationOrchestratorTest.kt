package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FundamentalValuationOrchestratorTest {
    private val timestamp = 1_000L

    private fun context(rights: AssetEconomicRightsEvidence = AssetEconomicRightsEvidence(
        assetId = "asset-1",
        holderClaimOnNetAssets = ValuationEvidenceFact.VERIFIED,
        holderAttributableCashFlows = ValuationEvidenceFact.VERIFIED
    )): AssetValuationResearchContext {
        val plan = AssetValuationMethodSelector.select(rights)
        return AssetValuationResearchContext(
            assetId = "asset-1",
            assetIdentityVerified = true,
            marketPriceUsd = 7.0,
            marketObservedAtEpochMs = timestamp,
            marketProvider = "provider",
            marketFreshnessStatus = "FRESH",
            marketEvidenceUsable = true,
            methodPlan = plan,
            blockers = emptyList()
        )
    }

    private fun metadata(
        methodTime: Long = timestamp,
        assetId: String = "asset-1",
        evidenceStatus: ValuationEvidenceStatus = ValuationEvidenceStatus.SUFFICIENT
    ) = ValuationRunMetadata(
        assetId = assetId,
        valuationObject = "token-holder-claim",
        valuationUnit = "token",
        currency = "USD",
        valuationTimestampEpochMs = methodTime,
        evidenceStatus = evidenceStatus,
        priceReference = ValuationPriceReference.EXECUTABLE_BUY,
        maximumTimestampSkewMs = 0L,
        liquidityAndDepthReviewed = true,
        executionCostsIncluded = true,
        sensitivityRobust = true,
        alternativeMethodsReviewed = true,
        riskGate = ValuationRiskGate.NO_VETO_IDENTIFIED
    )

    private fun navRequest(meta: ValuationRunMetadata = metadata(), evidenceId: String = "nav-source") =
        ValuationModelRequest.Nav(
            NavModelInput(
                grossAssetsLow = 100.0, grossAssetsHigh = 120.0,
                liabilitiesLow = 0.0, liabilitiesHigh = 0.0,
                costsLow = 0.0, costsHigh = 0.0,
                eligibleUnitsLow = 10.0, eligibleUnitsHigh = 10.0,
                holderClaimVerified = true,
                evidence = EvidenceReferences(listOf(evidenceId), timestamp)
            ), meta
        )

    private fun dcfRequest(meta: ValuationRunMetadata = metadata(), evidenceId: String = "dcf-source") =
        ValuationModelRequest.Dcf(
            DcfModelInput(
                annualCashFlows = listOf(DcfCashFlowRange(100.0, 120.0)),
                discountRateLow = 0.1, discountRateHigh = 0.2,
                eligibleUnitsLow = 10.0, eligibleUnitsHigh = 10.0,
                cashFlowsAttributableToTokenVerified = true,
                evidence = EvidenceReferences(listOf(evidenceId), timestamp)
            ), meta
        )

    @Test fun oneEligibleEstimateFlowsThroughClassificationEngine() {
        val result = FundamentalValuationOrchestrator.evaluate(
            context(), mapOf(ValuationMethod.NAV to navRequest())
        ) as ValuationOrchestratorResult.Classified

        assertEquals(1, result.runs.size)
        assertEquals(ValuationMethod.NAV, result.runs.single().method)
        assertEquals(FairValueRangeKind.MODEL_UNCERTAINTY, result.consolidated.rangeKind)
        assertEquals(ValuationClassification.POTENTIAL_UNDERVALUATION, result.valuation.classification)
    }

    @Test fun nonEligibleMethodIsNeverExecuted() {
        val onlyMonetary = context(AssetEconomicRightsEvidence(
            assetId = "asset-1",
            monetaryDemandBasis = ValuationEvidenceFact.VERIFIED
        ))
        val result = FundamentalValuationOrchestrator.evaluate(
            onlyMonetary,
            mapOf(ValuationMethod.NAV to navRequest())
        ) as ValuationOrchestratorResult.Abstained

        assertTrue(result.runs.isEmpty())
        assertTrue(result.blockers.contains("NO_ELIGIBLE_MODEL_PRODUCED_A_VALID_ESTIMATE"))
    }

    @Test fun independentComparableEstimatesUseConservativeEnvelopeNotMean() {
        val result = FundamentalValuationOrchestrator.evaluate(
            context(),
            mapOf(
                ValuationMethod.NAV to navRequest(evidenceId = "nav-only"),
                ValuationMethod.HOLDER_ATTRIBUTABLE_DCF to dcfRequest(evidenceId = "dcf-only")
            )
        ) as ValuationOrchestratorResult.Classified

        assertEquals(FairValueRangeKind.MODEL_CONSENSUS, result.consolidated.rangeKind)
        assertEquals(100.0 / 1.2 / 10.0, result.consolidated.lowerPerUnit, 0.000001)
        assertEquals(12.0, result.consolidated.upperPerUnit, 0.0)
        assertTrue(result.consolidated.assumptions.any { it.contains("not an average", ignoreCase = true) })
    }


    @Test fun unexecutedEligibleMethodPreventsDecisionEligibleUndervaluation() {
        val threeMethods = context(AssetEconomicRightsEvidence(
            assetId = "asset-1",
            holderClaimOnNetAssets = ValuationEvidenceFact.VERIFIED,
            holderAttributableCashFlows = ValuationEvidenceFact.VERIFIED,
            tokenValueCaptureMechanism = ValuationEvidenceFact.VERIFIED
        ))
        val result = FundamentalValuationOrchestrator.evaluate(
            threeMethods,
            mapOf(
                ValuationMethod.NAV to navRequest(),
                ValuationMethod.HOLDER_ATTRIBUTABLE_DCF to dcfRequest()
            )
        ) as ValuationOrchestratorResult.Classified

        assertEquals(ValuationClassification.POTENTIAL_UNDERVALUATION, result.valuation.classification)
        assertFalse(result.valuation.isDecisionEligible)
        assertTrue(result.valuation.reasons.contains("ALTERNATIVE_METHODS_NOT_REVIEWED"))
    }

    @Test fun overlappingEvidenceCannotBeMisrepresentedAsIndependentConsensus() {
        val result = FundamentalValuationOrchestrator.evaluate(
            context(),
            mapOf(
                ValuationMethod.NAV to navRequest(evidenceId = "shared-source"),
                ValuationMethod.HOLDER_ATTRIBUTABLE_DCF to dcfRequest(evidenceId = "shared-source")
            )
        ) as ValuationOrchestratorResult.Abstained

        assertTrue(result.blockers.contains("MODEL_ESTIMATES_NOT_COMPARABLE_OR_NOT_INDEPENDENT"))
    }

    @Test fun incompatibleAssetAndTimestampAreRejected() {
        val wrongAsset = FundamentalValuationOrchestrator.evaluate(
            context(), mapOf(ValuationMethod.NAV to navRequest(metadata(assetId = "other")))
        ) as ValuationOrchestratorResult.Abstained
        assertTrue((wrongAsset.runs.single().result as ValuationModelResult.Abstained)
            .reason == "MODEL_REQUEST_ASSET_MISMATCH")

        val wrongTime = FundamentalValuationOrchestrator.evaluate(
            context(), mapOf(ValuationMethod.NAV to navRequest(metadata(methodTime = 2_000L)))
        ) as ValuationOrchestratorResult.Abstained
        assertTrue(wrongTime.blockers.contains("MODEL_ESTIMATES_NOT_COMPARABLE_OR_NOT_INDEPENDENT"))
    }


    @Test fun differentCurrencyOrValuationObjectCannotBeCombined() {
        val currencyMismatch = FundamentalValuationOrchestrator.evaluate(
            context(),
            mapOf(
                ValuationMethod.NAV to navRequest(),
                ValuationMethod.HOLDER_ATTRIBUTABLE_DCF to dcfRequest(metadata().copy(currency = "EUR"))
            )
        ) as ValuationOrchestratorResult.Abstained
        assertTrue(currencyMismatch.blockers.contains("MODEL_ESTIMATES_NOT_COMPARABLE_OR_NOT_INDEPENDENT"))

        val objectMismatch = FundamentalValuationOrchestrator.evaluate(
            context(),
            mapOf(
                ValuationMethod.NAV to navRequest(),
                ValuationMethod.HOLDER_ATTRIBUTABLE_DCF to dcfRequest(metadata().copy(valuationObject = "protocol-revenue"))
            )
        ) as ValuationOrchestratorResult.Abstained
        assertTrue(objectMismatch.blockers.contains("MODEL_ESTIMATES_NOT_COMPARABLE_OR_NOT_INDEPENDENT"))
    }

    @Test fun missingMarketEvidenceAbstainsBeforeModelExecution() {
        val invalid = context().copy(marketEvidenceUsable = false, marketPriceUsd = null)
        val result = FundamentalValuationOrchestrator.evaluate(
            invalid, mapOf(ValuationMethod.NAV to navRequest())
        ) as ValuationOrchestratorResult.Abstained

        assertTrue(result.runs.isEmpty())
        assertTrue(result.blockers.contains("FRESH_COMPARABLE_MARKET_PRICE_NOT_AVAILABLE"))
    }

    @Test fun absentOrActiveRiskVetoNeverBecomesDecisionEligible() {
        val notAssessed = FundamentalValuationOrchestrator.evaluate(
            context(), mapOf(ValuationMethod.NAV to navRequest(metadata().copy(
                riskGate = ValuationRiskGate.VETO_NOT_ASSESSED
            )))
        ) as ValuationOrchestratorResult.Classified
        assertEquals(ValuationClassification.POTENTIAL_UNDERVALUATION, notAssessed.valuation.classification)
        assertFalse(notAssessed.valuation.isDecisionEligible)

        val active = FundamentalValuationOrchestrator.evaluate(
            context(), mapOf(ValuationMethod.NAV to navRequest(metadata().copy(
                riskGate = ValuationRiskGate.VETO_ACTIVE
            )))
        ) as ValuationOrchestratorResult.Classified
        assertEquals(ValuationClassification.POTENTIAL_UNDERVALUATION, active.valuation.classification)
        assertFalse(active.valuation.isDecisionEligible)
    }

    @Test fun insufficientEvidenceDoesNotProduceInvestmentClassification() {
        val result = FundamentalValuationOrchestrator.evaluate(
            context(),
            mapOf(ValuationMethod.NAV to navRequest(metadata(evidenceStatus = ValuationEvidenceStatus.PARTIAL)))
        ) as ValuationOrchestratorResult.Classified

        assertEquals(ValuationClassification.INSUFFICIENT_EVIDENCE, result.valuation.classification)
        assertFalse(result.valuation.isDecisionEligible)
    }
}
