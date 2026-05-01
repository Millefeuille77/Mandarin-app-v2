// StreakDao.kt — Mandarin Learn
// DAO for the streak table. Per ARCHITECTURE.md §2.2.
// Single-row table — id is always 1.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the daily streak.
 */
@Dao
interface StreakDao {

    /** Returns the single streak row, reactive. Null before first insert. */
    @Query("SELECT * FROM streak WHERE id = 1")
    fun get(): Flow<StreakEntity?>

    /** Inserts or replaces the streak row (id = 1 always). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StreakEntity)

    /** Returns the single streak row once (non-reactive). Used by StreakRepository.recordActivity(). */
    @Query("SELECT * FROM streak WHERE id = 1")
    suspend fun getOnce(): StreakEntity?

    /** Resets streak to 0 (ResetProgressUseCase). */
    @Query("UPDATE streak SET current_streak = 0, last_active_date = NULL WHERE id = 1")
    suspend fun resetStreak()
}
