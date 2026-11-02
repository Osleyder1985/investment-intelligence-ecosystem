package com.iie.android.domain.valuation

/**
 * Reviewed protocol-role metadata. A mapping identifies an address role only;
 * it never proves that a transfer was a distribution, repurchase or holder payment.
 */
enum class ProtocolEconomicAddressRole {
    DISTRIBUTION_SOURCE,
    BUYBACK_EXECUTOR,
    TREASURY,
    REVENUE_COLLECTOR
}

data class ProtocolEconomicAddressMapping(
    val protocolId: String,
    val assetId: String,
    val chainId: Long,
    val address: String,
    val role: ProtocolEconomicAddressRole,
    val validFromEpochMs: Long,
    val validUntilEpochMs: Long?,
    val evidenceUrl: String,
    val reviewStatus: String
) {
    fun validationErrors(): List<String> = buildList {
        if (protocolId.isBlank()) add("PROTOCOL_ID_MISSING")
        if (assetId.isBlank()) add("ASSET_ID_MISSING")
        if (chainId <= 0L) add("CHAIN_ID_INVALID")
        if (!address.matches(Regex("0x[a-fA-F0-9]{40}"))) add("ROLE_ADDRESS_INVALID")
        if (validFromEpochMs <= 0L || (validUntilEpochMs != null && validUntilEpochMs < validFromEpochMs)) {
            add("ROLE_VALIDITY_WINDOW_INVALID")
        }
        if (!evidenceUrl.startsWith("https://")) add("ROLE_EVIDENCE_URL_MUST_BE_HTTPS")
        if (reviewStatus != REVIEWED_STATUS) add("ROLE_MAPPING_NOT_REVIEWED")
    }

    companion object {
        const val REVIEWED_STATUS = "REVIEWED"
    }
}

data class ProtocolTransferForClassification(
    val assetId: String,
    val chainId: Long,
    val tokenContract: String,
    val fromAddress: String,
    val toAddress: String,
    val rawAmount: String,
    val observedAtEpochMs: Long,
    val transactionHash: String,
    val logIndex: Long
)

enum class ProtocolEconomicCandidateType {
    DISTRIBUTION_FLOW_CANDIDATE,
    BUYBACK_FUNDING_FLOW_CANDIDATE,
    TREASURY_MOVEMENT_CANDIDATE
}

data class ProtocolEconomicEventCandidate(
    val type: ProtocolEconomicCandidateType,
    val transactionHash: String,
    val logIndex: Long,
    val matchedRoleAddress: String,
    val evidenceStatus: String = "CANDIDATE_UNVERIFIED"
)

/**
 * Conservative classifier: only reviewed role mappings are considered.
 * It labels transfer flows as candidates and never claims a completed buyback,
 * holder distribution, revenue or value accrual.
 */
object ProtocolEconomicTransferClassifier {
    fun classify(
        transfer: ProtocolTransferForClassification,
        mappings: List<ProtocolEconomicAddressMapping>
    ): List<ProtocolEconomicEventCandidate> {
        if (transfer.assetId.isBlank() || transfer.chainId <= 0L ||
            !transfer.tokenContract.matches(Regex("0x[a-fA-F0-9]{40}")) ||
            !transfer.fromAddress.matches(Regex("0x[a-fA-F0-9]{40}")) ||
            !transfer.toAddress.matches(Regex("0x[a-fA-F0-9]{40}")) ||
            transfer.rawAmount.toBigIntegerOrNull()?.let { it.signum() > 0 } != true ||
            transfer.observedAtEpochMs <= 0L ||
            !transfer.transactionHash.matches(Regex("0x[a-fA-F0-9]{64}")) ||
            transfer.logIndex < 0L
        ) return emptyList()

        val active = mappings.filter { mapping ->
            mapping.validationErrors().isEmpty() &&
                mapping.assetId == transfer.assetId &&
                mapping.chainId == transfer.chainId &&
                transfer.observedAtEpochMs >= mapping.validFromEpochMs &&
                (mapping.validUntilEpochMs == null || transfer.observedAtEpochMs <= mapping.validUntilEpochMs)
        }
        return active.mapNotNull { mapping ->
            val outgoing = mapping.address.equals(transfer.fromAddress, ignoreCase = true)
            val incoming = mapping.address.equals(transfer.toAddress, ignoreCase = true)
            val type = when {
                mapping.role == ProtocolEconomicAddressRole.DISTRIBUTION_SOURCE && outgoing ->
                    ProtocolEconomicCandidateType.DISTRIBUTION_FLOW_CANDIDATE
                mapping.role == ProtocolEconomicAddressRole.BUYBACK_EXECUTOR && incoming ->
                    ProtocolEconomicCandidateType.BUYBACK_FUNDING_FLOW_CANDIDATE
                mapping.role == ProtocolEconomicAddressRole.TREASURY && (outgoing || incoming) ->
                    ProtocolEconomicCandidateType.TREASURY_MOVEMENT_CANDIDATE
                else -> null
            } ?: return@mapNotNull null
            ProtocolEconomicEventCandidate(type, transfer.transactionHash, transfer.logIndex, mapping.address)
        }.distinctBy { "${it.type}:${it.transactionHash}:${it.logIndex}" }
    }
}
