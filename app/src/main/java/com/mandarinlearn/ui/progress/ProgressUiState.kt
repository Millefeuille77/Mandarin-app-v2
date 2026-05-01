// ProgressUiState.kt — Mandarin Learn
// UI state for ProgressScreen. Phase 1: placeholder.
// Full implementation in Phase 8 (Progress & Dashboard).

package com.mandarinlearn.ui.progress

/**
 * UI state for [ProgressScreen].
 * TODO(phase_8): Add Content(streak, hskLevelProgress, examScores, readinessPerLevel).
 */
sealed class ProgressUiState {
    data object Loading : ProgressUiState()
    data object Placeholder : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}
