package com.iie.android.domain.valuation

/** A reproducible range produced by one method; it is not a statistical confidence interval by default. */
data class FairValueEstimate(
    val methodId: String,
    val lowerPerUnit: Double,
    val upperPerUnit: Double,
    val rangeKind: FairValueRangeKind,
    val asOfEpochMs: Long,
    val evidenceReferences: List<String>,
    val assumptions: List<String>
) {
    init {
        require(methodId.isNotBlank())
        require(lowerPerUnit.isFinite() && upperPerUnit.isFinite())
        require(lowerPerUnit > 0.0 && upperPerUnit >= lowerPerUnit)
        require(asOfEpochMs > 0L)
        require(evidenceReferences.isNotEmpty() && evidenceReferences.all { it.isNotBlank() })
    }
}

sealed class ValuationModelResult {
    data class Estimated(val estimate: FairValueEstimate) : ValuationModelResult()
    data class Abstained(val reason: String) : ValuationModelResult()
}

data class EvidenceReferences(val ids: List<String>, val asOfEpochMs: Long) {
    fun isUsable(): Boolean = ids.isNotEmpty() && ids.all { it.isNotBlank() } && asOfEpochMs > 0L
}

/** NAV is available only when the valued token has a verified claim on the listed net assets. */
data class NavModelInput(
    val grossAssetsLow: Double,
    val grossAssetsHigh: Double,
    val liabilitiesLow: Double,
    val liabilitiesHigh: Double,
    val costsLow: Double,
    val costsHigh: Double,
    val eligibleUnitsLow: Double,
    val eligibleUnitsHigh: Double,
    val holderClaimVerified: Boolean,
    val evidence: EvidenceReferences
)

/** DCF cash flows must be contractually/on-chain attributable to the token holder, not protocol revenue in general. */
data class DcfCashFlowRange(val low: Double, val high: Double)

data class DcfModelInput(
    val annualCashFlows: List<DcfCashFlowRange>,
    val discountRateLow: Double,
    val discountRateHigh: Double,
    val eligibleUnitsLow: Double,
    val eligibleUnitsHigh: Double,
    val cashFlowsAttributableToTokenVerified: Boolean,
    val evidence: EvidenceReferences
)

/** Relative valuation requires a demonstrated mechanism linking the selected metric to token value. */
data class ComparableModelInput(
    val fundamentalMetricLow: Double,
    val fundamentalMetricHigh: Double,
    val multipleLow: Double,
    val multipleHigh: Double,
    val tokenCaptureFractionLow: Double,
    val tokenCaptureFractionHigh: Double,
    val eligibleUnitsLow: Double,
    val eligibleUnitsHigh: Double,
    val tokenCaptureMechanismVerified: Boolean,
    val evidence: EvidenceReferences
)

/** Monetary scenarios are explicit assumptions; no probabilities are inferred or attached. */
data class MonetaryScenarioInput(
    val monetaryDemandLow: Double,
    val monetaryDemandHigh: Double,
    val effectiveSupplyLow: Double,
    val effectiveSupplyHigh: Double,
    val scenarioAssumptions: List<String>,
    val evidence: EvidenceReferences
)

/** Parity is only usable when the evaluated holder can actually redeem in the relevant scope. */
data class RedemptionModelInput(
    val redemptionValueLow: Double,
    val redemptionValueHigh: Double,
    val costsLow: Double,
    val costsHigh: Double,
    val holderEligibilityAndRedemptionVerified: Boolean,
    val evidence: EvidenceReferences
)

object FundamentalValuationModels {
    fun nav(input: NavModelInput): ValuationModelResult {
        if (!input.holderClaimVerified) return abstain("TOKEN_HOLDER_CLAIM_NOT_VERIFIED")
        if (!input.evidence.isUsable()) return abstain("NAV_EVIDENCE_REFERENCES_MISSING")
        if (!orderedNonNegative(input.grossAssetsLow, input.grossAssetsHigh) ||
            !orderedNonNegative(input.liabilitiesLow, input.liabilitiesHigh) ||
            !orderedNonNegative(input.costsLow, input.costsHigh) ||
            !orderedPositive(input.eligibleUnitsLow, input.eligibleUnitsHigh)
        ) return abstain("NAV_INPUTS_INVALID")
        val lower = (input.grossAssetsLow - input.liabilitiesHigh - input.costsHigh) / input.eligibleUnitsHigh
        val upper = (input.grossAssetsHigh - input.liabilitiesLow - input.costsLow) / input.eligibleUnitsLow
        return estimate("nav-v1", lower, upper, input.evidence,
            listOf("Net assets are attributed only to verified token-holder claims.", "Liabilities and costs are deducted before dividing by eligible units."))
    }

    fun dcf(input: DcfModelInput): ValuationModelResult {
        if (!input.cashFlowsAttributableToTokenVerified) return abstain("TOKEN_HOLDER_CASH_FLOW_RIGHT_NOT_VERIFIED")
        if (!input.evidence.isUsable()) return abstain("DCF_EVIDENCE_REFERENCES_MISSING")
        if (input.annualCashFlows.isEmpty() ||
            input.annualCashFlows.any { !orderedNonNegative(it.low, it.high) } ||
            !orderedPositive(input.eligibleUnitsLow, input.eligibleUnitsHigh) ||
            !orderedPositive(input.discountRateLow, input.discountRateHigh)
        ) return abstain("DCF_INPUTS_INVALID")
        var lowerPv = 0.0
        var upperPv = 0.0
        input.annualCashFlows.forEachIndexed { index, cashFlow ->
            val period = index + 1
            lowerPv += cashFlow.low / Math.pow(1.0 + input.discountRateHigh, period.toDouble())
            upperPv += cashFlow.high / Math.pow(1.0 + input.discountRateLow, period.toDouble())
        }
        return estimate("attributable-dcf-v1", lowerPv / input.eligibleUnitsHigh,
            upperPv / input.eligibleUnitsLow, input.evidence,
            listOf("No terminal value is assumed.", "Only verified holder-attributable net cash flows are capitalized.", "Supply dilution is represented by the eligible-unit range."))
    }

    fun comparable(input: ComparableModelInput): ValuationModelResult {
        if (!input.tokenCaptureMechanismVerified) return abstain("TOKEN_CAPTURE_MECHANISM_NOT_VERIFIED")
        if (!input.evidence.isUsable()) return abstain("COMPARABLE_EVIDENCE_REFERENCES_MISSING")
        if (!orderedPositive(input.fundamentalMetricLow, input.fundamentalMetricHigh) ||
            !orderedNonNegative(input.multipleLow, input.multipleHigh) ||
            !orderedFraction(input.tokenCaptureFractionLow, input.tokenCaptureFractionHigh) ||
            !orderedPositive(input.eligibleUnitsLow, input.eligibleUnitsHigh)
        ) return abstain("COMPARABLE_INPUTS_INVALID")
        val lower = input.fundamentalMetricLow * input.multipleLow *
            input.tokenCaptureFractionLow / input.eligibleUnitsHigh
        val upper = input.fundamentalMetricHigh * input.multipleHigh *
            input.tokenCaptureFractionHigh / input.eligibleUnitsLow
        return estimate("token-capture-comparables-v1", lower, upper, input.evidence,
            listOf("Metric and multiples must use a homogeneous comparable universe.", "Only the verified token-capture fraction is attributed to the token."))
    }

    fun monetaryScenario(input: MonetaryScenarioInput): ValuationModelResult {
        if (!input.evidence.isUsable()) return abstain("MONETARY_SCENARIO_EVIDENCE_REFERENCES_MISSING")
        if (!orderedPositive(input.monetaryDemandLow, input.monetaryDemandHigh) ||
            !orderedPositive(input.effectiveSupplyLow, input.effectiveSupplyHigh) ||
            input.scenarioAssumptions.isEmpty() || input.scenarioAssumptions.any { it.isBlank() }
        ) return abstain("MONETARY_SCENARIO_INPUTS_INVALID")
        val lower = input.monetaryDemandLow / input.effectiveSupplyHigh
        val upper = input.monetaryDemandHigh / input.effectiveSupplyLow
        return estimate("monetary-demand-scenario-v1", lower, upper, input.evidence,
            input.scenarioAssumptions + "Scenario envelope only; no probability or forecast accuracy is implied.")
    }

    fun redemption(input: RedemptionModelInput): ValuationModelResult {
        if (!input.holderEligibilityAndRedemptionVerified) return abstain("HOLDER_REDEMPTION_NOT_ACCESSIBLE_OR_VERIFIED")
        if (!input.evidence.isUsable()) return abstain("REDEMPTION_EVIDENCE_REFERENCES_MISSING")
        if (!orderedPositive(input.redemptionValueLow, input.redemptionValueHigh) ||
            !orderedNonNegative(input.costsLow, input.costsHigh)
        ) return abstain("REDEMPTION_INPUTS_INVALID")
        return estimate("net-redemption-v1",
            input.redemptionValueLow - input.costsHigh,
            input.redemptionValueHigh - input.costsLow,
            input.evidence,
            listOf("Net redemption value deducts holder-specific costs and penalties.", "Eligibility, capacity and operational redemption are assumed verified for the stated scope."))
    }

    private fun estimate(
        method: String,
        lower: Double,
        upper: Double,
        evidence: EvidenceReferences,
        assumptions: List<String>
    ): ValuationModelResult {
        if (!lower.isFinite() || !upper.isFinite() || lower <= 0.0 || upper < lower) {
            return abstain("MODEL_OUTPUT_NOT_POSITIVE_OR_NOT_ORDERED")
        }
        return ValuationModelResult.Estimated(FairValueEstimate(
            methodId = method,
            lowerPerUnit = lower,
            upperPerUnit = upper,
            rangeKind = if (method == "monetary-demand-scenario-v1") FairValueRangeKind.SCENARIO_ENVELOPE
                else FairValueRangeKind.MODEL_UNCERTAINTY,
            asOfEpochMs = evidence.asOfEpochMs,
            evidenceReferences = evidence.ids.distinct(),
            assumptions = assumptions
        ))
    }

    private fun abstain(reason: String) = ValuationModelResult.Abstained(reason)
    private fun orderedNonNegative(low: Double, high: Double) =
        low.isFinite() && high.isFinite() && low >= 0.0 && high >= low
    private fun orderedPositive(low: Double, high: Double) =
        low.isFinite() && high.isFinite() && low > 0.0 && high >= low
    private fun orderedFraction(low: Double, high: Double) =
        low.isFinite() && high.isFinite() && low in 0.0..1.0 && high in low..1.0
}
