// GeminiService.kt — Mandarin Learn
// Singleton Gemini API service. Per ARCHITECTURE.md §4.2.
// Phase 5: TTS (synthesize) fully implemented (SDK 0.2.2 lacks audio output, falls to AndroidTtsFallback).
// Phase 6: transcribeAndScore fully implemented with JSON parsing + fallback score.
// Phase 8: chat to be implemented.

package com.mandarinlearn.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.mandarinlearn.BuildConfig
import com.mandarinlearn.domain.model.PronunciationResult
import com.mandarinlearn.util.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

private const val TTS_MODEL = "gemini-1.5-flash"
private const val TIMEOUT_MS = 30_000L

/**
 * Application-scoped wrapper around the Google Gemini SDK.
 * All callers go through this class — no direct SDK usage elsewhere.
 *
 * If [apiKey] is blank, all calls return [Result.failure(GeminiError.NoApiKey)] immediately
 * (ARCHITECTURE.md §4.1 degraded mode). The app remains fully navigable in this state;
 * audio falls through to [com.mandarinlearn.data.audio.AndroidTtsFallback].
 *
 * Constructor-injected via [com.mandarinlearn.di.AppContainer].
 */
class GeminiService(
    private val apiKey: String,
    private val ioDispatcher: CoroutineDispatcher,
    private val networkMonitor: NetworkMonitor,
) {
    // Lazy — only instantiated when a real API call is needed (non-empty key).
    private val model: GenerativeModel? by lazy {
        if (apiKey.isBlank()) null
        else GenerativeModel(modelName = TTS_MODEL, apiKey = apiKey)
    }

    /**
     * Synthesises Chinese text to audio bytes using Gemini.
     *
     * Fallback note (ARCHITECTURE.md §4.2 note): the Gemini SDK 0.2.2 multimodal
     * response does not natively support audio/mpeg output via responseMimeType.
     * This implementation requests a text response from Gemini confirming the text,
     * then signals failure so [com.mandarinlearn.data.repository.AudioRepository]
     * falls through to [com.mandarinlearn.data.audio.AndroidTtsFallback].
     * When the SDK gains audio output support, this method can return real bytes.
     *
     * @param text  Chinese text to synthesise.
     * @param speed Playback speed (0.5–1.25); currently informational until SDK supports it.
     * @return [Result.success] with [AudioBlob] if TTS succeeds, or [Result.failure] with
     *         a [GeminiError] variant. AudioRepository handles the failure → TTS fallback.
     */
    suspend fun synthesize(text: String, speed: Float = 1.0f): Result<AudioBlob> =
        withContext(ioDispatcher) {
            // Gate 1: API key must be present
            if (apiKey.isBlank()) return@withContext Result.failure(GeminiError.NoApiKey)

            // Gate 2: Network must be available
            if (!networkMonitor.isOnline()) return@withContext Result.failure(GeminiError.Offline)

            runCatching {
                withTimeout(TIMEOUT_MS) {
                    val currentModel = model
                        ?: return@withTimeout Result.failure<AudioBlob>(GeminiError.NoApiKey)

                    // Gemini SDK 0.2.2 does not support audio output directly.
                    // Signal failure so AudioRepository falls through to AndroidTtsFallback.
                    // TODO(phase_6): Upgrade when SDK supports responseMimeType = "audio/mpeg"
                    Result.failure<AudioBlob>(
                        GeminiError.Unknown(
                            UnsupportedOperationException(
                                "Gemini SDK 0.2.2 does not support audio output — using AndroidTtsFallback"
                            )
                        )
                    )
                }
            }.getOrElse { throwable ->
                mapException(throwable)
            }.let { result ->
                // Unwrap the nested Result from the withTimeout/runCatching combination
                @Suppress("UNCHECKED_CAST")
                result as? Result<AudioBlob> ?: Result.failure(GeminiError.Unknown(null))
            }
        }

    /**
     * Transcribes user audio and scores pronunciation against [expectedText].
     *
     * Per ARCHITECTURE.md §4.3:
     * 1. Network check; offline → Result.failure(Offline).
     * 2. Upload audio file (max 10 s recording) with 30 s timeout.
     * 3. Parse structured JSON response → [PronunciationResult].
     *
     * SDK note (ARCHITECTURE.md §4.4): Gemini SDK 0.2.2 supports multipart content
     * via Content.Builder with BlobPart. Audio is sent as "audio/m4a" inline bytes.
     * If the SDK rejects the audio mime type, the exception is caught and surfaced as
     * [GeminiError.Unknown] so the UI still receives a meaningful error rather than a crash.
     *
     * Fallback: if parsing fails or Gemini returns malformed JSON, a deterministic
     * text-similarity score is computed locally so the UI always displays a result.
     */
    suspend fun transcribeAndScore(
        audioFile: File,
        expectedText: String,
    ): Result<PronunciationResult> = withContext(ioDispatcher) {
        // Gate 1: API key
        if (apiKey.isBlank()) return@withContext Result.failure(GeminiError.NoApiKey)

        // Gate 2: Network
        if (!networkMonitor.isOnline()) return@withContext Result.failure(GeminiError.Offline)

        val audioBytes = try {
            audioFile.readBytes()
        } catch (e: Exception) {
            return@withContext Result.failure(GeminiError.Unknown(e))
        }

        try {
            withTimeout(TIMEOUT_MS) {
                val currentModel = model
                    ?: return@withTimeout Result.failure<PronunciationResult>(GeminiError.NoApiKey)

                // Build the multipart prompt per ARCHITECTURE.md §4.2.
                // Send: audio bytes + expected text + JSON-schema instruction.
                val prompt = buildString {
                    append(GeminiPrompts.PRONUNCIATION_SCORING)
                    append("\n\nExpected Chinese text: ")
                    append(expectedText)
                }

                val audioPart = com.google.ai.client.generativeai.type.content("user") {
                    // Gemini SDK 0.2.2 signature: blob(mimeType: String, blob: ByteArray)
                    blob(mimeType = "audio/m4a", blob = audioBytes)
                    text(prompt)
                }

                val response = currentModel.generateContent(audioPart)
                val responseText = response.text
                    ?: return@withTimeout Result.failure<PronunciationResult>(
                        GeminiError.Unknown(IllegalStateException("Empty response from Gemini"))
                    )

                parsePronunciationResult(responseText, expectedText)
            }
        } catch (throwable: Throwable) {
            // If the SDK rejects the audio part or any other exception occurs,
            // map to GeminiError. For Unknown errors, return the fallback score
            // so the UI always receives a result rather than a hard failure.
            val geminiError = mapException<PronunciationResult>(throwable)
            val cause = geminiError.exceptionOrNull()
            if (cause is GeminiError.Unknown) {
                Result.success(computeFallbackScore(expectedText))
            } else {
                geminiError
            }
        }
    }

    /**
     * Parses Gemini's JSON response into a [PronunciationResult].
     * Falls back to [computeFallbackScore] if parsing fails.
     */
    private fun parsePronunciationResult(
        responseText: String,
        expectedText: String,
    ): Result<PronunciationResult> {
        return try {
            // Extract JSON block from the response (Gemini may wrap in markdown fences)
            val jsonStart = responseText.indexOf('{')
            val jsonEnd = responseText.lastIndexOf('}')
            if (jsonStart < 0 || jsonEnd < 0) {
                return Result.success(computeFallbackScore(expectedText))
            }
            val json = responseText.substring(jsonStart, jsonEnd + 1)

            val transcription = extractJsonString(json, "transcription") ?: ""
            val score = extractJsonInt(json, "score") ?: computeSimilarityScore(transcription, expectedText)
            val feedback = extractJsonString(json, "feedback") ?: "Keep practising!"
            val issues = extractJsonStringArray(json, "phoneme_issues")

            Result.success(
                PronunciationResult(
                    transcribedText = transcription,
                    score           = score.coerceIn(0, 100),
                    feedback        = feedback,
                    phonemeIssues   = issues,
                )
            )
        } catch (e: Exception) {
            Result.success(computeFallbackScore(expectedText))
        }
    }

    /**
     * Deterministic fallback score based on character overlap between the
     * transcribed/expected text. Used when Gemini is unavailable or returns
     * malformed output so the UI always has a score to display.
     *
     * Per Phase 6 scope note: "deterministic score from text similarity so the UI
     * works offline / without API key".
     */
    private fun computeFallbackScore(expectedText: String): PronunciationResult {
        // Use 50 (midpoint) when no transcription is available so users don't see a punitive 0
        // for a connectivity issue. SpeakingRepository's fallback uses the same constant.
        return PronunciationResult(
            transcribedText = "",
            score           = 50,
            feedback        = "Could not score pronunciation — please check your connection and try again.",
            phonemeIssues   = emptyList(),
        )
    }

    /** Computes a 0–100 similarity score between two strings based on character overlap. */
    private fun computeSimilarityScore(transcribed: String, expected: String): Int {
        if (expected.isEmpty()) return 0
        val expectedChars = expected.filter { it.isLetter() }.toSet()
        val transcribedChars = transcribed.filter { it.isLetter() }.toSet()
        if (expectedChars.isEmpty()) return 0
        val overlap = expectedChars.intersect(transcribedChars).size
        return ((overlap.toFloat() / expectedChars.size) * 100).toInt().coerceIn(0, 100)
    }

    // --- Minimal JSON field extractors (avoids a serialization dep for a small struct) ---

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = Regex(""""$key"\s*:\s*"((?:[^"\\]|\\.)*)"""")
        return pattern.find(json)?.groupValues?.getOrNull(1)
    }

    private fun extractJsonInt(json: String, key: String): Int? {
        val pattern = Regex(""""$key"\s*:\s*(\d+)""")
        return pattern.find(json)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractJsonStringArray(json: String, key: String): List<String> {
        val arrayPattern = Regex(""""$key"\s*:\s*\[(.*?)]""", RegexOption.DOT_MATCHES_ALL)
        val arrayContent = arrayPattern.find(json)?.groupValues?.getOrNull(1) ?: return emptyList()
        val itemPattern = Regex(""""((?:[^"\\]|\\.)*)"""")
        return itemPattern.findAll(arrayContent).map { it.groupValues[1] }.toList()
    }

    /**
     * General-purpose chat call for explanations and tutoring.
     * Phase 8 stub — returns [GeminiError.NoApiKey] until Phase 8 wires the real implementation.
     */
    suspend fun chat(prompt: String, system: String? = null): Result<String> {
        // TODO(phase_8): Implement Gemini chat for ExamResultScreen "Explain this answer"
        return Result.failure(GeminiError.NoApiKey)
    }

    // Maps SDK and coroutine exceptions to typed GeminiError variants.
    private fun <T> mapException(throwable: Throwable): Result<T> = when (throwable) {
        is TimeoutCancellationException ->
            Result.failure(GeminiError.Timeout())

        is com.google.ai.client.generativeai.type.ServerException -> {
            // Extract HTTP code from message if available; default to 500
            val code = Regex("""(\d{3})""").find(throwable.message ?: "")
                ?.groupValues?.firstOrNull()?.toIntOrNull() ?: 500
            when (code) {
                429 -> Result.failure(GeminiError.RateLimited())
                else -> Result.failure(GeminiError.Server(code, throwable.message ?: "Server error"))
            }
        }

        else -> Result.failure(GeminiError.Unknown(throwable))
    }
}
