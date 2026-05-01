// DataVersionEntity.kt — Mandarin Learn
// Room entity for the `data_version` table. Per ARCHITECTURE.md §2.1.
// Single-row table (id always 1) used by JsonImporter to detect first launch and
// future content version bumps. All *_version fields = 1 after Phase 2 import.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks the import version for each content category.
 * [vocabularyVersion] / [readingVersion] / [audioVersion] / [examVersion] start at 0.
 * JsonImporter sets them all to [CURRENT_VERSION] (= 1 in Phase 2) after a successful import.
 * Future releases bump the constant to trigger re-import of changed categories only.
 */
@Entity(tableName = "data_version")
data class DataVersionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    @ColumnInfo(name = "vocabulary_version")
    val vocabularyVersion: Int = 0,

    @ColumnInfo(name = "reading_version")
    val readingVersion: Int = 0,

    @ColumnInfo(name = "audio_version")
    val audioVersion: Int = 0,

    @ColumnInfo(name = "exam_version")
    val examVersion: Int = 0,

    // epoch millis when this row was last written
    @ColumnInfo(name = "imported_at")
    val importedAt: Long = 0L,
)
