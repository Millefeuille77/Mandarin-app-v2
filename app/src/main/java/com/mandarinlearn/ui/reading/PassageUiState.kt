// PassageUiState.kt — Mandarin Learn
// UI state for PassageScreen. Full Phase 4 implementation replacing placeholder.
// UX_SPECIFICATION.md §4 Screen 5.

package com.mandarinlearn.ui.reading

import com.mandarinlearn.domain.model.ReadingPassage
import com.mandarinlearn.domain.model.VocabularyWord

/**
 * UI state for [PassageScreen].
 * Sealed class consumed via collectAsStateWithLifecycle — never LiveData.
 */
sealed class PassageUiState {
    /** Room query in-flight or passage not yet found. */
    data object Loading : PassageUiState()

    /**
     * Passage loaded and ready to display.
     *
     * @param passage         The full reading passage with decoded pinyin annotations.
     * @param showPinyin      Whether the pinyin row is currently visible.
     * @param fontScale       Current font size multiplier (0.8–1.6).
     * @param selectedWord    If non-null, the CharacterDefinitionSheet is open for this word.
     * @param noDefinition    True when a tapped character was not found in the vocabulary table.
     * @param tappedCharacter The raw character string that was tapped (for the "not found" case).
     * @param isMarkingRead   True while the "Mark as read" Room write is in progress.
     * @param showTranslation True when English translation card is expanded.
     * @param isPlayingAll    True while "Play all" audio is loading or playing (Phase 5).
     * @param playAllFailed   True when the last "Play all" attempt failed (Phase 5).
     */
    data class Content(
        val passage: ReadingPassage,
        val showPinyin: Boolean,
        val fontScale: Float,
        val selectedWord: VocabularyWord?,
        val noDefinition: Boolean,
        val tappedCharacter: String,
        val isMarkingRead: Boolean,
        val showTranslation: Boolean,
        val isPlayingAll: Boolean = false,
        val playAllFailed: Boolean = false,
    ) : PassageUiState()

    /**
     * Room threw an exception while loading the passage.
     *
     * @param message User-friendly error message.
     */
    data class Error(val message: String) : PassageUiState()
}
