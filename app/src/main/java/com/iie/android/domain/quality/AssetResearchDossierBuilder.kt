package com.iie.android.domain.quality

import com.iie.android.data.market.CoinPaprikaHistoricalPoint
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.domain.model.MarketResearchSection
import com.iie.android.domain.model.ResearchDeployment
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.presentation.MarketQuoteUiState

/** Builds a transparent dossier from provider-validated observations; never infers investment merit. */
class AssetResearchDossierBuilder {
    fun build(
        asset: Asset,
        deployments: List<AssetDeploymentSummary>,
        quote: MarketQuoteUiState?,
        history: List<CoinPaprikaHistoricalPoint>,
        provenance: List<AssetObservationProvenance>,
        generatedAtEpochMs: Long
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
                provider = quote?.attribution,
                providerUpdatedAtEpochMs = quote?.providerUpdatedAtEpochMs,
                retrievedAtEpochMs = quote?.retrievedAtEpochMs,
                freshnessStatus = quote?.freshnessStatus ?: "UNKNOWN",
                historyPointCount = orderedHistory.size,
                latestDailyReturnPercent = dailyReturn,
                periodReturnPercent = periodReturn,
                periodHighUsd = orderedHistory.maxOfOrNull { it.priceUsd },
                periodLowUsd = orderedHistory.minOfOrNull { it.priceUsd }
            ),
            evidenceCount = provenance.size,
            generatedAtEpochMs = generatedAtEpochMs
        )
    }
}
