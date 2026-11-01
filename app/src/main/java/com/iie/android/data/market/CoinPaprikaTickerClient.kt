package com.iie.android.data.market

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class CoinPaprikaQuote(
    val providerId: String,
    val symbol: String,
    val priceUsd: Double,
    val providerUpdatedAtEpochMs: Long,
    val retrievedAtEpochMs: Long
)

/**
 * Minimal, bounded CoinPaprika ticker adapter. It validates provider identity and
 * payload shape but does not assess economic accuracy, liquidity, or investment merit.
 */
class CoinPaprikaTickerClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 8_000,
    private val maxResponseBytes: Int = 256 * 1024
) {
    init {
        require(connectTimeoutMs in 1..30_000)
        require(readTimeoutMs in 1..30_000)
        require(maxResponseBytes in 1..1_048_576)
    }

    suspend fun fetch(
        providerId: String,
        expectedSymbol: String,
        retrievedAtEpochMs: Long = System.currentTimeMillis()
    ): CoinPaprikaQuote = withContext(Dispatchers.IO) {
        require(providerId.matches(Regex("[a-z0-9-]{3,80}"))) { "Invalid CoinPaprika provider ID" }
        require(expectedSymbol.matches(Regex("[A-Za-z0-9]{2,12}"))) { "Invalid expected asset symbol" }

        val connection = (URL("https://api.coinpaprika.com/v1/tickers/$providerId")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "IIE-Android-Personal/0.1")
        }

        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("CoinPaprika HTTP $status")
            }
            val contentType = connection.contentType.orEmpty()
            if (!contentType.lowercase().contains("application/json")) {
                throw IOException("CoinPaprika returned an unexpected content type")
            }
            val declaredLength = connection.getHeaderFieldLong("Content-Length", -1L)
            if (declaredLength > maxResponseBytes) {
                throw IOException("CoinPaprika response exceeds the configured size limit")
            }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxResponseBytes) {
                        throw IOException("CoinPaprika response exceeds the configured size limit")
                    }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            parse(bytes.toString(Charsets.UTF_8), providerId, expectedSymbol, retrievedAtEpochMs)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parse(
        payload: String,
        requestedProviderId: String,
        expectedSymbol: String,
        retrievedAtEpochMs: Long
    ): CoinPaprikaQuote {
        val root = try {
            Json.parseToJsonElement(payload).jsonObject
        } catch (error: Exception) {
            throw IOException("Malformed CoinPaprika JSON", error)
        }
        val responseId = root["id"]?.jsonPrimitive?.contentOrNull
            ?: throw IOException("CoinPaprika response is missing id")
        if (responseId != requestedProviderId) {
            throw IOException("CoinPaprika response ID does not match the requested provider ID")
        }
        val symbol = root["symbol"]?.jsonPrimitive?.contentOrNull
            ?: throw IOException("CoinPaprika response is missing symbol")
        if (!symbol.equals(expectedSymbol, ignoreCase = true)) {
            throw IOException("CoinPaprika response symbol does not match the mapped asset")
        }
        val updatedAt = root["last_updated"]?.jsonPrimitive?.contentOrNull
            ?: throw IOException("CoinPaprika response is missing last_updated")
        val updatedAtMs = try {
            Instant.parse(updatedAt).toEpochMilli()
        } catch (error: Exception) {
            throw IOException("CoinPaprika last_updated is invalid", error)
        }
        if (updatedAtMs <= 0L || updatedAtMs > retrievedAtEpochMs + 60_000L) {
            throw IOException("CoinPaprika last_updated is outside the accepted time bounds")
        }
        val pricePrimitive = try {
            root["quotes"]?.jsonObject?.get("USD")?.jsonObject?.get("price")?.jsonPrimitive
        } catch (error: Exception) {
            null
        } ?: throw IOException("CoinPaprika response is missing quotes.USD.price")
        if (pricePrimitive.isString) throw IOException("CoinPaprika USD price must be a JSON number")
        val price = pricePrimitive.doubleOrNull
        if (price == null || !price.isFinite() || price <= 0.0) {
            throw IOException("CoinPaprika USD price must be finite and greater than zero")
        }
        return CoinPaprikaQuote(
            providerId = responseId,
            symbol = symbol,
            priceUsd = price,
            providerUpdatedAtEpochMs = updatedAtMs,
            retrievedAtEpochMs = retrievedAtEpochMs
        )
    }
}
