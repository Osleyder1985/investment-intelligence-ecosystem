package com.iie.android.domain.valuation

import java.math.BigInteger

data class BatchDistributionPayment(
    val beneficiaryAddress: String,
    val rawAmount: String,
    val transferLogIndex: Long
)

data class VerifiedBatchDistributionCandidate(
    val protocolId: String,
    val assetId: String,
    val chainId: Long,
    val distributorAddress: String,
    val tokenContract: String,
    val transactionHash: String,
    val blockNumber: Long,
    val blockHash: String,
    val blockTimestampEpochMs: Long,
    val payments: List<BatchDistributionPayment>,
    val evidenceStatus: String = "RPC_CROSSCHECKED_BATCH_TRANSFER_CANDIDATE_PENDING_FINALITY_AND_PROTOCOL_REVIEW"
)

sealed class BatchDistributionDecodeResult {
    data class Candidate(val evidence: VerifiedBatchDistributionCandidate) : BatchDistributionDecodeResult()
    data class Abstained(val reasonCodes: List<String>) : BatchDistributionDecodeResult()
}

/**
 * Groups multiple exact ERC-20 transfers from one reviewed distributor in a
 * single receipt. This is a flow candidate, not proof that a batch ABI method
 * was called or that the recipients were economically entitled to payment.
 */
object ReviewedBatchDistributionReceiptDecoder {
    private const val TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    private const val ZERO_ADDRESS = "0x0000000000000000000000000000000000000000"
    private val HASH = Regex("0x[a-fA-F0-9]{64}")
    private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")

    fun decode(
        receipt: EvmTransactionReceiptEvidence,
        sourceMapping: ProtocolEconomicAddressMapping,
        minimumRecipients: Int = 2
    ): BatchDistributionDecodeResult {
        val errors = mutableListOf<String>()
        errors += sourceMapping.validationErrors()
        if (sourceMapping.role != ProtocolEconomicAddressRole.DISTRIBUTION_SOURCE) {
            errors += "ROLE_IS_NOT_DISTRIBUTION_SOURCE"
        }
        if (minimumRecipients < 2) errors += "BATCH_MINIMUM_RECIPIENTS_MUST_BE_AT_LEAST_TWO"
        if (receipt.chainId != sourceMapping.chainId) errors += "BATCH_CHAIN_ID_MISMATCH"
        if (!receipt.succeeded) errors += "BATCH_RECEIPT_FAILED"
        if (!receipt.rpcCrosschecked) errors += "BATCH_RPC_CROSSCHECK_REQUIRED"
        if (!receipt.transactionHash.matches(HASH) || !receipt.blockHash.matches(HASH) ||
            receipt.blockNumber < 0L || receipt.blockTimestampEpochMs <= 0L
        ) errors += "BATCH_RECEIPT_BLOCK_REFERENCE_INVALID"
        if (receipt.blockTimestampEpochMs < sourceMapping.validFromEpochMs ||
            (sourceMapping.validUntilEpochMs != null &&
                receipt.blockTimestampEpochMs > sourceMapping.validUntilEpochMs)
        ) errors += "BATCH_DISTRIBUTOR_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"

        val logs = receipt.logs.filter { log ->
            log.transactionHash.equals(receipt.transactionHash, true) &&
                log.blockNumber == receipt.blockNumber &&
                log.blockHash.equals(receipt.blockHash, true) &&
                log.logIndex >= 0L
        }
        if (logs.size != receipt.logs.size) errors += "BATCH_RECEIPT_LOG_BLOCK_MISMATCH"

        val transfers = logs.filter { log ->
            log.address.equals(sourceMapping.tokenContract, true) &&
                log.topics.size == 3 &&
                log.topics[0].equals(TRANSFER_TOPIC, true) &&
                topicAddress(log.topics[1])?.equals(sourceMapping.address, true) == true
        }
        val payments = transfers.mapNotNull { log ->
            val beneficiary = topicAddress(log.topics[2]) ?: return@mapNotNull null
            val amount = decodeUint256(log.data) ?: return@mapNotNull null
            if (beneficiary.equals(ZERO_ADDRESS, true) || amount.signum() <= 0) return@mapNotNull null
            BatchDistributionPayment(beneficiary, amount.toString(), log.logIndex)
        }
        if (payments.size != transfers.size) errors += "BATCH_TRANSFER_LOG_MALFORMED"
        if (payments.size < minimumRecipients) errors += "BATCH_RECIPIENT_COUNT_BELOW_THRESHOLD"
        if (payments.map { it.beneficiaryAddress.lowercase() }.distinct().size != payments.size) {
            errors += "BATCH_BENEFICIARIES_MUST_BE_UNIQUE"
        }
        if (payments.map { it.transferLogIndex }.distinct().size != payments.size) {
            errors += "BATCH_TRANSFER_LOG_INDEX_DUPLICATE"
        }
        if (errors.isNotEmpty()) return BatchDistributionDecodeResult.Abstained(errors.distinct())

        return BatchDistributionDecodeResult.Candidate(
            VerifiedBatchDistributionCandidate(
                protocolId = sourceMapping.protocolId,
                assetId = sourceMapping.assetId,
                chainId = receipt.chainId,
                distributorAddress = sourceMapping.address,
                tokenContract = sourceMapping.tokenContract,
                transactionHash = receipt.transactionHash,
                blockNumber = receipt.blockNumber,
                blockHash = receipt.blockHash,
                blockTimestampEpochMs = receipt.blockTimestampEpochMs,
                payments = payments.sortedBy { it.transferLogIndex }
            )
        )
    }

    private fun topicAddress(topic: String): String? {
        val value = topic.removePrefix("0x")
        if (value.length != 64 || !value.matches(Regex("[a-fA-F0-9]{64}")) ||
            !value.substring(0, 24).all { it == '0' }
        ) return null
        return "0x" + value.takeLast(40)
    }

    private fun decodeUint256(data: String): BigInteger? {
        val value = data.removePrefix("0x")
        if (value.length != 64 || !value.matches(Regex("[a-fA-F0-9]{64}"))) return null
        return value.toBigInteger(16)
    }
}
