// MeUiState.kt — Mandarin Learn
// UI state for MeScreen. Phase 8: Progress & Dashboard.
// MeScreen is the Me tab root, linking to Progress and Settings.

package com.mandarinlearn.ui.me

/**
 * UI state for [MeScreen].
 *
 * The Me tab is a simple navigation hub; it shows the user's streak at a glance
 * and provides links to ProgressScreen and SettingsScreen.
 */
sealed class MeUiState {

    data object Loading : MeUiState()

    data class Content(
        val currentStreak: Int,
        val longestStreak: Int,
    ) : MeUiState()

    data class Error(val message: String) : MeUiState()
}
