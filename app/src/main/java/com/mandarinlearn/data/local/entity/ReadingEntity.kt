// ReadingEntity.kt — Mandarin Learn
// Room entity for the `reading_passages` table. Per ARCHITECTURE.md §2.1.
// pinyin_annotations is JSON-encoded List<PinyinAnnotation> (every char for HSK 1–3,
// key vocab only for HSK 4–5, per CLAUDE.md hard rule #2).

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Reading passage row.
 * [pinyinAnnotations] is a JSON string decoded by [com.mandarinlearn.data.local.Converters].
 * [vocabularyHighlights] is a JSON string (`List<String>`).
 */
@Entity(
    tableName = "reading_passages",
    indices = [Index(name = "idx_reading_level", value = ["hsk_level"])]
)
data class ReadingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "chinese_text")
    val chineseText: String,

    // JSON-encoded list of PinyinAnnotation objects
    @ColumnInfo(name = "pinyin_annotations")
    val pinyinAnnotations: String,

    @ColumnInfo(name = "english_translation")
    val englishTranslation: String,

    // JSON-encoded List<String> of key vocabulary
    @ColumnInfo(name = "vocabulary_highlights")
    val vocabularyHighlights: String,

    @ColumnInfo(name = "word_count")
    val wordCount: Int,

    // 0/1 whether the user has read this passage
    @ColumnInfo(name = "is_completed")
    val isCompleted: Int = 0,

    // epoch day when the user marked this reading as complete
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
)
