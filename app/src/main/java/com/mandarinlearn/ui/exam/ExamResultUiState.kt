// ExamResultUiState.kt — Mandarin Learn
// UI state for ExamResultScreen.
// UX_SPECIFICATION.md §4 Screen 9; IMPLEMENTATION_PLAN.md Phase 7 §C.

package com.mandarinlearn.ui.exam

import com.mandarinlearn.domain.model.AnswerRecord
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.domain.model.SectionScore

/**
 * UI state for [ExamResultScreen].
 *
 * Shows: pass/fail badge, total score, per-section breakdown, mistake review, history.
 */
sealed class ExamResultUiState {

    data object Loading : ExamResultUiState()

    data class Content(
        val result: ExamResult,
        val sectionScores: List<SectionScore>,
        /** Wrong answers with question text for the "Review mistakes" list. */
        val wrongAnswers: List<WrongAnswerDetail>,
        /** Up to 5 most recent attempts at this HSK level (including current). */
        val history: List<ExamResult>,
        val showMistakesExpanded: Boolean,
    ) : ExamResultUiState()

    data class Error(val message: String) : ExamResultUiState()
}

/**
 * Pairs an [AnswerRecord] with its question text for the review-mistakes list.
 */
data class WrongAnswerDetail(
    val questionText: String,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val questionId: String,
)
