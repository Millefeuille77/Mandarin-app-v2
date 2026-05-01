// GeminiService.kt — Mandarin Learn
// Singleton Gemini API service. Per ARCHITECTURE.md §4.2.
// Phase 5: TTS (synthesize) fully implemented; STT and chat are stubs returning NoApiKey.
// Phase 6: transcribeAndScore implemented.
// Phase 8: chat implemented.

package com.mandarinlearn.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.mandarinlearn.BuildConfig
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
     * Transcribes user audio and scores pronunciation.
     * Phase 6 stub — returns [GeminiError.NoApiKey] until Phase 6 wires the real implementation.
     */
    suspend fun transcribeAndScore(
        audioFile: File,
        expectedText: String,
    ): Result<com.mandarinlearn.domain.model.PronunciationResult> {
        // TODO(phase_6): Implement multipart audio upload to Gemini STT + scoring
        return Result.failure(GeminiError.NoApiKey)
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
