package com.iie.android.domain.valuation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
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

data class DefiLlamaDailyPoint(val timestampEpochMs: Long, val valueUsd: Double)

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
            val root = Json.parseToJsonElement(payload).jsonObject
            fun optionalNumber(key: String): Double? {
                val value = root[key]?.jsonPrimitive?.doubleOrNull
                return value?.takeIf { it.isFinite() && it >= 0.0 }
            }
            val name = root["name"]?.jsonPrimitive?.content
                ?.takeIf { it.isNotBlank() && it != "null" }
            val chart = root["totalDataChart"] as? JsonArray ?: JsonArray(emptyList())
            val points = chart.mapNotNull { item ->
                val pair = item as? JsonArray ?: return@mapNotNull null
                if (pair.size < 2) return@mapNotNull null
                val timestampSeconds = pair[0].jsonPrimitive.longOrNull ?: return@mapNotNull null
                val value = pair[1].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
                if (timestampSeconds <= 0L || !value.isFinite() || value < 0.0) return@mapNotNull null
                DefiLlamaDailyPoint(timestampSeconds * 1000L, value)
            }.distinctBy { it.timestampEpochMs }.sortedBy { it.timestampEpochMs }

            return DefiLlamaMetricSeries(
                metric = metric,
                protocolSlug = protocolSlug,
                protocolName = name,
                total24hUsd = optionalNumber("total24h"),
                total48hTo24hUsd = optionalNumber("total48hto24h"),
                total7dUsd = optionalNumber("total7d"),
                totalAllTimeUsd = optionalNumber("totalAllTime"),
                dailyPoints = points,
                sourceUrl = sourceUrl,
                retrievedAtEpochMs = retrievedAtEpochMs
            )
        }
    }
}
