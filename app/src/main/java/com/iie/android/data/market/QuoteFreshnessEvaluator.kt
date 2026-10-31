package com.iie.android.data.market

enum class QuoteFreshnessStatus { FRESH, STALE, EXPIRED, UNKNOWN }

data class QuoteFreshnessResult(
    val status: QuoteFreshnessStatus,
    val ageMs: Long?,
    val reason: String
)

/**
 * Local display policy only. It measures provider timestamp age; it does not
 * certify quote accuracy or provider publication guarantees.
 */
class QuoteFreshnessEvaluator(
    private val freshForMs: Long = 10 * 60 * 1000L,
    private val staleForMs: Long = 60 * 60 * 1000L
) {
    init {
        require(freshForMs > 0L)
        require(staleForMs >= freshForMs)
    }

    fun evaluate(providerUpdatedAtEpochMs: Long?, nowEpochMs: Long): QuoteFreshnessResult {
        if (providerUpdatedAtEpochMs == null || providerUpdatedAtEpochMs <= 0L) {
            return QuoteFreshnessResult(QuoteFreshnessStatus.UNKNOWN, null, "Timestamp del proveedor ausente o inválido")
        }
        if (nowEpochMs <= 0L || providerUpdatedAtEpochMs > nowEpochMs + FUTURE_TOLERANCE_MS) {
            return QuoteFreshnessResult(QuoteFreshnessStatus.UNKNOWN, null, "Reloj local o timestamp del proveedor fuera de rango")
        }
        val age = (nowEpochMs - providerUpdatedAtEpochMs).coerceAtLeast(0L)
        val status = when {
            age <= freshForMs -> QuoteFreshnessStatus.FRESH
            age <= staleForMs -> QuoteFreshnessStatus.STALE
            else -> QuoteFreshnessStatus.EXPIRED
        }
        val reason = when (status) {
            QuoteFreshnessStatus.FRESH -> "Dentro de la ventana local de frescura"
            QuoteFreshnessStatus.STALE -> "Cotización antigua; requiere actualización"
            QuoteFreshnessStatus.EXPIRED -> "Cotización caducada para visualización operativa"
            QuoteFreshnessStatus.UNKNOWN -> "No se pudo evaluar la frescura"
        }
        return QuoteFreshnessResult(status, age, reason)
    }

    companion object {
        const val FUTURE_TOLERANCE_MS = 60_000L
        const val FRESH_WINDOW_MS = 10 * 60 * 1000L
        const val STALE_WINDOW_MS = 60 * 60 * 1000L
    }
}
