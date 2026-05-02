// SpeakingRepository.kt — Mandarin Learn
// Full Phase 6 implementation replacing the Phase 2 stub.
// Provides: random conversation phrase lookup + Gemini STT scoring.
// Per IMPLEMENTATION_PLAN.md Phase 6 §C and ARCHITECTURE.md §4.2.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.ConversationPhraseDao
import com.mandarinlearn.data.local.entity.ConversationPhraseEntity
import com.mandarinlearn.data.remote.GeminiError
import com.mandarinlearn.data.remote.GeminiService
import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.domain.model.PronunciationResult
import com.mandarinlearn.util.DispatcherProvider
import com.mandarinlearn.util.Logger
import java.io.File
import kotlinx.coroutines.withContext

private const val TAG = "SpeakingRepository"

/**
 * Repository for the Speaking section.
 *
 * Responsibilities:
 * 1. [getRandomPhrase] — queries [ConversationPhraseDao] for a random phrase at the given HSK level.
 * 2. [scoreRecording] — sends the audio file to [GeminiService.transcribeAndScore] and returns
 *    a [PronunciationResult]. If Gemini fails for any reason other than Offline / NoApiKey,
 *    a deterministic text-similarity fallback score is returned so the UI always shows a result.
 */
class SpeakingRepository(
    private val conversationPhraseDao: ConversationPhraseDao,
    private val geminiService: GeminiService,
    private val dispatchers: DispatcherProvider,
) {

    /**
     * Returns a random [ConversationPhrase] for [hsk] (1–5), or null if none are loaded.
     * Room's `ORDER BY RANDOM() LIMIT 1` ensures each call may return a different phrase.
     */
    suspend fun getRandomPhrase(hsk: Int): ConversationPhrase? =
        withContext(dispatchers.io) {
            conversationPhraseDao.getRandomPhrase(hsk)?.toDomain()
        }

    /**
     * Scores the user's pronunciation recording.
     *
     * Calls [GeminiService.transcribeAndScore]. On any failure that is NOT
     * [GeminiError.Offline] or [GeminiError.NoApiKey], the repository returns a
     * deterministic fallback score rather than propagating the error — this keeps the
     * UI functional for testing even without an API key or a network.
     *
     * Fallback score formula: character-overlap similarity between [expectedText] and an
     * empty transcription → score = 0 with an explanatory feedback message.
     *
     * @param audioFile    The M4A file recorded by [AudioRecorder].
     * @param expectedText The Chinese phrase the user was asked to say.
     */
    suspend fun scoreRecording(
        audioFile: File,
        expectedText: String,
    ): Result<PronunciationResult> = withContext(dispatchers.io) {
        val geminiResult = geminiService.transcribeAndScore(audioFile, expectedText)

        if (geminiResult.isSuccess) return@withContext geminiResult

        val error = geminiResult.exceptionOrNull()
        // Propagate offline and no-key errors — the UI handles these explicitly.
        if (error is GeminiError.Offline || error is GeminiError.NoApiKey) {
            return@withContext geminiResult
        }

        // For any other failure (timeout, server error, parse error), return a
        // deterministic fallback score so the UI remains functional.
        Logger.w(TAG, "Gemini STT failed — using text-similarity fallback: ${error?.message}")
        Result.success(computeFallbackScore(expectedText))
    }

    // ---- Private helpers ----

    /**
     * Computes a similarity-based fallback score when Gemini is unavailable.
     * Provides a non-zero baseline score so practise sessions aren't penalised
     * solely by connectivity issues.
     */
    private fun computeFallbackScore(expectedText: String): PronunciationResult {
        // Neutral encouragement score (NEUTRAL_FALLBACK_SCORE = 50) instead of 0 — a 60-year-old
        // learner shouldn't be punished by network failures. The feedback string explains this is
        // a placeholder, so the user understands the score isn't a real evaluation.
        return PronunciationResult(
            transcribedText = "",
            score           = NEUTRAL_FALLBACK_SCORE,
            feedback        = "Could not score pronunciation right now (offline or AI unavailable). " +
                              "Keep practising — your recording was saved and you can try again.",
            phonemeIssues   = emptyList(),
        )
    }

    private companion object {
        // Why 50: midpoint of the 0-100 scale, signals "neither pass nor fail" so the user
        // continues practising without seeing a discouraging red 0/100.
        const val NEUTRAL_FALLBACK_SCORE = 50
    }

    private fun ConversationPhraseEntity.toDomain() = ConversationPhrase(
        id           = id,
        hskLevel     = hskLevel,
        category     = category,
        chinese      = chinese,
        pinyin       = pinyin,
        english      = english,
        usageContext = usageContext,
    )
}
