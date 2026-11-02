package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolBuybackEvidenceEvaluatorTest {
    private val executor = "0x1111111111111111111111111111111111111111"
    private val acquiredToken = "0x2222222222222222222222222222222222222222"
    private val inputToken = "0x3333333333333333333333333333333333333333"
    private val recipient = "0x4444444444444444444444444444444444444444"
    private val tx = "0x" + "a".repeat(64)
    private val blockHash = "0x" + "b".repeat(64)

    private val mapping = ProtocolEconomicAddressMapping(
        protocolId = "protocol:test",
        assetId = "asset:test",
        chainId = 1,
        address = executor,
        tokenContract = acquiredToken,
        role = ProtocolEconomicAddressRole.BUYBACK_EXECUTOR,
        validFromEpochMs = 1_000,
        validUntilEpochMs = null,
        evidenceUrl = "https://example.org/buyback-executor",
        reviewStatus = "REVIEWED"
    )

    private fun evidence() = DecodedSwapExecutionEvidence(
        chainId = 1,
        transactionHash = tx,
        blockNumber = 100,
        blockHash = blockHash,
        receiptSucceeded = true,
        rpcCrosschecked = true,
        executorAddress = executor,
        inputTokenContract = inputToken,
        inputRawAmount = "100000",
        outputTokenContract = acquiredToken,
        outputRawAmount = "25000",
        outputRecipient = recipient,
        decodedAtEpochMs = 2_000
    )

    @Test fun successfulRpcCrosscheckedSwapAcquiringProtocolTokenProducesCandidate() {
        val result = ProtocolBuybackEvidenceEvaluator.evaluate(evidence(), mapping)
        assertTrue(result is ProtocolBuybackEvidenceEvaluator.Result.Candidate)
        val candidate = (result as ProtocolBuybackEvidenceEvaluator.Result.Candidate).evidence
        assertEquals("25000", candidate.acquiredRawAmount)
        assertEquals(recipient, candidate.tokenRecipient)
        assertEquals("RPC_CROSSCHECKED_SWAP_PENDING_FINALITY_AND_PROTOCOL_REVIEW", candidate.evidenceStatus)
    }

    @Test fun failedReceiptIsRejected() {
        val result = ProtocolBuybackEvidenceEvaluator.evaluate(evidence().copy(receiptSucceeded = false), mapping)
        assertTrue(result is ProtocolBuybackEvidenceEvaluator.Result.Abstained)
        assertTrue((result as ProtocolBuybackEvidenceEvaluator.Result.Abstained).reasonCodes.contains("TRANSACTION_RECEIPT_FAILED"))
    }

    @Test fun rpcCrosscheckIsMandatory() {
        val result = ProtocolBuybackEvidenceEvaluator.evaluate(evidence().copy(rpcCrosschecked = false), mapping)
        assertTrue(result is ProtocolBuybackEvidenceEvaluator.Result.Abstained)
        assertTrue((result as ProtocolBuybackEvidenceEvaluator.Result.Abstained).reasonCodes.contains("RPC_CROSSCHECK_REQUIRED"))
    }

    @Test fun executorAndAcquiredTokenMustMatchReviewedMapping() {
        val result = ProtocolBuybackEvidenceEvaluator.evaluate(
            evidence().copy(executorAddress = recipient), mapping
        )
        assertTrue(result is ProtocolBuybackEvidenceEvaluator.Result.Abstained)
    }

    @Test fun inputAndOutputCannotBeSameToken() {
        val result = ProtocolBuybackEvidenceEvaluator.evaluate(
            evidence().copy(inputTokenContract = acquiredToken), mapping
        )
        assertTrue(result is ProtocolBuybackEvidenceEvaluator.Result.Abstained)
    }

    @Test fun pendingRoleMappingCannotAuthorizeBuybackCandidate() {
        val result = ProtocolBuybackEvidenceEvaluator.evaluate(
            evidence(), mapping.copy(reviewStatus = "PENDING")
        )
        assertTrue(result is ProtocolBuybackEvidenceEvaluator.Result.Abstained)
    }
}
