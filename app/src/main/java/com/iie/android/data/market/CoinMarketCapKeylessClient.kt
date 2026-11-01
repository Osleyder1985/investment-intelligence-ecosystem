package com.iie.android.data.market

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * CoinMarketCap's keyless public listings adapter.
 *
 * The keyless route is deliberately used: no API credential is embedded in the APK.
 * This adapter returns provider observations only; it does not persist them or claim
 * that a provider's circulating-supply methodology is independently verified.
 */
data class CoinMarketCapListing(
    val providerId: Long,
    val name: String,
    val symbol: String,
    val priceUsd: Double,
    val circulatingSupply: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val providerUpdatedAtEpochMs: Long,
    val retrievedAtEpochMs: Long
)

class CoinMarketCapKeylessClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 8_000,
    private val maxResponseBytes: Int = 2 * 1024 * 1024
) {
    init {
        require(connectTimeoutMs in 1..30_000)
        require(readTimeoutMs in 1..30_000)
        require(maxResponseBytes in 1..4_194_304)
    }

    suspend fun fetchListings(
        expectedSymbols: Set<String>,
        retrievedAtEpochMs: Long = System.currentTimeMillis()
    ): List<CoinMarketCapListing> = withContext(Dispatchers.IO) {
        require(expectedSymbols.isNotEmpty())
        require(expectedSymbols.size <= 50)
        require(expectedSymbols.all { it.matches(Regex("[A-Za-z0-9]{2,12}")) })
        require(retrievedAtEpochMs > 0L)

        val url = URL(
            "$BASE_URL/v3/cryptocurrency/listings/latest?start=1&limit=$LISTING_LIMIT&convert=USD"
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
            parse(bytes.toString(Charsets.UTF_8), expectedSymbols, retrievedAtEpochMs)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parse(
        payload: String,
        expectedSymbols: Set<String>,
        retrievedAtEpochMs: Long
    ): List<CoinMarketCapListing> {
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
        } ?: throw IOException("CoinMarketCap response is missing listings data")

        val normalized = expectedSymbols.map { it.uppercase() }.toSet()
        val matches = data.mapNotNull { element ->
            val item = element.jsonObject
            val symbol = item["symbol"]?.jsonPrimitive?.contentOrNull?.uppercase()
                ?: return@mapNotNull null
            if (symbol !in normalized) return@mapNotNull null
            val id = item["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
                ?: throw IOException("CoinMarketCap listing has an invalid ID for $symbol")
            val name = item["name"]?.jsonPrimitive?.contentOrNull
                ?: throw IOException("CoinMarketCap listing is missing name for $symbol")
            val lastUpdated = item["last_updated"]?.jsonPrimitive?.contentOrNull
                ?: throw IOException("CoinMarketCap listing is missing last_updated for $symbol")
            val updatedAt = try {
                Instant.parse(lastUpdated).toEpochMilli()
            } catch (error: Exception) {
                throw IOException("CoinMarketCap last_updated is invalid for $symbol", error)
            }
            if (updatedAt <= 0L || updatedAt > retrievedAtEpochMs + 60_000L) {
                throw IOException("CoinMarketCap timestamp is outside accepted bounds for $symbol")
            }
            val usd = try {
                item["quote"]?.jsonObject?.get("USD")?.jsonObject
            } catch (error: Exception) {
                null
            } ?: throw IOException("CoinMarketCap USD quote is missing for $symbol")
            val price = usd["price"]?.jsonPrimitive?.doubleOrNull
            if (price == null || !price.isFinite() || price <= 0.0) {
                throw IOException("CoinMarketCap USD price is invalid for $symbol")
            }
            fun optionalSupply(key: String): Double? {
                val primitive = item[key]?.jsonPrimitive ?: return null
                if (primitive.contentOrNull == null) return null
                val value = primitive.doubleOrNull
                    ?: throw IOException("CoinMarketCap $key is invalid for $symbol")
                if (!value.isFinite() || value < 0.0) {
                    throw IOException("CoinMarketCap $key is outside accepted bounds for $symbol")
                }
                return value
            }
            CoinMarketCapListing(
                providerId = id,
                name = name,
                symbol = symbol,
                priceUsd = price,
                circulatingSupply = optionalSupply("circulating_supply"),
                totalSupply = optionalSupply("total_supply"),
                maxSupply = optionalSupply("max_supply"),
                providerUpdatedAtEpochMs = updatedAt,
                retrievedAtEpochMs = retrievedAtEpochMs
            )
        }
        val ambiguous = matches.groupBy { it.symbol }.filterValues { it.size > 1 }.keys
        if (ambiguous.isNotEmpty()) {
            throw IOException("CoinMarketCap returned ambiguous symbols: ${ambiguous.sorted().joinToString()}")
        }
        return matches.sortedBy { it.symbol }
    }

    companion object {
        const val SOURCE_ID = "source:coinmarketcap"
        const val BASE_URL = "https://pro-api.coinmarketcap.com/public-api"
        const val TERMS_URL = "https://coinmarketcap.com/api/terms/"
        const val ATTRIBUTION = "Data provided by CoinMarketCap"
        const val LISTING_LIMIT = 500
    }
}
