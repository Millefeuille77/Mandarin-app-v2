// FlashcardUiState.kt — Mandarin Learn
// UI state for FlashcardScreen.
// UX_SPECIFICATION.md §4 Screen 3: card flip, SM-2 rating buttons, session progress.

package com.mandarinlearn.ui.vocabulary

import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.domain.srs.SrsQuality

/**
 * Sealed UI state for FlashcardScreen.
 * Exposed as StateFlow by FlashcardViewModel.
 */
sealed class FlashcardUiState {

    /** Loading the session queue from Room. */
    data object Loading : FlashcardUiState()

    /**
     * Active flashcard session with a card showing.
     *
     * @param currentCard      The card currently displayed.
     * @param isFlipped        Whether the back of the card (answer side) is visible.
     * @param currentIndex     0-based index in the session queue.
     * @param totalCards       Total cards in the session (due + new).
     * @param nextIntervals    Preview of intervals per quality, shown on rating buttons.
     * @param isAudioLoading   True while TTS playback is being fetched.
     * @param audioError       Non-null when the last audio attempt failed.
     */
    data class Reviewing(
        val currentCard: VocabularyWord,
        val isFlipped: Boolean,
        val currentIndex: Int,
        val totalCards: Int,
        val nextIntervals: Map<SrsQuality, Int>,
        val isAudioLoading: Boolean = false,
        val audioError: String? = null,
    ) : FlashcardUiState()

    /**
     * All cards in the session have been rated.
     *
     * @param reviewedCount Total cards rated in this session.
     * @param newCount      Cards that were newly introduced (is_introduced was false).
     * @param hasMoreNew    True if there are still unintroduced cards available for the level.
     */
    data class SessionComplete(
        val reviewedCount: Int,
        val newCount: Int,
        val hasMoreNew: Boolean,
    ) : FlashcardUiState()

    /** No cards available (empty due queue AND no new cards for the level). */
    data object Empty : FlashcardUiState()

    /** Room threw an unexpected exception. */
    data class Error(val message: String) : FlashcardUiState()
}
