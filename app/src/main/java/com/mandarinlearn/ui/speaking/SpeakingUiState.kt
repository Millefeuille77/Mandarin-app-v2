// SpeakingUiState.kt — Mandarin Learn
// UI state for SpeakingScreen. Phase 1: placeholder.
// Full implementation in Phase 6 (Speaking Section / Gemini STT).

package com.mandarinlearn.ui.speaking

/**
 * UI state for [SpeakingScreen].
 * TODO(phase_6): Add Content(phrase, isRecording, score, feedback, permissionGranted).
 */
sealed class SpeakingUiState {
    data object Loading : SpeakingUiState()
    data object Placeholder : SpeakingUiState()
    data class Error(val message: String) : SpeakingUiState()
}
