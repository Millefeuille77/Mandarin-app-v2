// ExamHubUiState.kt — Mandarin Learn
// UI state for ExamHubScreen.
// UX_SPECIFICATION.md §2 (ExamHubScreen); IMPLEMENTATION_PLAN.md Phase 7 §A.

package com.mandarinlearn.ui.exam

import com.mandarinlearn.domain.model.ExamResult

/**
 * UI state for [ExamHubScreen].
 * Shows 5 HSK level selector cards, each with last-attempt score and pass/fail history.
 */
sealed class ExamHubUiState {

    /** Loading exam history from Room. */
    data object Loading : ExamHubUiState()

    /** Content ready: list of per-level summaries. */
    data class Content(
        val levelSummaries: List<ExamLevelSummary>,
    ) : ExamHubUiState()

    /** Unrecoverable error (Room query failure). */
    data class Error(val message: String) : ExamHubUiState()
}

/**
 * Per-HSK-level summary shown on the hub.
 *
 * @param hskLevel    1–5.
 * @param bestResult  The most recent or best result, or null if never attempted.
 * @param attemptCount Total number of attempts at this level.
 */
data class ExamLevelSummary(
    val hskLevel: Int,
    val bestResult: ExamResult?,
    val attemptCount: Int,
)
