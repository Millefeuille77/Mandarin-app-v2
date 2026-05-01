// SpeakingRepository.kt — Mandarin Learn
// STUB for Phase 2. Full implementation in Phase 6 (Gemini STT integration).

package com.mandarinlearn.data.repository

import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.domain.model.PronunciationResult
import com.mandarinlearn.util.DispatcherProvider
import java.io.File

/**
 * Repository for speaking practice (conversation phrase lookup + STT scoring).
 * Phase 2: stub — scoring always returns a failure.
 * Phase 6: full implementation with Gemini STT.
 *
 * TODO(phase_6): Inject ConversationPhraseDao, GeminiService.
 */
class SpeakingRepository(
    @Suppress("UNUSED_PARAMETER") private val dispatchers: DispatcherProvider,
) {

    /**
     * Returns a random conversation phrase for the given HSK level.
     * Phase 2 stub always returns null.
     * TODO(phase_6): Query ConversationPhraseDao.getRandomPhrase(hsk).
     */
    suspend fun getRandomPhrase(hsk: Int): ConversationPhrase? = null

    /**
     * Submits an audio recording to Gemini for STT scoring.
     * Phase 2 stub always returns failure.
     * TODO(phase_6): Call GeminiService.transcribeAndScore(audioFile, expectedText).
     */
    suspend fun scoreRecording(
        audioFile: File,
        expectedText: String,
    ): Result<PronunciationResult> =
        Result.failure(UnsupportedOperationException("Speaking available in a future update"))
}
