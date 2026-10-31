package com.iie.android.data.market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuoteFreshnessEvaluatorTest {
    private val evaluator = QuoteFreshnessEvaluator()
    private val now = 1_800_000_000_000L

    @Test fun freshAtTenMinuteBoundary() {
        val result = evaluator.evaluate(now - QuoteFreshnessEvaluator.FRESH_WINDOW_MS, now)
        assertEquals(QuoteFreshnessStatus.FRESH, result.status)
    }

    @Test fun staleAfterFreshWindow() {
        val result = evaluator.evaluate(now - QuoteFreshnessEvaluator.FRESH_WINDOW_MS - 1L, now)
        assertEquals(QuoteFreshnessStatus.STALE, result.status)
    }

    @Test fun expiredAfterOneHourWindow() {
        val result = evaluator.evaluate(now - QuoteFreshnessEvaluator.STALE_WINDOW_MS - 1L, now)
        assertEquals(QuoteFreshnessStatus.EXPIRED, result.status)
    }

    @Test fun missingTimestampIsUnknown() {
        val result = evaluator.evaluate(null, now)
        assertEquals(QuoteFreshnessStatus.UNKNOWN, result.status)
        assertNull(result.ageMs)
    }

    @Test fun futureTimestampBeyondToleranceIsUnknown() {
        val result = evaluator.evaluate(now + QuoteFreshnessEvaluator.FUTURE_TOLERANCE_MS + 1L, now)
        assertEquals(QuoteFreshnessStatus.UNKNOWN, result.status)
    }
}
