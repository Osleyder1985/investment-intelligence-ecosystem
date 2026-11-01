package com.iie.android.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExactTokenAmountTest {
    @Test
    fun convertsLargeRawSupplyWithoutFloatingPointLoss() {
        val amount = ExactTokenAmount(
            rawAmount = "1000000000000000000000000000001",
            decimals = 18
        )
        assertEquals("1000000000000.000000000000000001", amount.toDecimalString())
    }

    @Test
    fun preservesIntegerWhenDecimalsAreZero() {
        assertEquals("1000000000000000000000000000001",
            ExactTokenAmount("1000000000000000000000000000001", 0).toDecimalString())
    }

    @Test
    fun normalizesTrailingFractionalZeros() {
        assertEquals("1.25", ExactTokenAmount("125000000", 8).toDecimalString())
        assertEquals("0", ExactTokenAmount("0", 18).toDecimalString())
    }

    @Test
    fun rejectsNegativeNonCanonicalAndMalformedRawValues() {
        listOf("-1", "+1", "01", "1.0", "").forEach { raw ->
            assertThrows(IllegalArgumentException::class.java) {
                ExactTokenAmount(raw, 18)
            }
        }
    }

    @Test
    fun rejectsDecimalsOutsideUint8Range() {
        assertThrows(IllegalArgumentException::class.java) { ExactTokenAmount("1", -1) }
        assertThrows(IllegalArgumentException::class.java) { ExactTokenAmount("1", 256) }
    }
}
