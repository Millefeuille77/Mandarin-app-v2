// SectionScore.kt — Mandarin Learn
// Domain model for a per-section score within an exam result.
// Serializable to round-trip through exam_results.section_scores_json.

package com.mandarinlearn.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Score for one section of a completed exam.
 * Stored as a JSON array in exam_results.section_scores_json.
 */
@Serializable
data class SectionScore(
    val name: String,
    val score: Int,
    @SerialName("max_score")
    val maxScore: Int,
    @SerialName("correct_count")
    val correctCount: Int,
    @SerialName("question_count")
    val questionCount: Int,
)
