// StreakEntity.kt — Mandarin Learn
// Room entity for the `streak` table. Per ARCHITECTURE.md §2.1.
// Single-row table (id always 1). Upserted rather than inserted/updated separately.

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table storing the user's daily learning streak.
 * Streak logic (increment / reset / no-op) lives in StreakRepository.
 */
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,

    @ColumnInfo(name = "longest_streak")
    val longestStreak: Int = 0,

    // epoch day — null if user has never completed any activity
    @ColumnInfo(name = "last_active_date")
    val lastActiveDate: Long? = null,
)
