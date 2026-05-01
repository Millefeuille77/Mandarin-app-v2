// SettingsViewModel.kt — Mandarin Learn
// ViewModel for SettingsScreen. Exposes StateFlow<SettingsUiState>.
// Phase 9 will add DataStore-backed preferences, export/import/reset actions.

package com.mandarinlearn.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [SettingsScreen].
 * Phase 1: skeleton.
 * TODO(phase_9): Inject UserPreferencesRepository, ExportProgressUseCase, ResetProgressUseCase.
 */
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Placeholder)

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
}
