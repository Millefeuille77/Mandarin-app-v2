// ReadingDao.kt — Mandarin Learn
// DAO for the reading_passages table. Per ARCHITECTURE.md §2.2.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for reading passages.
 */
@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(passages: List<ReadingEntity>)

    /** Returns all passages for a given HSK level, ordered by id. */
    @Query("SELECT * FROM reading_passages WHERE hsk_level = :hsk ORDER BY id ASC")
    fun getByLevel(hsk: Int): Flow<List<ReadingEntity>>

    /** Returns a single passage by id, reactive. */
    @Query("SELECT * FROM reading_passages WHERE id = :id")
    fun getById(id: String): Flow<ReadingEntity?>

    /** Marks a passage as completed with the given epoch-day timestamp. */
    @Query(
        """
        UPDATE reading_passages
        SET is_completed = 1, completed_at = :date
        WHERE id = :id
        """
    )
    suspend fun markCompleted(id: String, date: Long)

    /** Count of all passages for a given level — used to seed user_progress. */
    @Query("SELECT COUNT(*) FROM reading_passages WHERE hsk_level = :hsk")
    suspend fun countByLevel(hsk: Int): Int

    /** Count of completed passages for a given level — used by ProgressRepository. */
    @Query("SELECT COUNT(*) FROM reading_passages WHERE hsk_level = :hsk AND is_completed = 1")
    fun countCompletedByLevel(hsk: Int): Flow<Int>

    /** Resets is_completed and completed_at for all passages (used by ResetProgressUseCase). */
    @Query("UPDATE reading_passages SET is_completed = 0, completed_at = NULL")
    suspend fun resetAllProgress()
}
