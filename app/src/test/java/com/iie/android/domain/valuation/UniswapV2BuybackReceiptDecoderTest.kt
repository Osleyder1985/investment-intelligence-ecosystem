package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniswapV2BuybackReceiptDecoderTest {
    private val executor = "0x1111111111111111111111111111111111111111"
    private val input = "0x2222222222222222222222222222222222222222"
    private val acquired = "0x3333333333333333333333333333333333333333"
    private val pairAddress = "0x4444444444444444444444444444444444444444"
    private val recipient = "0x5555555555555555555555555555555555555555"
    private val tx = "0x" + "a".repeat(64)
    private val blockHash = "0x" + "b".repeat(64)
    private val swapTopic = "0xd78ad95fa46c994b6551d0da85fc275fe613ce37657fb8d5e3d130840159d822"
    private val transferTopic = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"

    private val executorMapping = ProtocolEconomicAddressMapping(
        protocolId = "protocol:test", assetId = "asset:test", chainId = 1,
        address = executor, tokenContract = acquired,
        role = ProtocolEconomicAddressRole.BUYBACK_EXECUTOR,
        validFromEpochMs = 1_000, validUntilEpochMs = null,
        evidenceUrl = "https://example.org/executor", reviewStatus = "REVIEWED"
    )
    private val reviewedPair = UniswapV2ReviewedPair(
        chainId = 1, pairAddress = pairAddress, token0 = input, token1 = acquired,
        evidenceUrl = "https://example.org/pair", reviewStatus = "REVIEWED"
    )

    private fun word(value: Long) = value.toString(16).padStart(64, '0')
    private fun addressTopic(address: String) = "0x" + address.removePrefix("0x").padStart(64, '0')

    private fun receipt(
        logs: List<EvmReceiptLog> = validLogs(),
        succeeded: Boolean = true,
        crosschecked: Boolean = true
    ) = EvmTransactionReceiptEvidence(
        chainId = 1, transactionHash = tx, transactionTo = executor,
        blockNumber = 100, blockHash = blockHash, blockTimestampEpochMs = 1_500, succeeded = succeeded,
        rpcCrosschecked = crosschecked, logs = logs, observedAtEpochMs = 2_000
    )

    private fun validLogs(): List<EvmReceiptLog> = listOf(
        EvmReceiptLog(
            address = pairAddress,
            topics = listOf(swapTopic, addressTopic(executor), addressTopic(recipient)),
            data = "0x" + word(100) + word(0) + word(0) + word(250),
            transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 1
        ),
        EvmReceiptLog(
            address = acquired,
            topics = listOf(transferTopic, addressTopic(pairAddress), addressTopic(recipient)),
            data = "0x" + word(250),
            transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 2
        )
    )

    @Test fun decodesSingleHopSwapAndExactPairOutputTransfer() {
        val result = UniswapV2BuybackReceiptDecoder.decode(receipt(), executorMapping, reviewedPair)
        assertTrue(result is UniswapV2BuybackDecodeResult.Decoded)
        val evidence = (result as UniswapV2BuybackDecodeResult.Decoded).evidence
        assertEquals(input, evidence.inputTokenContract)
        assertEquals("100", evidence.inputRawAmount)
        assertEquals(acquired, evidence.outputTokenContract)
        assertEquals("250", evidence.outputRawAmount)
        assertEquals(recipient, evidence.outputRecipient)
        assertTrue(ProtocolBuybackEvidenceEvaluator.evaluate(evidence, executorMapping)
            is ProtocolBuybackEvidenceEvaluator.Result.Candidate)
    }

    @Test fun rejectsUnreviewedPairAndFailedOrUncrosscheckedReceipt() {
        val unreviewed = UniswapV2BuybackReceiptDecoder.decode(
            receipt(), executorMapping, reviewedPair.copy(reviewStatus = "PENDING")
        ) as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(unreviewed.reasonCodes.contains("PAIR_MAPPING_NOT_REVIEWED"))

        val failed = UniswapV2BuybackReceiptDecoder.decode(
            receipt(succeeded = false), executorMapping, reviewedPair
        ) as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(failed.reasonCodes.contains("TRANSACTION_RECEIPT_FAILED"))

        val unchecked = UniswapV2BuybackReceiptDecoder.decode(
            receipt(crosschecked = false), executorMapping, reviewedPair
        ) as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(unchecked.reasonCodes.contains("RPC_CROSSCHECK_REQUIRED"))
    }

    @Test fun rejectsMismatchedOutputTransferAndAmbiguousMultipleSwaps() {
        val wrongPayment = validLogs().toMutableList()
        wrongPayment[1] = wrongPayment[1].copy(data = "0x" + word(249))
        val mismatch = UniswapV2BuybackReceiptDecoder.decode(
            receipt(wrongPayment), executorMapping, reviewedPair
        ) as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(mismatch.reasonCodes.contains("OUTPUT_TRANSFER_MUST_MATCH_EXACTLY_ONE_PAIR_PAYMENT"))

        val multiple = validLogs() + validLogs().first().copy(logIndex = 3)
        val ambiguous = UniswapV2BuybackReceiptDecoder.decode(
            receipt(multiple), executorMapping, reviewedPair
        ) as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(ambiguous.reasonCodes.contains("UNISWAP_V2_EXPECTED_EXACTLY_ONE_PAIR_SWAP"))
    }

    @Test fun rejectsExecutorMappingThatWasNotActiveAtBlockTime() {
        val expired = executorMapping.copy(validUntilEpochMs = 1_400)
        val result = UniswapV2BuybackReceiptDecoder.decode(receipt(), expired, reviewedPair)
            as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(result.reasonCodes.contains("EXECUTOR_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"))
    }

    @Test fun rejectsWrongExecutorAndReceiptLogFromDifferentBlock() {
        val wrongExecutor = UniswapV2BuybackReceiptDecoder.decode(
            receipt().copy(transactionTo = recipient), executorMapping, reviewedPair
        ) as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(wrongExecutor.reasonCodes.contains("TRANSACTION_EXECUTOR_MISMATCH"))

        val wrongBlockLog = validLogs().toMutableList()
        wrongBlockLog[0] = wrongBlockLog[0].copy(blockHash = "0x" + "c".repeat(64))
        val wrongBlock = UniswapV2BuybackReceiptDecoder.decode(
            receipt(wrongBlockLog), executorMapping, reviewedPair
        ) as UniswapV2BuybackDecodeResult.Abstained
        assertTrue(wrongBlock.reasonCodes.contains("RECEIPT_LOG_BLOCK_OR_TRANSACTION_MISMATCH"))
    }
}
