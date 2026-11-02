package com.iie.android.domain.valuation

/**
 * Explicit ABI layout for a reviewed claim event. A beneficiary may be indexed
 * in topics or encoded in a non-indexed data word, as required by the verified ABI.
 */
data class ReviewedDistributionClaimEvent(
    val chainId: Long,
    val distributorAddress: String,
    val tokenContract: String,
    val claimEventTopic: String,
    val beneficiaryTopicIndex: Int,
    val amountDataWordIndex: Int,
    val evidenceUrl: String,
    val reviewStatus: String,
    val validFromEpochMs: Long = 1L,
    val validUntilEpochMs: Long? = null,
    val beneficiaryDataWordIndex: Int? = null,
    val expectedDataWordCount: Int? = null,
    val expectedTopicCount: Int? = null
) {
    fun validationErrors(): List<String> = buildList {
        if (chainId <= 0L) add("CLAIM_SCHEMA_CHAIN_ID_INVALID")
        if (!distributorAddress.matches(ADDRESS)) add("CLAIM_SCHEMA_DISTRIBUTOR_INVALID")
        if (!tokenContract.matches(ADDRESS)) add("CLAIM_SCHEMA_TOKEN_INVALID")
        if (!claimEventTopic.matches(HASH)) add("CLAIM_SCHEMA_TOPIC_INVALID")
        if (beneficiaryDataWordIndex == null) {
            if (beneficiaryTopicIndex !in 1..3) add("CLAIM_SCHEMA_BENEFICIARY_TOPIC_INDEX_INVALID")
        } else {
            if (beneficiaryTopicIndex != 0) add("CLAIM_SCHEMA_BENEFICIARY_LOCATION_AMBIGUOUS")
            if (beneficiaryDataWordIndex < 0) add("CLAIM_SCHEMA_BENEFICIARY_DATA_WORD_INDEX_INVALID")
        }
        if (amountDataWordIndex < 0) add("CLAIM_SCHEMA_AMOUNT_WORD_INDEX_INVALID")
        if (expectedDataWordCount != null && expectedDataWordCount <= 0) add("CLAIM_SCHEMA_DATA_WORD_COUNT_INVALID")
        if (expectedTopicCount != null && expectedTopicCount <= 0) add("CLAIM_SCHEMA_TOPIC_COUNT_INVALID")
        if (!evidenceUrl.startsWith("https://")) add("CLAIM_SCHEMA_EVIDENCE_URL_MUST_BE_HTTPS")
        if (reviewStatus != REVIEWED) add("CLAIM_SCHEMA_NOT_REVIEWED")
        if (validFromEpochMs <= 0L || (validUntilEpochMs != null && validUntilEpochMs < validFromEpochMs)) {
            add("CLAIM_SCHEMA_VALIDITY_WINDOW_INVALID")
        }
    }

    companion object {
        const val REVIEWED = "REVIEWED"
        private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")
        private val HASH = Regex("0x[a-fA-F0-9]{64}")
    }
}

data class VerifiedDistributionClaimPaymentCandidate(
    val protocolId: String,
    val assetId: String,
    val chainId: Long,
    val transactionHash: String,
    val blockNumber: Long,
    val blockHash: String,
    val distributorAddress: String,
    val beneficiaryAddress: String,
    val tokenContract: String,
    val rawAmount: String,
    val claimEventLogIndex: Long,
    val paymentTransferLogIndex: Long,
    val blockTimestampEpochMs: Long,
    val finalityStatus: String = "OBSERVED",
    val safeBlockNumber: Long? = null,
    val finalizedBlockNumber: Long? = null,
    val evidenceStatus: String = "RPC_CROSSCHECKED_CLAIM_PAYMENT_PENDING_FINALITY_AND_HOLDER_REVIEW"
)

sealed class DistributionClaimDecodeResult {
    data class Candidate(val evidence: VerifiedDistributionClaimPaymentCandidate) : DistributionClaimDecodeResult()
    data class Abstained(val reasonCodes: List<String>) : DistributionClaimDecodeResult()
}

/**
 * Matches one reviewed claim event to one exact ERC-20 payment in the same
 * successful, RPC-crosschecked receipt. It deliberately does not assert that
 * the beneficiary is an ultimate holder or that the claim is economically
 * attributable to all holders.
 */
object ReviewedDistributionClaimReceiptDecoder {
    private const val TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    private const val ZERO_ADDRESS = "0x0000000000000000000000000000000000000000"
    private val HASH = Regex("0x[a-fA-F0-9]{64}")
    private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")

    fun decode(
        receipt: EvmTransactionReceiptEvidence,
        sourceMapping: ProtocolEconomicAddressMapping,
        schema: ReviewedDistributionClaimEvent
    ): DistributionClaimDecodeResult {
        val errors = mutableListOf<String>()
        errors += sourceMapping.validationErrors()
        errors += schema.validationErrors()
        if (sourceMapping.role != ProtocolEconomicAddressRole.DISTRIBUTION_SOURCE) {
            errors += "ROLE_IS_NOT_DISTRIBUTION_SOURCE"
        }
        if (receipt.chainId != sourceMapping.chainId || receipt.chainId != schema.chainId) {
            errors += "CHAIN_ID_MISMATCH"
        }
        if (!sourceMapping.address.equals(schema.distributorAddress, true) ||
            !sourceMapping.tokenContract.equals(schema.tokenContract, true)
        ) errors += "CLAIM_SCHEMA_DOES_NOT_MATCH_REVIEWED_SOURCE_MAPPING"
        if (!receipt.transactionTo.equals(schema.distributorAddress, true)) {
            errors += "TRANSACTION_TARGET_MUST_BE_REVIEWED_DISTRIBUTOR"
        }
        if (!receipt.succeeded) errors += "TRANSACTION_RECEIPT_FAILED"
        if (!receipt.rpcCrosschecked) errors += "RPC_CROSSCHECK_REQUIRED"
        if (!receipt.transactionHash.matches(HASH) || !receipt.blockHash.matches(HASH) ||
            receipt.blockNumber < 0L || receipt.observedAtEpochMs <= 0L
        ) errors += "RECEIPT_BLOCK_REFERENCE_INVALID"
        val blockTime = receipt.blockTimestampEpochMs
        if (blockTime <= 0L ||
            blockTime < sourceMapping.validFromEpochMs ||
            (sourceMapping.validUntilEpochMs != null && blockTime > sourceMapping.validUntilEpochMs)
        ) errors += "DISTRIBUTION_SOURCE_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"
        if (blockTime < schema.validFromEpochMs ||
            (schema.validUntilEpochMs != null && blockTime > schema.validUntilEpochMs)
        ) errors += "CLAIM_SCHEMA_NOT_ACTIVE_AT_BLOCK_TIME"

        val logs = receipt.logs.filter { log ->
            log.transactionHash.equals(receipt.transactionHash, true) &&
                log.blockNumber == receipt.blockNumber &&
                log.blockHash.equals(receipt.blockHash, true) &&
                log.logIndex >= 0L
        }
        if (logs.size != receipt.logs.size) errors += "RECEIPT_LOG_BLOCK_OR_TRANSACTION_MISMATCH"
        if (logs.map { it.logIndex }.distinct().size != logs.size) {
            errors += "RECEIPT_LOG_INDEX_DUPLICATE"
        }

        val claimLogs = logs.filter { log ->
            log.address.equals(schema.distributorAddress, true) &&
                log.topics.firstOrNull()?.equals(schema.claimEventTopic, true) == true
        }
        if (claimLogs.size != 1) errors += "EXPECTED_EXACTLY_ONE_REVIEWED_CLAIM_EVENT"
        val claim = claimLogs.singleOrNull()

        val beneficiary = claim?.let { event ->
            if (schema.beneficiaryDataWordIndex == null) {
                event.topics.getOrNull(schema.beneficiaryTopicIndex)?.let(::decodeTopicAddress)
            } else {
                decodeAddressDataWord(event.data, schema.beneficiaryDataWordIndex)
            }
        }
        if (beneficiary == null || beneficiary.equals(ZERO_ADDRESS, true) ||
            beneficiary.equals(schema.distributorAddress, true)
        ) errors += "CLAIM_BENEFICIARY_INVALID"

        if (claim != null && schema.expectedDataWordCount != null &&
            dataWordCount(claim.data) != schema.expectedDataWordCount
        ) errors += "CLAIM_EVENT_DATA_WORD_COUNT_MISMATCH"
        if (claim != null && schema.expectedTopicCount != null &&
            claim.topics.size != schema.expectedTopicCount
        ) errors += "CLAIM_EVENT_TOPIC_COUNT_MISMATCH"

        val claimAmount = claim?.let { decodeDataWord(it.data, schema.amountDataWordIndex) }
        if (claimAmount == null || claimAmount.signum() <= 0) errors += "CLAIM_AMOUNT_INVALID"

        val tokenTransfers = logs.filter { log ->
            log.address.equals(schema.tokenContract, true) &&
                log.topics.firstOrNull()?.equals(TRANSFER_TOPIC, true) == true
        }
        val distributorPayments = tokenTransfers.filter { log ->
            log.topics.getOrNull(1)?.let(::decodeTopicAddress)?.equals(schema.distributorAddress, true) == true
        }
        if (distributorPayments.size != 1) {
            errors += "EXPECTED_EXACTLY_ONE_DISTRIBUTOR_TOKEN_PAYMENT"
        }

        val payment = distributorPayments.singleOrNull()
        if (payment != null && payment.topics.size != 3) {
            errors += "ERC20_TRANSFER_TOPIC_COUNT_MISMATCH"
        }
        if (payment != null && !isSingleDataWord(payment.data)) {
            errors += "ERC20_TRANSFER_DATA_WORD_COUNT_MISMATCH"
        }
        val paymentRecipient = payment?.takeIf { it.topics.size == 3 }
            ?.topics?.getOrNull(2)?.let(::decodeTopicAddress)
        val paymentAmount = payment?.takeIf { isSingleDataWord(it.data) }
            ?.let { decodeDataWord(it.data, 0) }
        if (paymentRecipient == null || paymentRecipient.equals(ZERO_ADDRESS, true)) {
            errors += "PAYMENT_RECIPIENT_INVALID"
        }
        if (beneficiary == null || paymentRecipient == null ||
            !beneficiary.equals(paymentRecipient, true)
        ) errors += "CLAIM_BENEFICIARY_MUST_MATCH_PAYMENT_RECIPIENT"
        if (claimAmount == null || paymentAmount == null || claimAmount != paymentAmount) {
            errors += "CLAIM_AMOUNT_MUST_MATCH_EXACT_ERC20_PAYMENT"
        }
        if (errors.isNotEmpty()) return DistributionClaimDecodeResult.Abstained(errors.distinct())

        return DistributionClaimDecodeResult.Candidate(
            VerifiedDistributionClaimPaymentCandidate(
                protocolId = sourceMapping.protocolId,
                assetId = sourceMapping.assetId,
                chainId = receipt.chainId,
                transactionHash = receipt.transactionHash,
                blockNumber = receipt.blockNumber,
                blockHash = receipt.blockHash,
                distributorAddress = schema.distributorAddress,
                beneficiaryAddress = beneficiary!!,
                tokenContract = schema.tokenContract,
                rawAmount = claimAmount!!.toString(),
                claimEventLogIndex = claim!!.logIndex,
                paymentTransferLogIndex = payment!!.logIndex,
                blockTimestampEpochMs = blockTime,
                finalityStatus = receipt.finalityStatus,
                safeBlockNumber = receipt.safeBlockNumber,
                finalizedBlockNumber = receipt.finalizedBlockNumber
            )
        )
    }

    private fun decodeTopicAddress(topic: String): String? {
        val value = topic.removePrefix("0x")
        if (value.length != 64 || !value.matches(Regex("[a-fA-F0-9]{64}"))) return null
        if (!value.substring(0, 24).all { it == '0' }) return null
        return "0x" + value.takeLast(40)
    }

    private fun decodeAddressDataWord(data: String, wordIndex: Int): String? {
        val word = dataWord(data, wordIndex) ?: return null
        if (!word.substring(0, 24).all { it == '0' }) return null
        return "0x" + word.takeLast(40)
    }

    private fun decodeDataWord(data: String, wordIndex: Int): java.math.BigInteger? =
        dataWord(data, wordIndex)?.toBigInteger(16)

    private fun dataWord(data: String, wordIndex: Int): String? {
        val value = data.removePrefix("0x")
        if (value.isEmpty() || value.length % 64 != 0 ||
            !value.matches(Regex("[a-fA-F0-9]+"))
        ) return null
        val start = wordIndex * 64
        if (start < 0 || start + 64 > value.length) return null
        return value.substring(start, start + 64)
    }

    private fun isSingleDataWord(data: String): Boolean =
        data.startsWith("0x") && data.length == 66 &&
            data.drop(2).matches(Regex("[a-fA-F0-9]{64}"))

    private fun dataWordCount(data: String): Int? {
        val value = data.removePrefix("0x")
        if (value.isEmpty() || value.length % 64 != 0 ||
            !value.matches(Regex("[a-fA-F0-9]+"))
        ) return null
        return value.length / 64
    }
}
