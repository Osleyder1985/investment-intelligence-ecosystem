package com.iie.android.domain.valuation

import com.iie.android.domain.model.FundamentalResearchSection
import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchLicenseStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolEconomicEvidenceAdapterTest {
    private val now = 100_000L
    private fun <T> evidence(key: String, value: T?, source: String, observed: Long = 90_000L) =
        ResearchEvidence(
            fieldKey = key, value = value, status = ResearchFieldStatus.KNOWN,
            sourceId = source, sourceName = "Verified data source",
            sourceUrl = "https://example.org/$source", licenseReference = "license-$source",
            permittedUseStatus = "APPROVED_FOR_INTENDED_USE",
            observedAtEpochMs = observed, retrievedAtEpochMs = now, qualityStatus = "VALIDATED"
        )

    private val licenses = mapOf(
        "revenue" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "fees" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "holders" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "buybacks" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "burns" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "rights" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE
    )

    @Test fun keepsProtocolMetricsSemanticallySeparate() {
        val section = FundamentalResearchSection(
            metrics = listOf(
                evidence("protocol_revenue_usd", 1_000.0, "revenue"),
                evidence("protocol_fees_usd", 300.0, "fees"),
                evidence("holder_distributions_usd", 100.0, "holders"),
                evidence("token_buyback_spend_usd", 50.0, "buybacks"),
                evidence("tokens_burned", 25.0, "burns")
            )
        )
        val result = ProtocolEconomicEvidenceAdapter.build("asset-1", section, licenses, now)
            as ProtocolEconomicEvidenceResult.Ready

        assertEquals(5, result.snapshot.metrics.size)
        assertEquals(1_000.0, result.snapshot.metrics[ProtocolEconomicMetric.PROTOCOL_REVENUE_USD]!!.value, 0.0)
        assertEquals(100.0, result.snapshot.metrics[ProtocolEconomicMetric.HOLDER_DISTRIBUTIONS_USD]!!.value, 0.0)
        assertFalse(result.snapshot.limitations.isEmpty())
        assertTrue(result.snapshot.limitations.any { it.contains("not automatically token-holder cash flows") })
    }

    @Test fun missingMetricsAbstainInsteadOfInventingProtocolRevenue() {
        val result = ProtocolEconomicEvidenceAdapter.build(
            "asset-1", FundamentalResearchSection(metrics = emptyList()), licenses, now
        ) as ProtocolEconomicEvidenceResult.Abstained
        assertTrue(result.blockers.contains("NO_VERIFIABLE_PROTOCOL_ECONOMIC_METRICS"))
    }

    @Test fun rejectsUnlicensedOrTimeInconsistentMetrics() {
        val unlicensed = ProtocolEconomicEvidenceAdapter.build(
            "asset-1",
            FundamentalResearchSection(metrics = listOf(evidence("protocol_revenue_usd", 100.0, "revenue"))),
            licenses - "revenue", now
        ) as ProtocolEconomicEvidenceResult.Abstained
        assertTrue(unlicensed.blockers.any { it.contains("LICENSE_REQUIRES_REVIEW") })

        val skewed = ProtocolEconomicEvidenceAdapter.build(
            "asset-1",
            FundamentalResearchSection(metrics = listOf(
                evidence("protocol_revenue_usd", 100.0, "revenue", 1_000L),
                evidence("protocol_fees_usd", 20.0, "fees", 90_000L)
            )),
            licenses, now, maximumObservationSkewMs = 500L
        ) as ProtocolEconomicEvidenceResult.Abstained
        assertTrue(skewed.blockers.contains("PROTOCOL_ECONOMIC_METRICS_NOT_TIME_COMPARABLE"))
    }

    @Test fun qualitativeRightsDisclosureDoesNotBecomeVerifiedHolderClaim() {
        val section = FundamentalResearchSection(
            metrics = listOf(evidence("protocol_revenue_usd", 100.0, "revenue")),
            qualitativeFacts = listOf(evidence("holder_cashflow_right", "No direct distribution", "rights"))
        )
        val result = ProtocolEconomicEvidenceAdapter.build("asset-1", section, licenses, now)
            as ProtocolEconomicEvidenceResult.Ready

        assertEquals("No direct distribution", result.snapshot.qualitativeRightsDisclosures["holder_cashflow_right"])
        // This adapter only returns disclosures; selector eligibility remains a separate review.
        assertTrue(result.snapshot.metrics.containsKey(ProtocolEconomicMetric.PROTOCOL_REVENUE_USD))
    }
}
