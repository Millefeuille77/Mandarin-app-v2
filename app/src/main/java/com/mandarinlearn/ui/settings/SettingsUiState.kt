// SettingsUiState.kt — Mandarin Learn
// UI state for SettingsScreen. Phase 1: placeholder.
// Full implementation in Phase 9 (Settings & Polish).

package com.mandarinlearn.ui.settings

/**
 * UI state for [SettingsScreen].
 * TODO(phase_9): Add Content(theme, fontSize, audioSpeed, dailyNewCards, pinyinDefault, etc.)
 */
sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data object Placeholder : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
