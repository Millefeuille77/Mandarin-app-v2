// UserProgressDao.kt — Mandarin Learn
// DAO for the user_progress table. Per ARCHITECTURE.md §2.2.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for per-section, per-level user progress.
 * upsert uses REPLACE strategy so the composite PK (hsk_level, section) is honoured.
 */
@Dao
interface UserProgressDao {

    /** Returns all progress rows, ordered by level then section. */
    @Query("SELECT * FROM user_progress ORDER BY hsk_level ASC, section ASC")
    fun getAll(): Flow<List<UserProgressEntity>>

    /** Returns progress for a specific level and section — reactive. */
    @Query(
        """
        SELECT * FROM user_progress
        WHERE hsk_level = :hsk AND section = :section
        """
    )
    fun getByLevelAndSection(hsk: Int, section: String): Flow<UserProgressEntity?>

    /** Returns progress for a specific level and section — one-shot (non-reactive). */
    @Query(
        """
        SELECT * FROM user_progress
        WHERE hsk_level = :hsk AND section = :section
        """
    )
    suspend fun getByLevelAndSectionOnce(hsk: Int, section: String): UserProgressEntity?

    /** Inserts or replaces a progress row. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProgressEntity)

    /** Batch upsert used during first-launch seeding. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<UserProgressEntity>)

    /** Resets completed_items to 0 for all rows (ResetProgressUseCase). Content columns unchanged. */
    @Query("UPDATE user_progress SET completed_items = 0, last_activity_date = NULL")
    suspend fun resetAllProgress()

    /** Returns all progress rows once (non-reactive). Used by ExportProgressUseCase. */
    @Query("SELECT * FROM user_progress ORDER BY hsk_level ASC, section ASC")
    suspend fun getAllOnce(): List<UserProgressEntity>

    /**
     * Returns a single row by level + section (non-suspend, for use inside runInTransaction).
     * Used by ImportProgressUseCase to preserve existing total_items when restoring progress.
     */
    @Query("SELECT * FROM user_progress WHERE hsk_level = :hsk AND section = :section LIMIT 1")
    fun getByLevelAndSectionSync(hsk: Int, section: String): UserProgressEntity?
}
