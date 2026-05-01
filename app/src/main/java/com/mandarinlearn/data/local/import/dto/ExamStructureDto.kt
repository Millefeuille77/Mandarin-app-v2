// ExamStructureDto.kt — Mandarin Learn
// Data Transfer Object for deserialising hsk{1-5}_exam_structure.json.

package com.mandarinlearn.data.local.import.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the exam structure JSON schema from the Research agent.
 * [sections] has 2 entries for HSK 1–2 and 3 entries for HSK 3–5.
 * The variable length is preserved by storing sectionsJson as a JSON string in the entity.
 */
@Serializable
data class ExamStructureDto(
    @SerialName("hsk_level")
    val hskLevel: Int,
    @SerialName("total_duration_minutes")
    val totalDurationMinutes: Int,
    val sections: List<ExamSectionDto>,
    @SerialName("total_max_score")
    val totalMaxScore: Int,
    @SerialName("total_passing_score")
    val totalPassingScore: Int,
    @SerialName("vocabulary_required")
    val vocabularyRequired: Int,
    @SerialName("scoring_notes")
    val scoringNotes: String,
)

/**
 * One exam section within a structure.
 */
@Serializable
data class ExamSectionDto(
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
