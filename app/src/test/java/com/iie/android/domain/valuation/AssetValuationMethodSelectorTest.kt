package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetValuationMethodSelectorTest {
    @Test
    fun unknownRightsDoNotSelectAnyValuationMethod() {
        val plan = AssetValuationMethodSelector.select(AssetEconomicRightsEvidence(assetId = "asset-1"))

        assertTrue(plan.applicableMethods.isEmpty())
        assertTrue(plan.requiresMoreEvidence)
        assertEquals(setOf(AssetEconomicTypology.UNCLASSIFIED), plan.typologies)
    }

    @Test
    fun verifiedRightsSelectOnlyTheirCorrespondingMethods() {
        val plan = AssetValuationMethodSelector.select(
            AssetEconomicRightsEvidence(
                assetId = "asset-2",
                holderClaimOnNetAssets = ValuationEvidenceFact.VERIFIED,
                holderAttributableCashFlows = ValuationEvidenceFact.VERIFIED,
                tokenValueCaptureMechanism = ValuationEvidenceFact.NOT_VERIFIED,
                monetaryDemandBasis = ValuationEvidenceFact.UNKNOWN,
                holderRedemptionRight = ValuationEvidenceFact.NOT_VERIFIED
            )
        )

        assertEquals(
            setOf(ValuationMethod.NAV, ValuationMethod.HOLDER_ATTRIBUTABLE_DCF),
            plan.applicableMethods.toSet()
        )
        assertTrue(AssetEconomicTypology.ASSET_BACKED in plan.typologies)
        assertTrue(AssetEconomicTypology.CASH_FLOW_BEARING in plan.typologies)
        assertFalse(AssetEconomicTypology.UNCLASSIFIED in plan.typologies)
    }

    @Test
    fun conflictedRightsNeverEnableTheirMethod() {
        val plan = AssetValuationMethodSelector.select(
            AssetEconomicRightsEvidence(
                assetId = "asset-3",
                holderClaimOnNetAssets = ValuationEvidenceFact.CONFLICTED,
                holderAttributableCashFlows = ValuationEvidenceFact.VERIFIED
            )
        )

        assertEquals(listOf(ValuationMethod.HOLDER_ATTRIBUTABLE_DCF), plan.applicableMethods)
        assertTrue(plan.evidenceConflicted)
        assertEquals("EVIDENCE_CONFLICTED", plan.decisions.first().reasonCode)
    }

    @Test
    fun noVerifiableEconomicRightsIsNotMisclassifiedAsUndervalued() {
        val plan = AssetValuationMethodSelector.select(
            AssetEconomicRightsEvidence(
                assetId = "asset-4",
                holderClaimOnNetAssets = ValuationEvidenceFact.NOT_VERIFIED,
                holderAttributableCashFlows = ValuationEvidenceFact.NOT_VERIFIED,
                tokenValueCaptureMechanism = ValuationEvidenceFact.NOT_VERIFIED,
                monetaryDemandBasis = ValuationEvidenceFact.NOT_VERIFIED,
                holderRedemptionRight = ValuationEvidenceFact.NOT_VERIFIED
            )
        )

        assertEquals(setOf(AssetEconomicTypology.NO_VERIFIABLE_VALUE_CAPTURE), plan.typologies)
        assertTrue(plan.applicableMethods.isEmpty())
        assertTrue(plan.requiresMoreEvidence)
    }
}
