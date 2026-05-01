// ExamUiState.kt — Mandarin Learn
// UI state for ExamScreen. Phase 1: placeholder.
// Full implementation in Phase 7 (Exam Section).

package com.mandarinlearn.ui.exam

/**
 * UI state for [ExamScreen].
 * TODO(phase_7): Add Content(section, question, timerSeconds, selectedOption, progress).
 */
sealed class ExamUiState {
    data object Loading : ExamUiState()
    data object Placeholder : ExamUiState()
    data class Error(val message: String) : ExamUiState()
}
