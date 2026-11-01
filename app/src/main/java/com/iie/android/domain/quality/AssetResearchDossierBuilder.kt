package com.iie.android.domain.quality

import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.domain.model.MarketResearchSection
import com.iie.android.domain.model.ResearchDeployment
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchQuoteInput
import com.iie.android.domain.model.ResearchMarketHistoryPoint
import com.iie.android.domain.model.ResearchQuoteComparisonInput

/** Builds a transparent dossier from provider-validated observations; never infers investment merit. */
class AssetResearchDossierBuilder {
    fun build(
        asset: Asset,
        deployments: List<AssetDeploymentSummary>,
        quote: ResearchQuoteInput?,
        history: List<ResearchMarketHistoryPoint>,
        provenance: List<AssetObservationProvenance>,
        generatedAtEpochMs: Long,
        quoteComparison: ResearchQuoteComparisonInput? = null
    ): AssetResearchDossier {
        require(generatedAtEpochMs > 0L)
        val validQuote = quote?.priceUsd?.takeIf { it.isFinite() && it > 0.0 }
        val orderedHistory = history
            .filter { it.priceUsd.isFinite() && it.priceUsd > 0.0 && it.timestampEpochMs > 0L }
            .distinctBy { it.timestampEpochMs }
            .sortedBy { it.timestampEpochMs }
        val first = orderedHistory.firstOrNull()
        val last = orderedHistory.lastOrNull()
        val dailyReturn = if (orderedHistory.size >= 2) {
            val previous = orderedHistory[orderedHistory.lastIndex - 1].priceUsd
            if (previous > 0.0) ((last!!.priceUsd / previous) - 1.0) * 100.0 else null
        } else null
        val periodReturn = if (first != null && last != null && first.priceUsd > 0.0) {
            ((last.priceUsd / first.priceUsd) - 1.0) * 100.0
        } else null

        val comparison = evaluateQuoteComparison(quoteComparison, generatedAtEpochMs)

        return AssetResearchDossier(
            assetId = asset.assetId,
            assetName = asset.name,
            symbol = asset.symbol,
            identityStatus = asset.identityStatus,
            identityAssessment = when (asset.identityStatus.uppercase()) {
                "RESOLVED" -> ResearchFieldStatus.KNOWN
                "RESOLVED_WITH_LIMITATIONS" -> ResearchFieldStatus.UNKNOWN
                "CONFLICTED" -> ResearchFieldStatus.CONFLICTED
                else -> ResearchFieldStatus.UNKNOWN
            },
            deployments = deployments.map {
                ResearchDeployment(it.networkName, it.representationKind, it.namespace, it.reference, it.decimals)
            },
            market = MarketResearchSection(
                quoteStatus = if (validQuote != null) ResearchFieldStatus.KNOWN else ResearchFieldStatus.UNKNOWN,
                priceUsd = validQuote,
                provider = quote?.provider,
                providerUpdatedAtEpochMs = quote?.providerUpdatedAtEpochMs,
                retrievedAtEpochMs = quote?.retrievedAtEpochMs,
                freshnessStatus = quote?.freshnessStatus ?: "UNKNOWN",
                historyPointCount = orderedHistory.size,
                latestDailyReturnPercent = dailyReturn,
                periodReturnPercent = periodReturn,
                periodHighUsd = orderedHistory.maxOfOrNull { it.priceUsd },
                periodLowUsd = orderedHistory.minOfOrNull { it.priceUsd },
                quoteComparisonStatus = comparison.status,
                quoteDifferencePercent = comparison.differencePercent,
                quoteDifferenceThresholdPercent = quoteComparison?.thresholdPercent,
                quoteComparisonPrimaryObservationId = quoteComparison?.primaryObservationId,
                quoteComparisonSecondaryObservationId = quoteComparison?.secondaryObservationId,
                quoteComparedAtEpochMs = comparison.comparedAtEpochMs
            ),
            evidenceCount = provenance.size,
            generatedAtEpochMs = generatedAtEpochMs
        )
    }
    
    private data class QuoteComparisonResult(
        val status: String,
        val differencePercent: Double?,
        val comparedAtEpochMs: Long?
    )

    private fun evaluateQuoteComparison(
        input: ResearchQuoteComparisonInput?,
        generatedAtEpochMs: Long
    ): QuoteComparisonResult {
        if (input == null) return QuoteComparisonResult("NOT_AVAILABLE", null, null)
        require(input.thresholdPercent.isFinite() && input.thresholdPercent > 0.0)
        require(input.maxTimestampSkewMs >= 0L)
        val first = input.primaryQuote
        val second = input.secondaryQuote
        val firstPrice = first?.priceUsd?.takeIf { it.isFinite() && it > 0.0 }
        val secondPrice = second?.priceUsd?.takeIf { it.isFinite() && it > 0.0 }
        val firstTime = first?.providerUpdatedAtEpochMs
        val secondTime = second?.providerUpdatedAtEpochMs
        if (firstPrice == null || secondPrice == null || firstTime == null || secondTime == null ||
            firstTime <= 0L || secondTime <= 0L
        ) return QuoteComparisonResult("NOT_COMPARABLE", null, null)
        if (first.freshnessStatus != "FRESH" || second.freshnessStatus != "FRESH" ||
            kotlin.math.abs(firstTime - secondTime) > input.maxTimestampSkewMs
        ) return QuoteComparisonResult("NOT_COMPARABLE", null, null)
        val mean = (firstPrice + secondPrice) / 2.0
        val difference = kotlin.math.abs(firstPrice - secondPrice) / mean * 100.0
        if (!difference.isFinite()) return QuoteComparisonResult("NOT_COMPARABLE", null, null)
        return QuoteComparisonResult(
            status = if (difference > input.thresholdPercent) "CONFLICTED" else "CONSISTENT",
            differencePercent = difference,
            comparedAtEpochMs = generatedAtEpochMs
        )
    }

}
