// ExamResult.kt — Mandarin Learn
// Domain model for a completed exam result with decoded section scores and answer records.

package com.mandarinlearn.domain.model

/**
 * Domain representation of one completed exam attempt.
 * [sectionScores] and [answers] are decoded lists (not raw JSON).
 * [passed] is precomputed: totalScore >= passingScore.
 */
data class ExamResult(
    val id: Long,
    val hskLevel: Int,
    val startedAt: Long,
    val finishedAt: Long,
    val durationSeconds: Int,
    val sectionScores: List<SectionScore>,
    val totalScore: Int,
    val totalMaxScore: Int,
    val passingScore: Int,
    val passed: Boolean,
    val answers: List<AnswerRecord>,
)
