// VocabularyEntity.kt — Mandarin Learn
// Room entity for the `vocabulary` table. Per ARCHITECTURE.md §2.1.
// Each row is one HSK word with SM-2 spaced repetition state fields.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vocabulary word row with SM-2 SRS fields.
 * The composite index (hsk_level, next_review_date) speeds up daily due-card queries.
 * SM-2 fields (ease_factor, interval_days, repetition_count) follow ARCHITECTURE.md §5.1.
 */
@Entity(
    tableName = "vocabulary",
    indices = [Index(name = "idx_vocab_level_due", value = ["hsk_level", "next_review_date"])]
)
data class VocabularyEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,

    @ColumnInfo(name = "character")
    val character: String,

    @ColumnInfo(name = "pinyin")
    val pinyin: String,

    @ColumnInfo(name = "translation")
    val translation: String,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,

    @ColumnInfo(name = "example_chinese")
    val exampleChinese: String,

    @ColumnInfo(name = "example_pinyin")
    val examplePinyin: String,

    @ColumnInfo(name = "example_english")
    val exampleEnglish: String,

    // SM-2 fields — initial values follow ARCHITECTURE.md §5.1
    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double = 2.5,

    @ColumnInfo(name = "interval_days")
    val intervalDays: Int = 0,

    @ColumnInfo(name = "repetition_count")
    val repetitionCount: Int = 0,

    // epoch day; 0 = never reviewed (treated as due immediately)
    @ColumnInfo(name = "next_review_date")
    val nextReviewDate: Long = 0L,

    @ColumnInfo(name = "last_reviewed_date")
    val lastReviewedDate: Long? = null,

    // 0 = card not yet shown; 1 = user has seen this card at least once
    @ColumnInfo(name = "is_introduced")
    val isIntroduced: Int = 0,
)
