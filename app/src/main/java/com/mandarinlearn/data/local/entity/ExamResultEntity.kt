// ExamResultEntity.kt — Mandarin Learn
// Room entity for the `exam_results` table. Per ARCHITECTURE.md §2.1.
// Per-section scores stored as JSON to handle variable section counts cleanly.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One completed exam attempt.
 * [sectionScoresJson] is JSON-encoded List<SectionScore> — matches the section count
 * of the corresponding exam_structures row.
 * [answersJson] is JSON-encoded List<AnswerRecord> for "Review mistakes".
 * [totalMaxScore] and [passingScore] are copied from exam_structures at result time —
 * this preserves the score ceiling even if structures change later.
 */
@Entity(
    tableName = "exam_results",
    indices = [Index(name = "idx_result_level", value = ["hsk_level"])]
)
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,

    // epoch millis
    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    // epoch millis
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,

    // JSON-encoded List<SectionScore> — variable length matching exam_structures.sections_json
    @ColumnInfo(name = "section_scores_json")
    val sectionScoresJson: String,

    @ColumnInfo(name = "total_score")
    val totalScore: Int,

    // 200 (HSK 1–2) or 300 (HSK 3–5) — copied from exam_structures at result-insertion time
    @ColumnInfo(name = "total_max_score")
    val totalMaxScore: Int,

    // 120 (HSK 1–2) or 180 (HSK 3–5)
    @ColumnInfo(name = "passing_score")
    val passingScore: Int,

    // 0/1 precomputed: total_score >= passing_score
    @ColumnInfo(name = "passed")
    val passed: Int,

    // JSON-encoded List<AnswerRecord> for mistake review
    @ColumnInfo(name = "answers_json")
    val answersJson: String,
)
