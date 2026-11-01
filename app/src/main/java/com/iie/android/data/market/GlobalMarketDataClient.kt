package com.iie.android.data.market

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.contentOrNull

data class GlobalMarketMetrics(
    val marketCapUsd: Double,
    val volume24hUsd: Double,
    val bitcoinDominancePercent: Double,
    val activeCryptocurrencies: Int,
    val marketCapChange24hPercent: Double?,
    val volumeChange24hPercent: Double?,
    val providerUpdatedAtEpochMs: Long,
    val retrievedAtEpochMs: Long
)

data class FearGreedIndex(
    val value: Int,
    val classification: String,
    val providerUpdatedAtEpochMs: Long,
    val retrievedAtEpochMs: Long
)

abstract class BoundedJsonHttpClient(
    private val connectTimeoutMs: Int,
    private val readTimeoutMs: Int,
    private val maxResponseBytes: Int
) {
    protected fun getJson(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "IIE-Android-Personal/0.3")
        }
        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) throw IOException("Market data HTTP $status")
            if (!connection.contentType.orEmpty().lowercase().contains("application/json")) {
                throw IOException("Market data provider returned an unexpected content type")
            }
            val declaredLength = connection.getHeaderFieldLong("Content-Length", -1L)
            if (declaredLength > maxResponseBytes) throw IOException("Market data response exceeds size limit")
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxResponseBytes) throw IOException("Market data response exceeds size limit")
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            return bytes.toString(Charsets.UTF_8)
        } finally {
            connection.disconnect()
        }
    }

    protected fun optionalNumber(root: kotlinx.serialization.json.JsonObject, key: String): Double? {
        val primitive = root[key]?.jsonPrimitive ?: return null
        if (primitive.contentOrNull == null || primitive.content == "null") return null
        val value = primitive.doubleOrNull ?: throw IOException("Invalid numeric field: $key")
        if (!value.isFinite()) throw IOException("Non-finite numeric field: $key")
        return value
    }

    protected fun requiredNumber(root: kotlinx.serialization.json.JsonObject, key: String): Double =
        optionalNumber(root, key)?.takeIf { it >= 0.0 }
            ?: throw IOException("Missing or invalid numeric field: $key")
}

class CoinPaprikaGlobalMarketClient(
    connectTimeoutMs: Int = 8_000,
    readTimeoutMs: Int = 8_000,
    maxResponseBytes: Int = 64 * 1024
) : BoundedJsonHttpClient(connectTimeoutMs, readTimeoutMs, maxResponseBytes) {
    suspend fun fetch(retrievedAtEpochMs: Long = System.currentTimeMillis()): GlobalMarketMetrics =
        withContext(Dispatchers.IO) {
            require(retrievedAtEpochMs > 0L)
            parse(getJson("https://api.coinpaprika.com/v1/global"), retrievedAtEpochMs)
        }

    internal fun parse(payload: String, retrievedAtEpochMs: Long): GlobalMarketMetrics {
        val root = try { Json.parseToJsonElement(payload).jsonObject }
        catch (error: Exception) { throw IOException("Malformed CoinPaprika global JSON", error) }
        val marketCap = requiredNumber(root, "market_cap_usd")
        val volume = requiredNumber(root, "volume_24h_usd")
        val dominance = requiredNumber(root, "bitcoin_dominance_percentage")
        if (marketCap <= 0.0 || dominance !in 0.0..100.0) throw IOException("CoinPaprika global metrics outside accepted bounds")
        val count = root["cryptocurrencies_number"]?.jsonPrimitive?.intOrNull
            ?: throw IOException("CoinPaprika global response is missing cryptocurrencies_number")
        if (count <= 0) throw IOException("Invalid active cryptocurrency count")
        val updatedSeconds = root["last_updated"]?.jsonPrimitive?.longOrNull
            ?: throw IOException("CoinPaprika global response is missing last_updated")
        val updatedAt = updatedSeconds * 1000L
        if (updatedAt <= 0L || updatedAt > retrievedAtEpochMs + 60_000L) {
            throw IOException("CoinPaprika global timestamp is outside accepted bounds")
        }
        val marketCapChange = optionalNumber(root, "market_cap_change_24h")
        val volumeChange = optionalNumber(root, "volume_24h_change_24h")
        return GlobalMarketMetrics(marketCap, volume, dominance, count, marketCapChange, volumeChange, updatedAt, retrievedAtEpochMs)
    }
}

class AlternativeMeFearGreedClient(
    connectTimeoutMs: Int = 8_000,
    readTimeoutMs: Int = 8_000,
    maxResponseBytes: Int = 64 * 1024
) : BoundedJsonHttpClient(connectTimeoutMs, readTimeoutMs, maxResponseBytes) {
    suspend fun fetch(retrievedAtEpochMs: Long = System.currentTimeMillis()): FearGreedIndex =
        withContext(Dispatchers.IO) {
            require(retrievedAtEpochMs > 0L)
            parse(getJson("https://api.alternative.me/fng/?limit=1"), retrievedAtEpochMs)
        }

    internal fun parse(payload: String, retrievedAtEpochMs: Long): FearGreedIndex {
        val root = try { Json.parseToJsonElement(payload).jsonObject }
        catch (error: Exception) { throw IOException("Malformed Alternative.me JSON", error) }
        val metadataError = root["metadata"]?.jsonObject?.get("error")?.jsonPrimitive?.contentOrNull
        if (!metadataError.isNullOrBlank() && metadataError != "null") {
            throw IOException("Alternative.me reported an API error")
        }
        val data = root["data"] as? JsonArray ?: throw IOException("Alternative.me response is missing data")
        val item = data.firstOrNull()?.jsonObject ?: throw IOException("Alternative.me response contains no index")
        val value = item["value"]?.jsonPrimitive?.intOrNull
            ?: throw IOException("Alternative.me response is missing value")
        val classification = item["value_classification"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() } ?: throw IOException("Alternative.me response is missing classification")
        val timestampSeconds = item["timestamp"]?.jsonPrimitive?.longOrNull
            ?: throw IOException("Alternative.me response is missing timestamp")
        val updatedAt = timestampSeconds * 1000L
        if (value !in 0..100 || updatedAt <= 0L || updatedAt > retrievedAtEpochMs + 60_000L) {
            throw IOException("Alternative.me sentiment is outside accepted bounds")
        }
        return FearGreedIndex(value, classification, updatedAt, retrievedAtEpochMs)
    }
}
