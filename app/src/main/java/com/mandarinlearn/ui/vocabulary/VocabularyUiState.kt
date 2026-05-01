// VocabularyUiState.kt — Mandarin Learn
// UI state for VocabularyScreen.
// UX_SPECIFICATION.md §4 Screen 2: word list, search, stats strip, start-flashcards button.

package com.mandarinlearn.ui.vocabulary

import com.mandarinlearn.domain.model.VocabularyWord

/**
 * Sealed UI state for VocabularyScreen.
 * Exposed as StateFlow by VocabularyViewModel.
 */
sealed class VocabularyUiState {

    /** Initial loading while Room query runs. */
    data object Loading : VocabularyUiState()

    /**
     * Words loaded and ready to display.
     *
     * @param words         Filtered word list (empty if search has no results).
     * @param selectedLevel Active HSK chip (1–5).
     * @param searchQuery   Current search text (empty = no filter).
     * @param totalCount    Total words for this level.
     * @param masteredCount Words meeting the mastered definition (rep≥5, ef≥2.5, int≥21).
     * @param dueCount      Cards due today (is_introduced=1, next_review_date <= today).
     * @param newCount      Cards not yet introduced for this level.
     */
    data class Content(
        val words: List<VocabularyWord>,
        val selectedLevel: Int,
        val searchQuery: String,
        val totalCount: Int,
        val masteredCount: Int,
        val dueCount: Int,
        val newCount: Int,
    ) : VocabularyUiState()

    /** Room threw an unexpected exception. */
    data class Error(val message: String) : VocabularyUiState()
}
