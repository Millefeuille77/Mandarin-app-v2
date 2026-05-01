// ExamResultUiState.kt — Mandarin Learn
// UI state for ExamResultScreen. Phase 1: placeholder.
// Full implementation in Phase 7 (Exam Section).

package com.mandarinlearn.ui.exam

/**
 * UI state for [ExamResultScreen].
 * TODO(phase_7): Add Content(result, sectionScores, wrongAnswers, history).
 */
sealed class ExamResultUiState {
    data object Loading : ExamResultUiState()
    data object Placeholder : ExamResultUiState()
    data class Error(val message: String) : ExamResultUiState()
}
