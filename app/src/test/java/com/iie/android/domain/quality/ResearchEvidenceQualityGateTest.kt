package com.iie.android.domain.quality

import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchEvidenceQualityGate
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchLicenseStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResearchEvidenceQualityGateTest {
    private fun evidence(
        value: Double? = 100.0,
        retrievedAt: Long? = 900L,
        observedAt: Long? = 800L,
        status: ResearchFieldStatus = ResearchFieldStatus.KNOWN
    ) = ResearchEvidence(
        fieldKey = "circulating_supply",
        value = value,
        status = status,
        sourceId = "source:provider",
        sourceName = "Provider",
        sourceUrl = "https://provider.example/data",
        licenseReference = "https://provider.example/terms",
        permittedUseStatus = "ATTRIBUTION_REQUIRED",
        observedAtEpochMs = observedAt,
        retrievedAtEpochMs = retrievedAt,
        qualityStatus = "SCHEMA_VALIDATED"
    )

    @Test fun completeAttributedEvidencePasses() {
        val result = ResearchEvidenceQualityGate.evaluate(
            evidence(), 1_000L, ResearchLicenseStatus.ATTRIBUTION_REQUIRED
        )
        assertTrue(result.accepted)
        assertEquals(ResearchFieldStatus.KNOWN, result.status)
        assertTrue(result.reasons.isEmpty())
    }

    @Test
    fun explicitPersonalUsePermissionIsAcceptedWithAttributionLicenseDecision() {
        val result = ResearchEvidenceQualityGate.evaluate(
            evidence().copy(permittedUseStatus = "PERSONAL_USE_ONLY"),
            1_000L,
            ResearchLicenseStatus.ATTRIBUTION_REQUIRED
        )
        assertTrue(result.accepted)
        assertEquals(ResearchFieldStatus.KNOWN, result.status)
    }

    @Test fun missingValueNeverBecomesKnown() {
        val result = ResearchEvidenceQualityGate.evaluate(
            evidence(value = null, status = ResearchFieldStatus.UNKNOWN),
            1_000L,
            ResearchLicenseStatus.ATTRIBUTION_REQUIRED
        )
        assertFalse(result.accepted)
        assertEquals(ResearchFieldStatus.UNKNOWN, result.status)
        assertTrue(result.reasons.contains("VALUE_NOT_KNOWN"))
    }

    @Test fun conflictedEvidenceRemainsConflictedAfterRejection() {
        val result = ResearchEvidenceQualityGate.evaluate(
            evidence(status = ResearchFieldStatus.CONFLICTED),
            1_000L,
            ResearchLicenseStatus.ATTRIBUTION_REQUIRED
        )
        assertFalse(result.accepted)
        assertEquals(ResearchFieldStatus.CONFLICTED, result.status)
    }

    @Test fun nonFiniteNumericEvidenceIsRejected() {
        val result = ResearchEvidenceQualityGate.evaluate(
            evidence(value = Double.NaN),
            1_000L,
            ResearchLicenseStatus.ATTRIBUTION_REQUIRED
        )
        assertFalse(result.accepted)
        assertTrue(result.reasons.contains("NUMERIC_VALUE_NOT_FINITE"))
    }

    @Test fun unknownPermittedUseStatusIsRejected() {
        val unclassified = evidence().copy(permittedUseStatus = "SOME_UNKNOWN_STATUS")
        val result = ResearchEvidenceQualityGate.evaluate(
            unclassified, 1_000L, ResearchLicenseStatus.ATTRIBUTION_REQUIRED
        )
        assertFalse(result.accepted)
        assertTrue(result.reasons.contains("PERMITTED_USE_STATUS_UNRECOGNIZED"))
    }

    @Test fun futureRetrievalTimestampIsRejected() {
        val result = ResearchEvidenceQualityGate.evaluate(
            evidence(retrievedAt = 1_001L), 1_000L,
            ResearchLicenseStatus.APPROVED_FOR_INTENDED_USE
        )
        assertFalse(result.accepted)
        assertTrue(result.reasons.contains("RETRIEVAL_TIMESTAMP_INVALID"))
    }

    @Test fun prohibitedLicenseIsRejected() {
        val result = ResearchEvidenceQualityGate.evaluate(
            evidence(), 1_000L, ResearchLicenseStatus.PROHIBITED
        )
        assertFalse(result.accepted)
        assertTrue(result.reasons.contains("LICENSE_PROHIBITS_USE"))
    }

    @Test fun supplyContradictionsAreReportedRatherThanSilentlyCorrected() {
        val reasons = ResearchEvidenceQualityGate.validateSupplyRelations(
            circulatingSupply = 120.0, totalSupply = 100.0, maxSupply = 90.0
        )
        assertEquals(
            listOf("CIRCULATING_EXCEEDS_TOTAL", "TOTAL_EXCEEDS_MAX"),
            reasons
        )
    }

    @Test fun unknownSupplyIsNotTreatedAsZero() {
        assertTrue(
            ResearchEvidenceQualityGate.validateSupplyRelations(
                circulatingSupply = null, totalSupply = 100.0, maxSupply = null
            ).isEmpty()
        )
    }
}
