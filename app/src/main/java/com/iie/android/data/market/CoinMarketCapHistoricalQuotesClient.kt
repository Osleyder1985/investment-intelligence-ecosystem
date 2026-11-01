package com.iie.android.data.market

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun interface CoinMarketCapDailyHistoryFetcher {
    suspend fun fetchDaily(
        cmcAssetId: Long,
        apiKey: String,
        start: LocalDate,
        end: LocalDate,
        retrievedAtEpochMs: Long
    ): List<CoinPaprikaHistoricalPoint>
}


/** Usage metadata returned by CMC's zero-credit key-info endpoint. */
data class CoinMarketCapKeyValidation(
    val monthlyCreditLimit: Long?,
    val monthlyCreditsLeft: Long?,
    val dailyCreditsLeft: Long?,
    val minuteRateLimit: Long?,
    val minuteRequestsLeft: Long?
)

/**
 * Validates a credential without spending historical-data credits.
 * CMC documents /v1/key/info as zero-credit, though it still consumes a minute-rate slot.
 */
class CoinMarketCapApiKeyValidator(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 8_000,
    private val maxResponseBytes: Int = 64 * 1024
) {
    suspend fun validate(apiKey: String): CoinMarketCapKeyValidation = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank() && apiKey.length <= 256 && apiKey.none { it.isWhitespace() }) {
            "El formato de la API key no es válido"
        }
        val connection = (URL(BASE_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-CMC_PRO_API_KEY", apiKey)
            setRequestProperty("User-Agent", "IIE-Android-Personal/0.3")
        }
        try {
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val payload = stream?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxResponseBytes) throw IOException("CoinMarketCap key-info response exceeds size limit")
                    output.write(buffer, 0, count)
                }
                output.toString(Charsets.UTF_8.name())
            }.orEmpty()
            if (statusCode != HttpURLConnection.HTTP_OK) {
                val code = runCatching {
                    Json.parseToJsonElement(payload).jsonObject["status"]?.jsonObject
                        ?.get("error_code")?.jsonPrimitive?.contentOrNull
                }.getOrNull()
                throw IOException("CoinMarketCap rechazó la credencial (HTTP $statusCode" +
                    (code?.let { ", error $it" } ?: "") + ")")
            }
            parseKeyInfo(payload)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseKeyInfo(payload: String): CoinMarketCapKeyValidation {
        val root = try { Json.parseToJsonElement(payload).jsonObject } catch (error: Exception) {
            throw IOException("Respuesta de validación CoinMarketCap no válida", error)
        }
        val status = root["status"]?.jsonObject
            ?: throw IOException("CoinMarketCap key-info response is missing status")
        val errorCode = status["error_code"]?.jsonPrimitive?.contentOrNull
        if (errorCode != null && errorCode != "0") {
            throw IOException("CoinMarketCap key-info API error: $errorCode")
        }
        val data = root["data"]?.jsonObject
            ?: throw IOException("CoinMarketCap key-info response is missing data")
        val plan = data["plan"]?.jsonObject
        val usage = data["usage"]?.jsonObject
        fun optionalLong(value: kotlinx.serialization.json.JsonElement?): Long? {
            if (value == null || value.toString() == "null") return null
            return value.jsonPrimitive.contentOrNull?.toLongOrNull()
                ?.takeIf { it >= 0L }
                ?: throw IOException("CoinMarketCap returned invalid usage metadata")
        }
        return CoinMarketCapKeyValidation(
            monthlyCreditLimit = optionalLong(plan?.get("credit_limit_monthly")),
            monthlyCreditsLeft = optionalLong(usage?.get("current_month")?.jsonObject?.get("credits_left")),
            dailyCreditsLeft = optionalLong(usage?.get("current_day")?.jsonObject?.get("credits_left")),
            minuteRateLimit = optionalLong(plan?.get("rate_limit_minute")),
            minuteRequestsLeft = optionalLong(usage?.get("current_minute")?.jsonObject?.get("requests_left"))
        )
    }

    companion object {
        const val BASE_URL = "https://pro-api.coinmarketcap.com/v1/key/info"
    }
}

/** CMC Basic historical daily quotes; this is not an OHLCV candle adapter. */
class CoinMarketCapHistoricalQuotesClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 15_000,
    private val maxResponseBytes: Int = 4 * 1024 * 1024,
    private val maxPoints: Int = 370
) : CoinMarketCapDailyHistoryFetcher {
    init {
        require(connectTimeoutMs in 1..30_000)
        require(readTimeoutMs in 1..30_000)
        require(maxResponseBytes in 1..8 * 1024 * 1024)
        require(maxPoints in 1..370)
    }
    override suspend fun fetchDaily(cmcAssetId: Long, apiKey: String, start: LocalDate, end: LocalDate,
        retrievedAtEpochMs: Long): List<CoinPaprikaHistoricalPoint> =
        withContext(Dispatchers.IO) {
            require(cmcAssetId > 0L)
            require(apiKey.isNotBlank() && apiKey.length <= 256 && apiKey.none { it.isWhitespace() })
            require(!end.isBefore(start)) { "Historical end date precedes start date" }
            val latestAllowedStart = Instant.ofEpochMilli(retrievedAtEpochMs)
                .atZone(java.time.ZoneOffset.UTC).toLocalDate().minusYears(1)
            require(!start.isBefore(latestAllowedStart)) {
                "El plan Basic permite solicitar como máximo un año de histórico diario"
            }
            val query = "id=$cmcAssetId&time_start=${encode(start.toString())}" +
                "&time_end=${encode(end.toString())}&interval=daily&convert=USD"
            val connection = (URL("$BASE_URL?$query").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                instanceFollowRedirects = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("X-CMC_PRO_API_KEY", apiKey)
                setRequestProperty("User-Agent", "IIE-Android-Personal/0.3")
            }
            try {
                val status = connection.responseCode
                if (status != HttpURLConnection.HTTP_OK) {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    val code = runCatching {
                        Json.parseToJsonElement(errorBody).jsonObject["status"]?.jsonObject
                            ?.get("error_code")?.jsonPrimitive?.contentOrNull
                    }.getOrNull()
                    throw IOException("CoinMarketCap historical HTTP $status" +
                        (code?.let { " (API error $it)" } ?: ""))
                }
                if (!connection.contentType.orEmpty().lowercase().contains("application/json")) {
                    throw IOException("CoinMarketCap returned an unexpected content type")
                }
                val declaredLength = connection.getHeaderFieldLong("Content-Length", -1L)
                if (declaredLength > maxResponseBytes) throw IOException("Historical response exceeds configured size limit")
                val bytes = connection.inputStream.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8 * 1024)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > maxResponseBytes) throw IOException("Historical response exceeds configured size limit")
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                }
                parse(bytes.toString(Charsets.UTF_8), cmcAssetId, retrievedAtEpochMs, start, end)
            } finally { connection.disconnect() }
        }

    /**
     * Performs a deliberately small live entitlement probe against the Basic-compatible
     * historical quotes endpoint. Requests at most seven daily BTC snapshots.
     */
    suspend fun probeHistoricalAccess(
        apiKey: String,
        retrievedAtEpochMs: Long = System.currentTimeMillis()
    ): List<CoinPaprikaHistoricalPoint> {
        require(retrievedAtEpochMs > 0L)
        val (start, yesterdayUtc) = historicalProbeWindow(retrievedAtEpochMs)
        val points = fetchDaily(
            cmcAssetId = 1L,
            apiKey = apiKey,
            start = start,
            end = yesterdayUtc,
            retrievedAtEpochMs = retrievedAtEpochMs
        )
        if (points.isEmpty()) {
            throw IOException("CoinMarketCap aceptó la consulta, pero no devolvió snapshots históricos de BTC en los últimos siete días completos.")
        }
        return points
    }

    internal fun historicalProbeWindow(retrievedAtEpochMs: Long): Pair<LocalDate, LocalDate> {
        require(retrievedAtEpochMs > 0L)
        val yesterdayUtc = Instant.ofEpochMilli(retrievedAtEpochMs)
            .atZone(java.time.ZoneOffset.UTC).toLocalDate().minusDays(1)
        return yesterdayUtc.minusDays(6) to yesterdayUtc
    }

    internal fun parse(payload: String, cmcAssetId: Long, retrievedAtEpochMs: Long,
        start: LocalDate, end: LocalDate): List<CoinPaprikaHistoricalPoint> {
        val root = try { Json.parseToJsonElement(payload).jsonObject } catch (error: Exception) {
            throw IOException("Malformed CoinMarketCap historical JSON", error)
        }
        val status = root["status"]?.jsonObject ?: throw IOException("CoinMarketCap response is missing status")
        val errorCode = status["error_code"]?.jsonPrimitive?.contentOrNull
        if (errorCode != null && errorCode != "0") throw IOException("CoinMarketCap API error: $errorCode")
        val data = root["data"]?.jsonObject ?: throw IOException("CoinMarketCap response is missing data")
        val assetObject = data[cmcAssetId.toString()]?.jsonObject ?: data
        val returnedId = assetObject["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
        if (returnedId != null && returnedId != cmcAssetId) throw IOException("CoinMarketCap returned a different asset ID")
        val quotes = assetObject["quotes"] as? JsonArray
            ?: throw IOException("CoinMarketCap historical response is missing quotes")
        if (quotes.size > maxPoints) throw IOException("CoinMarketCap returned too many historical points")
        val points = quotes.map { element ->
            val row = try { element.jsonObject } catch (error: Exception) {
                throw IOException("CoinMarketCap historical quote is not an object", error)
            }
            val timestampText = row["timestamp"]?.jsonPrimitive?.contentOrNull
                ?: throw IOException("CoinMarketCap historical quote is missing timestamp")
            val timestamp = try { Instant.parse(timestampText).toEpochMilli() } catch (error: Exception) {
                throw IOException("CoinMarketCap historical timestamp is invalid", error)
            }
            val date = Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneOffset.UTC).toLocalDate()
            if (date.isBefore(start) || date.isAfter(end)) throw IOException("CoinMarketCap point falls outside requested date range")
            val usd = row["quote"]?.jsonObject?.get("USD")?.jsonObject
                ?: throw IOException("CoinMarketCap historical quote is missing USD metrics")
            fun number(key: String): Double? {
                val value = usd[key] ?: return null
                if (value.toString() == "null") return null
                val parsed = value.jsonPrimitive.doubleOrNull ?: throw IOException("CoinMarketCap historical $key is invalid")
                if (!parsed.isFinite() || parsed < 0.0) throw IOException("CoinMarketCap historical $key is outside accepted bounds")
                return parsed
            }
            val price = number("price")?.takeIf { it > 0.0 }
                ?: throw IOException("CoinMarketCap historical price is missing or invalid")
            CoinPaprikaHistoricalPoint(cmcAssetId.toString(), timestamp, price, number("volume_24h"),
                number("market_cap"), retrievedAtEpochMs, SOURCE_ID)
        }
        if (points.map { it.timestampEpochMs }.distinct().size != points.size) {
            throw IOException("CoinMarketCap returned duplicate historical timestamps")
        }
        return points.sortedBy { it.timestampEpochMs }
    }
    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
    companion object {
        const val SOURCE_ID = "source:coinmarketcap"
        const val BASE_URL = "https://pro-api.coinmarketcap.com/v3/cryptocurrency/quotes/historical"
    }
}
