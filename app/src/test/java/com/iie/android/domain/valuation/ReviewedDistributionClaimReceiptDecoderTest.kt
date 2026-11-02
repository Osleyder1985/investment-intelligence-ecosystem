package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewedDistributionClaimReceiptDecoderTest {
    private val distributor = "0x1111111111111111111111111111111111111111"
    private val beneficiary = "0x2222222222222222222222222222222222222222"
    private val other = "0x3333333333333333333333333333333333333333"
    private val token = "0x4444444444444444444444444444444444444444"
    private val tx = "0x" + "a".repeat(64)
    private val blockHash = "0x" + "b".repeat(64)
    private val claimTopic = "0x" + "c".repeat(64)
    private val transferTopic = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"

    private val mapping = ProtocolEconomicAddressMapping(
        protocolId = "protocol:fixture", assetId = "asset:fixture", chainId = 1,
        address = distributor, tokenContract = token,
        role = ProtocolEconomicAddressRole.DISTRIBUTION_SOURCE,
        validFromEpochMs = 1_000, validUntilEpochMs = null,
        evidenceUrl = "https://example.org/distributor", reviewStatus = "REVIEWED"
    )
    private val schema = ReviewedDistributionClaimEvent(
        chainId = 1, distributorAddress = distributor, tokenContract = token,
        claimEventTopic = claimTopic, beneficiaryTopicIndex = 1, amountDataWordIndex = 0,
        evidenceUrl = "https://example.org/verified-abi", reviewStatus = "REVIEWED"
    )

    private fun word(value: Long) = value.toString(16).padStart(64, '0')
    private fun addressTopic(address: String) = "0x" + address.removePrefix("0x").padStart(64, '0')

    private fun receipt(
        logs: List<EvmReceiptLog> = validLogs(),
        succeeded: Boolean = true,
        crosschecked: Boolean = true
    ) = EvmTransactionReceiptEvidence(
        chainId = 1, transactionHash = tx, transactionTo = distributor,
        blockNumber = 100, blockHash = blockHash, blockTimestampEpochMs = 1_500,
        succeeded = succeeded, rpcCrosschecked = crosschecked, logs = logs, observedAtEpochMs = 2_000
    )

    private fun validLogs(
        claimBeneficiary: String = beneficiary,
        paymentBeneficiary: String = beneficiary,
        claimAmount: Long = 500,
        paymentAmount: Long = 500
    ) = listOf(
        EvmReceiptLog(
            address = distributor,
            topics = listOf(claimTopic, addressTopic(claimBeneficiary)),
            data = "0x" + word(claimAmount),
            transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 4
        ),
        EvmReceiptLog(
            address = token,
            topics = listOf(transferTopic, addressTopic(distributor), addressTopic(paymentBeneficiary)),
            data = "0x" + word(paymentAmount),
            transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 5
        )
    )

    @Test fun matchesReviewedClaimToExactDirectTokenPayment() {
        val result = ReviewedDistributionClaimReceiptDecoder.decode(receipt(), mapping, schema)
        assertTrue(result is DistributionClaimDecodeResult.Candidate)
        val evidence = (result as DistributionClaimDecodeResult.Candidate).evidence
        assertEquals(beneficiary, evidence.beneficiaryAddress)
        assertEquals("500", evidence.rawAmount)
        assertEquals(4L, evidence.claimEventLogIndex)
        assertEquals(5L, evidence.paymentTransferLogIndex)
        assertEquals(
            "RPC_CROSSCHECKED_CLAIM_PAYMENT_PENDING_FINALITY_AND_HOLDER_REVIEW",
            evidence.evidenceStatus
        )
    }

    @Test fun rejectsUnreviewedSchemaWrongRoleAndFailedReceipt() {
        val unreviewed = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(), mapping, schema.copy(reviewStatus = "PENDING")
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(unreviewed.reasonCodes.contains("CLAIM_SCHEMA_NOT_REVIEWED"))

        val wrongRole = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(), mapping.copy(role = ProtocolEconomicAddressRole.TREASURY), schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(wrongRole.reasonCodes.contains("ROLE_IS_NOT_DISTRIBUTION_SOURCE"))

        val failed = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(succeeded = false), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(failed.reasonCodes.contains("TRANSACTION_RECEIPT_FAILED"))
    }

    @Test fun rejectsMissingRpcCrosscheckAndWrongChain() {
        val unchecked = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(crosschecked = false), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(unchecked.reasonCodes.contains("RPC_CROSSCHECK_REQUIRED"))

        val wrongChain = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt().copy(chainId = 10), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(wrongChain.reasonCodes.contains("CHAIN_ID_MISMATCH"))
    }

    @Test fun rejectsRecipientAndAmountMismatch() {
        val recipientMismatch = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(validLogs(paymentBeneficiary = other)), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(recipientMismatch.reasonCodes.contains("CLAIM_BENEFICIARY_MUST_MATCH_PAYMENT_RECIPIENT"))

        val amountMismatch = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(validLogs(paymentAmount = 499)), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(amountMismatch.reasonCodes.contains("CLAIM_AMOUNT_MUST_MATCH_EXACT_ERC20_PAYMENT"))
    }

    @Test fun rejectsMultiplePaymentsAndZeroBeneficiary() {
        val multiplePayments = validLogs() + validLogs().last().copy(logIndex = 6)
        val ambiguous = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(multiplePayments), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(ambiguous.reasonCodes.contains("EXPECTED_EXACTLY_ONE_DISTRIBUTOR_TOKEN_PAYMENT"))

        val zero = "0x0000000000000000000000000000000000000000"
        val zeroRecipient = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(validLogs(claimBeneficiary = zero, paymentBeneficiary = zero)), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(zeroRecipient.reasonCodes.contains("CLAIM_BENEFICIARY_INVALID"))
    }

    @Test fun rejectsExpiredSourceAndSchemaMappings() {
        val expiredSource = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(), mapping.copy(validUntilEpochMs = 1_400), schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(expiredSource.reasonCodes.contains("DISTRIBUTION_SOURCE_MAPPING_NOT_ACTIVE_AT_BLOCK_TIME"))

        val expiredSchema = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(), mapping, schema.copy(validUntilEpochMs = 1_400)
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(expiredSchema.reasonCodes.contains("CLAIM_SCHEMA_NOT_ACTIVE_AT_BLOCK_TIME"))
    }

    @Test fun rejectsSchemaForDifferentDistributorAndInconsistentReceiptLogs() {
        val wrongDistributor = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(), mapping, schema.copy(distributorAddress = other)
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(wrongDistributor.reasonCodes.contains("CLAIM_SCHEMA_DOES_NOT_MATCH_REVIEWED_SOURCE_MAPPING"))

        val inconsistentLog = validLogs().toMutableList()
        inconsistentLog[1] = inconsistentLog[1].copy(blockHash = "0x" + "d".repeat(64))
        val inconsistent = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(inconsistentLog), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(inconsistent.reasonCodes.contains("RECEIPT_LOG_BLOCK_OR_TRANSACTION_MISMATCH"))
    }


    @Test fun rejectsNonCanonicalTransferLogShapeAndDuplicateReceiptIndexes() {
        val extraTopicLogs = validLogs().mapIndexed { index, log ->
            if (index == 1) log.copy(topics = log.topics + word(1)) else log
        }
        val extraTopic = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(extraTopicLogs), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(extraTopic.reasonCodes.contains("ERC20_TRANSFER_TOPIC_COUNT_MISMATCH"))

        val extraDataLogs = validLogs().mapIndexed { index, log ->
            if (index == 1) log.copy(data = log.data + word(1)) else log
        }
        val extraData = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(extraDataLogs), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(extraData.reasonCodes.contains("ERC20_TRANSFER_DATA_WORD_COUNT_MISMATCH"))

        val duplicateIndexLogs = validLogs().mapIndexed { index, log ->
            if (index == 1) log.copy(logIndex = 4) else log
        }
        val duplicateIndex = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(duplicateIndexLogs), mapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(duplicateIndex.reasonCodes.contains("RECEIPT_LOG_INDEX_DUPLICATE"))
    }



    @Test fun decodesUniswapMerkleDistributorNonIndexedAbiLayout() {
        val uniswapSchema = UniswapUniMerkleDistribution.claimEventSchema()
        val uniswapMapping = mapping.copy(
            protocolId = UniswapUniMerkleDistribution.PROTOCOL_ID,
            address = UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS,
            tokenContract = UniswapUniMerkleDistribution.UNI_TOKEN_ADDRESS,
            evidenceUrl = UniswapUniMerkleDistribution.DISTRIBUTOR_ABI_EVIDENCE_URL
        )
        val uniswapReceipt = receipt(
            logs = listOf(
                EvmReceiptLog(
                    address = UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS,
                    topics = listOf(UniswapUniMerkleDistribution.CLAIMED_EVENT_TOPIC),
                    data = "0x" + word(7) +
                        beneficiary.removePrefix("0x").padStart(64, '0') +
                        word(500),
                    transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 4
                ),
                EvmReceiptLog(
                    address = UniswapUniMerkleDistribution.UNI_TOKEN_ADDRESS,
                    topics = listOf(
                        transferTopic,
                        addressTopic(UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS),
                        addressTopic(beneficiary)
                    ),
                    data = "0x" + word(500),
                    transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 5
                )
            )
        ).copy(
            transactionTo = UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS
        )

        val result = ReviewedDistributionClaimReceiptDecoder.decode(
            uniswapReceipt, uniswapMapping, uniswapSchema
        )
        assertTrue(result is DistributionClaimDecodeResult.Candidate)
        val evidence = (result as DistributionClaimDecodeResult.Candidate).evidence
        assertEquals(beneficiary, evidence.beneficiaryAddress)
        assertEquals("500", evidence.rawAmount)
        assertEquals(UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS, evidence.distributorAddress)
        assertEquals(UniswapUniMerkleDistribution.UNI_TOKEN_ADDRESS, evidence.tokenContract)
    }

    @Test fun rejectsUniswapClaimWithMalformedNonIndexedAddressOrWrongDataLength() {
        val schema = UniswapUniMerkleDistribution.claimEventSchema()
        val uniswapMapping = mapping.copy(
            protocolId = UniswapUniMerkleDistribution.PROTOCOL_ID,
            address = UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS,
            tokenContract = UniswapUniMerkleDistribution.UNI_TOKEN_ADDRESS,
            evidenceUrl = UniswapUniMerkleDistribution.DISTRIBUTOR_ABI_EVIDENCE_URL
        )
        val malformedAddress = "f".repeat(24) + beneficiary.removePrefix("0x")
        val malformedLogs = listOf(
            EvmReceiptLog(
                address = UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS,
                topics = listOf(UniswapUniMerkleDistribution.CLAIMED_EVENT_TOPIC),
                data = "0x" + word(7) + malformedAddress + word(500),
                transactionHash = tx, blockNumber = 100, blockHash = blockHash, logIndex = 4
            )
        )
        val result = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(logs = malformedLogs).copy(transactionTo = UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS),
            uniswapMapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(result.reasonCodes.contains("CLAIM_BENEFICIARY_INVALID"))

        val shortLogs = malformedLogs.map { it.copy(data = "0x" + word(7) + word(500)) }
        val shortResult = ReviewedDistributionClaimReceiptDecoder.decode(
            receipt(logs = shortLogs).copy(transactionTo = UniswapUniMerkleDistribution.DISTRIBUTOR_ADDRESS),
            uniswapMapping, schema
        ) as DistributionClaimDecodeResult.Abstained
        assertTrue(shortResult.reasonCodes.contains("CLAIM_EVENT_DATA_WORD_COUNT_MISMATCH"))
    }

}
