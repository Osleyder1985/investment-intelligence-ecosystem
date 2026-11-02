package com.iie.android.domain.valuation

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniswapV3BuybackReceiptDecoderTest {
    private val executor = "0x1111111111111111111111111111111111111111"
    private val input = "0x2222222222222222222222222222222222222222"
    private val acquired = "0x3333333333333333333333333333333333333333"
    private val poolAddress = "0x4444444444444444444444444444444444444444"
    private val recipient = "0x5555555555555555555555555555555555555555"
    private val otherRecipient = "0x6666666666666666666666666666666666666666"
    private val tx = "0x" + "a".repeat(64)
    private val blockHash = "0x" + "b".repeat(64)
    private val swapTopic = "0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67"
    private val transferTopic = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"

    private val executorMapping = ProtocolEconomicAddressMapping(
        protocolId = "protocol:test", assetId = "asset:test", chainId = 1,
        address = executor, tokenContract = acquired,
        role = ProtocolEconomicAddressRole.BUYBACK_EXECUTOR,
        validFromEpochMs = 1_000, validUntilEpochMs = null,
        evidenceUrl = "https://example.org/executor", reviewStatus = "REVIEWED"
    )
    private val reviewedPool = UniswapV3ReviewedPool(
        chainId = 1, poolAddress = poolAddress, token0 = acquired, token1 = input,
        evidenceUrl = "https://example.org/pool", reviewStatus = "REVIEWED"
    )

    private fun word(value: Long) = value.toString(16).padStart(64, '0')
    private fun signedWord(value: Long): String {
        val encoded = if (value < 0) BigInteger.ONE.shiftLeft(256).add(BigInteger.valueOf(value))
            else BigInteger.valueOf(value)
        return encoded.toString(16).padStart(64, '0')
    }
    private fun addressTopic(address: String) = "0x" + address.removePrefix("0x").padStart(64, '0')

    private fun receipt(
        logs: List<EvmReceiptLog> = validLogs(),
        succeeded: Boolean = true,
        crosschecked: Boolean = true
    ) = EvmTransactionReceiptEvidence(
        chainId = 1, transactionHash = tx, transactionTo = executor,
        blockNumber = 100, blockHash = blockHash, blockTimestampEpochMs = 1_500,
        succeeded = succeeded, rpcCrosschecked = crosschecked, logs = logs, observedAtEpochMs = 2_000
    )

    private fun validLogs(recipientAddress: String = recipient): List<EvmReceiptLog> = listOf(
        EvmReceiptLog(
            address = poolAddress,
            topics = listOf(swapTopic, addressTopic(executor), addressTopic(recipientAddress)),
            data = "0x" + signedWord(-250) + signedWord(100) + word(1) + word(2) + word(3),
            transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 1
        ),
        EvmReceiptLog(
            address = acquired,
            topics = listOf(transferTopic, addressTopic(poolAddress), addressTopic(recipientAddress)),
            data = "0x" + word(250),
            transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 2
        ),
        EvmReceiptLog(
            address = input,
            topics = listOf(transferTopic, addressTopic(executor), addressTopic(poolAddress)),
            data = "0x" + word(100),
            transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 3
        )
    )

    @Test fun decodesSignedV3AmountsAndExactPoolTransfers() {
        val result = UniswapV3BuybackReceiptDecoder.decode(receipt(), executorMapping, reviewedPool)
        assertTrue(result is UniswapV3BuybackDecodeResult.Decoded)
        val evidence = (result as UniswapV3BuybackDecodeResult.Decoded).evidence
        assertEquals(input, evidence.inputTokenContract)
        assertEquals("100", evidence.inputRawAmount)
        assertEquals(acquired, evidence.outputTokenContract)
        assertEquals("250", evidence.outputRawAmount)
        assertEquals(recipient, evidence.outputRecipient)
        assertTrue(ProtocolBuybackEvidenceEvaluator.evaluate(evidence, executorMapping)
            is ProtocolBuybackEvidenceEvaluator.Result.Candidate)
    }

    @Test fun rejectsUnreviewedPoolFailedReceiptAndMissingRpcCrosscheck() {
        val unreviewed = UniswapV3BuybackReceiptDecoder.decode(
            receipt(), executorMapping, reviewedPool.copy(reviewStatus = "PENDING")
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(unreviewed.reasonCodes.contains("POOL_MAPPING_NOT_REVIEWED"))

        val failed = UniswapV3BuybackReceiptDecoder.decode(
            receipt(succeeded = false), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(failed.reasonCodes.contains("TRANSACTION_RECEIPT_FAILED"))

        val unchecked = UniswapV3BuybackReceiptDecoder.decode(
            receipt(crosschecked = false), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(unchecked.reasonCodes.contains("RPC_CROSSCHECK_REQUIRED"))
    }

    @Test fun rejectsWrongSwapSenderAndPoolEventRecipientNotMatchingPayment() {
        val wrongSender = validLogs().toMutableList()
        wrongSender[0] = wrongSender[0].copy(
            topics = listOf(swapTopic, addressTopic(otherRecipient), addressTopic(recipient))
        )
        val senderResult = UniswapV3BuybackReceiptDecoder.decode(
            receipt(wrongSender), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(senderResult.reasonCodes.contains("SWAP_SENDER_MUST_MATCH_REVIEWED_EXECUTOR"))

        val recipientMismatch = UniswapV3BuybackReceiptDecoder.decode(
            receipt(validLogs(otherRecipient)), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(recipientMismatch.reasonCodes.contains("POOL_EVENT_RECIPIENT_MUST_MATCH_FINAL_OUTPUT_RECIPIENT"))
    }

    @Test fun rejectsMismatchedInputAndOutputTransfers() {
        val wrongOutput = validLogs().toMutableList()
        wrongOutput[1] = wrongOutput[1].copy(data = "0x" + word(249))
        val outputResult = UniswapV3BuybackReceiptDecoder.decode(
            receipt(wrongOutput), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(outputResult.reasonCodes.contains("OUTPUT_TRANSFER_MUST_MATCH_EXACTLY_ONE_POOL_PAYMENT"))

        val wrongInput = validLogs().toMutableList()
        wrongInput[2] = wrongInput[2].copy(data = "0x" + word(99))
        val inputResult = UniswapV3BuybackReceiptDecoder.decode(
            receipt(wrongInput), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(inputResult.reasonCodes.contains("INPUT_TRANSFER_MUST_MATCH_EXACTLY_ONE_POOL_FUNDING"))
    }

    @Test fun rejectsMultipleSwapsWrongChainAndExpiredPoolMapping() {
        val multiple = validLogs() + validLogs().first().copy(logIndex = 4)
        val ambiguous = UniswapV3BuybackReceiptDecoder.decode(
            receipt(multiple), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(ambiguous.reasonCodes.contains("UNISWAP_V3_EXPECTED_EXACTLY_ONE_POOL_SWAP"))

        val wrongChain = UniswapV3BuybackReceiptDecoder.decode(
            receipt().copy(chainId = 10), executorMapping, reviewedPool
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(wrongChain.reasonCodes.contains("CHAIN_ID_MISMATCH"))

        val expiredPool = UniswapV3BuybackReceiptDecoder.decode(
            receipt(), executorMapping, reviewedPool.copy(validUntilEpochMs = 1_400)
        ) as UniswapV3BuybackDecodeResult.Abstained
        assertTrue(expiredPool.reasonCodes.contains("POOL_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"))
    }
}
