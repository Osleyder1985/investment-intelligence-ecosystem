package com.iie.android.domain.valuation

import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.domain.model.ResearchFieldStatus

/**
 * Joins the persisted research dossier to explicitly reviewed economic-rights evidence.
 * Market observations are carried forward; economic rights are never inferred from quote data.
 */
data class AssetValuationResearchContext(
    val assetId: String,
    val assetIdentityVerified: Boolean,
    val marketPriceUsd: Double?,
    val marketObservedAtEpochMs: Long?,
    val marketProvider: String?,
    val marketFreshnessStatus: String,
    val marketEvidenceUsable: Boolean,
    val methodPlan: AssetValuationMethodPlan,
    val blockers: List<String>
)

object AssetValuationResearchAdapter {
    fun build(
        dossier: AssetResearchDossier,
        economicRights: AssetEconomicRightsEvidence
    ): AssetValuationResearchContext {
        require(dossier.assetId.isNotBlank())
        require(dossier.assetId == economicRights.assetId) {
            "Dossier and economic-rights evidence must refer to the same asset"
        }

        val blockers = mutableListOf<String>()
        val identityVerified = dossier.identityAssessment == ResearchFieldStatus.KNOWN &&
            dossier.identityStatus.equals("RESOLVED", ignoreCase = true)
        if (!identityVerified) blockers += "ASSET_IDENTITY_NOT_VERIFIED"

        val price = dossier.market.priceUsd
        val observedAt = dossier.market.providerUpdatedAtEpochMs
        val marketEvidenceUsable = dossier.market.quoteStatus == ResearchFieldStatus.KNOWN &&
            price != null && price.isFinite() && price > 0.0 &&
            observedAt != null && observedAt > 0L &&
            dossier.market.freshnessStatus.equals("FRESH", ignoreCase = true) &&
            dossier.market.quoteComparisonStatus != "CONFLICTED"
        if (!marketEvidenceUsable) blockers += "FRESH_COMPARABLE_MARKET_PRICE_NOT_AVAILABLE"

        val methodPlan = AssetValuationMethodSelector.select(economicRights)
        if (methodPlan.evidenceConflicted) blockers += "ECONOMIC_RIGHTS_EVIDENCE_CONFLICTED"
        if (methodPlan.applicableMethods.isEmpty()) blockers += "NO_VALUATION_METHOD_ESTABLISHED"

        return AssetValuationResearchContext(
            assetId = dossier.assetId,
            assetIdentityVerified = identityVerified,
            marketPriceUsd = price?.takeIf { it.isFinite() && it > 0.0 },
            marketObservedAtEpochMs = observedAt?.takeIf { it > 0L },
            marketProvider = dossier.market.provider,
            marketFreshnessStatus = dossier.market.freshnessStatus,
            marketEvidenceUsable = marketEvidenceUsable,
            methodPlan = methodPlan,
            blockers = blockers.distinct()
        )
    }
}
