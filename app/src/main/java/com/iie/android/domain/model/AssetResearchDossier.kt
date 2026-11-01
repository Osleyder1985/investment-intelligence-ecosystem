package com.iie.android.domain.model

/** Evidence-bounded research snapshot; deliberately contains no score or recommendation. */
data class AssetResearchDossier(
    val assetId: String,
    val assetName: String,
    val symbol: String,
    val identityStatus: String,
    val identityAssessment: ResearchFieldStatus,
    val deployments: List<ResearchDeployment>,
    val market: MarketResearchSection,
    val fundamentals: ResearchFieldStatus = ResearchFieldStatus.UNKNOWN,
    val tokenomics: ResearchFieldStatus = ResearchFieldStatus.UNKNOWN,
    val tokenomicsEvidence: TokenomicsResearchSection? = null,
    val security: ResearchFieldStatus = ResearchFieldStatus.NOT_ASSESSED,
    val governance: ResearchFieldStatus = ResearchFieldStatus.NOT_ASSESSED,
    val evidenceCount: Int,
    val generatedAtEpochMs: Long
) {
    val overallEvidenceStatus: String
        get() = when {
            identityAssessment == ResearchFieldStatus.CONFLICTED || market.quoteComparisonStatus == "CONFLICTED" -> "CONFLICTED"
            identityAssessment != ResearchFieldStatus.KNOWN -> "INSUFFICIENT_EVIDENCE"
            market.quoteStatus != ResearchFieldStatus.KNOWN -> "INSUFFICIENT_EVIDENCE"
            else -> "PARTIAL_COVERAGE"
        }
}

enum class ResearchFieldStatus { KNOWN, UNKNOWN, NOT_ASSESSED, CONFLICTED }

data class ResearchDeployment(
    val networkName: String,
    val representationKind: String,
    val namespace: String,
    val reference: String?,
    val decimals: Int?
)

data class MarketResearchSection(
    val quoteStatus: ResearchFieldStatus,
    val priceUsd: Double?,
    val provider: String?,
    val providerUpdatedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long?,
    val freshnessStatus: String,
    val historyPointCount: Int,
    val latestDailyReturnPercent: Double?,
    val periodReturnPercent: Double?,
    val periodHighUsd: Double?,
    val periodLowUsd: Double?,
    val quoteComparisonStatus: String = "NOT_AVAILABLE",
    val quoteDifferencePercent: Double? = null,
    val quoteDifferenceThresholdPercent: Double? = null,
    val quoteComparisonPrimaryObservationId: String? = null,
    val quoteComparisonSecondaryObservationId: String? = null,
    val quoteComparedAtEpochMs: Long? = null
)

/** Presentation-independent quote facts passed into the domain dossier builder. */
data class ResearchQuoteInput(
    val priceUsd: Double?,
    val provider: String?,
    val providerUpdatedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long?,
    val freshnessStatus: String
)

/** Daily market point facts decoupled from provider adapter types. */
data class ResearchMarketHistoryPoint(val timestampEpochMs: Long, val priceUsd: Double)


/** Two independently persisted provider observations used for an auditable quote comparison. */
data class ResearchQuoteComparisonInput(
    val primaryQuote: ResearchQuoteInput?,
    val secondaryQuote: ResearchQuoteInput?,
    val primaryObservationId: String?,
    val secondaryObservationId: String?,
    val thresholdPercent: Double = 2.0,
    val maxTimestampSkewMs: Long = 5 * 60 * 1000L
)
