// UserProgressEntity.kt — Mandarin Learn
// Room entity for the `user_progress` table. Per ARCHITECTURE.md §2.1.
// One row per (hsk_level, section) pair. Composite primary key.
// Seeded on first import with total_items from imported counts; completed_items = 0.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Per-section, per-level progress aggregated for the dashboard.
 * [section] is one of: vocabulary | reading | listening | speaking | exam.
 * [totalItems] is populated from import counts and updated when new content is added.
 * [completedItems] is incremented by domain events (card mastered, passage read, etc.).
 */
@Entity(
    tableName = "user_progress",
    primaryKeys = ["hsk_level", "section"]
)
data class UserProgressEntity(
    @ColumnInfo(name = "hsk_level")
    val hskLevel: Int,

    // "vocabulary" | "reading" | "listening" | "speaking" | "exam"
    @ColumnInfo(name = "section")
    val section: String,

    @ColumnInfo(name = "total_items")
    val totalItems: Int,

    @ColumnInfo(name = "completed_items")
    val completedItems: Int = 0,

    // epoch day — updated whenever an item in this section is completed
    @ColumnInfo(name = "last_activity_date")
    val lastActivityDate: Long? = null,
)
