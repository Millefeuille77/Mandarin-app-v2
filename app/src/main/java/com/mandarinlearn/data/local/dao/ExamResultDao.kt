// ExamResultDao.kt — Mandarin Learn
// DAO for the exam_results table. Per ARCHITECTURE.md §2.2.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.ExamResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for completed exam results.
 */
@Dao
interface ExamResultDao {

    /** Inserts a result and returns the auto-generated row id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: ExamResultEntity): Long

    /** Returns all results for a given level, newest first. */
    @Query(
        """
        SELECT * FROM exam_results
        WHERE hsk_level = :hsk
        ORDER BY finished_at DESC
        """
    )
    fun getByLevel(hsk: Int): Flow<List<ExamResultEntity>>

    /** Returns the [limit] most recent results across all levels. */
    @Query(
        """
        SELECT * FROM exam_results
        ORDER BY finished_at DESC
        LIMIT :limit
        """
    )
    fun getRecent(limit: Int): Flow<List<ExamResultEntity>>

    /** Returns a single result by its auto-generated id. */
    @Query("SELECT * FROM exam_results WHERE id = :id")
    fun getById(id: Long): Flow<ExamResultEntity?>

    /** Deletes all results (used by ResetProgressUseCase). */
    @Query("DELETE FROM exam_results")
    suspend fun deleteAll()
}
