package com.iie.android.domain.valuation

import java.math.BigInteger

/**
 * Strict single-pool, single-hop Uniswap V3 receipt decoder.
 *
 * Supports only direct pool settlement where the pool's Swap recipient is
 * also the final ERC-20 recipient. Router-mediated forwarding, multihop,
 * split routes and fee-on-transfer behavior deliberately abstain.
 */
data class UniswapV3ReviewedPool(
    val chainId: Long,
    val poolAddress: String,
    val token0: String,
    val token1: String,
    val evidenceUrl: String,
    val reviewStatus: String,
    val validFromEpochMs: Long = 1L,
    val validUntilEpochMs: Long? = null
) {
    fun validationErrors(): List<String> = buildList {
        if (chainId <= 0L) add("POOL_CHAIN_ID_INVALID")
        if (!poolAddress.matches(Regex("0x[a-fA-F0-9]{40}"))) add("POOL_ADDRESS_INVALID")
        if (!token0.matches(Regex("0x[a-fA-F0-9]{40}")) ||
            !token1.matches(Regex("0x[a-fA-F0-9]{40}"))) add("POOL_TOKEN_ADDRESS_INVALID")
        if (token0.equals(token1, true)) add("POOL_TOKENS_MUST_DIFFER")
        if (!evidenceUrl.startsWith("https://")) add("POOL_EVIDENCE_URL_MUST_BE_HTTPS")
        if (reviewStatus != REVIEWED) add("POOL_MAPPING_NOT_REVIEWED")
        if (validFromEpochMs <= 0L || (validUntilEpochMs != null && validUntilEpochMs < validFromEpochMs)) {
            add("POOL_VALIDITY_WINDOW_INVALID")
        }
    }

    companion object { const val REVIEWED = "REVIEWED" }
}

sealed class UniswapV3BuybackDecodeResult {
    data class Decoded(val evidence: DecodedSwapExecutionEvidence) : UniswapV3BuybackDecodeResult()
    data class Abstained(val reasonCodes: List<String>) : UniswapV3BuybackDecodeResult()
}

object UniswapV3BuybackReceiptDecoder {
    private const val SWAP_TOPIC = "0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67"
    private const val TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    private val HASH = Regex("0x[a-fA-F0-9]{64}")
    private val UINT_256_MODULUS = BigInteger.ONE.shiftLeft(256)
    private val UINT_255 = BigInteger.ONE.shiftLeft(255)

    fun decode(
        receipt: EvmTransactionReceiptEvidence,
        executorMapping: ProtocolEconomicAddressMapping,
        pool: UniswapV3ReviewedPool
    ): UniswapV3BuybackDecodeResult {
        val errors = mutableListOf<String>()
        errors += pool.validationErrors()
        errors += executorMapping.validationErrors()
        if (executorMapping.role != ProtocolEconomicAddressRole.BUYBACK_EXECUTOR) errors += "ROLE_IS_NOT_BUYBACK_EXECUTOR"
        if (receipt.chainId != executorMapping.chainId || receipt.chainId != pool.chainId) errors += "CHAIN_ID_MISMATCH"
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
        if (receipt.blockTimestampEpochMs < pool.validFromEpochMs ||
            (pool.validUntilEpochMs != null && receipt.blockTimestampEpochMs > pool.validUntilEpochMs)
        ) errors += "POOL_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"

        val acquired = executorMapping.tokenContract.lowercase()
        val token0IsAcquired = pool.token0.equals(acquired, true)
        val token1IsAcquired = pool.token1.equals(acquired, true)
        if (!token0IsAcquired && !token1IsAcquired) errors += "REVIEWED_POOL_DOES_NOT_CONTAIN_ACQUIRED_TOKEN"

        val relevant = receipt.logs.filter { log ->
            log.transactionHash.equals(receipt.transactionHash, true) &&
                log.blockNumber == receipt.blockNumber &&
                log.blockHash.equals(receipt.blockHash, true) &&
                log.logIndex >= 0L
        }
        if (relevant.size != receipt.logs.size) errors += "RECEIPT_LOG_BLOCK_OR_TRANSACTION_MISMATCH"

        val swapLogs = relevant.filter { log ->
            log.address.equals(pool.poolAddress, true) &&
                log.topics.firstOrNull()?.equals(SWAP_TOPIC, true) == true
        }
        if (swapLogs.size != 1) errors += "UNISWAP_V3_EXPECTED_EXACTLY_ONE_POOL_SWAP"

        val swap = swapLogs.singleOrNull()
        val amounts = swap?.let(::decodeSwapAmounts)
        if (swap != null && amounts == null) errors += "UNISWAP_V3_SWAP_DATA_INVALID"

        val sender = swap?.topics?.getOrNull(1)?.let(::topicAddress)
        val eventRecipient = swap?.topics?.getOrNull(2)?.let(::topicAddress)
        if (sender == null || !sender.equals(executorMapping.address, true)) {
            errors += "SWAP_SENDER_MUST_MATCH_REVIEWED_EXECUTOR"
        }

        val legs = amounts?.let { values ->
            val amount0 = values.first
            val amount1 = values.second
            when {
                token0IsAcquired && amount0.signum() < 0 && amount1.signum() > 0 ->
                    Triple(pool.token1, amount1.toString(), amount0.abs().toString())
                token1IsAcquired && amount1.signum() < 0 && amount0.signum() > 0 ->
                    Triple(pool.token0, amount0.toString(), amount1.abs().toString())
                else -> null
            }
        }
        if (legs == null) errors += "SWAP_MUST_HAVE_ONE_INPUT_AND_ACQUIRE_MAPPED_TOKEN"

        val outputTransfers = relevant.filter { log ->
            log.address.equals(acquired, true) &&
                log.topics.size == 3 &&
                log.topics[0].equals(TRANSFER_TOPIC, true) &&
                topicAddress(log.topics[1])?.equals(pool.poolAddress, true) == true
        }
        val matchingOutputRecipients = outputTransfers.mapNotNull { log ->
            val to = topicAddress(log.topics[2])
            val amount = decodeUint256(log.data)
            if (to != null && amount != null && legs != null && amount.toString() == legs.third) to else null
        }.distinct()
        if (outputTransfers.size != 1 || matchingOutputRecipients.size != 1) {
            errors += "OUTPUT_TRANSFER_MUST_MATCH_EXACTLY_ONE_POOL_PAYMENT"
        }
        if (eventRecipient == null || matchingOutputRecipients.singleOrNull()?.equals(eventRecipient, true) != true) {
            errors += "POOL_EVENT_RECIPIENT_MUST_MATCH_FINAL_OUTPUT_RECIPIENT"
        }

        val inputTransfers = legs?.let { swapLegs ->
            relevant.filter { log ->
                log.address.equals(swapLegs.first, true) &&
                    log.topics.size == 3 &&
                    log.topics[0].equals(TRANSFER_TOPIC, true) &&
                    topicAddress(log.topics[2])?.equals(pool.poolAddress, true) == true &&
                    decodeUint256(log.data)?.toString() == swapLegs.second
            }
        }.orEmpty()
        if (inputTransfers.size != 1) errors += "INPUT_TRANSFER_MUST_MATCH_EXACTLY_ONE_POOL_FUNDING"

        if (errors.isNotEmpty()) return UniswapV3BuybackDecodeResult.Abstained(errors.distinct())

        return UniswapV3BuybackDecodeResult.Decoded(
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
                outputRecipient = matchingOutputRecipients.single(),
                decodedAtEpochMs = receipt.observedAtEpochMs,
                blockTimestampEpochMs = receipt.blockTimestampEpochMs
            )
        )
    }

    private fun decodeSwapAmounts(log: EvmReceiptLog): Pair<BigInteger, BigInteger>? {
        if (log.topics.size != 3 || !log.topics[1].isHexWord() || !log.topics[2].isHexWord()) return null
        val data = log.data.removePrefix("0x")
        if (data.length != 320 || !data.matches(Regex("[a-fA-F0-9]{320}"))) return null
        return decodeInt256(data.substring(0, 64)) to decodeInt256(data.substring(64, 128))
    }

    /** ABI int256 uses two's-complement encoding in a 256-bit word. */
    private fun decodeInt256(word: String): BigInteger {
        val unsigned = word.toBigInteger(16)
        return if (unsigned >= UINT_255) unsigned.subtract(UINT_256_MODULUS) else unsigned
    }

    private fun decodeUint256(data: String): BigInteger? {
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
}
