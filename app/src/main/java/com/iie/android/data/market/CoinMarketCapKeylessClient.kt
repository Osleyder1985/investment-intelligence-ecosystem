package com.iie.android.data.market

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * CoinMarketCap public/keyless quote. Missing provider fields remain null.
 * The public route requires no API key and is rate-limited independently of Pro access.
 */
data class CoinMarketCapQuote(
    val providerId: Long,
    val name: String,
    val symbol: String,
    val priceUsd: Double,
    val circulatingSupply: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val marketCapUsd: Double?,
    val fullyDilutedMarketCapUsd: Double?,
    val providerUpdatedAtEpochMs: Long,
    val retrievedAtEpochMs: Long
)

class CoinMarketCapKeylessClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 8_000,
    private val maxResponseBytes: Int = 512 * 1024
) {
    init {
        require(connectTimeoutMs in 1..30_000)
        require(readTimeoutMs in 1..30_000)
        require(maxResponseBytes in 1..1_048_576)
    }

    suspend fun fetchQuote(
        expectedProviderId: Long,
        retrievedAtEpochMs: Long = System.currentTimeMillis()
    ): CoinMarketCapQuote = withContext(Dispatchers.IO) {
        require(expectedProviderId > 0L)
        require(retrievedAtEpochMs > 0L)
        val url = URL(
            "$BASE_URL/v3/cryptocurrency/quotes/latest?id=$expectedProviderId&convert=USD"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "IIE-Android-Personal/0.2")
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
            val bytes = connection.inputStream.use { input ->
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
                output.toByteArray()
            }
            parse(bytes.toString(Charsets.UTF_8), expectedProviderId, retrievedAtEpochMs)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parse(
        payload: String,
        expectedProviderId: Long,
        retrievedAtEpochMs: Long
    ): CoinMarketCapQuote {
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
        val data = root["data"] ?: throw IOException("CoinMarketCap response is missing quote data")
        val items = when (data) {
            is JsonObject -> data.values.map { it.jsonObject }
            is JsonArray -> data.map { it.jsonObject }
            else -> throw IOException("CoinMarketCap quote data has an unexpected shape")
        }
        val matches = items.filter {
            it["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() == expectedProviderId
        }
        if (matches.size != 1) {
            throw IOException(
                if (matches.isEmpty()) "CoinMarketCap did not resolve provider ID $expectedProviderId"
                else "CoinMarketCap returned an ambiguous result for provider ID $expectedProviderId"
            )
        }
        val item = matches.single()
        val id = item["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: throw IOException("CoinMarketCap returned an invalid asset ID")
        val name = item["name"]?.jsonPrimitive?.contentOrNull
            ?: throw IOException("CoinMarketCap response is missing asset name")
        val symbol = item["symbol"]?.jsonPrimitive?.contentOrNull?.uppercase()
            ?: throw IOException("CoinMarketCap response is missing asset symbol")
        val quoteElement = item["quote"] ?: throw IOException("CoinMarketCap quote is missing")
        val usd = when (quoteElement) {
            is JsonObject -> quoteElement["USD"]?.jsonObject
            is JsonArray -> quoteElement.firstOrNull {
                it.jsonObject["symbol"]?.jsonPrimitive?.contentOrNull == "USD"
            }?.jsonObject
            else -> null
        } ?: throw IOException("CoinMarketCap USD quote is missing")
        fun optionalNonNegativeNumber(container: JsonObject, key: String): Double? {
            val element = container[key] ?: return null
            if (element.toString() == "null") return null
            val value = element.jsonPrimitive.doubleOrNull
                ?: throw IOException("CoinMarketCap $key is invalid")
            if (!value.isFinite() || value < 0.0) {
                throw IOException("CoinMarketCap $key is outside accepted bounds")
            }
            return value
        }
        val price = optionalNonNegativeNumber(usd, "price")
            ?: throw IOException("CoinMarketCap USD price is missing")
        if (price <= 0.0) throw IOException("CoinMarketCap USD price is invalid")
        val updatedAtText = usd["last_updated"]?.jsonPrimitive?.contentOrNull
            ?: item["last_updated"]?.jsonPrimitive?.contentOrNull
            ?: throw IOException("CoinMarketCap response is missing quote timestamp")
        val updatedAt = try {
            Instant.parse(updatedAtText).toEpochMilli()
        } catch (error: Exception) {
            throw IOException("CoinMarketCap quote timestamp is invalid", error)
        }
        if (updatedAt <= 0L || updatedAt > retrievedAtEpochMs + 60_000L) {
            throw IOException("CoinMarketCap timestamp is outside accepted bounds")
        }
        return CoinMarketCapQuote(
            providerId = id,
            name = name,
            symbol = symbol,
            priceUsd = price,
            circulatingSupply = optionalNonNegativeNumber(item, "circulating_supply"),
            totalSupply = optionalNonNegativeNumber(item, "total_supply"),
            maxSupply = optionalNonNegativeNumber(item, "max_supply"),
            marketCapUsd = optionalNonNegativeNumber(usd, "market_cap"),
            fullyDilutedMarketCapUsd = optionalNonNegativeNumber(usd, "fully_diluted_market_cap"),
            providerUpdatedAtEpochMs = updatedAt,
            retrievedAtEpochMs = retrievedAtEpochMs
        )
    }

    companion object {
        const val SOURCE_ID = "source:coinmarketcap"
        const val BASE_URL = "https://pro-api.coinmarketcap.com/public-api"
        const val TERMS_URL = "https://coinmarketcap.com/api/terms/"
        const val ATTRIBUTION = "Data provided by CoinMarketCap"
    }
}
