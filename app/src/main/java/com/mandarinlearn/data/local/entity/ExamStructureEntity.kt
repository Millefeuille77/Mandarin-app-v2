// ExamStructureEntity.kt — Mandarin Learn
// Room entity for the `exam_structures` table. Per ARCHITECTURE.md §2.1 and §2.3.
// sections_json stores a variable-length JSON array so HSK 1–2 (2 sections) and
// HSK 3–5 (3 sections) share the same schema with no ALTER TABLE needed.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One exam structure row, keyed by HSK level.
 * [sectionsJson] is a JSON-encoded List<ExamSection> — variable length per level.
 * [totalMaxScore] is 200 for HSK 1–2 and 300 for HSK 3–5 (stored per row, never hardcoded).
 */
@Entity(tableName = "exam_structures")
data class ExamStructureEntity(
    @PrimaryKey
    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,

    @ColumnInfo(name = "total_duration_minutes")
    val totalDurationMinutes: Int,

    // JSON array of ExamSection objects — 2 items for HSK 1–2, 3 items for HSK 3–5
    @ColumnInfo(name = "sections_json")
    val sectionsJson: String,

    // 200 (HSK 1–2) or 300 (HSK 3–5) — schema-driven, no level-specific code paths
    @ColumnInfo(name = "total_max_score")
    val totalMaxScore: Int,

    // 120 (HSK 1–2) or 180 (HSK 3–5)
    @ColumnInfo(name = "total_passing_score")
    val totalPassingScore: Int,

    @ColumnInfo(name = "vocabulary_required")
    val vocabularyRequired: Int,

    @ColumnInfo(name = "scoring_notes")
    val scoringNotes: String,
)
