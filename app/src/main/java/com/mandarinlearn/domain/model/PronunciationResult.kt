// PronunciationResult.kt — Mandarin Learn
// Domain model for a Gemini STT pronunciation scoring result.
// Used by SpeakingRepository and ScorePronunciationUseCase.

package com.mandarinlearn.domain.model

/**
 * Result of a Gemini-scored pronunciation attempt.
 * [score] is 0–100. [phonemeIssues] may be empty if no specific issues were identified.
 */
data class PronunciationResult(
    val transcribedText: String,
    val score: Int,
    val feedback: String,
    val phonemeIssues: List<String>,
)
