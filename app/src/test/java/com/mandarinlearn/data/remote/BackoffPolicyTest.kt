// BackoffPolicyTest.kt — Mandarin Learn
// Unit tests for BackoffPolicy exponential retry logic.
// Per IMPLEMENTATION_PLAN.md Phase 5 acceptance criteria.

package com.mandarinlearn.data.remote

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [BackoffPolicy.retry].
 * Verifies: success on first attempt, retry on rate-limit, max-attempts cap,
 * non-retriable errors not retried.
 */
class BackoffPolicyTest {

    @Test
    fun `success on first attempt does not retry`() = runTest {
        var callCount = 0
        val result = BackoffPolicy.retry(
            maxAttempts  = 3,
            initialDelay = 1.milliseconds,
        ) {
            callCount++
            Result.success("ok")
        }

        assertTrue(result.isSuccess)
        assertEquals("ok", result.getOrNull())
        assertEquals(1, callCount)
    }

    @Test
    fun `rate-limited error triggers retries up to maxAttempts`() = runTest {
        var callCount = 0
        val result = BackoffPolicy.retry(
            maxAttempts  = 3,
            initialDelay = 1.milliseconds,
            multiplier   = 1.0,
        ) {
            callCount++
            Result.failure(GeminiError.RateLimited(retryAfterMs = 0L))
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.RateLimited)
        assertEquals(3, callCount) // All 3 attempts used
    }

    @Test
    fun `non-retriable error returns immediately without retrying`() = runTest {
        var callCount = 0
        val result = BackoffPolicy.retry(maxAttempts = 3, initialDelay = 1.milliseconds) {
            callCount++
            Result.failure(GeminiError.Timeout())
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.Timeout)
        assertEquals(1, callCount) // No retry for Timeout
    }

    @Test
    fun `succeeds on second attempt after rate limit`() = runTest {
        var callCount = 0
        val result = BackoffPolicy.retry(
            maxAttempts  = 3,
            initialDelay = 1.milliseconds,
        ) {
            callCount++
            if (callCount == 1) {
                Result.failure(GeminiError.RateLimited(retryAfterMs = 0L))
            } else {
                Result.success("recovered")
            }
        }

        assertTrue(result.isSuccess)
        assertEquals("recovered", result.getOrNull())
        assertEquals(2, callCount)
    }

    @Test
    fun `offline error is not retried`() = runTest {
        var callCount = 0
        val result = BackoffPolicy.retry(maxAttempts = 3, initialDelay = 1.milliseconds) {
            callCount++
            Result.failure(GeminiError.Offline)
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.Offline)
        assertEquals(1, callCount)
    }

    @Test
    fun `server error is not retried`() = runTest {
        var callCount = 0
        val result = BackoffPolicy.retry(maxAttempts = 3, initialDelay = 1.milliseconds) {
            callCount++
            Result.failure(GeminiError.Server(500, "Internal Server Error"))
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.Server)
        assertEquals(1, callCount)
    }

    @Test
    fun `no api key error is not retried`() = runTest {
        var callCount = 0
        val result = BackoffPolicy.retry(maxAttempts = 3, initialDelay = 1.milliseconds) {
            callCount++
            Result.failure(GeminiError.NoApiKey)
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.NoApiKey)
        assertEquals(1, callCount)
    }
}
