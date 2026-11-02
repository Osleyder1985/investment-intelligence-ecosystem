package com.iie.android.domain.valuation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import java.math.BigDecimal
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL

fun interface RoutescanHttpTransport {
    fun get(url: String): String
}

class HttpRoutescanTransport(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000
) : RoutescanHttpTransport {
    override fun get(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "InvestmentIntelligenceEcosystem/0.1")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw IllegalStateException("ROUTESCAN_HTTP_$status")
            return body
        } finally {
            connection.disconnect()
        }
    }
}

data class RoutescanTransferLog(
    val contractAddress: String,
    val transactionHash: String,
    val blockNumber: Long,
    val logIndex: Long,
    val timestampEpochMs: Long,
    val topics: List<String>,
    val data: String
)

data class VerifiedErc20BurnEvent(
    val assetId: String,
    val chainId: Long,
    val tokenContract: String,
    val transactionHash: String,
    val blockNumber: Long,
    val logIndex: Long,
    val observedAtEpochMs: Long,
    val rawAmount: BigInteger,
    val tokenAmount: BigDecimal,
    val tokenDecimals: Int,
    val sourceUrl: String,
    val evidenceStatus: String = "EXPLORER_INDEXED_EVENT_PENDING_RPC_CROSSCHECK",
    val rpcBlockHash: String? = null,
    val rpcEndpoint: String? = null,
    val rpcCheckedAtEpochMs: Long? = null
)

sealed class RoutescanBurnFetchResult {
    data class Ready(val events: List<VerifiedErc20BurnEvent>) : RoutescanBurnFetchResult()
    data class Abstained(val reasonCode: String) : RoutescanBurnFetchResult()
}

/**
 * Fetches ERC-20 Transfer logs for one confirmed deployment, then recognizes only
 * transfers whose indexed recipient is the canonical zero address. A burn-address
 * transfer, fee, buyback or distribution is not inferred from this event.
 */
class RoutescanErc20BurnEvidenceSource(
    private val transport: RoutescanHttpTransport
) {
    fun fetch(
        assetId: String,
        chainId: Long,
        tokenContract: String,
        decimals: Int,
        fromBlock: Long,
        toBlock: Long
    ): RoutescanBurnFetchResult {
        if (assetId.isBlank()) return RoutescanBurnFetchResult.Abstained("ASSET_ID_MISSING")
        if (chainId <= 0L || decimals !in 0..36 || fromBlock < 0L || toBlock < fromBlock) {
            return RoutescanBurnFetchResult.Abstained("QUERY_RANGE_OR_TOKEN_METADATA_INVALID")
        }
        if (!tokenContract.matches(Regex("0x[a-fA-F0-9]{40}"))) {
            return RoutescanBurnFetchResult.Abstained("TOKEN_CONTRACT_INVALID")
        }

        val base = "https://api.routescan.io/v2/network/mainnet/evm/$chainId/etherscan/api"
        val url = "$base?module=logs&action=getLogs&address=$tokenContract&fromBlock=$fromBlock&toBlock=$toBlock&page=1&offset=1000"
        val logs = try {
            parseLogs(transport.get(url))
        } catch (error: Exception) {
            return RoutescanBurnFetchResult.Abstained("ROUTESCAN_LOG_QUERY_FAILED")
        }
        if (logs.size >= MAX_PAGE_SIZE) {
            return RoutescanBurnFetchResult.Abstained("LOG_RANGE_MAY_BE_TRUNCATED_SPLIT_BLOCK_RANGE")
        }

        val events = logs.mapNotNull { log ->
            if (!log.contractAddress.equals(tokenContract, ignoreCase = true)) return@mapNotNull null
            if (log.topics.size < 3 || !log.topics[0].equals(TRANSFER_TOPIC, ignoreCase = true)) return@mapNotNull null
            if (!log.topics[2].equals(ZERO_TOPIC, ignoreCase = true)) return@mapNotNull null
            val rawHex = log.data.removePrefix("0x")
            if (rawHex.isBlank() || !rawHex.matches(Regex("[a-fA-F0-9]+"))) return@mapNotNull null
            val raw = try { BigInteger(rawHex, 16) } catch (_: NumberFormatException) { return@mapNotNull null }
            if (raw <= BigInteger.ZERO || log.timestampEpochMs <= 0L || log.blockNumber < fromBlock || log.blockNumber > toBlock) {
                return@mapNotNull null
            }
            VerifiedErc20BurnEvent(
                assetId = assetId,
                chainId = chainId,
                tokenContract = tokenContract.lowercase(),
                transactionHash = log.transactionHash,
                blockNumber = log.blockNumber,
                logIndex = log.logIndex,
                observedAtEpochMs = log.timestampEpochMs,
                rawAmount = raw,
                tokenAmount = BigDecimal(raw).movePointLeft(decimals),
                tokenDecimals = decimals,
                sourceUrl = url
            )
        }.distinctBy { "${it.transactionHash}:${it.logIndex}" }
        return RoutescanBurnFetchResult.Ready(events)
    }

    companion object {
        const val TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
        const val ZERO_TOPIC = "0x0000000000000000000000000000000000000000000000000000000000000000"
        const val MAX_PAGE_SIZE = 1000

        private fun parseHexOrDecimal(value: String?): Long? {
            if (value.isNullOrBlank()) return null
            return if (value.startsWith("0x", ignoreCase = true)) {
                value.drop(2).toLongOrNull(16)
            } else {
                value.toLongOrNull()
            }
        }

        fun parseLogs(payload: String): List<RoutescanTransferLog> {
            val root = Json.parseToJsonElement(payload).jsonObject
            if (root["status"]?.jsonPrimitive?.content != "1") {
                val message = root["message"]?.jsonPrimitive?.content.orEmpty()
                if (message.contains("no records", ignoreCase = true) ||
                    message.contains("no transactions", ignoreCase = true)) return emptyList()
                val result = root["result"]?.jsonPrimitive?.contentOrNull.orEmpty()
                if (result.contains("no records", ignoreCase = true)) return emptyList()
                throw IllegalStateException("ROUTESCAN_RESPONSE_NOT_SUCCESS")
            }
            val result = root["result"] as? JsonArray ?: return emptyList()
            return result.mapNotNull { item ->
                val row = item.jsonObject
                val topics = row["topics"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                val block = parseHexOrDecimal(row["blockNumber"]?.jsonPrimitive?.contentOrNull)
                    ?: return@mapNotNull null
                val timestamp = parseHexOrDecimal(row["timeStamp"]?.jsonPrimitive?.contentOrNull)
                    ?: return@mapNotNull null
                val logIndex = parseHexOrDecimal(row["logIndex"]?.jsonPrimitive?.contentOrNull)
                    ?: return@mapNotNull null
                RoutescanTransferLog(
                    contractAddress = row["address"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                    transactionHash = row["transactionHash"]?.jsonPrimitive?.content
                        ?.takeIf { it.matches(Regex("0x[a-fA-F0-9]{64}")) } ?: return@mapNotNull null,
                    blockNumber = block,
                    logIndex = logIndex,
                    timestampEpochMs = timestamp * 1000L,
                    topics = topics,
                    data = row["data"]?.jsonPrimitive?.content ?: return@mapNotNull null
                )
            }
        }
    }
}
