package com.iie.android.domain.valuation

import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.domain.model.MarketResearchSection
import com.iie.android.domain.model.ResearchFieldStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetValuationResearchAdapterTest {
    private fun dossier(
        identityStatus: String = "RESOLVED",
        identity: ResearchFieldStatus = ResearchFieldStatus.KNOWN,
        freshness: String = "FRESH",
        quoteStatus: ResearchFieldStatus = ResearchFieldStatus.KNOWN,
        price: Double? = 10.0,
        observedAt: Long? = 1_000L,
        comparison: String = "CONSISTENT"
    ) = AssetResearchDossier(
        assetId = "asset-1",
        assetName = "Asset",
        symbol = "AST",
        identityStatus = identityStatus,
        identityAssessment = identity,
        deployments = emptyList(),
        market = MarketResearchSection(
            quoteStatus = quoteStatus,
            priceUsd = price,
            provider = "provider-a",
            providerUpdatedAtEpochMs = observedAt,
            retrievedAtEpochMs = 1_001L,
            freshnessStatus = freshness,
            historyPointCount = 0,
            latestDailyReturnPercent = null,
            periodReturnPercent = null,
            periodHighUsd = null,
            periodLowUsd = null,
            quoteComparisonStatus = comparison
        ),
        evidenceCount = 1,
        generatedAtEpochMs = 1_001L
    )

    @Test
    fun carriesFreshMarketEvidenceAndOnlyExplicitlyVerifiedRights() {
        val context = AssetValuationResearchAdapter.build(
            dossier(),
            AssetEconomicRightsEvidence(
                assetId = "asset-1",
                monetaryDemandBasis = ValuationEvidenceFact.VERIFIED
            )
        )

        assertTrue(context.assetIdentityVerified)
        assertTrue(context.marketEvidenceUsable)
        assertEquals(10.0, context.marketPriceUsd!!, 0.0)
        assertEquals(listOf(ValuationMethod.MONETARY_DEMAND_SCENARIO), context.methodPlan.applicableMethods)
        assertTrue(context.blockers.isEmpty())
    }

    @Test
    fun staleOrConflictedQuoteBlocksMarketEvidence() {
        val context = AssetValuationResearchAdapter.build(
            dossier(freshness = "STALE", comparison = "CONFLICTED"),
            AssetEconomicRightsEvidence(assetId = "asset-1")
        )

        assertFalse(context.marketEvidenceUsable)
        assertTrue("FRESH_COMPARABLE_MARKET_PRICE_NOT_AVAILABLE" in context.blockers)
    }

    @Test
    fun unresolvedIdentityBlocksIdentityGate() {
        val context = AssetValuationResearchAdapter.build(
            dossier(identityStatus = "UNRESOLVED", identity = ResearchFieldStatus.UNKNOWN),
            AssetEconomicRightsEvidence(assetId = "asset-1")
        )

        assertFalse(context.assetIdentityVerified)
        assertTrue("ASSET_IDENTITY_NOT_VERIFIED" in context.blockers)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsRightsEvidenceForAnotherAsset() {
        AssetValuationResearchAdapter.build(
            dossier(),
            AssetEconomicRightsEvidence(assetId = "different-asset")
        )
    }
}
