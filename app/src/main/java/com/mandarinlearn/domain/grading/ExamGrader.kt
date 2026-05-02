// ExamGrader.kt — Mandarin Learn
// Domain service for grading a completed exam.
// IMPLEMENTATION_PLAN.md Phase 7 §D; ARCHITECTURE.md §2.2 (exam_results table).
// Per-section score and total are DATA-DRIVEN — read from ExamStructure, NOT hardcoded.

package com.mandarinlearn.domain.grading

import com.mandarinlearn.domain.model.ExamSection
import com.mandarinlearn.domain.model.ExamStructure
import com.mandarinlearn.domain.model.SectionScore
import kotlin.math.roundToInt

/**
 * Stateless grader for HSK mock exams.
 *
 * Design:
 *  - [scaleSection] maps a raw correct-count to a 0–[maxScore] section score using a
 *    proportional formula. This handles the QA-flagged shortfall where fewer questions
 *    are presented than spec'd (e.g. HSK 3 writing has 5 instead of 10).
 *  - [gradeExam] builds [SectionScore] objects for every section from the presented questions,
 *    computes the total, and evaluates pass/fail against the schema-driven thresholds.
 *
 * Grading is DATA-DRIVEN: totals and pass thresholds are read from [ExamStructure] which
 * holds values from the JSON import — 200/120 for HSK 1-2, 300/180 for HSK 3-5.
 * These values are NEVER hardcoded here.
 */
class ExamGrader {

    /**
     * Scales a section score proportionally from the presented question count to [maxScore].
     *
     * Example: HSK 3 writing — only 5 questions presented vs 10 spec'd.
     *   scaleSection(correctCount=4, presentedCount=5, maxScore=100) → 80
     *   scaleSection(correctCount=0, presentedCount=0, maxScore=100) → 0 (no questions → 0)
     *
     * @param correctCount   Number of questions answered correctly.
     * @param presentedCount Number of questions actually shown (may be < spec count).
     * @param maxScore       Maximum score for this section (read from ExamSection.maxScore).
     * @return               Integer score in [0, maxScore].
     */
    fun scaleSection(correctCount: Int, presentedCount: Int, maxScore: Int): Int {
        if (presentedCount == 0 || maxScore == 0) return 0
        require(correctCount in 0..presentedCount) {
            "correctCount ($correctCount) must be in 0..$presentedCount"
        }
        val ratio = correctCount.toDouble() / presentedCount.toDouble()
        return (ratio * maxScore).roundToInt().coerceIn(0, maxScore)
    }

    /**
     * Grades a full exam from per-section answer tallies.
     *
     * @param structure     The exam structure for this HSK level (provides section list
     *                      and total_max_score / total_passing_score from the JSON).
     * @param sectionTallies Map of section name → Pair(correctCount, presentedCount).
     *                       Sections not in the map are treated as 0/0 (score = 0).
     * @return              [GradingResult] with per-section scores, total, and pass/fail.
     */
    fun gradeExam(
        structure: ExamStructure,
        sectionTallies: Map<String, Pair<Int, Int>>,
    ): GradingResult {
        val sectionScores = structure.sections.map { section ->
            val (correct, presented) = sectionTallies[section.name] ?: (0 to 0)
            val score = scaleSection(correct, presented, section.maxScore)
            SectionScore(
                name          = section.name,
                score         = score,
                maxScore      = section.maxScore,
                correctCount  = correct,
                questionCount = presented,
            )
        }

        // Total is the sum of section scores. Must not exceed total_max_score.
        val totalScore = sectionScores.sumOf { it.score }
            .coerceAtMost(structure.totalMaxScore)

        // Pass/fail read directly from the schema — data-driven, never hardcoded.
        val passed = totalScore >= structure.totalPassingScore

        return GradingResult(
            sectionScores = sectionScores,
            totalScore    = totalScore,
            totalMaxScore = structure.totalMaxScore,
            passingScore  = structure.totalPassingScore,
            passed        = passed,
        )
    }

    /**
     * Convenience overload that accepts a list of [ExamSection] instead of [ExamStructure]
     * when the full structure object is not readily available.
     */
    fun gradeExamFromSections(
        sections: List<ExamSection>,
        totalMaxScore: Int,
        totalPassingScore: Int,
        sectionTallies: Map<String, Pair<Int, Int>>,
    ): GradingResult {
        val sectionScores = sections.map { section ->
            val (correct, presented) = sectionTallies[section.name] ?: (0 to 0)
            val score = scaleSection(correct, presented, section.maxScore)
            SectionScore(
                name          = section.name,
                score         = score,
                maxScore      = section.maxScore,
                correctCount  = correct,
                questionCount = presented,
            )
        }
        val totalScore = sectionScores.sumOf { it.score }.coerceAtMost(totalMaxScore)
        val passed = totalScore >= totalPassingScore
        return GradingResult(sectionScores, totalScore, totalMaxScore, totalPassingScore, passed)
    }
}

/**
 * Result produced by [ExamGrader.gradeExam].
 */
data class GradingResult(
    val sectionScores: List<SectionScore>,
    val totalScore: Int,
    val totalMaxScore: Int,
    val passingScore: Int,
    val passed: Boolean,
)
