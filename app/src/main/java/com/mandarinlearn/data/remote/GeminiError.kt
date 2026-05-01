// GeminiError.kt — Mandarin Learn
// Sealed class hierarchy for all Gemini API error variants.
// Per ARCHITECTURE.md §4.2 and §4.4 error-handling matrix.

package com.mandarinlearn.data.remote

/**
 * All errors that can be returned by [GeminiService].
 * UI code must handle every variant; the error matrix (ARCHITECTURE.md §4.4) maps
 * each variant to a specific UX response (snackbar, banner, retry button, etc.).
 */
sealed class GeminiError : Throwable() {

    /** API key is blank in BuildConfig — app was built without setting local.properties. */
    data object NoApiKey : GeminiError() {
        override val message: String = "Gemini API key is not configured"
    }

    /** Device has no network connection at the time of the call. */
    data object Offline : GeminiError() {
        override val message: String = "Device is offline"
    }

    /** The 30-second timeout fired before the API responded. */
    data class Timeout(override val message: String = "Request timed out after 30 seconds") :
        GeminiError()

    /**
     * HTTP 429 — API rate limit exceeded.
     * @param retryAfterMs Milliseconds to wait before retrying (from Retry-After header).
     */
    data class RateLimited(val retryAfterMs: Long = 1_000L) : GeminiError() {
        override val message: String = "Rate limited — retry after ${retryAfterMs}ms"
    }

    /**
     * HTTP 4xx / 5xx server error.
     * @param httpCode The HTTP status code.
     */
    data class Server(val httpCode: Int, override val message: String) : GeminiError()

    /**
     * Any other exception not captured above.
     * @param cause The underlying throwable.
     */
    data class Unknown(override val cause: Throwable?) : GeminiError() {
        override val message: String = cause?.message ?: "Unknown error"
    }
}
