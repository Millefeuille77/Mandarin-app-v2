// ProgressViewModel.kt — Mandarin Learn
// ViewModel for ProgressScreen. Exposes StateFlow<ProgressUiState>.
// Phase 8 will add GetDashboardUseCase, ReadinessCalculator, exam history.

package com.mandarinlearn.ui.progress

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [ProgressScreen].
 * Phase 1: skeleton.
 * TODO(phase_8): Inject ProgressRepository, ExamRepository, ReadinessCalculator.
 */
class ProgressViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Placeholder)

    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
}
