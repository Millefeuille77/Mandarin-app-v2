// ExamSection.kt — Mandarin Learn
// Domain model for one section within an exam structure.
// Serializable to round-trip through exam_structures.sections_json.

package com.mandarinlearn.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One exam section (e.g. listening, reading, writing).
 * Serializable because it is stored as JSON in exam_structures.sections_json.
 */
@Serializable
data class ExamSection(
    val name: String,
    @SerialName("question_count")
    val questionCount: Int,
    @SerialName("duration_minutes")
    val durationMinutes: Int,
    @SerialName("question_types")
    val questionTypes: List<String>,
    @SerialName("max_score")
    val maxScore: Int,
    @SerialName("passing_score")
    val passingScore: Int,
    val description: String,
)
