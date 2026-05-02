// ExamUiState.kt — Mandarin Learn
// UI state sealed class for ExamScreen.
// UX_SPECIFICATION.md §4 Screen 8; IMPLEMENTATION_PLAN.md Phase 7 §B.

package com.mandarinlearn.ui.exam

import com.mandarinlearn.domain.model.ExamStructure
import com.mandarinlearn.domain.usecase.ExamSectionWithQuestions

/**
 * State machine for [ExamScreen].
 *
 * Transitions:
 *   Loading → ActiveSection → SectionBreak → ActiveSection → ... → Submitting → Done
 *   Any state → QuitConfirm (back pressed) → ActiveSection (cancel) or popped (confirm)
 *   ActiveSection → Error (load failure)
 */
sealed class ExamUiState {

    /** Initial state while the exam data is loading. */
    data object Loading : ExamUiState()

    /** Exam is in progress; user is answering questions. */
    data class ActiveSection(
        val structure: ExamStructure,
        val allSections: List<ExamSectionWithQuestions>,
        val currentSectionIndex: Int,
        val currentQuestionIndex: Int,
        val selectedAnswer: String?,           // null until user picks an option
        val answeredQuestions: Map<String, String>, // questionId → userAnswer
        val timerSecondsRemaining: Int,
        val showQuitDialog: Boolean,
        val totalQuestionsAnswered: Int,
        val totalQuestions: Int,
        val startedAtMillis: Long,
    ) : ExamUiState() {

        val currentSection: ExamSectionWithQuestions
            get() = allSections[currentSectionIndex]

        val currentQuestion
            get() = currentSection.questions.getOrNull(currentQuestionIndex)

        val isLastQuestionInSection: Boolean
            get() = currentQuestionIndex >= currentSection.questions.size - 1

        val isLastSection: Boolean
            get() = currentSectionIndex >= allSections.size - 1

        val progressFraction: Float
            get() = if (totalQuestions == 0) 0f
                    else totalQuestionsAnswered.toFloat() / totalQuestions.toFloat()

        val timerMinutes: Int get() = timerSecondsRemaining / 60
        val timerSeconds: Int get() = timerSecondsRemaining % 60

        /** True when timer is < 5 minutes (warning color). */
        val isTimerWarning: Boolean get() = timerSecondsRemaining in 60..299

        /** True when timer is < 1 minute (error color). */
        val isTimerCritical: Boolean get() = timerSecondsRemaining in 1..59
    }

    /**
     * 30-second break between sections.
     * User can skip the wait by tapping "Continue".
     */
    data class SectionBreak(
        val structure: ExamStructure,
        val allSections: List<ExamSectionWithQuestions>,
        val completedSectionIndex: Int,
        val breakSecondsRemaining: Int,
        val answeredQuestions: Map<String, String>,
        val timerSecondsRemaining: Int,
        val startedAtMillis: Long,
    ) : ExamUiState()

    /** Waiting for exam result to be persisted. */
    data object Submitting : ExamUiState()

    /** Exam submitted; navigate to ExamResultScreen. */
    data class Done(val resultId: Long) : ExamUiState()

    /** Unrecoverable error (structure not found, Room error). */
    data class Error(val message: String) : ExamUiState()
}
