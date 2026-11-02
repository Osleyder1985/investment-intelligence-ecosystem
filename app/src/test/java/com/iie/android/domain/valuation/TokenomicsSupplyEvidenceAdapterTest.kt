package com.iie.android.domain.valuation

import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchLicenseStatus
import com.iie.android.domain.model.TokenomicsResearchSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenomicsSupplyEvidenceAdapterTest {
    private val now = 10_000L

    private fun evidence(
        key: String,
        value: Double?,
        source: String,
        observedAt: Long = 9_000L,
        status: ResearchFieldStatus = ResearchFieldStatus.KNOWN,
        permitted: String = "APPROVED_FOR_INTENDED_USE"
    ) = ResearchEvidence(
        fieldKey = key,
        value = value,
        status = status,
        sourceId = source,
        sourceName = "Research provider",
        sourceUrl = "https://example.org/$source",
        licenseReference = "license-$source",
        permittedUseStatus = permitted,
        observedAtEpochMs = observedAt,
        retrievedAtEpochMs = now,
        qualityStatus = "VALIDATED"
    )

    private fun section(
        circulating: ResearchEvidence<Double> = evidence("circulating_supply", 60.0, "source-a"),
        total: ResearchEvidence<Double> = evidence("total_supply", 100.0, "source-b"),
        max: ResearchEvidence<Double> = evidence("max_supply", 120.0, "source-c")
    ) = TokenomicsResearchSection(
        circulatingSupply = circulating,
        totalSupply = total,
        maxSupply = max,
        marketCapUsd = evidence("market_cap", 600.0, "market"),
        fullyDilutedMarketCapUsd = evidence("fdv", 1_000.0, "fdv"),
        annualInflationPercent = evidence("inflation", 2.0, "inflation"),
        emissionSchedule = ResearchEvidence(
            fieldKey = "emission_schedule", value = null, status = ResearchFieldStatus.UNKNOWN,
            sourceId = null, sourceName = null, sourceUrl = null, licenseReference = null,
            permittedUseStatus = "UNKNOWN", observedAtEpochMs = null, retrievedAtEpochMs = null,
            qualityStatus = "NOT_ASSESSED"
        ),
        unlockEvents = emptyList()
    )

    private val licenses = mapOf(
        "source-a" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "source-b" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "source-c" to ResearchLicenseStatus.ATTRIBUTION_REQUIRED
    )

    @Test fun returnsOnlyVerifiedSupplyFactsAndTheirProvenance() {
        val result = TokenomicsSupplyEvidenceAdapter.build(section(), licenses, now)
            as TokenomicsSupplyEvidenceResult.Ready

        assertEquals(60.0, result.supply.circulatingSupply, 0.0)
        assertEquals(100.0, result.supply.totalSupply, 0.0)
        assertEquals(120.0, result.supply.maxSupply!!, 0.0)
        assertEquals(9_000L, result.supply.evidence.asOfEpochMs)
        assertEquals(3, result.supply.evidence.ids.size)
        assertTrue(result.supply.notes.any { it.contains("not proof of effective") })
    }

    @Test fun missingLicenseOrProhibitedUseAbstains() {
        val missingLicense = TokenomicsSupplyEvidenceAdapter.build(
            section(), licenses - "source-b", now
        ) as TokenomicsSupplyEvidenceResult.Abstained
        assertTrue(missingLicense.blockers.any { it.contains("LICENSE_REQUIRES_REVIEW") })

        val prohibited = TokenomicsSupplyEvidenceAdapter.build(
            section(total = evidence(
                "total_supply", 100.0, "source-b", permitted = "PROHIBITED"
            )), licenses, now
        ) as TokenomicsSupplyEvidenceResult.Abstained
        assertTrue(prohibited.blockers.any { it.contains("LICENSE_PROHIBITS_USE") })
    }

    @Test fun invalidSupplyRelationsAreRejected() {
        val result = TokenomicsSupplyEvidenceAdapter.build(
            section(
                circulating = evidence("circulating_supply", 110.0, "source-a"),
                total = evidence("total_supply", 100.0, "source-b")
            ), licenses, now
        ) as TokenomicsSupplyEvidenceResult.Abstained

        assertTrue(result.blockers.contains("CIRCULATING_EXCEEDS_TOTAL"))
    }

    @Test fun observationsOutsideTimeWindowAreNotCombined() {
        val result = TokenomicsSupplyEvidenceAdapter.build(
            section(total = evidence("total_supply", 100.0, "source-b", observedAt = 1_000L)),
            licenses,
            now,
            maximumObservationSkewMs = 500L
        ) as TokenomicsSupplyEvidenceResult.Abstained

        assertTrue(result.blockers.contains("SUPPLY_OBSERVATIONS_NOT_TIME_COMPARABLE"))
    }

    @Test fun unknownOptionalMaxSupplyDoesNotBlockKnownCirculatingAndTotalSupply() {
        val unknownMax = evidence(
            "max_supply", null, "source-c", status = ResearchFieldStatus.UNKNOWN
        )
        val result = TokenomicsSupplyEvidenceAdapter.build(
            section(max = unknownMax), licenses, now
        ) as TokenomicsSupplyEvidenceResult.Ready

        assertEquals(null, result.supply.maxSupply)
        assertEquals(2, result.supply.evidence.ids.size)
    }
}
