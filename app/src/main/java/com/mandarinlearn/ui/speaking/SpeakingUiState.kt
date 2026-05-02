// SpeakingUiState.kt — Mandarin Learn
// UI state for SpeakingScreen. Per UX_SPECIFICATION.md §4 Screen 6.
// Phase 6: full state machine — Idle, PermissionDenied, Recording, Processing, Result, Error.

package com.mandarinlearn.ui.speaking

import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.domain.model.PronunciationResult

/**
 * State machine for [SpeakingScreen].
 *
 * Transitions:
 *   Loading → Idle (phrase loaded)
 *   Idle ──(tap mic)──► Recording ──(tap mic / 10 s)──► Processing ──► Result
 *   Result ──(Try again)──► Idle (same phrase)
 *   Result ──(Next phrase)──► Loading → Idle (new phrase)
 *   Any state + no permission ──► PermissionDenied
 *   Any state + offline ──► Error
 */
sealed class SpeakingUiState {

    /** Loading a phrase from the database. */
    data object Loading : SpeakingUiState()

    /**
     * Phrase is loaded; ready for the user to tap the mic.
     */
    data class Idle(
        val phrase: ConversationPhrase,
        val selectedHsk: Int,
    ) : SpeakingUiState()

    /**
     * Microphone is active and recording.
     * [elapsedSeconds] counts up from 0 to 10.
     */
    data class Recording(
        val phrase: ConversationPhrase,
        val selectedHsk: Int,
        val elapsedSeconds: Int,
    ) : SpeakingUiState()

    /**
     * Audio has been sent to Gemini; waiting for the score.
     */
    data class Processing(
        val phrase: ConversationPhrase,
        val selectedHsk: Int,
    ) : SpeakingUiState()

    /**
     * Score received and displayed.
     * [result] contains the score (0–100), feedback, and phoneme issues.
     */
    data class Result(
        val phrase: ConversationPhrase,
        val selectedHsk: Int,
        val result: PronunciationResult,
    ) : SpeakingUiState()

    /**
     * RECORD_AUDIO permission has not been granted.
     * [isPermanentlyDenied] is true if the user selected "Don't ask again",
     * in which case the UI shows "Open device settings" instead of the rationale dialog.
     */
    data class PermissionDenied(
        val isPermanentlyDenied: Boolean = false,
    ) : SpeakingUiState()

    /**
     * An error occurred (offline, timeout, no phrases loaded, etc.).
     * The screen shows an ErrorState card with [message].
     */
    data class Error(
        val message: String,
    ) : SpeakingUiState()
}

/** One-time events emitted by [SpeakingViewModel] via a SharedFlow. */
sealed class SpeakingEvent {
    /** Recording auto-stopped at the 10-second limit. */
    data object RecordingLimitReached : SpeakingEvent()
}
