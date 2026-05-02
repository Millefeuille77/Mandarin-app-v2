// VocabularyDao.kt — Mandarin Learn
// DAO for the vocabulary table. Per ARCHITECTURE.md §2.2.
// All queries use Flow or suspend — never blocking. No LiveData.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mandarinlearn.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for vocabulary.
 * "mastered" definition per ARCHITECTURE.md §5.5:
 *   repetition_count >= 5 AND ease_factor >= 2.5 AND interval_days >= 21
 */
@Dao
interface VocabularyDao {

    /** Insert one or more words. IGNORE strategy skips duplicate IDs (HSK 3 has 4 known dupes). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<VocabularyEntity>)

    /** Returns all words for a given HSK level, ordered by ID (curriculum order). */
    @Query("SELECT * FROM vocabulary WHERE hsk_level = :hsk ORDER BY id ASC")
    fun getByLevel(hsk: Int): Flow<List<VocabularyEntity>>

    /**
     * Returns cards due for review today.
     * Due = introduced (is_introduced=1) AND next_review_date <= today.
     * Ordered by next_review_date ASC (oldest due first), then id ASC for stability.
     */
    @Query(
        """
        SELECT * FROM vocabulary
        WHERE is_introduced = 1
          AND hsk_level = :hsk
          AND next_review_date <= :today
        ORDER BY next_review_date ASC, id ASC
        """
    )
    suspend fun getDueCards(hsk: Int, today: Long): List<VocabularyEntity>

    /**
     * Returns new (not-yet-introduced) cards for a level, capped by limit.
     * Ordered by id ASC to preserve curriculum order.
     */
    @Query(
        """
        SELECT * FROM vocabulary
        WHERE is_introduced = 0 AND hsk_level = :hsk
        ORDER BY id ASC
        LIMIT :limit
        """
    )
    suspend fun getNewCards(hsk: Int, limit: Int): List<VocabularyEntity>

    /** Updates a card's SM-2 fields after a review. */
    @Update
    suspend fun update(card: VocabularyEntity)

    /**
     * Full-text search across character, pinyin, and translation.
     * Uses LIKE with wildcards — adequate for ≤ 300 HSK 5 words.
     */
    @Query(
        """
        SELECT * FROM vocabulary
        WHERE character LIKE '%' || :query || '%'
           OR pinyin LIKE '%' || :query || '%'
           OR translation LIKE '%' || :query || '%'
        ORDER BY hsk_level ASC, id ASC
        """
    )
    fun searchByText(query: String): Flow<List<VocabularyEntity>>

    /** Total count of all vocabulary rows, reactive. */
    @Query("SELECT COUNT(*) FROM vocabulary")
    fun count(): Flow<Int>

    /** Count of words for a level that meet the "mastered" definition (ARCHITECTURE.md §5.5). */
    @Query(
        """
        SELECT COUNT(*) FROM vocabulary
        WHERE hsk_level = :hsk
          AND repetition_count >= 5
          AND ease_factor >= 2.5
          AND interval_days >= 21
        """
    )
    fun countMastered(hsk: Int): Flow<Int>

    /** Count of all words for a given level — used to populate user_progress.total_items. */
    @Query("SELECT COUNT(*) FROM vocabulary WHERE hsk_level = :hsk")
    suspend fun countByLevel(hsk: Int): Int

    /**
     * Reactive count of cards due for review today at a given HSK level.
     * Used by Phase 8 HomeScreen to determine the focus level and total due count.
     * Due = is_introduced = 1 AND next_review_date <= today.
     */
    @Query(
        """
        SELECT COUNT(*) FROM vocabulary
        WHERE hsk_level = :hsk
          AND is_introduced = 1
          AND next_review_date <= :today
        """
    )
    fun countDueForLevel(hsk: Int, today: Long): Flow<Int>

    /**
     * Finds a vocabulary word by its hanzi character (exact match).
     * Used by PassageScreen character-tap popup to look up definitions.
     * Returns null if the character is not in the HSK 1–5 vocabulary.
     */
    @Query("SELECT * FROM vocabulary WHERE character = :character LIMIT 1")
    suspend fun findByCharacter(character: String): VocabularyEntity?

    /**
     * Resets all SM-2 fields to defaults for SettingsRepository.resetProgress().
     * Content columns (character, pinyin, translation, etc.) are untouched.
     */
    @Query(
        """
        UPDATE vocabulary
        SET ease_factor = 2.5,
            interval_days = 0,
            repetition_count = 0,
            next_review_date = 0,
            last_reviewed_date = NULL,
            is_introduced = 0
        """
    )
    suspend fun resetAllProgress()
}
