// HomeUiState.kt — Mandarin Learn
// UI state for HomeScreen. Phase 8: Progress & Dashboard.
// UX_SPECIFICATION.md §4 Screen 1.

package com.mandarinlearn.ui.home

/**
 * Sealed UI state for [HomeScreen].
 *
 * Follows the standard MVVM pattern: ViewModel exposes StateFlow<HomeUiState>;
 * the composable collects it and renders accordingly.
 */
sealed class HomeUiState {

    /** Initial state while Room queries are running. */
    data object Loading : HomeUiState()

    /** Data is available; screen renders real content. */
    data class Content(
        /** Current learning streak in days. */
        val currentStreak: Int,
        /** Longest streak ever. */
        val longestStreak: Int,
        /** Total vocabulary cards due today across all levels. */
        val totalDueCount: Int,
        /** Per-level summary rows for the HSK progress section. */
        val levelRows: List<LevelProgressRow>,
        /** Focus level for the "Review now" CTA (lowest level with due cards, or 1 if none). */
        val focusLevel: Int,
    ) : HomeUiState()

    /** Room threw an exception (very rare). */
    data class Error(val message: String) : HomeUiState()
}

/**
 * Summary data for one HSK level row in the HomeScreen progress section.
 *
 * @param hskLevel          1–5.
 * @param masteredCount     Words meeting the mastered definition (rep >= 5, ef >= 2.5, interval >= 21).
 * @param totalCount        Total vocabulary words imported for this level.
 */
data class LevelProgressRow(
    val hskLevel: Int,
    val masteredCount: Int,
    val totalCount: Int,
) {
    /** Fraction 0.0..1.0 for the progress bar. Returns 0 if totalCount is 0. */
    val masteredFraction: Float
        get() = if (totalCount == 0) 0f else (masteredCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
}
