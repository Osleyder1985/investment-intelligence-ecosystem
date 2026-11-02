package com.iie.android.domain.valuation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

sealed class RpcTransactionReceiptResult {
    data class Confirmed(val receipt: EvmTransactionReceiptEvidence) : RpcTransactionReceiptResult()
    data class Abstained(val reasonCode: String) : RpcTransactionReceiptResult()
}

/**
 * Fetches a complete transaction receipt and canonical block from public Ethereum
 * JSON-RPC. No explorer API key is required; RPC evidence is cross-checked before
 * any protocol decoder may consume it.
 */
class EthereumRpcTransactionReceiptSource(
    private val transport: EthereumRpcTransport,
    private val clock: () -> Long = System::currentTimeMillis,
    private val endpoint: String = EthereumRpcBurnCrossChecker.ETHEREUM_MAINNET_RPC,
    private val secondaryEndpoint: String = "https://ethereum-rpc.publicnode.com"
) {
    /** Two independently configured RPC operators must agree before evidence is crosschecked. */
    fun fetch(transactionHash: String): RpcTransactionReceiptResult {
        if (endpoint.isBlank() || secondaryEndpoint.isBlank() || endpoint == secondaryEndpoint) return RpcTransactionReceiptResult.Abstained("RPC_INDEPENDENT_ENDPOINTS_REQUIRED")
        val primary = fetchFrom(transactionHash, endpoint)
        val secondary = fetchFrom(transactionHash, secondaryEndpoint)
        if (primary is RpcTransactionReceiptResult.Abstained && secondary is RpcTransactionReceiptResult.Abstained && primary.reasonCode == secondary.reasonCode) return primary
        if (primary !is RpcTransactionReceiptResult.Confirmed || secondary !is RpcTransactionReceiptResult.Confirmed) return RpcTransactionReceiptResult.Abstained("RPC_PROVIDER_DISAGREEMENT_OR_UNAVAILABLE")
        val first = primary.receipt.copy(
            observedAtEpochMs = 0L, rpcCrosschecked = false,
            finalityStatus = "OBSERVED", safeBlockNumber = null, finalizedBlockNumber = null
        )
        val second = secondary.receipt.copy(
            observedAtEpochMs = 0L, rpcCrosschecked = false,
            finalityStatus = "OBSERVED", safeBlockNumber = null, finalizedBlockNumber = null
        )
        if (first != second) return RpcTransactionReceiptResult.Abstained("RPC_PROVIDER_EVIDENCE_MISMATCH")
        val finalized = listOfNotNull(
            primary.receipt.finalizedBlockNumber, secondary.receipt.finalizedBlockNumber
        ).takeIf { it.size == 2 }?.minOrNull()
        val safe = listOfNotNull(
            primary.receipt.safeBlockNumber, secondary.receipt.safeBlockNumber
        ).takeIf { it.size == 2 }?.minOrNull()
        val finality = when {
            finalized != null && finalized >= primary.receipt.blockNumber -> "FINALIZED"
            safe != null && safe >= primary.receipt.blockNumber -> "SAFE"
            primary.receipt.finalizedBlockNumber == null || secondary.receipt.finalizedBlockNumber == null ||
                primary.receipt.safeBlockNumber == null || secondary.receipt.safeBlockNumber == null -> "FINALITY_UNSUPPORTED"
            else -> "CONFIRMED"
        }
        return RpcTransactionReceiptResult.Confirmed(
            primary.receipt.copy(
                rpcCrosschecked = true,
                finalityStatus = finality,
                safeBlockNumber = safe,
                finalizedBlockNumber = finalized
            )
        )
    }

    private fun fetchFrom(transactionHash: String, endpointUrl: String): RpcTransactionReceiptResult {
        if (!transactionHash.matches(HASH)) return RpcTransactionReceiptResult.Abstained("TRANSACTION_HASH_INVALID")
        return try {
            val chainId = rpc(endpointUrl, "eth_chainId", buildJsonArray { }).resultHexLong()
                ?: return RpcTransactionReceiptResult.Abstained("RPC_CHAIN_ID_MISSING")
            if (chainId != EthereumRpcBurnCrossChecker.ETHEREUM_MAINNET_CHAIN_ID) {
                return RpcTransactionReceiptResult.Abstained("RPC_CHAIN_NOT_ETHEREUM_MAINNET")
            }

            val transaction = rpc(
                endpointUrl, "eth_getTransactionByHash",
                buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive(transactionHash)) }
            ).resultObjectOrNull() ?: return RpcTransactionReceiptResult.Abstained("RPC_TRANSACTION_NOT_FOUND")
            val receipt = rpc(
                endpointUrl, "eth_getTransactionReceipt",
                buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive(transactionHash)) }
            ).resultObjectOrNull() ?: return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_NOT_FOUND")

            val txHash = transaction.string("hash")
                ?: return RpcTransactionReceiptResult.Abstained("RPC_TRANSACTION_HASH_MISSING")
            val receiptHash = receipt.string("transactionHash")
                ?: return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_HASH_MISSING")
            if (!txHash.equals(transactionHash, true) || !receiptHash.equals(transactionHash, true)) {
                return RpcTransactionReceiptResult.Abstained("RPC_TRANSACTION_HASH_MISMATCH")
            }
            val transactionTo = transaction.string("to")
                ?: return RpcTransactionReceiptResult.Abstained("RPC_TRANSACTION_TARGET_MISSING")
            val receiptTo = receipt.string("to")
            if (receiptTo != null && !receiptTo.equals(transactionTo, true)) {
                return RpcTransactionReceiptResult.Abstained("RPC_TRANSACTION_TARGET_MISMATCH")
            }
            val blockNumber = receipt.hexLong("blockNumber")
                ?: return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_BLOCK_NUMBER_INVALID")
            val blockHash = receipt.string("blockHash")
                ?.takeIf { it.matches(HASH) }
                ?: return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_BLOCK_HASH_INVALID")
            val txBlockNumber = transaction.hexLong("blockNumber")
            val txBlockHash = transaction.string("blockHash")
            if (txBlockNumber != blockNumber || txBlockHash?.equals(blockHash, true) != true) {
                return RpcTransactionReceiptResult.Abstained("RPC_TRANSACTION_RECEIPT_BLOCK_MISMATCH")
            }

            val block = rpc(
                endpointUrl, "eth_getBlockByNumber",
                buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive("0x" + blockNumber.toString(16)))
                    add(kotlinx.serialization.json.JsonPrimitive(false))
                }
            ).resultObjectOrNull() ?: return RpcTransactionReceiptResult.Abstained("RPC_CANONICAL_BLOCK_NOT_FOUND")
            val canonicalHash = block.string("hash")
                ?.takeIf { it.matches(HASH) }
                ?: return RpcTransactionReceiptResult.Abstained("RPC_CANONICAL_BLOCK_HASH_MISSING")
            if (!canonicalHash.equals(blockHash, true)) {
                return RpcTransactionReceiptResult.Abstained("RPC_BLOCK_REORGED_OR_PROVIDER_INCONSISTENT")
            }
            val finalizedBlockNumber = readBlockTagNumber(endpointUrl, "finalized")
            val safeBlockNumber = readBlockTagNumber(endpointUrl, "safe")
            val finalityStatus = when {
                finalizedBlockNumber != null && finalizedBlockNumber >= blockNumber -> "FINALIZED"
                safeBlockNumber != null && safeBlockNumber >= blockNumber -> "SAFE"
                finalizedBlockNumber == null && safeBlockNumber == null -> "FINALITY_UNSUPPORTED"
                else -> "CONFIRMED"
            }
            val timestampSeconds = block.hexLong("timestamp")
                ?: return RpcTransactionReceiptResult.Abstained("RPC_BLOCK_TIMESTAMP_INVALID")
            val blockTimestampMs = timestampSeconds * 1000L
            val observedAt = clock()
            if (observedAt <= 0L || blockTimestampMs <= 0L || blockTimestampMs > observedAt) {
                return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_TIMESTAMP_INVALID")
            }

            val logsJson = receipt["logs"] as? JsonArray
                ?: return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_LOGS_MISSING")
            val logs = logsJson.map { item ->
                val row = item as? JsonObject
                    ?: return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_LOG_INVALID")
                val address = row.string("address")
                    ?.takeIf { it.matches(ADDRESS) }
                    ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_ADDRESS_INVALID")
                val topics = (row["topics"] as? JsonArray)?.map { topic ->
                    topic.jsonPrimitive.contentOrNull
                        ?.takeIf { it.matches(HASH) }
                        ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_TOPIC_INVALID")
                } ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_TOPICS_MISSING")
                val data = row.string("data")
                    ?.takeIf { it.startsWith("0x") && it.drop(2).matches(Regex("[a-fA-F0-9]*")) }
                    ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_DATA_INVALID")
                val logTx = row.string("transactionHash")
                    ?.takeIf { it.equals(transactionHash, true) }
                    ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_TRANSACTION_HASH_MISMATCH")
                val logBlock = row.hexLong("blockNumber")
                    ?.takeIf { it == blockNumber }
                    ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_BLOCK_NUMBER_MISMATCH")
                val logBlockHash = row.string("blockHash")
                    ?.takeIf { it.equals(blockHash, true) }
                    ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_BLOCK_HASH_MISMATCH")
                val logIndex = row.hexLong("logIndex")
                    ?: return RpcTransactionReceiptResult.Abstained("RPC_LOG_INDEX_INVALID")
                EvmReceiptLog(address, topics, data, logTx, logBlock, logBlockHash, logIndex)
            }

            val status = receipt.string("status")
                ?: return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_STATUS_MISSING")
            if (!status.equals("0x1", true) && !status.equals("0x0", true)) {
                return RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_STATUS_INVALID")
            }
            RpcTransactionReceiptResult.Confirmed(
                EvmTransactionReceiptEvidence(
                    chainId = chainId,
                    transactionHash = transactionHash,
                    transactionTo = transactionTo,
                    blockNumber = blockNumber,
                    blockHash = canonicalHash,
                    blockTimestampEpochMs = blockTimestampMs,
                    succeeded = status.equals("0x1", true),
                    rpcCrosschecked = false,
                    logs = logs,
                    observedAtEpochMs = observedAt,
                    finalityStatus = finalityStatus,
                    safeBlockNumber = safeBlockNumber,
                    finalizedBlockNumber = finalizedBlockNumber
                )
            )
        } catch (error: Exception) {
            RpcTransactionReceiptResult.Abstained("RPC_RECEIPT_FETCH_FAILED:${error.javaClass.simpleName}")
        }
    }

    private fun readBlockTagNumber(endpointUrl: String, tag: String): Long? = try {
        rpc(endpointUrl, "eth_getBlockByNumber", buildJsonArray {
            add(kotlinx.serialization.json.JsonPrimitive(tag))
            add(kotlinx.serialization.json.JsonPrimitive(false))
        }).resultObjectOrNull()?.hexLong("number")
    } catch (_: Exception) {
        null
    }

    private fun rpc(endpointUrl: String, method: String, params: JsonArray): JsonObject {
        val request = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", method)
            put("params", params)
        }.toString()
        val response = Json.parseToJsonElement(transport.post(endpointUrl, request)).jsonObject
        if (response["error"] != null) throw IllegalStateException("RPC_${method}_ERROR")
        return response
    }

    private fun JsonObject.resultObjectOrNull(): JsonObject? =
        (this["result"]?.takeUnless { it == JsonNull } as? JsonObject)

    private fun JsonObject.resultHexLong(): Long? =
        (this["result"]?.jsonPrimitive?.contentOrNull).toHexLong()

    private fun JsonObject.string(key: String): String? =
        this[key]?.takeUnless { it == JsonNull }?.jsonPrimitive?.contentOrNull

    private fun JsonObject.hexLong(key: String): Long? = string(key).toHexLong()

    private fun String?.toHexLong(): Long? =
        this?.takeIf { it.startsWith("0x") && it.length > 2 }?.drop(2)?.toLongOrNull(16)

    companion object {
        private val HASH = Regex("0x[a-fA-F0-9]{64}")
        private val ADDRESS = Regex("0x[a-fA-F0-9]{40}")
    }
}
