// ExamResultViewModel.kt — Mandarin Learn
// ViewModel for ExamResultScreen. Exposes StateFlow<ExamResultUiState>.
// Phase 7 will load the exam result by id and past attempts history.

package com.mandarinlearn.ui.exam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [ExamResultScreen].
 * Phase 1: skeleton. TODO(phase_7): Inject ExamRepository, GeminiService (for explain).
 */
class ExamResultViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ExamResultUiState>(ExamResultUiState.Placeholder)

    val uiState: StateFlow<ExamResultUiState> = _uiState.asStateFlow()
}
