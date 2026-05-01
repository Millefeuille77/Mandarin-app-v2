// AudioBlob.kt — Mandarin Learn
// Value type returned by GeminiService.synthesize() on success.
// Per ARCHITECTURE.md §4.2.

package com.mandarinlearn.data.remote

/**
 * Raw audio data returned by a successful Gemini TTS call.
 *
 * @param bytes    Raw audio bytes (MP3 or WAV depending on Gemini response).
 * @param mimeType MIME type of the audio data, e.g. "audio/mpeg".
 */
data class AudioBlob(
    val bytes: ByteArray,
    val mimeType: String,
) {
    // ByteArray requires manual equals/hashCode to compare contents, not references.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioBlob) return false
        return bytes.contentEquals(other.bytes) && mimeType == other.mimeType
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}
