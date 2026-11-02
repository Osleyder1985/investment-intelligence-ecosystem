package com.iie.android.domain.valuation

import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchLicenseStatus
import com.iie.android.domain.model.TokenomicsResearchSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenomicsEmissionEvidenceAdapterTest {
    private val now = 50_000L
    private fun <T> ev(
        key: String, value: T?, source: String, observed: Long = 45_000L,
        status: ResearchFieldStatus = ResearchFieldStatus.KNOWN
    ) = ResearchEvidence(
        fieldKey = key, value = value, status = status, sourceId = source,
        sourceName = "Issuer or analytics source", sourceUrl = "https://example.org/$source",
        licenseReference = "license-$source", permittedUseStatus = "APPROVED_FOR_INTENDED_USE",
        observedAtEpochMs = observed, retrievedAtEpochMs = now, qualityStatus = "VALIDATED"
    )

    private fun section(
        inflation: ResearchEvidence<Double> = ev("annual_inflation", 4.5, "inflation"),
        schedule: ResearchEvidence<String> = ev("emission_schedule", "Scheduled block issuance", "schedule"),
        unlocks: List<ResearchEvidence<String>> = listOf(ev("unlock_event", "Team allocation disclosure", "unlock"))
    ) = TokenomicsResearchSection(
        circulatingSupply = ev("circulating_supply", 60.0, "circ"),
        totalSupply = ev("total_supply", 100.0, "total"),
        maxSupply = ev("max_supply", null, "max", status = ResearchFieldStatus.UNKNOWN),
        marketCapUsd = ev("market_cap", 600.0, "mcap"),
        fullyDilutedMarketCapUsd = ev("fdv", 1_000.0, "fdv"),
        annualInflationPercent = inflation,
        emissionSchedule = schedule,
        unlockEvents = unlocks
    )

    private val licenses = mapOf(
        "inflation" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "schedule" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE,
        "unlock" to ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE
    )

    @Test fun acceptsReportedInflationScheduleAndQualitativeUnlocks() {
        val result = TokenomicsEmissionEvidenceAdapter.build(section(), licenses, now)
            as TokenomicsEmissionEvidenceResult.Ready

        assertEquals(4.5, result.evidence.annualInflationPercent, 0.0)
        assertEquals("Scheduled block issuance", result.evidence.emissionScheduleDisclosure)
        assertEquals(listOf("Team allocation disclosure"), result.evidence.unlockDisclosures)
        assertTrue(result.evidence.limitations.any { it.contains("no unlock amount") })
    }

    @Test fun missingOrUnlicensedRequiredEmissionEvidenceAbstains() {
        val missing = TokenomicsEmissionEvidenceAdapter.build(
            section(schedule = ev("emission_schedule", null, "schedule", status = ResearchFieldStatus.UNKNOWN)),
            licenses, now
        ) as TokenomicsEmissionEvidenceResult.Abstained
        assertTrue(missing.blockers.isNotEmpty())

        val unlicensed = TokenomicsEmissionEvidenceAdapter.build(
            section(), licenses - "inflation", now
        ) as TokenomicsEmissionEvidenceResult.Abstained
        assertTrue(unlicensed.blockers.any { it.contains("LICENSE_REQUIRES_REVIEW") })
    }

    @Test fun rejectsInvalidInflationAndTimeInconsistentEvidence() {
        val invalid = TokenomicsEmissionEvidenceAdapter.build(
            section(inflation = ev("annual_inflation", Double.NaN, "inflation")),
            licenses, now
        ) as TokenomicsEmissionEvidenceResult.Abstained
        assertTrue(invalid.blockers.contains("ANNUAL_INFLATION_VALUE_INVALID") ||
            invalid.blockers.any { it.contains("NUMERIC_VALUE_NOT_FINITE") })

        val stale = TokenomicsEmissionEvidenceAdapter.build(
            section(schedule = ev("emission_schedule", "Schedule", "schedule", observed = 1_000L)),
            licenses, now, maximumObservationSkewMs = 100L
        ) as TokenomicsEmissionEvidenceResult.Abstained
        assertTrue(stale.blockers.contains("EMISSION_EVIDENCE_NOT_TIME_COMPARABLE"))
    }

    @Test fun unknownOptionalUnlockEventsDoNotBecomeNumericDilution() {
        val result = TokenomicsEmissionEvidenceAdapter.build(
            section(unlocks = listOf(
                ev("unlock_event", null, "unlock", status = ResearchFieldStatus.UNKNOWN)
            )), licenses, now
        ) as TokenomicsEmissionEvidenceResult.Ready

        assertTrue(result.evidence.unlockDisclosures.isEmpty())
        assertTrue(result.evidence.limitations.any { it.contains("quantitative dilution forecast", ignoreCase = true) })
    }
}
