package com.iie.android.domain.valuation

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Public DefiLlama fees/revenue endpoint client.
 *
 * Values remain separated by provider metric and observation timestamp. This client
 * does not claim that protocol-level fees/revenue belong to token holders.
 */
enum class DefiLlamaMetric(val dataType: String) {
    PROTOCOL_FEES("dailyFees"),
    PROTOCOL_REVENUE("dailyRevenue"),
    HOLDER_REVENUE("dailyHoldersRevenue")
}

data class DefiLlamaDailyPoint(
    val timestampEpochMs: Long,
    val valueUsd: Double
)

data class DefiLlamaMetricSeries(
    val metric: DefiLlamaMetric,
    val protocolSlug: String,
    val protocolName: String?,
    val total24hUsd: Double?,
    val total48hTo24hUsd: Double?,
    val total7dUsd: Double?,
    val totalAllTimeUsd: Double?,
    val dailyPoints: List<DefiLlamaDailyPoint>,
    val sourceUrl: String,
    val retrievedAtEpochMs: Long
)

sealed class DefiLlamaFetchResult {
    data class Success(val series: List<DefiLlamaMetricSeries>) : DefiLlamaFetchResult()
    data class Failure(val reasonCode: String, val detail: String? = null) : DefiLlamaFetchResult()
}

fun interface DefiLlamaHttpTransport {
    fun get(url: String): String
}

/** Synchronous transport: callers must invoke it from an IO/background dispatcher. */
class HttpDefiLlamaHttpTransport(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000
) : DefiLlamaHttpTransport {
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
            if (status !in 200..299) throw DefiLlamaHttpException("HTTP_$status", body.take(500))
            return body
        } finally {
            connection.disconnect()
        }
    }
}

class DefiLlamaHttpException(val reasonCode: String, message: String?) : Exception(message)

class DefiLlamaEconomicDataSource(
    private val transport: DefiLlamaHttpTransport,
    private val clock: () -> Long = System::currentTimeMillis
) {
    fun fetchProtocol(protocolSlug: String): DefiLlamaFetchResult {
        if (!protocolSlug.matches(Regex("[a-z0-9][a-z0-9-]{0,79}"))) {
            return DefiLlamaFetchResult.Failure("PROTOCOL_SLUG_INVALID")
        }
        val results = mutableListOf<DefiLlamaMetricSeries>()
        for (metric in DefiLlamaMetric.entries) {
            val url = "https://api.llama.fi/summary/fees/$protocolSlug?dataType=${metric.dataType}"
            try {
                val payload = transport.get(url)
                val retrievedAt = clock()
                if (retrievedAt <= 0L) return DefiLlamaFetchResult.Failure("RETRIEVAL_TIMESTAMP_INVALID")
                results += parse(payload, metric, protocolSlug, url, retrievedAt)
            } catch (error: DefiLlamaHttpException) {
                return DefiLlamaFetchResult.Failure(error.reasonCode, error.message)
            } catch (error: Exception) {
                return DefiLlamaFetchResult.Failure("PROVIDER_REQUEST_FAILED", error.message)
            }
        }
        return DefiLlamaFetchResult.Success(results)
    }

    companion object {
        fun parse(
            payload: String,
            metric: DefiLlamaMetric,
            protocolSlug: String,
            sourceUrl: String,
            retrievedAtEpochMs: Long
        ): DefiLlamaMetricSeries {
            require(protocolSlug.matches(Regex("[a-z0-9][a-z0-9-]{0,79}")))
            require(retrievedAtEpochMs > 0L)
            val root = JSONObject(payload)
            val name = root.optString("name").takeIf { it.isNotBlank() && it != "null" }
            fun optionalNumber(key: String): Double? {
                if (!root.has(key) || root.isNull(key)) return null
                val value = root.optDouble(key, Double.NaN)
                return value.takeIf { it.isFinite() && it >= 0.0 }
            }

            val chart = root.optJSONArray("totalDataChart") ?: JSONArray()
            val points = mutableListOf<DefiLlamaDailyPoint>()
            for (index in 0 until chart.length()) {
                val pair = chart.optJSONArray(index) ?: continue
                if (pair.length() < 2) continue
                val timestampSeconds = pair.optLong(0, -1L)
                val value = pair.optDouble(1, Double.NaN)
                if (timestampSeconds <= 0L || !value.isFinite() || value < 0.0) continue
                points += DefiLlamaDailyPoint(timestampSeconds * 1000L, value)
            }
            val ordered = points.distinctBy { it.timestampEpochMs }.sortedBy { it.timestampEpochMs }
            return DefiLlamaMetricSeries(
                metric = metric,
                protocolSlug = protocolSlug,
                protocolName = name,
                total24hUsd = optionalNumber("total24h"),
                total48hTo24hUsd = optionalNumber("total48hto24h"),
                total7dUsd = optionalNumber("total7d"),
                totalAllTimeUsd = optionalNumber("totalAllTime"),
                dailyPoints = ordered,
                sourceUrl = sourceUrl,
                retrievedAtEpochMs = retrievedAtEpochMs
            )
        }
    }
}
