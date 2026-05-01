// ConversationPhraseDao.kt — Mandarin Learn
// DAO for the conversation_phrases table. Per ARCHITECTURE.md §2.2.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.ConversationPhraseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for conversation phrases (used by Speaking section).
 */
@Dao
interface ConversationPhraseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(phrases: List<ConversationPhraseEntity>)

    /** Returns all phrases for a given HSK level, ordered by id. */
    @Query("SELECT * FROM conversation_phrases WHERE hsk_level = :hsk ORDER BY id ASC")
    fun getByLevel(hsk: Int): Flow<List<ConversationPhraseEntity>>

    /**
     * Returns one phrase at random for a given level.
     * Returns null if no phrases exist for this level.
     */
    @Query("SELECT * FROM conversation_phrases WHERE hsk_level = :hsk ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPhrase(hsk: Int): ConversationPhraseEntity?

    /** Count by level — used to seed user_progress for the speaking section. */
    @Query("SELECT COUNT(*) FROM conversation_phrases WHERE hsk_level = :hsk")
    suspend fun countByLevel(hsk: Int): Int
}
