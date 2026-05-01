// ExamHubViewModel.kt — Mandarin Learn
// ViewModel for ExamHubScreen. Exposes StateFlow<ExamHubUiState>.
// Phase 7 will add ExamRepository queries (past results per level).

package com.mandarinlearn.ui.exam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [ExamHubScreen].
 * Phase 1: skeleton. TODO(phase_7): Inject ExamRepository.
 */
class ExamHubViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ExamHubUiState>(ExamHubUiState.Placeholder)

    val uiState: StateFlow<ExamHubUiState> = _uiState.asStateFlow()
}
