// ImportProgress.kt — Mandarin Learn
// Data class emitted by JsonImporter to report first-launch import progress.
// Per ARCHITECTURE.md §3.2: final emission is (1f, "Done"); errors emit (fraction, message).

package com.mandarinlearn.data.local.import

/**
 * One import progress event.
 * [fraction] is 0f..1f — drives the LinearProgressIndicator in ImportLoadingScreen.
 * [message]  is an English status string (will be displayed as resource in Phase 2's screen).
 * [isError]  flags a failure so the screen can show the Retry button.
 */
data class ImportProgress(
    val fraction: Float,
    val message: String,
    val isError: Boolean = false,
)
