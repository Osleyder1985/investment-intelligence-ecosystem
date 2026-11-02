package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolEconomicTransferClassifierTest {
    private val distributor = "0x1111111111111111111111111111111111111111"
    private val holder = "0x2222222222222222222222222222222222222222"
    private val token = "0x3333333333333333333333333333333333333333"
    private val tx = "0x" + "a".repeat(64)

    private fun mapping(
        role: ProtocolEconomicAddressRole = ProtocolEconomicAddressRole.DISTRIBUTION_SOURCE,
        status: String = "REVIEWED"
    ) = ProtocolEconomicAddressMapping(
        protocolId = "protocol:test",
        assetId = "asset:test",
        chainId = 1,
        address = distributor,
        role = role,
        validFromEpochMs = 1_000,
        validUntilEpochMs = null,
        evidenceUrl = "https://example.org/role-evidence",
        reviewStatus = status
    )

    private fun transfer() = ProtocolTransferForClassification(
        assetId = "asset:test",
        chainId = 1,
        tokenContract = token,
        fromAddress = distributor,
        toAddress = holder,
        rawAmount = "1000000",
        observedAtEpochMs = 2_000,
        transactionHash = tx,
        logIndex = 0
    )

    @Test fun reviewedDistributionSourceProducesOnlyCandidate() {
        val result = ProtocolEconomicTransferClassifier.classify(transfer(), listOf(mapping()))
        assertEquals(1, result.size)
        assertEquals(ProtocolEconomicCandidateType.DISTRIBUTION_FLOW_CANDIDATE, result.single().type)
        assertEquals("CANDIDATE_UNVERIFIED", result.single().evidenceStatus)
    }

    @Test fun unreviewedMappingIsIgnored() {
        assertTrue(ProtocolEconomicTransferClassifier.classify(
            transfer(), listOf(mapping(status = "PENDING"))
        ).isEmpty())
    }

    @Test fun mappingOutsideValidityWindowIsIgnored() {
        val expired = mapping().copy(validUntilEpochMs = 1_500)
        assertTrue(ProtocolEconomicTransferClassifier.classify(transfer(), listOf(expired)).isEmpty())
    }

    @Test fun transferToBuybackExecutorIsFundingCandidateNotCompletedBuyback() {
        val executor = mapping(role = ProtocolEconomicAddressRole.BUYBACK_EXECUTOR)
        val inbound = transfer().copy(fromAddress = holder, toAddress = distributor)
        val result = ProtocolEconomicTransferClassifier.classify(inbound, listOf(executor))
        assertEquals(ProtocolEconomicCandidateType.BUYBACK_FUNDING_FLOW_CANDIDATE, result.single().type)
        assertEquals("CANDIDATE_UNVERIFIED", result.single().evidenceStatus)
    }
}
