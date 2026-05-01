// MeUiState.kt — Mandarin Learn
// UI state for MeScreen. Phase 1: placeholder.
// Full implementation in Phase 8 (Progress & Dashboard).

package com.mandarinlearn.ui.me

/**
 * UI state for [MeScreen].
 * TODO(phase_8): Add Content(streak, level summaries) — Me tab shows links to Progress/Settings.
 */
sealed class MeUiState {
    data object Loading : MeUiState()
    data object Placeholder : MeUiState()
    data class Error(val message: String) : MeUiState()
}
