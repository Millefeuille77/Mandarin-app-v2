// ImportLoadingUiState.kt — Mandarin Learn
// UI state for ImportLoadingScreen. Phase 2: full implementation.

package com.mandarinlearn.ui.importing

/**
 * UI state for [ImportLoadingScreen].
 * [Progress] is emitted repeatedly with a fraction 0f..1f and a status string.
 * [Done] means the import completed — the screen navigates away.
 * [Error] means the import failed — shows a Retry button.
 */
sealed class ImportLoadingUiState {
    data object Idle : ImportLoadingUiState()
    data class Progress(val fraction: Float, val message: String) : ImportLoadingUiState()
    data object Done : ImportLoadingUiState()
    data class Error(val message: String) : ImportLoadingUiState()
}
