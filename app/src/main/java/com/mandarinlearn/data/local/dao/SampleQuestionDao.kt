// SampleQuestionDao.kt — Mandarin Learn
// DAO for the sample_questions table. Per ARCHITECTURE.md §2.2.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.SampleQuestionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for sample exam questions.
 */
@Dao
interface SampleQuestionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<SampleQuestionEntity>)

    /** Returns all questions for a given level and section, reactive. */
    @Query(
        """
        SELECT * FROM sample_questions
        WHERE hsk_level = :hsk AND section = :section
        ORDER BY id ASC
        """
    )
    fun getByLevelAndSection(hsk: Int, section: String): Flow<List<SampleQuestionEntity>>

    /**
     * Returns up to [limit] questions for an exam session (non-reactive, called once per exam).
     * Questions are shuffled per ARCHITECTURE.md §7 exam-screen note about variation.
     */
    @Query(
        """
        SELECT * FROM sample_questions
        WHERE hsk_level = :hsk AND section = :section
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    suspend fun getQuestionsForExam(hsk: Int, section: String, limit: Int): List<SampleQuestionEntity>

    /** Total count for a given level — used to seed user_progress for the exam section. */
    @Query("SELECT COUNT(*) FROM sample_questions WHERE hsk_level = :hsk")
    suspend fun countByLevel(hsk: Int): Int
}
