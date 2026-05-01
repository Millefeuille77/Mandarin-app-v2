// BackoffPolicy.kt — Mandarin Learn
// Exponential backoff helper for Gemini API rate-limit retries.
// Per ARCHITECTURE.md §4.4: initial delay 1s, multiplier 2.0, max 3 attempts.

package com.mandarinlearn.data.remote

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Reusable exponential backoff policy for API calls.
 *
 * Usage:
 * ```kotlin
 * BackoffPolicy.retry(maxAttempts = 3) {
 *     geminiService.synthesize(text, speed)
 * }
 * ```
 *
 * Per ARCHITECTURE.md §4.4: initial 1 s, multiplier 2.0, max 3 attempts.
 * On [GeminiError.RateLimited], the retryAfterMs from the error is honoured if longer.
 */
object BackoffPolicy {

    /**
     * Retries [block] up to [maxAttempts] times with exponential backoff.
     *
     * @param maxAttempts   Maximum number of attempts (including the first).
     * @param initialDelay  Delay before the second attempt.
     * @param multiplier    Factor applied to the delay on each subsequent retry.
     * @param block         Suspending block that returns [Result<T>].
     * @return The first successful [Result], or the last failure after all attempts.
     */
    suspend fun <T> retry(
        maxAttempts: Int = 3,
        initialDelay: Duration = 1.seconds,
        multiplier: Double = 2.0,
        block: suspend () -> Result<T>,
    ): Result<T> {
        var currentDelay = initialDelay
        var lastResult: Result<T> = Result.failure(GeminiError.Unknown(null))

        repeat(maxAttempts) { attempt ->
            lastResult = block()

            // Return immediately on success or non-retriable error
            if (lastResult.isSuccess) return lastResult
            val error = lastResult.exceptionOrNull()
            if (error !is GeminiError.RateLimited) return lastResult

            // Rate-limited: wait, then retry (unless last attempt)
            if (attempt < maxAttempts - 1) {
                // Honour the Retry-After header if it specifies a longer delay
                val retryAfter = error.retryAfterMs.milliseconds
                val actualDelay = maxOf(currentDelay, retryAfter)
                delay(actualDelay)
                currentDelay *= multiplier
            }
        }

        return lastResult
    }
}
