// MeViewModel.kt — Mandarin Learn
// ViewModel for MeScreen. Exposes StateFlow<MeUiState>.
// Phase 8 will add streak data and navigation to Progress/Settings.

package com.mandarinlearn.ui.me

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [MeScreen].
 * Phase 1: skeleton. TODO(phase_8): Inject StreakRepository, ProgressRepository.
 */
class MeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MeUiState>(MeUiState.Placeholder)

    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()
}
