// HomeViewModel.kt — Mandarin Learn
// ViewModel for HomeScreen. Exposes StateFlow<HomeUiState> (no LiveData).
// Phase 8 will add real Room queries (streak, due count, HSK progress).
// Manual DI via companion-object factory (no Hilt/Dagger — ARCHITECTURE.md §6).

package com.mandarinlearn.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [HomeScreen].
 * Phase 1: skeleton with placeholder state.
 *
 * TODO(phase_8): Inject GetDashboardUseCase, StreakRepository; emit real Content state.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Placeholder)

    /** Observable UI state for HomeScreen. Never LiveData. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
