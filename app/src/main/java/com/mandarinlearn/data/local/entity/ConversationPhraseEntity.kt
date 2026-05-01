// ConversationPhraseEntity.kt — Mandarin Learn
// Room entity for the `conversation_phrases` table. Per ARCHITECTURE.md §2.1.
// Source data: data/audio/conversation_phrases.json, used by Speaking section (Phase 6).

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One phrase used in speaking practice sessions.
 */
@Entity(
    tableName = "conversation_phrases",
    indices = [Index(name = "idx_phrase_level", value = ["hsk_level"])]
)
data class ConversationPhraseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "chinese")
    val chinese: String,

    @ColumnInfo(name = "pinyin")
    val pinyin: String,

    @ColumnInfo(name = "english")
    val english: String,

    @ColumnInfo(name = "usage_context")
    val usageContext: String,
)
