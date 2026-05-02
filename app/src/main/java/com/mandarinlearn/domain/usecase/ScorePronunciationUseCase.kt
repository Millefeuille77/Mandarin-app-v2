// ScorePronunciationUseCase.kt — Mandarin Learn
// Use case wrapping SpeakingRepository.scoreRecording.
// Per IMPLEMENTATION_PLAN.md Phase 6 and FOLDER_STRUCTURE.md.

package com.mandarinlearn.domain.usecase

import com.mandarinlearn.data.repository.SpeakingRepository
import com.mandarinlearn.domain.model.PronunciationResult
import java.io.File

/**
 * Domain use case for scoring a pronunciation recording.
 *
 * Delegates to [SpeakingRepository.scoreRecording], which handles
 * the Gemini STT call and the text-similarity fallback.
 * ViewModels call this use case — they never touch the repository directly.
 */
class ScorePronunciationUseCase(
    private val speakingRepository: SpeakingRepository,
) {

    /**
     * Scores the pronunciation in [audioFile] against [expectedText].
     *
     * @param audioFile    The M4A recording file from [AudioRecorder].
     * @param expectedText The Chinese phrase the user was asked to say.
     * @return [Result.success] with [PronunciationResult] (score 0–100),
     *         or [Result.failure] with a [com.mandarinlearn.data.remote.GeminiError].
     */
    suspend operator fun invoke(
        audioFile: File,
        expectedText: String,
    ): Result<PronunciationResult> =
        speakingRepository.scoreRecording(audioFile, expectedText)
}
