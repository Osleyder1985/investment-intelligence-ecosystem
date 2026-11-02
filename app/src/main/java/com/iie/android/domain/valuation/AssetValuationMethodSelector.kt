package com.iie.android.domain.valuation

/**
 * Explicit, evidence-driven economic rights used to choose valuation methods.
 * The selector never infers rights from a token name, ticker, market cap or price history.
 */
enum class ValuationEvidenceFact {
    VERIFIED,
    UNKNOWN,
    NOT_VERIFIED,
    CONFLICTED
}

enum class AssetEconomicTypology {
    CASH_FLOW_BEARING,
    ASSET_BACKED,
    REDEEMABLE,
    MONETARY_NETWORK_ASSET,
    VALUE_CAPTURE_COMPARABLE,
    NO_VERIFIABLE_VALUE_CAPTURE,
    UNCLASSIFIED
}

enum class ValuationMethod {
    NAV,
    HOLDER_ATTRIBUTABLE_DCF,
    TOKEN_CAPTURE_COMPARABLES,
    MONETARY_DEMAND_SCENARIO,
    NET_REDEMPTION
}

data class AssetEconomicRightsEvidence(
    val assetId: String,
    val holderClaimOnNetAssets: ValuationEvidenceFact = ValuationEvidenceFact.UNKNOWN,
    val holderAttributableCashFlows: ValuationEvidenceFact = ValuationEvidenceFact.UNKNOWN,
    val tokenValueCaptureMechanism: ValuationEvidenceFact = ValuationEvidenceFact.UNKNOWN,
    val monetaryDemandBasis: ValuationEvidenceFact = ValuationEvidenceFact.UNKNOWN,
    val holderRedemptionRight: ValuationEvidenceFact = ValuationEvidenceFact.UNKNOWN
)

data class ValuationMethodDecision(
    val method: ValuationMethod,
    val applicable: Boolean,
    val reasonCode: String
)

data class AssetValuationMethodPlan(
    val assetId: String,
    val typologies: Set<AssetEconomicTypology>,
    val decisions: List<ValuationMethodDecision>,
    val evidenceConflicted: Boolean
) {
    val applicableMethods: List<ValuationMethod>
        get() = decisions.filter { it.applicable }.map { it.method }

    val requiresMoreEvidence: Boolean
        get() = applicableMethods.isEmpty() && !evidenceConflicted
}

/**
 * Chooses model eligibility only. It does not calculate fair value or emit an
 * investment classification; each selected model still validates its own inputs.
 */
object AssetValuationMethodSelector {
    fun select(evidence: AssetEconomicRightsEvidence): AssetValuationMethodPlan {
        require(evidence.assetId.isNotBlank())

        val facts = listOf(
            evidence.holderClaimOnNetAssets,
            evidence.holderAttributableCashFlows,
            evidence.tokenValueCaptureMechanism,
            evidence.monetaryDemandBasis,
            evidence.holderRedemptionRight
        )
        val conflicted = facts.any { it == ValuationEvidenceFact.CONFLICTED }
        val typologies = linkedSetOf<AssetEconomicTypology>()

        if (evidence.holderClaimOnNetAssets == ValuationEvidenceFact.VERIFIED) {
            typologies += AssetEconomicTypology.ASSET_BACKED
        }
        if (evidence.holderAttributableCashFlows == ValuationEvidenceFact.VERIFIED) {
            typologies += AssetEconomicTypology.CASH_FLOW_BEARING
        }
        if (evidence.holderRedemptionRight == ValuationEvidenceFact.VERIFIED) {
            typologies += AssetEconomicTypology.REDEEMABLE
        }
        if (evidence.monetaryDemandBasis == ValuationEvidenceFact.VERIFIED) {
            typologies += AssetEconomicTypology.MONETARY_NETWORK_ASSET
        }
        if (evidence.tokenValueCaptureMechanism == ValuationEvidenceFact.VERIFIED) {
            typologies += AssetEconomicTypology.VALUE_CAPTURE_COMPARABLE
        }

        val decisions = listOf(
            decision(ValuationMethod.NAV, evidence.holderClaimOnNetAssets,
                "NAV_HOLDER_CLAIM_VERIFIED", "NAV_HOLDER_CLAIM_NOT_VERIFIED"),
            decision(ValuationMethod.HOLDER_ATTRIBUTABLE_DCF, evidence.holderAttributableCashFlows,
                "DCF_HOLDER_CASH_FLOWS_VERIFIED", "DCF_HOLDER_CASH_FLOWS_NOT_VERIFIED"),
            decision(ValuationMethod.TOKEN_CAPTURE_COMPARABLES, evidence.tokenValueCaptureMechanism,
                "COMPARABLE_TOKEN_CAPTURE_VERIFIED", "COMPARABLE_TOKEN_CAPTURE_NOT_VERIFIED"),
            decision(ValuationMethod.MONETARY_DEMAND_SCENARIO, evidence.monetaryDemandBasis,
                "MONETARY_DEMAND_BASIS_VERIFIED", "MONETARY_DEMAND_BASIS_NOT_VERIFIED"),
            decision(ValuationMethod.NET_REDEMPTION, evidence.holderRedemptionRight,
                "REDEMPTION_RIGHT_VERIFIED", "REDEMPTION_RIGHT_NOT_VERIFIED")
        )

        if (typologies.isEmpty()) typologies += AssetEconomicTypology.UNCLASSIFIED
        if (facts.all { it == ValuationEvidenceFact.NOT_VERIFIED }) {
            typologies.clear()
            typologies += AssetEconomicTypology.NO_VERIFIABLE_VALUE_CAPTURE
        }

        return AssetValuationMethodPlan(
            assetId = evidence.assetId,
            typologies = typologies,
            decisions = decisions,
            evidenceConflicted = conflicted
        )
    }

    private fun decision(
        method: ValuationMethod,
        fact: ValuationEvidenceFact,
        verifiedReason: String,
        unavailableReason: String
    ): ValuationMethodDecision = ValuationMethodDecision(
        method = method,
        applicable = fact == ValuationEvidenceFact.VERIFIED,
        reasonCode = when (fact) {
            ValuationEvidenceFact.VERIFIED -> verifiedReason
            ValuationEvidenceFact.CONFLICTED -> "EVIDENCE_CONFLICTED"
            ValuationEvidenceFact.UNKNOWN -> unavailableReason
            ValuationEvidenceFact.NOT_VERIFIED -> unavailableReason
        }
    )
}
