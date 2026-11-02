package com.iie.android.data.repository

import com.iie.android.domain.valuation.DistributionClaimDecodeResult
import com.iie.android.domain.valuation.EthereumRpcTransactionReceiptSource
import com.iie.android.domain.valuation.RpcTransactionReceiptResult
import com.iie.android.domain.valuation.ReviewedDistributionClaimReceiptDecoder
import com.iie.android.domain.valuation.UniswapUniMerkleDistribution

sealed class UniswapDistributionProcessingResult {
    data class Persisted(
        val transactionHash: String,
        val inserted: Int,
        val duplicates: Int
    ) : UniswapDistributionProcessingResult()

    data class Abstained(val reasonCodes: List<String>) : UniswapDistributionProcessingResult()
}

/**
 * End-to-end adapter: fetches a canonical Ethereum receipt over public RPC,
 * applies the reviewed Uniswap MerkleDistributor ABI profile, then persists
 * only a matching Claimed + UNI Transfer pair.
 */
class UniswapUniMerkleDistributionAdapter(
    private val receiptSource: EthereumRpcTransactionReceiptSource,
    private val observationStore: DistributionClaimObservationStore
) {
    suspend fun process(
        transactionHash: String,
        assetId: String,
        distributorDeploymentTimestampEpochMs: Long
    ): UniswapDistributionProcessingResult {
        if (assetId.isBlank() || distributorDeploymentTimestampEpochMs <= 0L) {
            return UniswapDistributionProcessingResult.Abstained(listOf("UNISWAP_DISTRIBUTION_MAPPING_INPUT_INVALID"))
        }
        val receipt = when (val result = receiptSource.fetch(transactionHash)) {
            is RpcTransactionReceiptResult.Confirmed -> result.receipt
            is RpcTransactionReceiptResult.Abstained ->
                return UniswapDistributionProcessingResult.Abstained(listOf(result.reasonCode))
        }
        val mapping = UniswapUniMerkleDistribution.sourceMapping(
            assetId = assetId,
            deploymentTimestampEpochMs = distributorDeploymentTimestampEpochMs
        )
        val schema = UniswapUniMerkleDistribution.claimEventSchema(
            validFromEpochMs = distributorDeploymentTimestampEpochMs
        )
        return when (val decoded = ReviewedDistributionClaimReceiptDecoder.decode(receipt, mapping, schema)) {
            is DistributionClaimDecodeResult.Abstained ->
                UniswapDistributionProcessingResult.Abstained(decoded.reasonCodes)
            is DistributionClaimDecodeResult.Candidate -> when (
                val stored = observationStore.persist(DistributionClaimDecodeResult.Candidate(decoded.evidence))
            ) {
                is DistributionClaimStoreResult.Persisted ->
                    UniswapDistributionProcessingResult.Persisted(
                        transactionHash = transactionHash,
                        inserted = stored.inserted,
                        duplicates = stored.duplicates
                    )
                is DistributionClaimStoreResult.Abstained ->
                    UniswapDistributionProcessingResult.Abstained(stored.blockers)
            }
        }
    }
}
