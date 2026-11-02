package com.iie.android.domain.valuation

/**
 * Normalized swap evidence emitted by a chain/protocol-specific receipt decoder.
 * The decoder must derive both legs from the same successful transaction receipt.
 */
data class DecodedSwapExecutionEvidence(
    val chainId: Long,
    val transactionHash: String,
    val blockNumber: Long,
    val blockHash: String,
    val receiptSucceeded: Boolean,
    val rpcCrosschecked: Boolean,
    val executorAddress: String,
    val inputTokenContract: String,
    val inputRawAmount: String,
    val outputTokenContract: String,
    val outputRawAmount: String,
    val outputRecipient: String,
    val decodedAtEpochMs: Long,
    val blockTimestampEpochMs: Long = decodedAtEpochMs
)

data class ConfirmedBuybackExecutionCandidate(
    val protocolId: String,
    val assetId: String,
    val transactionHash: String,
    val blockNumber: Long,
    val executorAddress: String,
    val inputTokenContract: String,
    val inputRawAmount: String,
    val acquiredTokenContract: String,
    val acquiredRawAmount: String,
    val tokenRecipient: String,
    val blockTimestampEpochMs: Long,
    val evidenceStatus: String = "RPC_CROSSCHECKED_SWAP_PENDING_FINALITY_AND_PROTOCOL_REVIEW"
)

object ProtocolBuybackEvidenceEvaluator {
    fun evaluate(
        evidence: DecodedSwapExecutionEvidence,
        mapping: ProtocolEconomicAddressMapping
    ): Result {
        val errors = buildList {
            if (mapping.validationErrors().isNotEmpty()) addAll(mapping.validationErrors())
            if (mapping.role != ProtocolEconomicAddressRole.BUYBACK_EXECUTOR) add("ROLE_IS_NOT_BUYBACK_EXECUTOR")
            if (evidence.chainId != mapping.chainId) add("CHAIN_ID_MISMATCH")
            if (!evidence.executorAddress.equals(mapping.address, ignoreCase = true)) add("EXECUTOR_ADDRESS_MISMATCH")
            if (!evidence.outputTokenContract.equals(mapping.tokenContract, ignoreCase = true)) add("ACQUIRED_TOKEN_CONTRACT_MISMATCH")
            if (!evidence.receiptSucceeded) add("TRANSACTION_RECEIPT_FAILED")
            if (!evidence.rpcCrosschecked) add("RPC_CROSSCHECK_REQUIRED")
            if (!evidence.transactionHash.matches(Regex("0x[a-fA-F0-9]{64}"))) add("TRANSACTION_HASH_INVALID")
            if (evidence.blockNumber < 0L || !evidence.blockHash.matches(Regex("0x[a-fA-F0-9]{64}"))) add("BLOCK_REFERENCE_INVALID")
            if (!evidence.inputTokenContract.matches(Regex("0x[a-fA-F0-9]{40}")) ||
                evidence.inputTokenContract.equals(evidence.outputTokenContract, ignoreCase = true)) add("SWAP_INPUT_TOKEN_INVALID")
            if (!evidence.outputTokenContract.matches(Regex("0x[a-fA-F0-9]{40}"))) add("SWAP_OUTPUT_TOKEN_INVALID")
            if (evidence.inputRawAmount.toBigIntegerOrNull()?.signum() != 1) add("SWAP_INPUT_AMOUNT_INVALID")
            if (evidence.outputRawAmount.toBigIntegerOrNull()?.signum() != 1) add("SWAP_OUTPUT_AMOUNT_INVALID")
            if (!evidence.outputRecipient.matches(Regex("0x[a-fA-F0-9]{40}"))) add("SWAP_OUTPUT_RECIPIENT_INVALID")
            if (evidence.decodedAtEpochMs <= 0L) add("DECODE_TIMESTAMP_INVALID")
            if (evidence.blockTimestampEpochMs <= 0L ||
                evidence.blockTimestampEpochMs < mapping.validFromEpochMs ||
                (mapping.validUntilEpochMs != null && evidence.blockTimestampEpochMs > mapping.validUntilEpochMs)
            ) add("BUYBACK_EXECUTOR_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME")
        }
        if (errors.isNotEmpty()) return Result.Abstained(errors.distinct())
        return Result.Candidate(
            ConfirmedBuybackExecutionCandidate(
                protocolId = mapping.protocolId,
                assetId = mapping.assetId,
                transactionHash = evidence.transactionHash,
                blockNumber = evidence.blockNumber,
                executorAddress = evidence.executorAddress,
                inputTokenContract = evidence.inputTokenContract,
                inputRawAmount = evidence.inputRawAmount,
                acquiredTokenContract = evidence.outputTokenContract,
                acquiredRawAmount = evidence.outputRawAmount,
                tokenRecipient = evidence.outputRecipient,
                blockTimestampEpochMs = evidence.blockTimestampEpochMs
            )
        )
    }

    sealed class Result {
        data class Candidate(val evidence: ConfirmedBuybackExecutionCandidate) : Result()
        data class Abstained(val reasonCodes: List<String>) : Result()
    }
}
