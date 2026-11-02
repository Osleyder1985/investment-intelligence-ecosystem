package com.iie.android.domain.valuation

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun interface EthereumRpcTransport {
    fun post(endpoint: String, body: String): String
}

class HttpEthereumRpcTransport(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000
) : EthereumRpcTransport {
    override fun post(endpoint: String, body: String): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "InvestmentIntelligenceEcosystem/0.1")
        }
        try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw IllegalStateException("ETHEREUM_RPC_HTTP_$status")
            return response
        } finally {
            connection.disconnect()
        }
    }
}

sealed class RpcBurnCrosscheckResult {
    data class Confirmed(
        val event: VerifiedErc20BurnEvent,
        val canonicalBlockHash: String,
        val rpcEndpoint: String,
        val checkedAtEpochMs: Long
    ) : RpcBurnCrosscheckResult()

    data class Abstained(val reasonCode: String) : RpcBurnCrosscheckResult()
}

/** Independent Ethereum-mainnet RPC check; unsupported chains are explicitly refused. */
class EthereumRpcBurnCrossChecker(
    private val transport: EthereumRpcTransport,
    private val clock: () -> Long = System::currentTimeMillis
) {
    fun crosscheck(event: VerifiedErc20BurnEvent): RpcBurnCrosscheckResult {
        if (event.chainId != ETHEREUM_MAINNET_CHAIN_ID) {
            return RpcBurnCrosscheckResult.Abstained("RPC_ENDPOINT_NOT_CONFIGURED_FOR_CHAIN")
        }
        if (!event.tokenContract.matches(Regex("0x[a-fA-F0-9]{40}")) ||
            !event.transactionHash.matches(Regex("0x[a-fA-F0-9]{64}")) ||
            event.blockNumber < 0L || event.logIndex < 0L ||
            event.rawAmount <= java.math.BigInteger.ZERO
        ) return RpcBurnCrosscheckResult.Abstained("EVENT_FIELDS_INVALID")

        val endpoint = ETHEREUM_MAINNET_RPC
        return try {
            val filter = buildJsonObject {
                put("address", event.tokenContract)
                put("fromBlock", "0x" + event.blockNumber.toString(16))
                put("toBlock", "0x" + event.blockNumber.toString(16))
                put("topics", kotlinx.serialization.json.buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive(RoutescanErc20BurnEvidenceSource.TRANSFER_TOPIC))
                    add(kotlinx.serialization.json.JsonNull)
                    add(kotlinx.serialization.json.JsonPrimitive(RoutescanErc20BurnEvidenceSource.ZERO_TOPIC))
                })
            }
            val logsRequest = rpcRequest("eth_getLogs", kotlinx.serialization.json.buildJsonArray { add(filter) })
            val logsResponse = Json.parseToJsonElement(transport.post(endpoint, logsRequest)).jsonObject
            if (logsResponse["error"] != null) return RpcBurnCrosscheckResult.Abstained("RPC_LOG_QUERY_ERROR")
            val logs = logsResponse["result"] as? kotlinx.serialization.json.JsonArray
                ?: return RpcBurnCrosscheckResult.Abstained("RPC_LOG_RESULT_MISSING")
            val matches = logs.map { it.jsonObject }.filter { row ->
                row["transactionHash"]?.jsonPrimitive?.content.equals(event.transactionHash, ignoreCase = true) &&
                    parseHex(row["logIndex"]?.jsonPrimitive?.content) == event.logIndex &&
                    row["address"]?.jsonPrimitive?.content.equals(event.tokenContract, ignoreCase = true)
            }
            if (matches.size != 1) return RpcBurnCrosscheckResult.Abstained("ROUTESCAN_EVENT_NOT_FOUND_OR_AMBIGUOUS_IN_RPC")
            val matching = matches.single()
            val topics = matching["topics"] as? kotlinx.serialization.json.JsonArray
                ?: return RpcBurnCrosscheckResult.Abstained("RPC_EVENT_TOPICS_MISSING")
            if (topics.size < 3 ||
                !topics[0].jsonPrimitive.content.equals(RoutescanErc20BurnEvidenceSource.TRANSFER_TOPIC, true) ||
                !topics[2].jsonPrimitive.content.equals(RoutescanErc20BurnEvidenceSource.ZERO_TOPIC, true)
            ) return RpcBurnCrosscheckResult.Abstained("RPC_EVENT_TOPIC_MISMATCH")
            val rpcRaw = matching["data"]?.jsonPrimitive?.content?.removePrefix("0x")
                ?.takeIf { it.isNotBlank() && it.matches(Regex("[a-fA-F0-9]+")) }
                ?.let { java.math.BigInteger(it, 16) }
                ?: return RpcBurnCrosscheckResult.Abstained("RPC_EVENT_AMOUNT_INVALID")
            if (rpcRaw != event.rawAmount) return RpcBurnCrosscheckResult.Abstained("RPC_EVENT_AMOUNT_MISMATCH")
            val blockHash = matching["blockHash"]?.jsonPrimitive?.content
                ?.takeIf { it.matches(Regex("0x[a-fA-F0-9]{64}")) }
                ?: return RpcBurnCrosscheckResult.Abstained("RPC_BLOCK_HASH_MISSING")
            val blockRequest = rpcRequest(
                "eth_getBlockByNumber",
                kotlinx.serialization.json.buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive("0x" + event.blockNumber.toString(16)))
                    add(kotlinx.serialization.json.JsonPrimitive(false))
                }
            )
            val blockResponse = Json.parseToJsonElement(transport.post(endpoint, blockRequest)).jsonObject
            if (blockResponse["error"] != null) return RpcBurnCrosscheckResult.Abstained("RPC_BLOCK_QUERY_ERROR")
            val block = blockResponse["result"]?.jsonObject
                ?: return RpcBurnCrosscheckResult.Abstained("RPC_BLOCK_NOT_FOUND")
            val canonicalHash = block["hash"]?.jsonPrimitive?.content
                ?: return RpcBurnCrosscheckResult.Abstained("RPC_CANONICAL_BLOCK_HASH_MISSING")
            if (!canonicalHash.equals(blockHash, ignoreCase = true)) {
                return RpcBurnCrosscheckResult.Abstained("RPC_BLOCK_HASH_MISMATCH_REORG_OR_INCONSISTENT_INDEX")
            }
            val timestamp = parseHex(block["timestamp"]?.jsonPrimitive?.content)
                ?: return RpcBurnCrosscheckResult.Abstained("RPC_BLOCK_TIMESTAMP_INVALID")
            if (timestamp * 1000L != event.observedAtEpochMs) {
                return RpcBurnCrosscheckResult.Abstained("RPC_BLOCK_TIMESTAMP_MISMATCH")
            }
            val checkedAt = clock()
            if (checkedAt <= 0L) return RpcBurnCrosscheckResult.Abstained("CHECK_TIMESTAMP_INVALID")
            RpcBurnCrosscheckResult.Confirmed(
                event = event.copy(
                    evidenceStatus = "RPC_CROSSCHECKED_ETHEREUM_MAINNET_PENDING_FINALITY",
                    rpcBlockHash = canonicalHash,
                    rpcEndpoint = endpoint,
                    rpcCheckedAtEpochMs = checkedAt
                ),
                canonicalBlockHash = canonicalHash,
                rpcEndpoint = endpoint,
                checkedAtEpochMs = checkedAt
            )
        } catch (error: Exception) {
            RpcBurnCrosscheckResult.Abstained("RPC_CROSSCHECK_FAILED:${error.javaClass.simpleName}")
        }
    }

    private fun rpcRequest(method: String, params: kotlinx.serialization.json.JsonArray): String =
        buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", method)
            put("params", params)
        }.toString()

    private fun parseHex(value: String?): Long? =
        value?.takeIf { it.startsWith("0x") }?.drop(2)?.toLongOrNull(16)

    companion object {
        const val ETHEREUM_MAINNET_CHAIN_ID = 1L
        const val ETHEREUM_MAINNET_RPC = "https://cloudflare-eth.com"
    }
}
