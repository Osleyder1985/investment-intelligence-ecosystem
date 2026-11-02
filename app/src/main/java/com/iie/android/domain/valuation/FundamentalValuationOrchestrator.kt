package com.iie.android.domain.valuation

/** Shared comparability and decision metadata for one independently sourced model run. */
data class ValuationRunMetadata(
    val assetId: String,
    val valuationObject: String,
    val valuationUnit: String,
    val currency: String,
    val valuationTimestampEpochMs: Long,
    val evidenceStatus: ValuationEvidenceStatus,
    val priceReference: ValuationPriceReference = ValuationPriceReference.LAST_TRADE,
    val maximumTimestampSkewMs: Long = 0L,
    val liquidityAndDepthReviewed: Boolean = false,
    val executionCostsIncluded: Boolean = false,
    val sensitivityRobust: Boolean = false,
    val alternativeMethodsReviewed: Boolean = false,
    val riskGate: ValuationRiskGate = ValuationRiskGate.VETO_NOT_ASSESSED
)

sealed class ValuationModelRequest {
    abstract val metadata: ValuationRunMetadata

    data class Nav(val input: NavModelInput, override val metadata: ValuationRunMetadata) : ValuationModelRequest()
    data class Dcf(val input: DcfModelInput, override val metadata: ValuationRunMetadata) : ValuationModelRequest()
    data class Comparable(val input: ComparableModelInput, override val metadata: ValuationRunMetadata) : ValuationModelRequest()
    data class MonetaryScenario(val input: MonetaryScenarioInput, override val metadata: ValuationRunMetadata) : ValuationModelRequest()
    data class Redemption(val input: RedemptionModelInput, override val metadata: ValuationRunMetadata) : ValuationModelRequest()
}

data class ValuationModelRun(
    val method: ValuationMethod,
    val result: ValuationModelResult
)

data class ConsolidatedFairValue(
    val lowerPerUnit: Double,
    val upperPerUnit: Double,
    val rangeKind: FairValueRangeKind,
    val methodId: String,
    val evidenceReferences: List<String>,
    val assumptions: List<String>,
    val asOfEpochMs: Long
)

sealed class ValuationOrchestratorResult {
    data class Classified(
        val runs: List<ValuationModelRun>,
        val consolidated: ConsolidatedFairValue,
        val valuation: FundamentalValuationResult
    ) : ValuationOrchestratorResult()

    data class Abstained(
        val runs: List<ValuationModelRun>,
        val blockers: List<String>
    ) : ValuationOrchestratorResult()
}

/**
 * Executes only selector-approved methods. Multiple estimates are consolidated only
 * when they are independent, same-scope, same-currency, same-time model-uncertainty
 * ranges. No mean, probability, confidence score or "extreme cheapness" is fabricated.
 */
object FundamentalValuationOrchestrator {
    fun evaluate(
        context: AssetValuationResearchContext,
        requests: Map<ValuationMethod, ValuationModelRequest>,
        riskMetadata: ValuationRunMetadata? = null
    ): ValuationOrchestratorResult {
        val blockers = context.blockers.toMutableList()
        if (!context.assetIdentityVerified) blockers += "ASSET_IDENTITY_NOT_VERIFIED"
        if (context.methodPlan.assetId != context.assetId) blockers += "METHOD_PLAN_ASSET_MISMATCH"
        if (context.methodPlan.evidenceConflicted) blockers += "ECONOMIC_RIGHTS_EVIDENCE_CONFLICTED"
        if (!context.marketEvidenceUsable || context.marketPriceUsd == null ||
            context.marketObservedAtEpochMs == null
        ) blockers += "FRESH_COMPARABLE_MARKET_PRICE_NOT_AVAILABLE"
        if (blockers.isNotEmpty()) return ValuationOrchestratorResult.Abstained(emptyList(), blockers.distinct())

        val eligible = context.methodPlan.applicableMethods
        val runs = eligible.mapNotNull { method ->
            val request = requests[method] ?: return@mapNotNull null
            if (request.metadata.assetId != context.assetId) return@mapNotNull ValuationModelRun(
                method, ValuationModelResult.Abstained("MODEL_REQUEST_ASSET_MISMATCH")
            )
            ValuationModelRun(method, execute(method, request))
        }
        val estimates = runs.mapNotNull { (it.result as? ValuationModelResult.Estimated)?.estimate }
        val abstentions = runs.mapNotNull { (it.result as? ValuationModelResult.Abstained)?.reason }
        if (estimates.isEmpty()) {
            return ValuationOrchestratorResult.Abstained(
                runs, (abstentions + "NO_ELIGIBLE_MODEL_PRODUCED_A_VALID_ESTIMATE").distinct()
            )
        }

        val metadataByMethod = eligible.mapNotNull { method ->
            requests[method]?.let { method to it.metadata }
        }.toMap()
        val estimateMethods = runs.filter { it.result is ValuationModelResult.Estimated }.map { it.method }
        val runMetadata = estimateMethods.mapNotNull { metadataByMethod[it] }
        if (runMetadata.size != estimates.size) {
            return ValuationOrchestratorResult.Abstained(runs, listOf("MODEL_METADATA_MISSING"))
        }
        val consolidated = consolidate(estimates, runMetadata)
            ?: return ValuationOrchestratorResult.Abstained(
                runs, listOf("MODEL_ESTIMATES_NOT_COMPARABLE_OR_NOT_INDEPENDENT")
            )

        val metadata = runMetadata.first()
        if (metadata.valuationTimestampEpochMs <= 0L ||
            metadata.valuationTimestampEpochMs != consolidated.asOfEpochMs
        ) {
            return ValuationOrchestratorResult.Abstained(runs, listOf("VALUATION_TIMESTAMP_NOT_ALIGNED_WITH_EVIDENCE"))
        }
        if (context.marketObservedAtEpochMs <= 0L) {
            return ValuationOrchestratorResult.Abstained(runs, listOf("MARKET_TIMESTAMP_INVALID"))
        }
        val decisionMetadata = riskMetadata ?: metadata
        val allEligibleMethodsProducedEstimates = eligible.isNotEmpty() && eligible.all { method ->
            runs.any { it.method == method && it.result is ValuationModelResult.Estimated }
        }
        val valuation = FundamentalValuationEngine.evaluate(
            FundamentalValuationInput(
                assetId = context.assetId,
                valuationObject = metadata.valuationObject,
                valuationUnit = metadata.valuationUnit,
                currency = metadata.currency,
                methodId = consolidated.methodId,
                applicability = ValuationApplicability.APPLICABLE,
                conditionalRequirementsSatisfied = true,
                evidenceStatus = metadata.evidenceStatus,
                identityResolved = context.assetIdentityVerified,
                fairValueLower = consolidated.lowerPerUnit,
                fairValueUpper = consolidated.upperPerUnit,
                rangeKind = consolidated.rangeKind,
                marketPrice = context.marketPriceUsd,
                priceReference = decisionMetadata.priceReference,
                valuationTimestampEpochMs = consolidated.asOfEpochMs,
                marketTimestampEpochMs = context.marketObservedAtEpochMs,
                maximumTimestampSkewMs = decisionMetadata.maximumTimestampSkewMs,
                sameEconomicObjectAndUnit = true,
                liquidityAndDepthReviewed = decisionMetadata.liquidityAndDepthReviewed,
                executionCostsIncluded = decisionMetadata.executionCostsIncluded,
                sensitivityRobust = decisionMetadata.sensitivityRobust,
                alternativeMethodsReviewed = decisionMetadata.alternativeMethodsReviewed &&
                    estimates.size > 1 && allEligibleMethodsProducedEstimates,
                riskGate = decisionMetadata.riskGate
            )
        )
        return ValuationOrchestratorResult.Classified(runs, consolidated, valuation)
    }

    private fun execute(method: ValuationMethod, request: ValuationModelRequest): ValuationModelResult =
        when {
            method == ValuationMethod.NAV && request is ValuationModelRequest.Nav ->
                FundamentalValuationModels.nav(request.input)
            method == ValuationMethod.HOLDER_ATTRIBUTABLE_DCF && request is ValuationModelRequest.Dcf ->
                FundamentalValuationModels.dcf(request.input)
            method == ValuationMethod.TOKEN_CAPTURE_COMPARABLES && request is ValuationModelRequest.Comparable ->
                FundamentalValuationModels.comparable(request.input)
            method == ValuationMethod.MONETARY_DEMAND_SCENARIO && request is ValuationModelRequest.MonetaryScenario ->
                FundamentalValuationModels.monetaryScenario(request.input)
            method == ValuationMethod.NET_REDEMPTION && request is ValuationModelRequest.Redemption ->
                FundamentalValuationModels.redemption(request.input)
            else -> ValuationModelResult.Abstained("MODEL_REQUEST_TYPE_DOES_NOT_MATCH_ELIGIBLE_METHOD")
        }

    private fun consolidate(
        estimates: List<FairValueEstimate>,
        metadata: List<ValuationRunMetadata>
    ): ConsolidatedFairValue? {
        if (estimates.isEmpty() || estimates.size != metadata.size) return null
        val first = metadata.first()
        if (metadata.any {
                it.assetId != first.assetId ||
                    it.valuationObject != first.valuationObject ||
                    it.valuationUnit != first.valuationUnit ||
                    it.currency != first.currency ||
                    it.valuationTimestampEpochMs != first.valuationTimestampEpochMs
            }
        ) return null
        if (estimates.any { it.asOfEpochMs != first.valuationTimestampEpochMs }) return null
        if (estimates.size > 1) {
            if (metadata.any { it.evidenceStatus != ValuationEvidenceStatus.SUFFICIENT }) return null
            if (estimates.any { it.rangeKind != FairValueRangeKind.MODEL_UNCERTAINTY }) return null
            val evidenceSets = estimates.map { it.evidenceReferences.toSet() }
            for (i in evidenceSets.indices) for (j in i + 1 until evidenceSets.size) {
                if (evidenceSets[i].intersect(evidenceSets[j]).isNotEmpty()) return null
            }
        }
        val methods = estimates.map { it.methodId }
        if (methods.distinct().size != methods.size) return null
        return ConsolidatedFairValue(
            lowerPerUnit = estimates.minOf { it.lowerPerUnit },
            upperPerUnit = estimates.maxOf { it.upperPerUnit },
            rangeKind = if (estimates.size == 1) estimates.single().rangeKind else FairValueRangeKind.MODEL_CONSENSUS,
            methodId = methods.joinToString("+"),
            evidenceReferences = estimates.flatMap { it.evidenceReferences }.distinct(),
            assumptions = estimates.flatMap { it.assumptions }.distinct() +
                if (estimates.size > 1) listOf("Conservative envelope across independent comparable model ranges; not an average or probability-weighted estimate.") else emptyList(),
            asOfEpochMs = first.valuationTimestampEpochMs
        )
    }
}
