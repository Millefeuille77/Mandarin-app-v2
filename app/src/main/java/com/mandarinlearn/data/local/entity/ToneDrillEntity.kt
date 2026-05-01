// ToneDrillEntity.kt — Mandarin Learn
// Room entity for the `tone_drills` table. Per ARCHITECTURE.md §2.1.
// Source data: data/audio/tone_drills.json.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One tone-pair drill entry (e.g. "1st + 2nd tone" with example word).
 * [additionalExamples] is a JSON-encoded list of example objects.
 */
@Entity(tableName = "tone_drills")
data class ToneDrillEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "tone_pair")
    val tonePair: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "example_word")
    val exampleWord: String,

    @ColumnInfo(name = "pinyin")
    val pinyin: String,

    @ColumnInfo(name = "translation")
    val translation: String,

    // JSON-encoded list of additional example objects
    @ColumnInfo(name = "additional_examples")
    val additionalExamples: String,
)
