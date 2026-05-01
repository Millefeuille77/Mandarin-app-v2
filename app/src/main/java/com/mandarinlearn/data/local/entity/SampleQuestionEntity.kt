// SampleQuestionEntity.kt — Mandarin Learn
// Room entity for the `sample_questions` table. Per ARCHITECTURE.md §2.1.
// Composite index on (hsk_level, section) for efficient exam and listening queries.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One exam sample question for a given HSK level and section.
 * [optionsJson] is a JSON-encoded List<String> of answer choices.
 * [audioTextChinese] and [audioTextPinyin] are non-null only for listening questions.
 */
@Entity(
    tableName = "sample_questions",
    indices = [Index(name = "idx_q_level_section", value = ["hsk_level", "section"])]
)
data class SampleQuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,

    // "listening" | "reading" | "writing"
    @ColumnInfo(name = "section")
    val section: String,

    @ColumnInfo(name = "question_type")
    val questionType: String,

    @ColumnInfo(name = "question_text")
    val questionText: String,

    // Non-null for listening questions only — this is the TTS source text
    @ColumnInfo(name = "audio_text_chinese")
    val audioTextChinese: String? = null,

    @ColumnInfo(name = "audio_text_pinyin")
    val audioTextPinyin: String? = null,

    // JSON-encoded List<String> of answer option strings
    @ColumnInfo(name = "options_json")
    val optionsJson: String,

    // e.g. "A", "B", "C", "D"
    @ColumnInfo(name = "correct_answer")
    val correctAnswer: String,

    @ColumnInfo(name = "explanation")
    val explanation: String,
)
