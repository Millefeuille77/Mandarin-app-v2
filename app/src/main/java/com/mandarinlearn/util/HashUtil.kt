// HashUtil.kt — Mandarin Learn
// SHA-256 hashing helper for audio cache keys. Per ARCHITECTURE.md §4.3.
// Cache key = sha256("text|voice|speed") — speed rounded to 2 decimals for key stability.

package com.mandarinlearn.util

import java.security.MessageDigest

/**
 * Utility for computing SHA-256 hashes used as audio cache keys.
 */
object HashUtil {

    /**
     * Returns a lowercase hex SHA-256 digest of the given UTF-8 string.
     * Used by AudioRepository to compute cache keys per ARCHITECTURE.md §4.3.
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes the cache key for a TTS audio clip.
     * Speed is rounded to 2 decimal places to keep keys stable across slider stops.
     */
    fun audioCacheKey(text: String, voice: String, speed: Float): String =
        sha256("$text|$voice|${"%.2f".format(speed)}")
}
