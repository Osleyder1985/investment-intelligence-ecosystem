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
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun interface CoinPaprikaDailyHistoryFetcher {
    override suspend fun fetchDaily(
        providerId: String,
        start: LocalDate,
        end: LocalDate? = null,
        retrievedAtEpochMs: Long = System.currentTimeMillis()
    ): List<CoinPaprikaHistoricalPoint>
}

data class CoinPaprikaHistoricalPoint(
    val providerId: String,
    val timestampEpochMs: Long,
    val priceUsd: Double,
    val volume24hUsd: Double?,
    val marketCapUsd: Double?,
    val retrievedAtEpochMs: Long
)

/**
 * Daily historical ticker adapter. These are price/volume/market-cap points,
 * not OHLCV candles. Free-plan requests are bounded to a rolling year.
 */
class CoinPaprikaHistoricalTickerClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 12_000,
    private val maxResponseBytes: Int = 2 * 1024 * 1024,
    private val maxPoints: Int = 500
) : CoinPaprikaDailyHistoryFetcher {
    init {
        require(connectTimeoutMs in 1..30_000)
        require(readTimeoutMs in 1..30_000)
        require(maxResponseBytes in 1..4 * 1024 * 1024)
        require(maxPoints in 1..500)
    }

    suspend fun fetchDaily(
        providerId: String,
        start: LocalDate,
        end: LocalDate?,
        retrievedAtEpochMs: Long
    ): List<CoinPaprikaHistoricalPoint> = withContext(Dispatchers.IO) {
        require(providerId.matches(Regex("[a-z0-9-]{3,80}"))) { "Invalid CoinPaprika provider ID" }
        require(end == null || !end.isBefore(start)) { "Historical end date precedes start date" }
        val latestAllowedStart = Instant.ofEpochMilli(retrievedAtEpochMs).atZone(java.time.ZoneOffset.UTC)
            .toLocalDate().minusYears(1)
        require(!start.isBefore(latestAllowedStart)) { "Free-plan daily history is limited to one year" }

        val query = buildString {
            append("start=").append(encode(start.toString()))
            if (end != null) append("&end=").append(encode(end.toString()))
            append("&interval=1d&quote=usd&limit=").append(maxPoints)
        }
        val connection = (URL("https://api.coinpaprika.com/v1/tickers/$providerId/historical?$query")
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
            if (status != HttpURLConnection.HTTP_OK) throw IOException("CoinPaprika historical HTTP $status")
            if (!connection.contentType.orEmpty().lowercase().contains("application/json")) {
                throw IOException("CoinPaprika historical response has an unexpected content type")
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
            parse(bytes.toString(Charsets.UTF_8), providerId, retrievedAtEpochMs, start, end)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parse(
        payload: String,
        requestedProviderId: String,
        retrievedAtEpochMs: Long,
        start: LocalDate,
        end: LocalDate? = null
    ): List<CoinPaprikaHistoricalPoint> {
        val rows = try {
            Json.parseToJsonElement(payload).jsonArray
        } catch (error: Exception) {
            throw IOException("Malformed CoinPaprika historical JSON array", error)
        }
        if (rows.size > maxPoints) throw IOException("CoinPaprika returned more points than the configured maximum")
        val points = rows.map { element ->
            val row = try { element.jsonObject } catch (error: Exception) {
                throw IOException("Historical point is not a JSON object", error)
            }
            val timestamp = row["timestamp"]?.jsonPrimitive?.contentOrNull
                ?: throw IOException("Historical point is missing timestamp")
            val timestampMs = try { Instant.parse(timestamp).toEpochMilli() } catch (error: Exception) {
                throw IOException("Historical timestamp is invalid", error)
            }
            val date = Instant.ofEpochMilli(timestampMs).atZone(java.time.ZoneOffset.UTC).toLocalDate()
            if (date.isBefore(start) || (end != null && date.isAfter(end))) {
                throw IOException("Historical point falls outside the requested date range")
            }
            val price = requiredNumber(row, "price")
            if (!price.isFinite() || price <= 0.0) throw IOException("Historical price must be finite and positive")
            val volume = optionalNumber(row, "volume_24h")
            val marketCap = optionalNumber(row, "market_cap")
            if (volume != null && (!volume.isFinite() || volume < 0.0)) throw IOException("Historical volume is invalid")
            if (marketCap != null && (!marketCap.isFinite() || marketCap < 0.0)) throw IOException("Historical market cap is invalid")
            CoinPaprikaHistoricalPoint(requestedProviderId, timestampMs, price, volume, marketCap, retrievedAtEpochMs)
        }
        if (points.map { it.timestampEpochMs }.distinct().size != points.size) {
            throw IOException("CoinPaprika returned duplicate historical timestamps")
        }
        return points.sortedBy { it.timestampEpochMs }
    }

    private fun requiredNumber(row: kotlinx.serialization.json.JsonObject, field: String): Double =
        optionalNumber(row, field) ?: throw IOException("Historical point is missing numeric $field")

    private fun optionalNumber(row: kotlinx.serialization.json.JsonObject, field: String): Double? {
        val primitive = row[field]?.jsonPrimitive ?: return null
        if (primitive.isString) throw IOException("Historical $field must be a JSON number")
        return primitive.doubleOrNull ?: throw IOException("Historical $field is invalid")
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
