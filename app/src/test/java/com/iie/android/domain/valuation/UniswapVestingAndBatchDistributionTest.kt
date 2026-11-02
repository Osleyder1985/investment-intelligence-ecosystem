package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniswapVestingAndBatchDistributionTest {
    private val distributor = "0x1111111111111111111111111111111111111111"
    private val token = "0x2222222222222222222222222222222222222222"
    private val recipientA = "0x3333333333333333333333333333333333333333"
    private val recipientB = "0x4444444444444444444444444444444444444444"
    private val tx = "0x" + "a".repeat(64)
    private val blockHash = "0x" + "b".repeat(64)
    private val transferTopic = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    private val eventTopic = "0x" + "c".repeat(64)

    private fun word(value: Long) = value.toString(16).padStart(64, '0')
    private fun addressTopic(address: String) = "0x" + address.removePrefix("0x").padStart(64, '0')

    private val mapping = ProtocolEconomicAddressMapping(
        protocolId = "protocol:test",
        assetId = "asset:test",
        chainId = 1,
        address = distributor,
        tokenContract = token,
        role = ProtocolEconomicAddressRole.DISTRIBUTION_SOURCE,
        validFromEpochMs = 1_000,
        validUntilEpochMs = null,
        evidenceUrl = "https://example.org/distributor",
        reviewStatus = "REVIEWED"
    )

    private fun receipt(
        logs: List<EvmReceiptLog>,
        succeeded: Boolean = true,
        crosschecked: Boolean = true
    ) = EvmTransactionReceiptEvidence(
        chainId = 1,
        transactionHash = tx,
        transactionTo = distributor,
        blockNumber = 100,
        blockHash = blockHash,
        blockTimestampEpochMs = 1_500,
        succeeded = succeeded,
        rpcCrosschecked = crosschecked,
        logs = logs,
        observedAtEpochMs = 2_000
    )

    private fun transfer(from: String, to: String, amount: Long, logIndex: Long) = EvmReceiptLog(
        address = token,
        topics = listOf(transferTopic, addressTopic(from), addressTopic(to)),
        data = "0x" + word(amount),
        transactionHash = tx,
        blockNumber = 100,
        blockHash = blockHash,
        logIndex = logIndex
    )

    @Test fun recognizesTwoExactRecipientPaymentsAsBatchCandidate() {
        val result = ReviewedBatchDistributionReceiptDecoder.decode(
            receipt(listOf(
                transfer(distributor, recipientA, 100, 1),
                transfer(distributor, recipientB, 200, 2)
            )),
            mapping
        )
        assertTrue(result is BatchDistributionDecodeResult.Candidate)
        val evidence = (result as BatchDistributionDecodeResult.Candidate).evidence
        assertEquals(2, evidence.payments.size)
        assertEquals(listOf("100", "200"), evidence.payments.map { it.rawAmount })
        assertEquals(
            "RPC_CROSSCHECKED_BATCH_TRANSFER_CANDIDATE_PENDING_FINALITY_AND_PROTOCOL_REVIEW",
            evidence.evidenceStatus
        )
    }

    @Test fun rejectsSingleRecipientDuplicateBeneficiaryAndMissingRpcCrosscheck() {
        val single = ReviewedBatchDistributionReceiptDecoder.decode(
            receipt(listOf(transfer(distributor, recipientA, 100, 1))), mapping
        ) as BatchDistributionDecodeResult.Abstained
        assertTrue(single.reasonCodes.contains("BATCH_RECIPIENT_COUNT_BELOW_THRESHOLD"))

        val duplicate = ReviewedBatchDistributionReceiptDecoder.decode(
            receipt(listOf(
                transfer(distributor, recipientA, 100, 1),
                transfer(distributor, recipientA, 200, 2)
            )), mapping
        ) as BatchDistributionDecodeResult.Abstained
        assertTrue(duplicate.reasonCodes.contains("BATCH_BENEFICIARIES_MUST_BE_UNIQUE"))

        val unchecked = ReviewedBatchDistributionReceiptDecoder.decode(
            receipt(listOf(
                transfer(distributor, recipientA, 100, 1),
                transfer(distributor, recipientB, 200, 2)
            ), crosschecked = false), mapping
        ) as BatchDistributionDecodeResult.Abstained
        assertTrue(unchecked.reasonCodes.contains("BATCH_RPC_CROSSCHECK_REQUIRED"))
    }

    private val vestingRecipient = "0x5555555555555555555555555555555555555555"
    private val vestingTopic = UniswapUniVestingProfile.WITHDRAWN_EVENT_TOPIC

    private fun vestingSchedule() = UniswapVestingScheduleSnapshot(
        chainId = 1,
        vestingContractAddress = UniswapUniVestingProfile.VESTING_CONTRACT,
        tokenContract = UniswapUniVestingProfile.UNI_TOKEN,
        recipientAddress = vestingRecipient,
        quarterlyVestingAmountRaw = "1000000000000000000000000",
        lastUnlockTimestampSeconds = 1_767_225_600,
        quartersPassed = 2,
        blockNumber = 100,
        blockHash = blockHash,
        rpcCrosschecked = true,
        observedAtEpochMs = 2_000
    )

    private fun vestingSchema() = UniswapUniVestingProfile.withdrawalEventSchema(vestingTopic)

    private fun vestingReceipt(
        recipient: String = vestingRecipient,
        amount: Long = 500,
        quarters: Long = 2
    ) = receipt(listOf(
        EvmReceiptLog(
            address = UniswapUniVestingProfile.VESTING_CONTRACT,
            topics = listOf(vestingTopic, addressTopic(recipient)),
            data = "0x" + word(amount) + word(quarters),
            transactionHash = tx,
            blockNumber = 100,
            blockHash = blockHash,
            logIndex = 3
        ),
        EvmReceiptLog(
            address = UniswapUniVestingProfile.UNI_TOKEN,
            topics = listOf(
                transferTopic,
                addressTopic(UniswapUniVestingProfile.VESTING_CONTRACT),
                addressTopic(recipient)
            ),
            data = "0x" + word(amount),
            transactionHash = tx,
            blockNumber = 100,
            blockHash = blockHash,
            logIndex = 4
        )
    )).copy(transactionTo = UniswapUniVestingProfile.VESTING_CONTRACT)

    @Test fun decodesVerifiedUniswapVestingWithdrawalAgainstExactTokenPayment() {
        val result = UniswapVestingWithdrawalDecoder.decode(
            vestingReceipt(), vestingSchedule(), vestingSchema()
        )
        assertTrue(result is VestingWithdrawalDecodeResult.Candidate)
        val evidence = (result as VestingWithdrawalDecodeResult.Candidate).evidence
        assertEquals(vestingRecipient, evidence.recipientAddress)
        assertEquals("500", evidence.rawAmount)
        assertEquals(2L, evidence.quartersPaid)
        assertEquals(UniswapUniVestingProfile.VESTING_CONTRACT, evidence.vestingContractAddress)
    }

    @Test fun rejectsVestingRecipientAmountAndBlockDisagreement() {
        val recipientMismatch = UniswapVestingWithdrawalDecoder.decode(
            vestingReceipt(recipient = recipientA), vestingSchedule(), vestingSchema()
        ) as VestingWithdrawalDecodeResult.Abstained
        assertTrue(recipientMismatch.reasonCodes.contains("VESTING_WITHDRAWAL_RECIPIENT_MISMATCH"))

        val amountMismatch = UniswapVestingWithdrawalDecoder.decode(
            vestingReceipt(amount = 499), vestingSchedule(), vestingSchema()
        ) as VestingWithdrawalDecodeResult.Abstained
        assertTrue(amountMismatch.reasonCodes.contains("VESTING_EVENT_AMOUNT_MUST_MATCH_TOKEN_TRANSFER"))

        val wrongBlock = UniswapVestingWithdrawalDecoder.decode(
            vestingReceipt().copy(blockNumber = 101), vestingSchedule(), vestingSchema()
        ) as VestingWithdrawalDecodeResult.Abstained
        assertTrue(wrongBlock.reasonCodes.contains("VESTING_SCHEDULE_AND_WITHDRAWAL_BLOCK_MISMATCH"))
    }
}
