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
     * The screen maps [errorCode] to a localised string via [SpeakingErrorCode].
     * Using an error code keeps ViewModels free of Context (ARCHITECTURE.md §6).
     */
    data class Error(
        val errorCode: SpeakingErrorCode,
    ) : SpeakingUiState()
}

/**
 * Error codes for [SpeakingUiState.Error].
 * Mapped to user-facing strings in [SpeakingScreen] via stringResource().
 * This keeps ViewModels free of Context references.
 */
enum class SpeakingErrorCode {
    /** No microphone permission granted. */
    NO_PERMISSION,
    /** No conversation phrases found in the database for this HSK level. */
    NO_PHRASES,
    /** Device is offline — speaking practice requires internet for STT. */
    OFFLINE,
    /** Gemini API key is not configured in local.properties. */
    NO_API_KEY,
    /** Gemini STT timed out — transient, user can retry. */
    TIMEOUT,
    /** MediaRecorder failed to start — hardware issue. */
    RECORD_FAILED,
    /** Catch-all for unexpected errors. */
    UNKNOWN,
}

/** One-time events emitted by [SpeakingViewModel] via a SharedFlow. */
sealed class SpeakingEvent {
    /** Recording auto-stopped at the 10-second limit. */
    data object RecordingLimitReached : SpeakingEvent()
}
