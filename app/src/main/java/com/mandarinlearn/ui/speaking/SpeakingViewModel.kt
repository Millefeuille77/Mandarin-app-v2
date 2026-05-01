// SpeakingViewModel.kt — Mandarin Learn
// ViewModel for SpeakingScreen. Exposes StateFlow<SpeakingUiState>.
// Phase 6 will add AudioRecorder, GeminiService STT, permission handling.

package com.mandarinlearn.ui.speaking

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [SpeakingScreen].
 * Phase 1: skeleton. TODO(phase_6): Inject SpeakingRepository, AudioRepository.
 */
class SpeakingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SpeakingUiState>(SpeakingUiState.Placeholder)

    val uiState: StateFlow<SpeakingUiState> = _uiState.asStateFlow()
}
