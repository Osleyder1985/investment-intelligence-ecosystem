package com.iie.android.domain.model

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Exact ERC-20-style token quantity.
 *
 * rawAmount is the unsigned integer returned by the contract/provider before
 * applying token decimals. It is deliberately stored as text so persistence
 * and JSON transport never pass through floating-point arithmetic.
 */
data class ExactTokenAmount(
    val rawAmount: String,
    val decimals: Int
) {
    init {
        require(rawAmount.matches(Regex("0|[1-9][0-9]*"))) {
            "Raw token amount must be a canonical unsigned integer"
        }
        require(decimals in 0..255) {
            "Token decimals must fit the uint8 range"
        }
    }

    /** Exact human-readable decimal representation, with no scientific notation. */
    fun toDecimalString(): String =
        BigDecimal(BigInteger(rawAmount), decimals)
            .stripTrailingZeros()
            .toPlainString()
}
