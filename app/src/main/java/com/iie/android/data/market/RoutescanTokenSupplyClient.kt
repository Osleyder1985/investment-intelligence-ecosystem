package com.iie.android.data.market

import com.iie.android.domain.model.ExactTokenAmount
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

/**
 * Reads ERC-20 totalSupply() and decimals() at one pinned EVM block.
 *
 * The caller must supply a previously verified chain ID and contract address.
 * No symbol-based contract discovery is performed here.
 */
data class RoutescanTokenSupplyObservation(
    val chainId: String,
    val contractAddress: String,
    val amount: ExactTokenAmount,
    val blockNumber: Long,
    val retrievedAtEpochMs: Long
)

class RoutescanTokenSupplyClient(
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) {
    fun fetch(
        chainId: String,
        contractAddress: String,
        apiKey: String? = null
    ): RoutescanTokenSupplyObservation {
        require(chainId.matches(Regex("[1-9][0-9]*"))) { "Invalid EVM chain ID" }
        require(contractAddress.matches(Regex("0x[0-9a-fA-F]{40}"))) {
            "Invalid EVM contract address"
        }
        val retrievedAt = nowEpochMs()
        require(retrievedAt > 0L) { "Invalid retrieval clock" }

        val base = "https://api.routescan.io/v2/network/mainnet/evm/$chainId/etherscan/api"
        val blockHex = requestResult(base, "module=proxy&action=eth_blockNumber", apiKey)
        val blockNumber = parseHexQuantity(blockHex, "block number")
        require(blockNumber <= BigInteger.valueOf(Long.MAX_VALUE)) { "Block number exceeds supported range" }
        val blockTag = "0x" + blockNumber.toString(16)

        // ERC-20 ABI selectors: totalSupply() = 0x18160ddd, decimals() = 0x313ce567.
        val rawSupply = parseHexQuantity(
            requestResult(
                base,
                "module=proxy&action=eth_call&to=$contractAddress&data=0x18160ddd&tag=$blockTag",
                apiKey
            ),
            "totalSupply"
        )
        val decimalsValue = parseHexQuantity(
            requestResult(
                base,
                "module=proxy&action=eth_call&to=$contractAddress&data=0x313ce567&tag=$blockTag",
                apiKey
            ),
            "decimals"
        )
        require(decimalsValue <= BigInteger.valueOf(255L)) { "Token decimals exceed uint8 range" }

        return RoutescanTokenSupplyObservation(
            chainId = chainId,
            contractAddress = contractAddress.lowercase(),
            amount = ExactTokenAmount(rawSupply.toString(), decimalsValue.toInt()),
            blockNumber = blockNumber.toLong(),
            retrievedAtEpochMs = retrievedAt
        )
    }

    private fun requestResult(base: String, query: String, apiKey: String?): String {
        val connection = (URL("$base?$query").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            if (!apiKey.isNullOrBlank()) setRequestProperty("apikey", apiKey)
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw java.io.IOException("Routescan HTTP $status")
            val json = JSONObject(body)
            if (json.has("error") && !json.isNull("error")) {
                throw java.io.IOException("Routescan RPC returned an error")
            }
            val result = json.optString("result")
            if (result.isBlank() || result == "null") {
                throw java.io.IOException("Routescan response omitted result")
            }
            return result
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        internal fun parseHexQuantity(value: String, field: String): BigInteger {
            if (!value.matches(Regex("0x[0-9a-fA-F]{1,64}"))) {
                throw java.io.IOException("Routescan returned invalid $field")
            }
            return try {
                BigInteger(value.substring(2), 16)
            } catch (error: NumberFormatException) {
                throw java.io.IOException("Routescan returned invalid $field", error)
            }
        }
    }
}
