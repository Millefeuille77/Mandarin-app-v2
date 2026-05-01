// ExamViewModel.kt — Mandarin Learn
// ViewModel for ExamScreen. Exposes StateFlow<ExamUiState>.
// Phase 7 will add timer, section management, answer recording, ExamGrader.

package com.mandarinlearn.ui.exam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [ExamScreen].
 * Phase 1: skeleton.
 * TODO(phase_7): Inject ExamRepository, SampleQuestionRepository, AudioRepository.
 */
class ExamViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Placeholder)

    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()
}
