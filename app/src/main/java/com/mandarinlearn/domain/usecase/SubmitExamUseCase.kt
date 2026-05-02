// SubmitExamUseCase.kt — Mandarin Learn
// Use case: grade and persist a completed exam.
// IMPLEMENTATION_PLAN.md Phase 7 §E.

package com.mandarinlearn.domain.usecase

import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.domain.grading.ExamGrader
import com.mandarinlearn.domain.grading.GradingResult
import com.mandarinlearn.domain.model.AnswerRecord
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.domain.model.ExamStructure

/**
 * Grades a completed exam and inserts an [ExamResult] row.
 *
 * This is the single place where grading math happens — ViewModels call this use case
 * and receive back the generated [ExamResult.id] for navigation to [ExamResultScreen].
 *
 * Grading is DATA-DRIVEN: max_score and passing_score come from [ExamStructure] which
 * is loaded from the database; no 200/120 or 300/180 literals exist in this file.
 */
class SubmitExamUseCase(
    private val examRepository: ExamRepository,
    private val examGrader: ExamGrader,
) {

    /**
     * @param structure       The structure for this exam level (contains sections + thresholds).
     * @param answers         All per-question answer records (correct + incorrect).
     * @param sectionTallies  Map of section name → Pair(correctCount, presentedCount).
     * @param startedAt       Epoch millis when the exam started.
     * @param finishedAt      Epoch millis when the exam was submitted / timed out.
     * @return                The inserted [ExamResult] (with populated id), or failure.
     */
    suspend operator fun invoke(
        structure: ExamStructure,
        answers: List<AnswerRecord>,
        sectionTallies: Map<String, Pair<Int, Int>>,
        startedAt: Long,
        finishedAt: Long,
    ): Result<ExamResult> = runCatching {
        val grading: GradingResult = examGrader.gradeExam(structure, sectionTallies)

        val durationSeconds = ((finishedAt - startedAt) / 1_000L).toInt().coerceAtLeast(0)

        val result = ExamResult(
            id              = 0L,                       // Room auto-generates
            hskLevel        = structure.hskLevel,
            startedAt       = startedAt,
            finishedAt      = finishedAt,
            durationSeconds = durationSeconds,
            sectionScores   = grading.sectionScores,
            totalScore      = grading.totalScore,
            totalMaxScore   = grading.totalMaxScore,
            passingScore    = grading.passingScore,
            passed          = grading.passed,
            answers         = answers,
        )

        val insertedId = examRepository.insertResult(result)
        result.copy(id = insertedId)
    }
}
