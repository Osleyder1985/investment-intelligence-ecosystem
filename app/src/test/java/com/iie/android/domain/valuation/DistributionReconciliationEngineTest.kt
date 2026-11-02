package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistributionReconciliationEngineTest {
    private val beneficiaryA = "0x1111111111111111111111111111111111111111"
    private val beneficiaryB = "0x2222222222222222222222222222222222222222"
    private val tx = "0x" + "a".repeat(64)
    private val windowStart = 1_000L
    private val windowEnd = 9_000L

    private fun schedule(id: String, beneficiary: String, amount: String, at: Long = 2_000L) =
        ScheduledTokenUnlock(id, beneficiary, at, amount, "schedule:$id", true)

    private fun movement(
        id: String,
        beneficiary: String,
        amount: String,
        kind: DistributionMovementKind,
        at: Long = 3_000L
    ) = ObservedDistributionMovement(id, beneficiary, at, amount, kind, "movement:$id", tx, true)

    private fun input(
        schedules: List<ScheduledTokenUnlock> = listOf(schedule("s1", beneficiaryA, "1000")),
        movements: List<ObservedDistributionMovement> = listOf(
            movement("m1", beneficiaryA, "600", DistributionMovementKind.CLAIMED_AND_TRANSFERRED),
            movement("m2", beneficiaryB, "400", DistributionMovementKind.CLAIMED_AND_TRANSFERRED)
        ),
        sellThrough: SellThroughAssumption = SellThroughAssumption(
            1_000, 5_000, "Illustrative scenario only", "assumption:sell-through"
        )
    ) = DistributionReconciliationInput(
        windowStart, windowEnd, "10000", schedules, movements, sellThrough
    )

    @Test fun reconcilesScheduleClaimsTransfersAndScenarioPressureSeparately() {
        val report = (DistributionReconciliationEngine.reconcile(input()) as
            DistributionReconciliationResult.Ready).report

        assertEquals("1000", report.scheduledUnlockRaw)
        assertEquals("0", report.observedUnlockedRaw)
        assertEquals("0", report.claimedRaw)
        assertEquals("1000", report.transferredToBeneficiariesRaw)
        assertEquals("0", report.scheduleVsObservedClaimDeltaRaw)
        assertEquals("10", report.transferredAsCirculatingSupplyPercent.toPlainString())
        assertEquals("100", report.estimatedSellPressureLowRaw)
        assertEquals("500", report.estimatedSellPressureHighRaw)
        assertEquals("1", report.estimatedSellPressureLowOfCirculatingPercent.toPlainString())
        assertEquals("5", report.estimatedSellPressureHighOfCirculatingPercent.toPlainString())
    }

    @Test fun calculatesRecipientConcentrationWithoutClaimingBeneficialOwnership() {
        val report = (DistributionReconciliationEngine.reconcile(input()) as
            DistributionReconciliationResult.Ready).report

        assertEquals(2, report.topBeneficiaries.size)
        assertEquals("60", report.topBeneficiaries[0].shareOfObservedTransfersPercent.toPlainString())
        assertEquals("100", report.topBeneficiariesSharePercent.toPlainString())
        assertTrue(report.caveats.any { it.contains("beneficial ownership") })
    }

    @Test fun keepsUnlockAndClaimStatesDistinctFromTransfers() {
        val report = (DistributionReconciliationEngine.reconcile(input(
            movements = listOf(
                movement("m1", beneficiaryA, "200", DistributionMovementKind.UNLOCK_CONFIRMED),
                movement("m2", beneficiaryA, "300", DistributionMovementKind.CLAIMED)
            )
        )) as DistributionReconciliationResult.Ready).report

        assertEquals("200", report.observedUnlockedRaw)
        assertEquals("300", report.claimedRaw)
        assertEquals("0", report.transferredToBeneficiariesRaw)
        assertEquals("0", report.estimatedSellPressureHighRaw)
    }

    @Test fun ignoresEvidenceOutsideTheSelectedWindow() {
        val report = (DistributionReconciliationEngine.reconcile(input(
            schedules = listOf(schedule("s1", beneficiaryA, "1000", at = 10_000L))
        )) as DistributionReconciliationResult.Ready).report

        assertEquals("0", report.scheduledUnlockRaw)
        assertEquals("1000", report.transferredToBeneficiariesRaw)
    }

    @Test fun rejectsUnverifiedAndDuplicateEvidence() {
        val unverified = input(schedules = listOf(schedule("s1", beneficiaryA, "1000").copy(verified = false)))
        val result = DistributionReconciliationEngine.reconcile(unverified)
        assertTrue((result as DistributionReconciliationResult.Abstained)
            .blockers.contains("SCHEDULE_ENTRY_INVALID_OR_UNVERIFIED"))

        val duplicate = input(movements = listOf(
            movement("m1", beneficiaryA, "100", DistributionMovementKind.TRANSFERRED),
            movement("m1", beneficiaryB, "100", DistributionMovementKind.TRANSFERRED)
        ))
        assertTrue((DistributionReconciliationEngine.reconcile(duplicate) as
            DistributionReconciliationResult.Abstained).blockers.contains("DUPLICATE_MOVEMENT_IDENTITIES"))
    }

    @Test fun rejectsInvalidSellThroughRangeAndMissingProvenance() {
        val invalid = input(sellThrough = SellThroughAssumption(
            8_000, 2_000, "bad range", "assumption:bad"
        ))
        val result = DistributionReconciliationEngine.reconcile(invalid)
        assertTrue((result as DistributionReconciliationResult.Abstained)
            .blockers.contains("SELL_THROUGH_RANGE_INVALID"))
    }

    @Test fun refusesEmptyEvidenceAndInvalidSupply() {
        val result = DistributionReconciliationEngine.reconcile(input(
            schedules = emptyList(), movements = emptyList()
        ))
        assertTrue((result as DistributionReconciliationResult.Abstained)
            .blockers.contains("DISTRIBUTION_EVIDENCE_EMPTY"))

        val badSupply = input().copy(circulatingSupplyRaw = "0")
        assertTrue((DistributionReconciliationEngine.reconcile(badSupply) as
            DistributionReconciliationResult.Abstained).blockers.contains("CIRCULATING_SUPPLY_RAW_INVALID"))
    }
}
