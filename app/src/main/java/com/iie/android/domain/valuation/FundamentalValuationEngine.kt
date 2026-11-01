package com.iie.android.domain.valuation

/**
 * Conservative, evidence-bounded comparison of an executable market reference
 * against a defensible fair-value range. This is not a price predictor.
 */
enum class ValuationApplicability { APPLICABLE, CONDITIONAL, NOT_APPLICABLE, INSUFFICIENT_EVIDENCE }
enum class ValuationEvidenceStatus { SUFFICIENT, PARTIAL, INSUFFICIENT, CONFLICTED }
enum class FairValueRangeKind { STATISTICAL_INTERVAL, MODEL_UNCERTAINTY, SCENARIO_ENVELOPE, MODEL_CONSENSUS }
enum class ValuationPriceReference { EXECUTABLE_BUY, LAST_TRADE, EXECUTABLE_SELL }
enum class ValuationRiskGate { NO_VETO_IDENTIFIED, VETO_REVIEW_REQUIRED, VETO_ACTIVE, VETO_NOT_ASSESSED }
enum class ValuationClassification {
    INSUFFICIENT_EVIDENCE,
    NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE,
    OVERVALUED_BY_MODEL,
    WITHIN_FAIR_VALUE_RANGE,
    POTENTIAL_UNDERVALUATION,
    MODEL_SUPPORTED_UNDERVALUATION
}

/** Inputs must already be normalized to one asset representation, unit and currency. */
data class FundamentalValuationInput(
    val assetId: String,
    val valuationObject: String,
    val valuationUnit: String,
    val currency: String,
    val methodId: String,
    val applicability: ValuationApplicability,
    val conditionalRequirementsSatisfied: Boolean,
    val evidenceStatus: ValuationEvidenceStatus,
    val identityResolved: Boolean,
    val fairValueLower: Double?,
    val fairValueUpper: Double?,
    val rangeKind: FairValueRangeKind,
    val marketPrice: Double?,
    val priceReference: ValuationPriceReference,
    val valuationTimestampEpochMs: Long,
    val marketTimestampEpochMs: Long,
    /** Explicitly justified temporal tolerance; zero requires the same timestamp. */
    val maximumTimestampSkewMs: Long,
    val sameEconomicObjectAndUnit: Boolean,
    val liquidityAndDepthReviewed: Boolean,
    val executionCostsIncluded: Boolean,
    val sensitivityRobust: Boolean,
    val alternativeMethodsReviewed: Boolean,
    val riskGate: ValuationRiskGate
)

data class FundamentalValuationResult(
    val classification: ValuationClassification,
    /** (fairValueLower - marketPrice) / fairValueLower; descriptive gap, not expected return. */
    val conservativeGapPercent: Double?,
    val absoluteGap: Double?,
    val isDecisionEligible: Boolean,
    val reasons: List<String>,
    val fairValueLower: Double?,
    val fairValueUpper: Double?,
    val rangeKind: FairValueRangeKind,
    val methodId: String
)

object FundamentalValuationEngine {
    fun evaluate(input: FundamentalValuationInput): FundamentalValuationResult {
        val reasons = mutableListOf<String>()
        fun result(
            classification: ValuationClassification,
            gap: Double? = null,
            absoluteGap: Double? = null,
            eligible: Boolean = false
        ) = FundamentalValuationResult(
            classification, gap, absoluteGap, eligible, reasons.toList(),
            input.fairValueLower, input.fairValueUpper, input.rangeKind, input.methodId
        )

        if (input.assetId.isBlank() || input.valuationObject.isBlank() ||
            input.valuationUnit.isBlank() || input.currency.isBlank() || input.methodId.isBlank()
        ) reasons += "VALUATION_IDENTITY_OR_METHOD_MISSING"
        if (!input.identityResolved) reasons += "ASSET_IDENTITY_NOT_RESOLVED"
        if (!input.sameEconomicObjectAndUnit) reasons += "VALUATION_AND_MARKET_OBJECT_NOT_COMPARABLE"
        if (input.valuationTimestampEpochMs <= 0L || input.marketTimestampEpochMs <= 0L) {
            reasons += "COMPARABLE_TIMESTAMP_MISSING"
        }
        if (input.maximumTimestampSkewMs < 0L) reasons += "TIMESTAMP_SKEW_TOLERANCE_INVALID"
        if (input.valuationTimestampEpochMs > 0L && input.marketTimestampEpochMs > 0L &&
            kotlin.math.abs(input.valuationTimestampEpochMs - input.marketTimestampEpochMs) > input.maximumTimestampSkewMs
        ) reasons += "VALUATION_AND_MARKET_TIMESTAMPS_DIFFER"
        if (reasons.isNotEmpty()) return result(ValuationClassification.INSUFFICIENT_EVIDENCE)

        when (input.applicability) {
            ValuationApplicability.NOT_APPLICABLE -> {
                reasons += "METHOD_NOT_APPLICABLE_TO_ASSET"
                return result(ValuationClassification.NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE)
            }
            ValuationApplicability.INSUFFICIENT_EVIDENCE -> {
                reasons += "METHOD_APPLICABILITY_NOT_ESTABLISHED"
                return result(ValuationClassification.INSUFFICIENT_EVIDENCE)
            }
            ValuationApplicability.CONDITIONAL -> if (!input.conditionalRequirementsSatisfied) {
                reasons += "CONDITIONAL_METHOD_REQUIREMENTS_NOT_MET"
                return result(ValuationClassification.NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE)
            }
            ValuationApplicability.APPLICABLE -> Unit
        }

        if (input.evidenceStatus != ValuationEvidenceStatus.SUFFICIENT) {
            reasons += when (input.evidenceStatus) {
                ValuationEvidenceStatus.PARTIAL -> "EVIDENCE_COVERAGE_PARTIAL"
                ValuationEvidenceStatus.INSUFFICIENT -> "EVIDENCE_INSUFFICIENT"
                ValuationEvidenceStatus.CONFLICTED -> "EVIDENCE_CONFLICTED"
                ValuationEvidenceStatus.SUFFICIENT -> error("Unreachable")
            }
            return result(ValuationClassification.INSUFFICIENT_EVIDENCE)
        }

        val lower = input.fairValueLower
        val upper = input.fairValueUpper
        val price = input.marketPrice
        if (lower == null || upper == null || price == null ||
            !lower.isFinite() || !upper.isFinite() || !price.isFinite() ||
            lower <= 0.0 || upper < lower || price <= 0.0
        ) {
            reasons += "FAIR_VALUE_RANGE_OR_MARKET_PRICE_INVALID"
            return result(ValuationClassification.NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE)
        }

        val absoluteGap = lower - price
        val gapPercent = absoluteGap / lower * 100.0
        if (!gapPercent.isFinite()) {
            reasons += "VALUATION_GAP_NOT_FINITE"
            return result(ValuationClassification.NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE)
        }

        if (price > upper) {
            reasons += "MARKET_PRICE_ABOVE_FAIR_VALUE_RANGE"
            return result(ValuationClassification.OVERVALUED_BY_MODEL, gapPercent, absoluteGap)
        }
        if (price >= lower) {
            reasons += "MARKET_PRICE_WITHIN_FAIR_VALUE_RANGE"
            return result(ValuationClassification.WITHIN_FAIR_VALUE_RANGE, gapPercent, absoluteGap)
        }

        reasons += "MARKET_PRICE_BELOW_FAIR_VALUE_LOWER_BOUND"
        val gatesPassed = mutableListOf<String>()
        if (input.priceReference != ValuationPriceReference.EXECUTABLE_BUY) {
            gatesPassed += "EXECUTABLE_BUY_PRICE_REQUIRED"
        }
        if (!input.liquidityAndDepthReviewed) gatesPassed += "LIQUIDITY_AND_DEPTH_NOT_REVIEWED"
        if (!input.executionCostsIncluded) gatesPassed += "EXECUTION_COSTS_NOT_INCLUDED"
        if (!input.sensitivityRobust) gatesPassed += "VALUATION_NOT_ROBUST_TO_SENSITIVITY"
        if (!input.alternativeMethodsReviewed) gatesPassed += "ALTERNATIVE_METHODS_NOT_REVIEWED"
        if (input.riskGate != ValuationRiskGate.NO_VETO_IDENTIFIED) {
            gatesPassed += when (input.riskGate) {
                ValuationRiskGate.VETO_ACTIVE -> "CRITICAL_RISK_VETO_ACTIVE"
                ValuationRiskGate.VETO_REVIEW_REQUIRED -> "CRITICAL_RISK_REVIEW_REQUIRED"
                ValuationRiskGate.VETO_NOT_ASSESSED -> "RISK_VETO_NOT_ASSESSED"
                ValuationRiskGate.NO_VETO_IDENTIFIED -> error("Unreachable")
            }
        }
        if (gatesPassed.isNotEmpty()) {
            reasons += gatesPassed
            return result(ValuationClassification.POTENTIAL_UNDERVALUATION, gapPercent, absoluteGap)
        }

        return result(
            ValuationClassification.MODEL_SUPPORTED_UNDERVALUATION,
            gapPercent,
            absoluteGap,
            eligible = true
        )
    }
}
