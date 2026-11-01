package com.iie.android.domain.quality

import com.iie.android.domain.model.ResearchMarketHistoryPoint
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchQuoteInput
import com.iie.android.domain.model.ResearchQuoteComparisonInput
import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.TokenomicsResearchSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssetResearchDossierBuilderTest {
    private val builder = AssetResearchDossierBuilder()
    private val asset = Asset("asset:btc", "bitcoin", "Bitcoin", "BTC", "NATIVE", "RESOLVED", 1L, 1L)

    @Test fun missingObservationsRemainUnknown() {
        val dossier = builder.build(asset, emptyList(), null, emptyList(), emptyList(), 100L)
        assertEquals(ResearchFieldStatus.UNKNOWN, dossier.market.quoteStatus)
        assertNull(dossier.market.priceUsd)
        assertNull(dossier.market.periodReturnPercent)
        assertEquals(0, dossier.market.historyPointCount)
        assertEquals(ResearchFieldStatus.UNKNOWN, dossier.fundamentals)
        assertEquals("INSUFFICIENT_EVIDENCE", dossier.overallEvidenceStatus)
    }

    @Test fun marketReturnsUseChronologicalPoints() {
        val points = listOf(point(3L, 120.0), point(1L, 100.0), point(2L, 110.0))
        val quote = ResearchQuoteInput(
            priceUsd = 121.0, provider = "CoinPaprika",
            providerUpdatedAtEpochMs = 3L, retrievedAtEpochMs = 4L, freshnessStatus = "FRESH"
        )
        val dossier = builder.build(asset, emptyList(), quote, points, emptyList(), 5L)
        assertEquals(3, dossier.market.historyPointCount)
        assertEquals(20.0, dossier.market.periodReturnPercent!!, 0.000001)
        assertEquals(9.090909, dossier.market.latestDailyReturnPercent!!, 0.00001)
        assertEquals(120.0, dossier.market.periodHighUsd!!, 0.0)
        assertEquals(100.0, dossier.market.periodLowUsd!!, 0.0)
        assertEquals(ResearchFieldStatus.KNOWN, dossier.market.quoteStatus)
    }

    @Test fun quoteDiscrepancyIsPersistableAndMarkedConflicted() {
        val primary = ResearchQuoteInput(100.0, "CoinPaprika", 1_000L, 1_100L, "FRESH")
        val secondary = ResearchQuoteInput(104.0, "CoinMarketCap", 1_000L, 1_100L, "FRESH")
        val dossier = builder.build(asset, emptyList(), primary, emptyList(), emptyList(), 2_000L,
            com.iie.android.domain.model.ResearchQuoteComparisonInput(primary, secondary, "obs-cp", "obs-cmc"))
        assertEquals("CONFLICTED", dossier.market.quoteComparisonStatus)
        assertEquals(3.9215686, dossier.market.quoteDifferencePercent!!, 0.00001)
        assertEquals("obs-cp", dossier.market.quoteComparisonPrimaryObservationId)
        assertEquals("obs-cmc", dossier.market.quoteComparisonSecondaryObservationId)
        assertEquals(2_000L, dossier.market.quoteComparedAtEpochMs)
        assertEquals("CONFLICTED", dossier.overallEvidenceStatus)
    }

    @Test fun quoteComparisonRejectsStaleOrUnsynchronizedObservations() {
        val primary = ResearchQuoteInput(100.0, "CoinPaprika", 1_000L, 1_100L, "FRESH")
        val stale = ResearchQuoteInput(100.1, "CoinMarketCap", 1_000L, 1_100L, "STALE")
        val dossier = builder.build(asset, emptyList(), primary, emptyList(), emptyList(), 2_000L,
            com.iie.android.domain.model.ResearchQuoteComparisonInput(primary, stale, "obs-cp", "obs-cmc"))
        assertEquals("NOT_COMPARABLE", dossier.market.quoteComparisonStatus)
        assertNull(dossier.market.quoteDifferencePercent)
    }

    @Test fun unresolvedIdentityPreventsSufficientEvidence() {
        val dossier = builder.build(
            asset.copy(identityStatus = "UNVERIFIED"), emptyList(),
            ResearchQuoteInput(priceUsd = 10.0, provider = null, providerUpdatedAtEpochMs = null, retrievedAtEpochMs = null, freshnessStatus = "UNKNOWN"), emptyList(), emptyList(), 10L
        )
        assertEquals(ResearchFieldStatus.UNKNOWN, dossier.identityAssessment)
        assertEquals("INSUFFICIENT_EVIDENCE", dossier.overallEvidenceStatus)
    }

    @Test fun tokenomicsEvidenceIsNotMarkedCompleteWhenFieldsAreMissing() {
        val unknown = ResearchEvidence<Double>(
            fieldKey = "circulating_supply", value = null,
            status = ResearchFieldStatus.UNKNOWN, sourceId = null, sourceName = null,
            sourceUrl = null, licenseReference = null, permittedUseStatus = "UNKNOWN",
            observedAtEpochMs = null, retrievedAtEpochMs = null, qualityStatus = "NOT_PROVIDED"
        )
        val textUnknown = com.iie.android.domain.model.ResearchEvidence<String>(
            fieldKey = "emission_schedule", value = null,
            status = ResearchFieldStatus.UNKNOWN, sourceId = null, sourceName = null,
            sourceUrl = null, licenseReference = null, permittedUseStatus = "UNKNOWN",
            observedAtEpochMs = null, retrievedAtEpochMs = null, qualityStatus = "NOT_PROVIDED"
        )
        val section = TokenomicsResearchSection(
            circulatingSupply = unknown,
            totalSupply = unknown.copy(fieldKey = "total_supply"),
            maxSupply = unknown.copy(fieldKey = "max_supply"),
            marketCapUsd = unknown.copy(fieldKey = "market_cap_usd"),
            fullyDilutedMarketCapUsd = unknown.copy(fieldKey = "fully_diluted_market_cap_usd"),
            annualInflationPercent = unknown.copy(fieldKey = "annual_inflation_percent"),
            emissionSchedule = textUnknown,
            unlockEvents = emptyList()
        )
        val dossier = builder.build(
            asset, emptyList(), null, emptyList(), emptyList(), 100L,
            tokenomicsEvidence = section
        )
        assertEquals(ResearchFieldStatus.UNKNOWN, dossier.tokenomics)
        assertEquals(ResearchFieldStatus.UNKNOWN, dossier.tokenomicsEvidence?.circulatingSupply?.status)
    }

    private fun point(timestamp: Long, price: Double) = ResearchMarketHistoryPoint(
        timestampEpochMs = timestamp, priceUsd = price
    )
}
