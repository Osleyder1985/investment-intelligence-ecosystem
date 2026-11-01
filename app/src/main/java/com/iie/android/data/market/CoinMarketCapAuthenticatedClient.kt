package com.iie.android.data.market

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class CoinMarketCapAuthenticatedQuote(
    val providerId: Long,
    val name: String,
    val symbol: String,
    val priceUsd: Double,
    val providerUpdatedAtEpochMs: Long,
    val retrievedAtEpochMs: Long
)

/**
 * Authenticated CoinMarketCap Pro API adapter.
 * The API key is supplied at call time and is never retained by this client.
 */
class CoinMarketCapAuthenticatedClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 8_000,
    private val maxResponseBytes: Int = 512 * 1024
) {
    init {
        require(connectTimeoutMs in 1..30_000)
        require(readTimeoutMs in 1..30_000)
        require(maxResponseBytes in 1..1_048_576)
    }

    fun fetchLatestQuote(
        apiKey: String,
        expectedSymbol: String,
        retrievedAtEpochMs: Long = System.currentTimeMillis()
    ): CoinMarketCapAuthenticatedQuote {
        require(apiKey.isNotBlank()) { "CoinMarketCap API key is not configured" }
        require(expectedSymbol.matches(Regex("[A-Za-z0-9]{2,12}")))
        require(retrievedAtEpochMs > 0L)

        val symbol = expectedSymbol.uppercase()
        val url = URL("$BASE_URL/v3/cryptocurrency/quotes/latest?symbol=$symbol&convert=USD")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "IIE-Android-Personal/0.2")
            setRequestProperty(API_KEY_HEADER, apiKey.trim())
        }
        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("CoinMarketCap HTTP $status")
            }
            if (!connection.contentType.orEmpty().lowercase().contains("application/json")) {
                throw IOException("CoinMarketCap returned an unexpected content type")
            }
            val declaredLength = connection.getHeaderFieldLong("Content-Length", -1L)
            if (declaredLength > maxResponseBytes) {
                throw IOException("CoinMarketCap response exceeds the configured size limit")
            }
            val payload = connection.inputStream.buffered().use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxResponseBytes) {
                        throw IOException("CoinMarketCap response exceeds the configured size limit")
                    }
                    output.write(buffer, 0, count)
                }
                output.toString(Charsets.UTF_8.name())
            }
            return parse(payload, symbol, retrievedAtEpochMs)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parse(
        payload: String,
        expectedSymbol: String,
        retrievedAtEpochMs: Long
    ): CoinMarketCapAuthenticatedQuote {
        val root = try {
            Json.parseToJsonElement(payload).jsonObject
        } catch (error: Exception) {
            throw IOException("Malformed CoinMarketCap JSON", error)
        }
        val status = root["status"]?.jsonObject
            ?: throw IOException("CoinMarketCap response is missing status")
        val errorCode = status["error_code"]?.jsonPrimitive?.contentOrNull
        if (errorCode != null && errorCode != "0") {
            throw IOException("CoinMarketCap API error: $errorCode")
        }
        val data = try {
            root["data"]?.jsonArray
        } catch (error: Exception) {
            null
        } ?: throw IOException("CoinMarketCap response is missing quote data")

        val matches = data.filter { element ->
            element.jsonObject["symbol"]?.jsonPrimitive?.contentOrNull
                ?.equals(expectedSymbol, ignoreCase = true) == true
        }
        if (matches.size != 1) {
            throw IOException(
                if (matches.isEmpty()) "CoinMarketCap did not resolve $expectedSymbol"
                else "CoinMarketCap returned an ambiguous result for $expectedSymbol"
            )
        }
        val item = matches.single().jsonObject
        val id = item["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: throw IOException("CoinMarketCap returned an invalid asset ID")
        val name = item["name"]?.jsonPrimitive?.contentOrNull
            ?: throw IOException("CoinMarketCap response is missing asset name")
        val quotes = try {
            item["quote"]?.jsonArray
        } catch (error: Exception) {
            null
        } ?: throw IOException("CoinMarketCap response is missing quote values")
        val usdQuotes = quotes.filter {
            it.jsonObject["symbol"]?.jsonPrimitive?.contentOrNull == "USD"
        }
        if (usdQuotes.size != 1) throw IOException("CoinMarketCap USD quote is missing or ambiguous")
        val usd = usdQuotes.single().jsonObject
        val price = usd["price"]?.jsonPrimitive?.doubleOrNull
        if (price == null || !price.isFinite() || price <= 0.0) {
            throw IOException("CoinMarketCap returned an invalid USD price")
        }
        val updatedAtText = usd["last_updated"]?.jsonPrimitive?.contentOrNull
            ?: item["last_updated"]?.jsonPrimitive?.contentOrNull
            ?: throw IOException("CoinMarketCap response is missing quote timestamp")
        val updatedAt = try {
            Instant.parse(updatedAtText).toEpochMilli()
        } catch (error: Exception) {
            throw IOException("CoinMarketCap returned an invalid quote timestamp", error)
        }
        if (updatedAt <= 0L || updatedAt > retrievedAtEpochMs + 60_000L) {
            throw IOException("CoinMarketCap quote timestamp is outside accepted bounds")
        }
        return CoinMarketCapAuthenticatedQuote(
            providerId = id,
            name = name,
            symbol = expectedSymbol.uppercase(),
            priceUsd = price,
            providerUpdatedAtEpochMs = updatedAt,
            retrievedAtEpochMs = retrievedAtEpochMs
        )
    }

    companion object {
        const val SOURCE_ID = "source:coinmarketcap"
        const val BASE_URL = "https://pro-api.coinmarketcap.com"
        const val API_KEY_HEADER = "X-CMC_PRO_API_KEY"
        const val TERMS_URL = "https://coinmarketcap.com/api/terms/"
        const val ATTRIBUTION = "Data provided by CoinMarketCap"
    }
}
