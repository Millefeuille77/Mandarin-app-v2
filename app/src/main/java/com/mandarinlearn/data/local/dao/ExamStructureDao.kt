// ExamStructureDao.kt — Mandarin Learn
// DAO for the exam_structures table. Per ARCHITECTURE.md §2.2.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.ExamStructureEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for exam structures (one row per HSK level).
 */
@Dao
interface ExamStructureDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(structures: List<ExamStructureEntity>)

    /** Returns the exam structure for a given HSK level, reactive. Null if not yet imported. */
    @Query("SELECT * FROM exam_structures WHERE hsk_level = :hsk")
    fun getStructure(hsk: Int): Flow<ExamStructureEntity?>

    /** All structures — used by ExamRepository to list available levels. */
    @Query("SELECT * FROM exam_structures ORDER BY hsk_level ASC")
    fun getAll(): Flow<List<ExamStructureEntity>>
}
