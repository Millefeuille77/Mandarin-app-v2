// PracticeHubUiState.kt — Mandarin Learn
// UI state for PracticeHubScreen.
// UX_SPECIFICATION.md §2: Practice tab lists 4 sub-sections.

package com.mandarinlearn.ui.practice

/**
 * Sealed UI state for PracticeHubScreen.
 * Exposed as StateFlow by PracticeHubViewModel.
 */
sealed class PracticeHubUiState {

    /** Loading section info. */
    data object Loading : PracticeHubUiState()

    /**
     * Ready to display the 4 practice sub-sections.
     *
     * @param selectedLevel The currently selected HSK level (defaults to 1).
     * @param vocabDueCount Cards due today for vocab review at the selected level.
     */
    data class Content(
        val selectedLevel: Int,
        val vocabDueCount: Int,
    ) : PracticeHubUiState()

    /** Error loading section data. */
    data class Error(val message: String) : PracticeHubUiState()
}
