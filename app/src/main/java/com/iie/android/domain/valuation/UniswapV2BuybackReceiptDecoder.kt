package com.iie.android.domain.valuation

/**
 * Strict single-hop Uniswap V2-style receipt decoder.
 *
 * This is deliberately not a router/aggregator decoder: it accepts exactly one
 * Swap event and one matching ERC-20 output Transfer from the reviewed pair.
 * Fee-on-transfer tokens, multihop routes and split outputs abstain.
 */
data class UniswapV2ReviewedPair(
    val chainId: Long,
    val pairAddress: String,
    val token0: String,
    val token1: String,
    val evidenceUrl: String,
    val reviewStatus: String,
    val validFromEpochMs: Long = 1L,
    val validUntilEpochMs: Long? = null
) {
    fun validationErrors(): List<String> = buildList {
        if (chainId <= 0L) add("PAIR_CHAIN_ID_INVALID")
        if (!pairAddress.isEvmAddress()) add("PAIR_ADDRESS_INVALID")
        if (!token0.isEvmAddress() || !token1.isEvmAddress()) add("PAIR_TOKEN_ADDRESS_INVALID")
        if (token0.equals(token1, true)) add("PAIR_TOKENS_MUST_DIFFER")
        if (!evidenceUrl.startsWith("https://")) add("PAIR_EVIDENCE_URL_MUST_BE_HTTPS")
        if (reviewStatus != REVIEWED) add("PAIR_MAPPING_NOT_REVIEWED")
        if (validFromEpochMs <= 0L || (validUntilEpochMs != null && validUntilEpochMs < validFromEpochMs)) {
            add("PAIR_VALIDITY_WINDOW_INVALID")
        }
    }

    companion object { const val REVIEWED = "REVIEWED" }
}

data class EvmReceiptLog(
    val address: String,
    val topics: List<String>,
    val data: String,
    val transactionHash: String,
    val blockNumber: Long,
    val blockHash: String,
    val logIndex: Long
)

data class EvmTransactionReceiptEvidence(
    val chainId: Long,
    val transactionHash: String,
    val transactionTo: String,
    val blockNumber: Long,
    val blockHash: String,
    val blockTimestampEpochMs: Long,
    val succeeded: Boolean,
    val rpcCrosschecked: Boolean,
    val logs: List<EvmReceiptLog>,
    val observedAtEpochMs: Long
)

sealed class UniswapV2BuybackDecodeResult {
    data class Decoded(val evidence: DecodedSwapExecutionEvidence) : UniswapV2BuybackDecodeResult()
    data class Abstained(val reasonCodes: List<String>) : UniswapV2BuybackDecodeResult()
}

object UniswapV2BuybackReceiptDecoder {
    private const val SWAP_TOPIC = "0xd78ad95fa46c994b6551d0da85fc275fe613ce37657fb8d5e3d130840159d822"
    private const val TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    private val HASH = Regex("0x[a-fA-F0-9]{64}")

    fun decode(
        receipt: EvmTransactionReceiptEvidence,
        executorMapping: ProtocolEconomicAddressMapping,
        pair: UniswapV2ReviewedPair
    ): UniswapV2BuybackDecodeResult {
        val errors = mutableListOf<String>()
        errors += pair.validationErrors()
        errors += executorMapping.validationErrors()
        if (executorMapping.role != ProtocolEconomicAddressRole.BUYBACK_EXECUTOR) errors += "ROLE_IS_NOT_BUYBACK_EXECUTOR"
        if (receipt.chainId != executorMapping.chainId || receipt.chainId != pair.chainId) errors += "CHAIN_ID_MISMATCH"
        if (!receipt.transactionTo.equals(executorMapping.address, true)) errors += "TRANSACTION_EXECUTOR_MISMATCH"
        if (!receipt.succeeded) errors += "TRANSACTION_RECEIPT_FAILED"
        if (!receipt.rpcCrosschecked) errors += "RPC_CROSSCHECK_REQUIRED"
        if (!receipt.transactionHash.matches(HASH) || !receipt.blockHash.matches(HASH) ||
            receipt.blockNumber < 0L || receipt.observedAtEpochMs <= 0L
        ) errors += "RECEIPT_BLOCK_REFERENCE_INVALID"
        if (receipt.blockTimestampEpochMs <= 0L ||
            receipt.blockTimestampEpochMs < executorMapping.validFromEpochMs ||
            (executorMapping.validUntilEpochMs != null && receipt.blockTimestampEpochMs > executorMapping.validUntilEpochMs)
        ) errors += "EXECUTOR_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"
        if (receipt.blockTimestampEpochMs < pair.validFromEpochMs ||
            (pair.validUntilEpochMs != null && receipt.blockTimestampEpochMs > pair.validUntilEpochMs)
        ) errors += "PAIR_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"
        if (!pair.token0.equals(executorMapping.tokenContract, true) &&
            !pair.token1.equals(executorMapping.tokenContract, true)
        ) errors += "REVIEWED_PAIR_DOES_NOT_CONTAIN_ACQUIRED_TOKEN"

        val relevant = receipt.logs.filter { log ->
            log.transactionHash.equals(receipt.transactionHash, true) &&
                log.blockNumber == receipt.blockNumber &&
                log.blockHash.equals(receipt.blockHash, true) &&
                log.logIndex >= 0L
        }
        if (relevant.size != receipt.logs.size) errors += "RECEIPT_LOG_BLOCK_OR_TRANSACTION_MISMATCH"

        val swapLogs = relevant.filter { log ->
            log.address.equals(pair.pairAddress, true) &&
                log.topics.firstOrNull()?.equals(SWAP_TOPIC, true) == true
        }
        if (swapLogs.size != 1) errors += "UNISWAP_V2_EXPECTED_EXACTLY_ONE_PAIR_SWAP"

        val swap = swapLogs.singleOrNull()
        val decodedSwap = swap?.let(::decodeSwapData)
        if (swap != null && decodedSwap == null) errors += "UNISWAP_V2_SWAP_DATA_INVALID"

        val acquired = executorMapping.tokenContract.lowercase()
        val token0IsAcquired = pair.token0.equals(acquired, true)
        val token1IsAcquired = pair.token1.equals(acquired, true)
        val legs = decodedSwap?.let { values ->
            val input0 = values[0]
            val input1 = values[1]
            val output0 = values[2]
            val output1 = values[3]
            when {
                token0IsAcquired && output0.signum() > 0 && input1.signum() > 0 &&
                    input0 == java.math.BigInteger.ZERO && output1 == java.math.BigInteger.ZERO ->
                    Triple(pair.token1, input1.toString(), output0.toString())
                token1IsAcquired && output1.signum() > 0 && input0.signum() > 0 &&
                    input1 == java.math.BigInteger.ZERO && output0 == java.math.BigInteger.ZERO ->
                    Triple(pair.token0, input0.toString(), output1.toString())
                else -> null
            }
        }
        if (legs == null) errors += "SWAP_MUST_BE_SINGLE_INPUT_AND_ACQUIRE_MAPPED_TOKEN"

        val transferLogs = relevant.filter { log ->
            log.address.equals(acquired, true) &&
                log.topics.size == 3 &&
                log.topics[0].equals(TRANSFER_TOPIC, true) &&
                topicAddress(log.topics[1])?.equals(pair.pairAddress, true) == true
        }
        val outputRecipient = transferLogs.mapNotNull { log ->
            val recipient = topicAddress(log.topics[2])
            val amount = decodeUint256(log.data)
            if (recipient != null && amount != null && legs != null && amount.toString() == legs.third) recipient
            else null
        }.distinct()
        if (outputRecipient.size != 1 || transferLogs.size != 1) errors += "OUTPUT_TRANSFER_MUST_MATCH_EXACTLY_ONE_PAIR_PAYMENT"

        val inputTransfers = legs?.let { swapLegs ->
            relevant.filter { log ->
                log.address.equals(swapLegs.first, true) &&
                    log.topics.size == 3 &&
                    log.topics[0].equals(TRANSFER_TOPIC, true) &&
                    topicAddress(log.topics[2])?.equals(pair.pairAddress, true) == true &&
                    decodeUint256(log.data)?.toString() == swapLegs.second
            }
        }.orEmpty()
        if (inputTransfers.size != 1) errors += "INPUT_TRANSFER_MUST_MATCH_EXACTLY_ONE_PAIR_FUNDING"

        if (errors.isNotEmpty()) return UniswapV2BuybackDecodeResult.Abstained(errors.distinct())

        return UniswapV2BuybackDecodeResult.Decoded(
            DecodedSwapExecutionEvidence(
                chainId = receipt.chainId,
                transactionHash = receipt.transactionHash,
                blockNumber = receipt.blockNumber,
                blockHash = receipt.blockHash,
                receiptSucceeded = receipt.succeeded,
                rpcCrosschecked = receipt.rpcCrosschecked,
                executorAddress = receipt.transactionTo,
                inputTokenContract = legs!!.first,
                inputRawAmount = legs.second,
                outputTokenContract = executorMapping.tokenContract,
                outputRawAmount = legs.third,
                outputRecipient = outputRecipient.single(),
                decodedAtEpochMs = receipt.observedAtEpochMs,
                blockTimestampEpochMs = receipt.blockTimestampEpochMs
            )
        )
    }

    private fun decodeSwapData(log: EvmReceiptLog): List<java.math.BigInteger>? {
        if (log.topics.size != 3 || !log.topics[1].isHexWord() || !log.topics[2].isHexWord()) return null
        val data = log.data.removePrefix("0x")
        if (data.length != 256 || !data.matches(Regex("[a-fA-F0-9]{256}"))) return null
        return (0 until 4).map { index ->
            data.substring(index * 64, (index + 1) * 64).toBigInteger(16)
        }
    }

    private fun decodeUint256(data: String): java.math.BigInteger? {
        val value = data.removePrefix("0x")
        if (value.length != 64 || !value.matches(Regex("[a-fA-F0-9]{64}"))) return null
        return value.toBigInteger(16)
    }

    private fun topicAddress(topic: String): String? {
        val value = topic.removePrefix("0x")
        if (value.length != 64 || !value.matches(Regex("[a-fA-F0-9]{64}"))) return null
        if (!value.substring(0, 24).all { it == '0' }) return null
        return "0x" + value.takeLast(40)
    }

    private fun String.isHexWord() = removePrefix("0x").let {
        it.length == 64 && it.matches(Regex("[a-fA-F0-9]{64}"))
    }

    private fun String.isEvmAddress() = matches(Regex("0x[a-fA-F0-9]{40}"))
}
