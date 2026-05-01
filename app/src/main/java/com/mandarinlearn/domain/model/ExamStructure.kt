// ExamStructure.kt — Mandarin Learn
// Domain model for an exam structure (one per HSK level).

package com.mandarinlearn.domain.model

/**
 * Domain representation of a complete exam structure.
 * [sections] is the decoded list from exam_structures.sections_json.
 * [totalMaxScore] = 200 for HSK 1–2, 300 for HSK 3–5 (schema-driven).
 */
data class ExamStructure(
    val hskLevel: Int,
    val totalDurationMinutes: Int,
    val sections: List<ExamSection>,
    val totalMaxScore: Int,
    val totalPassingScore: Int,
    val vocabularyRequired: Int,
    val scoringNotes: String,
)
