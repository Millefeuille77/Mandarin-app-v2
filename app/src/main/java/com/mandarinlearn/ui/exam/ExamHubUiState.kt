// ExamHubUiState.kt — Mandarin Learn
// UI state for ExamHubScreen. Phase 1: placeholder.
// Full implementation in Phase 7 (Exam Section).

package com.mandarinlearn.ui.exam

/**
 * UI state for [ExamHubScreen].
 * TODO(phase_7): Add Content(levels with best score / attempts per level).
 */
sealed class ExamHubUiState {
    data object Loading : ExamHubUiState()
    data object Placeholder : ExamHubUiState()
    data class Error(val message: String) : ExamHubUiState()
}
