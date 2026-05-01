// ListeningUiState.kt — Mandarin Learn
// Full Phase 5 UI state for ListeningScreen.
// UX_SPECIFICATION.md §4 Screen 7: play audio, 4-option multiple-choice, immediate feedback.

package com.mandarinlearn.ui.listening

import com.mandarinlearn.domain.model.SampleQuestion

/**
 * Sealed UI state for [ListeningScreen].
 * Exposed as [kotlinx.coroutines.flow.StateFlow] by [ListeningViewModel].
 */
sealed class ListeningUiState {

    /** Loading questions from Room. */
    data object Loading : ListeningUiState()

    /** Questions loaded, session active. */
    data class Content(
        val currentQuestion: SampleQuestion,
        /** All 4 answer options for the current question (including distractors). */
        val options: List<String>,
        /** Index into [options] that maps to the correct answer label. */
        val correctOptionIndex: Int,
        /** 1-based question index for display ("Question 3 of 10"). */
        val questionIndex: Int,
        val totalQuestions: Int,
        /** True while AudioRepository is loading or playing audio. */
        val isAudioPlaying: Boolean = false,
        /** True when audio could not be loaded (show "Skip" option per UX spec). */
        val audioFailed: Boolean = false,
        /** Number of times the user has replayed audio for this question (cap = 3). */
        val replayCount: Int = 0,
        /** User's selected option index, or null before answering. */
        val selectedOptionIndex: Int? = null,
        /** True after the user taps an answer (locks options, shows feedback). */
        val hasAnswered: Boolean = false,
        /** True iff the selected answer is correct. Only meaningful when [hasAnswered]. */
        val isCorrect: Boolean = false,
        /** HSK level for this session. */
        val hskLevel: Int = 1,
    ) : ListeningUiState()

    /** Session finished — all questions answered. */
    data class SessionComplete(
        val correctCount: Int,
        val totalQuestions: Int,
        val hskLevel: Int,
    ) : ListeningUiState()

    /** No listening questions available for the selected HSK level. */
    data class Empty(val hskLevel: Int) : ListeningUiState()

    /** Unrecoverable error loading questions. */
    data class Error(val message: String) : ListeningUiState()
}
