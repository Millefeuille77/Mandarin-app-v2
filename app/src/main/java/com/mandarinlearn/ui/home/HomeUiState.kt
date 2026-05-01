// HomeUiState.kt — Mandarin Learn
// UI state for HomeScreen. Implements MVVM pattern per ARCHITECTURE.md §6.
// Phase 8 will populate this with real data fields (streak, due count, progress).
// No LiveData — StateFlow only (CLAUDE.md hard rule).

package com.mandarinlearn.ui.home

/**
 * UI state sealed class for [HomeScreen].
 * Phase 1: placeholder. Full implementation in Phase 8 (Progress & Dashboard).
 *
 * TODO(phase_8): Add Content data class with streak, dueCount, hskProgress fields.
 */
sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Placeholder : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
