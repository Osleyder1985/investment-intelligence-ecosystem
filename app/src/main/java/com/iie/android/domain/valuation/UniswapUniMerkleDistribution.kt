package com.iie.android.domain.valuation

/**
 * Reviewed ABI/deployment profile for Uniswap's historical UNI Merkle airdrop
 * on Ethereum mainnet. The deployment start time must be supplied from the
 * verified creation transaction/block; it is intentionally not guessed here.
 */
object UniswapUniMerkleDistribution {
    const val PROTOCOL_ID = "protocol:uniswap"
    const val CHAIN_ID = 1L
    const val DISTRIBUTOR_ADDRESS = "0x090D4613473dEE047c3f2706764f49E0821D256e"
    const val UNI_TOKEN_ADDRESS = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984"

    // keccak256("Claimed(uint256,address,uint256)")
    const val CLAIMED_EVENT_TOPIC =
        "0x4ec90e965519d92681267467f775ada5bd214aa92c0dc93d90a5e880ce9ed026"

    // claim(uint256,address,uint256,bytes32[]) selector from the verified ABI.
    const val CLAIM_FUNCTION_SELECTOR = "0x2e7ba6ef"

    const val OFFICIAL_TECHNICAL_REFERENCE =
        "https://developers.uniswap.org/docs/ecosystem/governance/technical-reference"
    const val DISTRIBUTOR_ABI_EVIDENCE_URL =
        "https://etherscan.io/address/0x090d4613473dee047c3f2706764f49e0821d256e#code"

    /**
     * Claimed event ABI has all three arguments non-indexed:
     * data[0] = index, data[1] = account, data[2] = amount; topics contains topic0 only.
     */
    fun claimEventSchema(
        validFromEpochMs: Long = 1L,
        validUntilEpochMs: Long? = null
    ) = ReviewedDistributionClaimEvent(
        chainId = CHAIN_ID,
        distributorAddress = DISTRIBUTOR_ADDRESS,
        tokenContract = UNI_TOKEN_ADDRESS,
        claimEventTopic = CLAIMED_EVENT_TOPIC,
        beneficiaryTopicIndex = 0,
        amountDataWordIndex = 2,
        evidenceUrl = DISTRIBUTOR_ABI_EVIDENCE_URL,
        reviewStatus = ReviewedDistributionClaimEvent.REVIEWED,
        validFromEpochMs = validFromEpochMs,
        validUntilEpochMs = validUntilEpochMs,
        beneficiaryDataWordIndex = 1,
        expectedDataWordCount = 3,
        expectedTopicCount = 1
    )

    /**
     * The deployment timestamp is deliberately a required input sourced from
     * the canonical creation transaction, rather than an inferred date.
     */
    fun sourceMapping(
        assetId: String,
        deploymentTimestampEpochMs: Long,
        validUntilEpochMs: Long? = null
    ) = ProtocolEconomicAddressMapping(
        protocolId = PROTOCOL_ID,
        assetId = assetId,
        chainId = CHAIN_ID,
        address = DISTRIBUTOR_ADDRESS,
        tokenContract = UNI_TOKEN_ADDRESS,
        role = ProtocolEconomicAddressRole.DISTRIBUTION_SOURCE,
        validFromEpochMs = deploymentTimestampEpochMs,
        validUntilEpochMs = validUntilEpochMs,
        evidenceUrl = OFFICIAL_TECHNICAL_REFERENCE,
        reviewStatus = ProtocolEconomicAddressMapping.REVIEWED_STATUS
    )
}
