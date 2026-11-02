package com.iie.android.domain.valuation

import java.math.BigInteger

/**
 * Snapshot of the verified UNIVesting read-only ABI at one canonical block.
 * All fields must come from eth_call responses cross-checked against that block.
 */
data class UniswapVestingScheduleSnapshot(
    val chainId: Long,
    val vestingContractAddress: String,
    val tokenContract: String,
    val recipientAddress: String,
    val quarterlyVestingAmountRaw: String,
    val lastUnlockTimestampSeconds: Long,
    val quartersPassed: Long,
    val blockNumber: Long,
    val blockHash: String,
    val rpcCrosschecked: Boolean,
    val observedAtEpochMs: Long
) {
    fun validationErrors(): List<String> = buildList {
        if (chainId != UniswapUniVestingProfile.CHAIN_ID) add("VESTING_CHAIN_ID_MISMATCH")
        if (!vestingContractAddress.equals(UniswapUniVestingProfile.VESTING_CONTRACT, true)) {
            add("VESTING_CONTRACT_MISMATCH")
        }
        if (!tokenContract.equals(UniswapUniVestingProfile.UNI_TOKEN, true)) add("VESTING_TOKEN_MISMATCH")
        if (!recipientAddress.matches(ADDRESS)) add("VESTING_RECIPIENT_INVALID")
        if (quarterlyVestingAmountRaw.toBigIntegerOrNull()?.signum() != 1) {
            add("VESTING_QUARTERLY_AMOUNT_INVALID")
        }
        if (lastUnlockTimestampSeconds <= 0L || quartersPassed < 0L) add("VESTING_SCHEDULE_VALUES_INVALID")
        if (blockNumber < 0L || !blockHash.matches(HASH) || observedAtEpochMs <= 0L) {
            add("VESTING_SNAPSHOT_BLOCK_REFERENCE_INVALID")
        }
        if (!rpcCrosschecked) add("VESTING_SNAPSHOT_RPC_CROSSCHECK_REQUIRED")
    }

    companion object {
        private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")
        private val HASH = Regex("0x[a-fA-F0-9]{64}")
    }
}

data class ReviewedVestingWithdrawalEvent(
    val chainId: Long,
    val vestingContractAddress: String,
    val tokenContract: String,
    val withdrawnEventTopic: String,
    val recipientTopicIndex: Int,
    val amountDataWordIndex: Int,
    val quartersPaidDataWordIndex: Int,
    val expectedTopicCount: Int,
    val expectedDataWordCount: Int,
    val evidenceUrl: String,
    val reviewStatus: String
) {
    fun validationErrors(): List<String> = buildList {
        if (chainId <= 0L) add("VESTING_EVENT_CHAIN_INVALID")
        if (!vestingContractAddress.matches(ADDRESS)) add("VESTING_EVENT_CONTRACT_INVALID")
        if (!tokenContract.matches(ADDRESS)) add("VESTING_EVENT_TOKEN_INVALID")
        if (!withdrawnEventTopic.matches(HASH)) add("VESTING_EVENT_TOPIC_INVALID")
        if (recipientTopicIndex !in 1..3) add("VESTING_RECIPIENT_TOPIC_INDEX_INVALID")
        if (amountDataWordIndex < 0 || quartersPaidDataWordIndex < 0) add("VESTING_EVENT_DATA_INDEX_INVALID")
        if (expectedTopicCount <= 0 || expectedDataWordCount <= 0) add("VESTING_EVENT_LAYOUT_INVALID")
        if (!evidenceUrl.startsWith("https://")) add("VESTING_EVENT_EVIDENCE_URL_INVALID")
        if (reviewStatus != REVIEWED) add("VESTING_EVENT_SCHEMA_NOT_REVIEWED")
    }

    companion object {
        const val REVIEWED = "REVIEWED"
        private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")
        private val HASH = Regex("0x[a-fA-F0-9]{64}")
    }
}

data class VestingWithdrawalCandidate(
    val protocolId: String,
    val chainId: Long,
    val vestingContractAddress: String,
    val recipientAddress: String,
    val tokenContract: String,
    val rawAmount: String,
    val quartersPaid: Long,
    val transactionHash: String,
    val blockNumber: Long,
    val blockHash: String,
    val blockTimestampEpochMs: Long,
    val withdrawalLogIndex: Long,
    val transferLogIndex: Long,
    val evidenceStatus: String = "RPC_CROSSCHECKED_VESTING_WITHDRAWAL_PENDING_FINALITY_AND_SCHEDULE_REVIEW"
)

sealed class VestingWithdrawalDecodeResult {
    data class Candidate(val evidence: VestingWithdrawalCandidate) : VestingWithdrawalDecodeResult()
    data class Abstained(val reasonCodes: List<String>) : VestingWithdrawalDecodeResult()
}

/** ABI-specific decoder for the verified Uniswap UNIVesting Withdrawn event. */
object UniswapVestingWithdrawalDecoder {
    private const val TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    private const val ZERO_ADDRESS = "0x0000000000000000000000000000000000000000"
    private val HASH = Regex("0x[a-fA-F0-9]{64}")

    fun decode(
        receipt: EvmTransactionReceiptEvidence,
        schedule: UniswapVestingScheduleSnapshot,
        schema: ReviewedVestingWithdrawalEvent
    ): VestingWithdrawalDecodeResult {
        val errors = mutableListOf<String>()
        errors += schedule.validationErrors()
        errors += schema.validationErrors()
        if (receipt.chainId != schema.chainId || receipt.chainId != schedule.chainId) {
            errors += "VESTING_RECEIPT_CHAIN_MISMATCH"
        }
        if (!schema.vestingContractAddress.equals(schedule.vestingContractAddress, true) ||
            !schema.tokenContract.equals(schedule.tokenContract, true)
        ) errors += "VESTING_SCHEMA_SNAPSHOT_MISMATCH"
        if (!receipt.succeeded) errors += "VESTING_WITHDRAWAL_RECEIPT_FAILED"
        if (!receipt.rpcCrosschecked) errors += "VESTING_WITHDRAWAL_RPC_CROSSCHECK_REQUIRED"
        if (!receipt.transactionHash.matches(HASH) || !receipt.blockHash.matches(HASH) ||
            receipt.blockNumber < 0L || receipt.blockTimestampEpochMs <= 0L
        ) errors += "VESTING_RECEIPT_BLOCK_REFERENCE_INVALID"
        if (receipt.blockNumber != schedule.blockNumber ||
            !receipt.blockHash.equals(schedule.blockHash, true)
        ) errors += "VESTING_SCHEDULE_AND_WITHDRAWAL_BLOCK_MISMATCH"

        val logs = receipt.logs.filter { log ->
            log.transactionHash.equals(receipt.transactionHash, true) &&
                log.blockNumber == receipt.blockNumber &&
                log.blockHash.equals(receipt.blockHash, true) &&
                log.logIndex >= 0L
        }
        if (logs.size != receipt.logs.size) errors += "VESTING_RECEIPT_LOG_BLOCK_MISMATCH"

        val withdrawalLogs = logs.filter { log ->
            log.address.equals(schema.vestingContractAddress, true) &&
                log.topics.firstOrNull()?.equals(schema.withdrawnEventTopic, true) == true
        }
        if (withdrawalLogs.size != 1) errors += "EXPECTED_EXACTLY_ONE_VESTING_WITHDRAWN_EVENT"
        val withdrawal = withdrawalLogs.singleOrNull()
        if (withdrawal != null && (
                withdrawal.topics.size != schema.expectedTopicCount ||
                    dataWordCount(withdrawal.data) != schema.expectedDataWordCount
            )
        ) errors += "VESTING_WITHDRAWN_EVENT_LAYOUT_MISMATCH"

        val recipient = withdrawal?.topics?.getOrNull(schema.recipientTopicIndex)?.let(::decodeTopicAddress)
        if (recipient == null || recipient.equals(ZERO_ADDRESS, true) ||
            !recipient.equals(schedule.recipientAddress, true)
        ) errors += "VESTING_WITHDRAWAL_RECIPIENT_MISMATCH"

        val amount = withdrawal?.let { decodeUint256(it.data, schema.amountDataWordIndex) }
        val quartersPaid = withdrawal?.let { decodeUint256(it.data, schema.quartersPaidDataWordIndex) }
        if (amount == null || amount.signum() <= 0) errors += "VESTING_WITHDRAWAL_AMOUNT_INVALID"
        if (quartersPaid == null || quartersPaid.signum() <= 0 ||
            (quartersPaid != null && quartersPaid.bitLength() > 63)
        ) errors += "VESTING_QUARTERS_PAID_INVALID"

        val transfers = logs.filter { log ->
            log.address.equals(schema.tokenContract, true) &&
                log.topics.size == 3 &&
                log.topics[0].equals(TRANSFER_TOPIC, true) &&
                topicAddress(log.topics[1])?.equals(schema.vestingContractAddress, true) == true
        }
        if (transfers.size != 1) errors += "EXPECTED_EXACTLY_ONE_VESTING_TOKEN_TRANSFER"
        val transfer = transfers.singleOrNull()
        val transferRecipient = transfer?.topics?.getOrNull(2)?.let(::decodeTopicAddress)
        val transferAmount = transfer?.let { decodeUint256(it.data, 0) }
        if (recipient == null || transferRecipient == null ||
            !recipient.equals(transferRecipient, true)
        ) errors += "VESTING_EVENT_RECIPIENT_MUST_MATCH_TOKEN_TRANSFER"
        if (amount == null || transferAmount == null || amount != transferAmount) {
            errors += "VESTING_EVENT_AMOUNT_MUST_MATCH_TOKEN_TRANSFER"
        }
        if (errors.isNotEmpty()) return VestingWithdrawalDecodeResult.Abstained(errors.distinct())

        return VestingWithdrawalDecodeResult.Candidate(
            VestingWithdrawalCandidate(
                protocolId = UniswapUniVestingProfile.PROTOCOL_ID,
                chainId = receipt.chainId,
                vestingContractAddress = schema.vestingContractAddress,
                recipientAddress = recipient!!,
                tokenContract = schema.tokenContract,
                rawAmount = amount!!.toString(),
                quartersPaid = quartersPaid!!.toLong(),
                transactionHash = receipt.transactionHash,
                blockNumber = receipt.blockNumber,
                blockHash = receipt.blockHash,
                blockTimestampEpochMs = receipt.blockTimestampEpochMs,
                withdrawalLogIndex = withdrawal!!.logIndex,
                transferLogIndex = transfer!!.logIndex
            )
        )
    }

    private fun decodeTopicAddress(topic: String): String? = topicAddress(topic)

    private fun topicAddress(topic: String): String? {
        val value = topic.removePrefix("0x")
        if (value.length != 64 || !value.matches(Regex("[a-fA-F0-9]{64}")) ||
            !value.substring(0, 24).all { it == '0' }
        ) return null
        return "0x" + value.takeLast(40)
    }

    private fun decodeUint256(data: String, wordIndex: Int): BigInteger? {
        val word = dataWord(data, wordIndex) ?: return null
        return word.toBigInteger(16)
    }

    private fun dataWord(data: String, wordIndex: Int): String? {
        val value = data.removePrefix("0x")
        if (value.isEmpty() || value.length % 64 != 0 ||
            !value.matches(Regex("[a-fA-F0-9]+"))
        ) return null
        val start = wordIndex * 64
        if (start < 0 || start + 64 > value.length) return null
        return value.substring(start, start + 64)
    }

    private fun dataWordCount(data: String): Int? {
        val value = data.removePrefix("0x")
        if (value.isEmpty() || value.length % 64 != 0 ||
            !value.matches(Regex("[a-fA-F0-9]+"))
        ) return null
        return value.length / 64
    }
}

object UniswapUniVestingProfile {
    const val PROTOCOL_ID = "protocol:uniswap"
    const val CHAIN_ID = 1L
    const val VESTING_CONTRACT = "0xCa046A83EDB78F74aE338bb5A291bF6FdAc9e1D2"
    const val UNI_TOKEN = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984"
    const val ABI_EVIDENCE_URL =
        "https://etherscan.io/address/0xca046a83edb78f74ae338bb5a291bf6fdac9e1d2#code"
    const val SOURCE_EVIDENCE_URL =
        "https://github.com/Uniswap/protocol-fees/blob/main/src/UNIVesting.sol"
}
