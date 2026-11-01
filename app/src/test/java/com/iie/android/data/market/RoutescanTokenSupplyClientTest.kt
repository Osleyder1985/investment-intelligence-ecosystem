package com.iie.android.data.market

import java.io.IOException
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RoutescanTokenSupplyClientTest {
    @Test
    fun parsesLargeUint256WithoutFloatingPointConversion() {
        val value = "0x" + BigInteger("1000000000000000000000000000001").toString(16)
        assertEquals(
            BigInteger("1000000000000000000000000000001"),
            RoutescanTokenSupplyClient.parseHexQuantity(value, "totalSupply")
        )
    }

    @Test
    fun parsesZeroAndMaximumUint8Decimals() {
        assertEquals(BigInteger.ZERO, RoutescanTokenSupplyClient.parseHexQuantity("0x0", "decimals"))
        assertEquals(BigInteger.valueOf(255L), RoutescanTokenSupplyClient.parseHexQuantity("0xff", "decimals"))
    }

    @Test
    fun rejectsMissingPrefixEmptyAndNonHexValues() {
        listOf("", "0x", "1234", "0xgg", "0x-1").forEach { value ->
            assertThrows(IOException::class.java) {
                RoutescanTokenSupplyClient.parseHexQuantity(value, "totalSupply")
            }
        }
    }

    @Test
    fun rejectsValuesLongerThanUint256() {
        val oversized = "0x" + "f".repeat(65)
        assertThrows(IOException::class.java) {
            RoutescanTokenSupplyClient.parseHexQuantity(oversized, "totalSupply")
        }
    }

    @Test
    fun acceptsFullWidthUint256() {
        val maxUint256 = "0x" + "f".repeat(64)
        assertEquals(
            BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE),
            RoutescanTokenSupplyClient.parseHexQuantity(maxUint256, "totalSupply")
        )
    }
}
