package com.iie.android.domain.quality

import com.iie.android.domain.model.ResearchMarketHistoryPoint
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchQuoteInput
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

    @Test fun unresolvedIdentityPreventsSufficientEvidence() {
        val dossier = builder.build(
            asset.copy(identityStatus = "UNVERIFIED"), emptyList(),
            ResearchQuoteInput(priceUsd = 10.0, provider = null, providerUpdatedAtEpochMs = null, retrievedAtEpochMs = null, freshnessStatus = "UNKNOWN"), emptyList(), emptyList(), 10L
        )
        assertEquals(ResearchFieldStatus.UNKNOWN, dossier.identityAssessment)
        assertEquals("INSUFFICIENT_EVIDENCE", dossier.overallEvidenceStatus)
    }

    private fun point(timestamp: Long, price: Double) = ResearchMarketHistoryPoint(
        timestampEpochMs = timestamp, priceUsd = price
    )
}
