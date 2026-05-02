// ProgressUiState.kt — Mandarin Learn
// UI state for ProgressScreen. Phase 8: Progress & Dashboard.
// UX_SPECIFICATION.md §4 Screen 10: streak, per-HSK level cards, exam chart, readiness.

package com.mandarinlearn.ui.progress

import com.mandarinlearn.domain.model.ExamResult

/**
 * Sealed UI state for [ProgressScreen].
 */
sealed class ProgressUiState {

    data object Loading : ProgressUiState()

    data class Content(
        val currentStreak: Int,
        val longestStreak: Int,
        /** Days this week (Sun–Sat index 0–6) with at least one activity. */
        val activeWeekDays: Set<Int>,
        val levelCards: List<LevelProgressCard>,
        /** All exam results for the currently selected level filter (null = All). */
        val chartResults: List<ExamResult>,
        /** Level filter for the chart: null = all levels, 1–5 = specific. */
        val chartFilterLevel: Int?,
    ) : ProgressUiState()

    data class Error(val message: String) : ProgressUiState()
}

/**
 * Summary card data for one HSK level on ProgressScreen.
 *
 * @param hskLevel             1–5.
 * @param masteredCount        Words meeting mastered definition.
 * @param totalVocabCount      Total words at this level.
 * @param readingCompleted     Passages completed.
 * @param readingTotal         Total passages at this level.
 * @param bestExamResult       Best exam result for this level, or null if no exam taken.
 * @param totalMaxScore        Max score for this level (200 or 300).
 * @param readinessPct         0–100 readiness percentage (from ReadinessCalculator).
 */
data class LevelProgressCard(
    val hskLevel: Int,
    val masteredCount: Int,
    val totalVocabCount: Int,
    val readingCompleted: Int,
    val readingTotal: Int,
    val bestExamResult: ExamResult?,
    val totalMaxScore: Int,
    val readinessPct: Float,
) {
    val vocabPct: Float
        get() = if (totalVocabCount == 0) 0f
                else (masteredCount.toFloat() / totalVocabCount.toFloat() * 100f).coerceIn(0f, 100f)

    val readingPct: Float
        get() = if (readingTotal == 0) 0f
                else (readingCompleted.toFloat() / readingTotal.toFloat() * 100f).coerceIn(0f, 100f)

    val bestExamPct: Float
        get() {
            val result = bestExamResult ?: return 0f
            if (result.totalMaxScore == 0) return 0f
            return (result.totalScore.toFloat() / result.totalMaxScore.toFloat() * 100f).coerceIn(0f, 100f)
        }
}
