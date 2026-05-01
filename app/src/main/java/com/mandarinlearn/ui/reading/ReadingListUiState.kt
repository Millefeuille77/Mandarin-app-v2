// ReadingListUiState.kt — Mandarin Learn
// UI state for ReadingListScreen. Full implementation replacing Phase 1 placeholder.
// UX_SPECIFICATION.md §4 Screen 4: reading passage list with HSK level selector.

package com.mandarinlearn.ui.reading

import com.mandarinlearn.domain.model.ReadingPassage

/**
 * UI state for [ReadingListScreen].
 * Sealed class consumed by the screen composable via collectAsStateWithLifecycle.
 */
sealed class ReadingListUiState {
    /** Room query is in progress. */
    data object Loading : ReadingListUiState()

    /**
     * Passages successfully loaded.
     *
     * @param passages       Passage list for the selected HSK level.
     * @param selectedLevel  The currently selected HSK level (1–5).
     */
    data class Content(
        val passages: List<ReadingPassage>,
        val selectedLevel: Int,
    ) : ReadingListUiState()

    /**
     * Room threw an exception.
     *
     * @param message User-friendly error message.
     */
    data class Error(val message: String) : ReadingListUiState()
}
