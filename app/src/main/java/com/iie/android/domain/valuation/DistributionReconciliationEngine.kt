package com.iie.android.domain.valuation

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * Pure reconciliation of protocol-published unlock schedules and independently observed
 * on-chain movements. Amounts remain raw token units; callers must not mix token decimals.
 */
data class ScheduledTokenUnlock(
    val scheduleId: String,
    val beneficiary: String,
    val unlockAtEpochMs: Long,
    val rawAmount: String,
    val evidenceId: String,
    val verified: Boolean
)

enum class DistributionMovementKind {
    UNLOCK_CONFIRMED,
    CLAIMED,
    TRANSFERRED,
    CLAIMED_AND_TRANSFERRED
}

/**
 * TRANSFERRED means a transfer from the vesting/distributor contract to the beneficiary.
 * It is not evidence that the beneficiary sold the tokens on a market.
 */
data class ObservedDistributionMovement(
    val movementId: String,
    val beneficiary: String,
    val observedAtEpochMs: Long,
    val rawAmount: String,
    val kind: DistributionMovementKind,
    val evidenceId: String,
    val transactionHash: String,
    val verified: Boolean
)

data class SellThroughAssumption(
    val lowBasisPoints: Int,
    val highBasisPoints: Int,
    val rationale: String,
    val evidenceId: String
)

data class DistributionReconciliationInput(
    val windowStartEpochMs: Long,
    val windowEndEpochMs: Long,
    val circulatingSupplyRaw: String,
    val schedules: List<ScheduledTokenUnlock>,
    val movements: List<ObservedDistributionMovement>,
    val sellThrough: SellThroughAssumption,
    val topBeneficiaryLimit: Int = 5
)

data class BeneficiaryDistributionExposure(
    val beneficiary: String,
    val rawTransferredAmount: String,
    val shareOfObservedTransfersPercent: BigDecimal
)

data class DistributionReconciliationReport(
    val windowStartEpochMs: Long,
    val windowEndEpochMs: Long,
    val scheduledUnlockRaw: String,
    val observedUnlockedRaw: String,
    val claimedRaw: String,
    val transferredToBeneficiariesRaw: String,
    /** observed claimed + transferred minus scheduled unlocks in the same window */
    val scheduleVsObservedClaimDeltaRaw: String,
    val transferredAsCirculatingSupplyPercent: BigDecimal,
    val topBeneficiaries: List<BeneficiaryDistributionExposure>,
    val topBeneficiariesSharePercent: BigDecimal,
    val estimatedSellPressureLowRaw: String,
    val estimatedSellPressureHighRaw: String,
    val estimatedSellPressureLowOfCirculatingPercent: BigDecimal,
    val estimatedSellPressureHighOfCirculatingPercent: BigDecimal,
    val sellThroughAssumption: SellThroughAssumption,
    val caveats: List<String>,
    val evidenceIds: List<String>
)

sealed class DistributionReconciliationResult {
    data class Ready(val report: DistributionReconciliationReport) : DistributionReconciliationResult()
    data class Abstained(val blockers: List<String>) : DistributionReconciliationResult()
}

object DistributionReconciliationEngine {
    private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")
    private val HASH = Regex("0x[a-fA-F0-9]{64}")
    private val ID = Regex("[A-Za-z0-9:_./-]{1,256}")
    private val ZERO = BigInteger.ZERO
    private val HUNDRED = BigDecimal("100")
    private const val BASIS_POINTS = 10_000L

    fun reconcile(input: DistributionReconciliationInput): DistributionReconciliationResult {
        val blockers = validate(input)
        if (blockers.isNotEmpty()) return DistributionReconciliationResult.Abstained(blockers)

        val start = input.windowStartEpochMs
        val end = input.windowEndEpochMs
        val schedules = input.schedules.filter { it.unlockAtEpochMs in start..end }
        val movements = input.movements.filter { it.observedAtEpochMs in start..end }

        val scheduled = schedules.fold(ZERO) { total, item -> total + item.rawAmount.toBigInteger() }
        val unlocked = sum(movements, DistributionMovementKind.UNLOCK_CONFIRMED)
        val claimed = sum(movements, DistributionMovementKind.CLAIMED)
        val transferred = movements.filter {
            it.kind == DistributionMovementKind.TRANSFERRED ||
                it.kind == DistributionMovementKind.CLAIMED_AND_TRANSFERRED
        }
        val transferredTotal = transferred.fold(ZERO) { total, item -> total + item.rawAmount.toBigInteger() }
        val observedClaimedAndTransferred = claimed + transferredTotal
        val delta = observedClaimedAndTransferred - scheduled
        val supply = input.circulatingSupplyRaw.toBigInteger()

        val byBeneficiary = transferred.groupBy { it.beneficiary.lowercase() }
            .mapValues { (_, entries) ->
                entries.fold(ZERO) { total, item -> total + item.rawAmount.toBigInteger() }
            }
        val top = byBeneficiary.entries.sortedWith(
            compareByDescending<Map.Entry<String, BigInteger>> { it.value }.thenBy { it.key }
        ).take(input.topBeneficiaryLimit)
        val topTotal = top.fold(ZERO) { total, entry -> total + entry.value }
        val totalShare = percent(topTotal, transferredTotal)
        val exposures = top.map { (beneficiary, amount) ->
            BeneficiaryDistributionExposure(
                beneficiary = beneficiary,
                rawTransferredAmount = amount.toString(),
                shareOfObservedTransfersPercent = percent(amount, transferredTotal)
            )
        }

        val lowPressure = transferredTotal.multiply(
            BigInteger.valueOf(input.sellThrough.lowBasisPoints.toLong())
        ).divide(BigInteger.valueOf(BASIS_POINTS))
        val highPressure = transferredTotal.multiply(
            BigInteger.valueOf(input.sellThrough.highBasisPoints.toLong())
        ).divide(BigInteger.valueOf(BASIS_POINTS))

        return DistributionReconciliationResult.Ready(
            DistributionReconciliationReport(
                windowStartEpochMs = start,
                windowEndEpochMs = end,
                scheduledUnlockRaw = scheduled.toString(),
                observedUnlockedRaw = unlocked.toString(),
                claimedRaw = claimed.toString(),
                transferredToBeneficiariesRaw = transferredTotal.toString(),
                scheduleVsObservedClaimDeltaRaw = delta.toString(),
                transferredAsCirculatingSupplyPercent = percent(transferredTotal, supply),
                topBeneficiaries = exposures,
                topBeneficiariesSharePercent = totalShare,
                estimatedSellPressureLowRaw = lowPressure.toString(),
                estimatedSellPressureHighRaw = highPressure.toString(),
                estimatedSellPressureLowOfCirculatingPercent = percent(lowPressure, supply),
                estimatedSellPressureHighOfCirculatingPercent = percent(highPressure, supply),
                sellThroughAssumption = input.sellThrough,
                caveats = listOf(
                    "A scheduled unlock is not proof that tokens became claimable on-chain.",
                    "An observed unlock is not a claim; a claim is not necessarily a transfer to a beneficiary.",
                    "A transfer to a beneficiary is not evidence of a market sale.",
                    "Sell-pressure values are scenario estimates driven only by the explicit sell-through assumption.",
                    "Recipient concentration describes observed transfer destinations, not beneficial ownership."
                ),
                evidenceIds = (
                    schedules.map { it.evidenceId } +
                        movements.map { it.evidenceId } +
                        listOf(input.sellThrough.evidenceId)
                    ).distinct().sorted()
            )
        )
    }

    private fun validate(input: DistributionReconciliationInput): List<String> = buildList {
        if (input.windowStartEpochMs <= 0L || input.windowEndEpochMs < input.windowStartEpochMs) {
            add("RECONCILIATION_WINDOW_INVALID")
        }
        val supply = input.circulatingSupplyRaw.toBigIntegerOrNull()
        if (supply == null || supply.signum() <= 0) add("CIRCULATING_SUPPLY_RAW_INVALID")
        if (input.schedules.isEmpty() && input.movements.isEmpty()) add("DISTRIBUTION_EVIDENCE_EMPTY")
        if (input.topBeneficiaryLimit !in 1..100) add("TOP_BENEFICIARY_LIMIT_INVALID")
        if (input.sellThrough.lowBasisPoints !in 0..10_000 ||
            input.sellThrough.highBasisPoints !in input.sellThrough.lowBasisPoints..10_000
        ) add("SELL_THROUGH_RANGE_INVALID")
        if (input.sellThrough.rationale.isBlank() || !input.sellThrough.evidenceId.matches(ID)) {
            add("SELL_THROUGH_ASSUMPTION_PROVENANCE_MISSING")
        }
        if (input.schedules.map { it.scheduleId }.distinct().size != input.schedules.size) {
            add("DUPLICATE_SCHEDULE_IDENTITIES")
        }
        input.schedules.forEach { item ->
            if (!item.scheduleId.matches(ID) || !item.evidenceId.matches(ID) ||
                !item.beneficiary.matches(ADDRESS) || item.unlockAtEpochMs <= 0L ||
                item.rawAmount.toBigIntegerOrNull()?.signum() != 1 || !item.verified
            ) add("SCHEDULE_ENTRY_INVALID_OR_UNVERIFIED")
        }
        if (input.movements.map { it.movementId }.distinct().size != input.movements.size) {
            add("DUPLICATE_MOVEMENT_IDENTITIES")
        }
        input.movements.forEach { item ->
            if (!item.movementId.matches(ID) || !item.evidenceId.matches(ID) ||
                !item.transactionHash.matches(HASH) || !item.beneficiary.matches(ADDRESS) ||
                item.observedAtEpochMs <= 0L || item.rawAmount.toBigIntegerOrNull()?.signum() != 1 ||
                !item.verified
            ) add("MOVEMENT_INVALID_OR_UNVERIFIED")
        }
    }.distinct()

    private fun sum(
        movements: List<ObservedDistributionMovement>,
        kind: DistributionMovementKind
    ): BigInteger = movements.filter { it.kind == kind }
        .fold(ZERO) { total, item -> total + item.rawAmount.toBigInteger() }

    private fun percent(numerator: BigInteger, denominator: BigInteger): BigDecimal {
        if (denominator.signum() <= 0) return BigDecimal.ZERO
        return BigDecimal(numerator).multiply(HUNDRED)
            .divide(BigDecimal(denominator), 8, RoundingMode.HALF_UP)
            .stripTrailingZeros()
    }
}
