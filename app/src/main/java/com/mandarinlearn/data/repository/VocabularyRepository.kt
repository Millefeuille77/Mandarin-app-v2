// VocabularyRepository.kt — Mandarin Learn
// Full implementation for Phase 2. Maps VocabularyEntity ↔ VocabularyWord domain model.
// All Room operations wrapped in withContext(Dispatchers.IO) per ARCHITECTURE.md §7.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.VocabularyDao
import com.mandarinlearn.data.local.entity.VocabularyEntity
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.util.DateUtil
import com.mandarinlearn.util.DispatcherProvider
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val TAG = "VocabularyRepository"

/**
 * Repository for all vocabulary data and SM-2 state updates.
 * ViewModels NEVER access VocabularyDao directly — only through this class.
 */
class VocabularyRepository(
    private val dao: VocabularyDao,
    private val dispatchers: DispatcherProvider,
) {

    /** Returns all words for the given HSK level as a reactive Flow. */
    fun getWordsByLevel(hsk: Int): Flow<List<VocabularyWord>> =
        dao.getByLevel(hsk).map { list -> list.map { it.toDomain() } }

    /** Searches across character, pinyin, and translation. */
    fun searchWords(query: String): Flow<List<VocabularyWord>> =
        dao.searchByText(query).map { list -> list.map { it.toDomain() } }

    /** Returns cards due today plus up to [newCardsLimit] new cards. */
    suspend fun getDueAndNewCards(hsk: Int, newCardsLimit: Int): List<VocabularyWord> =
        withContext(dispatchers.io) {
            val today = DateUtil.today()
            val due = dao.getDueCards(hsk, today).map { it.toDomain() }
            val new = dao.getNewCards(hsk, newCardsLimit).map { it.toDomain() }
            due + new
        }

    /** Persists updated SM-2 fields after a review rating. */
    suspend fun updateCard(word: VocabularyWord) {
        withContext(dispatchers.io) {
            try {
                dao.update(word.toEntity())
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to update card ${word.id}", e)
                throw e
            }
        }
    }

    /**
     * Looks up a word by its hanzi character for the PassageScreen tap-to-define popup.
     * Returns null if the character is not in the HSK 1–5 vocabulary.
     */
    suspend fun findByCharacter(character: String): VocabularyWord? =
        withContext(dispatchers.io) {
            dao.findByCharacter(character)?.toDomain()
        }

    /** Reactive count of mastered words for a level (used by ProgressScreen). */
    fun getMasteredCount(hsk: Int): Flow<Int> = dao.countMastered(hsk)

    /** Total vocabulary count (reactive). */
    fun getTotalCount(): Flow<Int> = dao.count()

    /** Resets all SM-2 state (used by ResetProgressUseCase in Phase 9). */
    suspend fun resetAllProgress() {
        withContext(dispatchers.io) { dao.resetAllProgress() }
    }
}

// ---- Entity ↔ Domain mappers ----

private fun VocabularyEntity.toDomain() = VocabularyWord(
    id = id,
    hskLevel = hskLevel,
    character = character,
    pinyin = pinyin,
    translation = translation,
    partOfSpeech = partOfSpeech,
    exampleChinese = exampleChinese,
    examplePinyin = examplePinyin,
    exampleEnglish = exampleEnglish,
    easeFactor = easeFactor,
    intervalDays = intervalDays,
    repetitionCount = repetitionCount,
    nextReviewDate = nextReviewDate,
    lastReviewedDate = lastReviewedDate,
    isIntroduced = isIntroduced == 1,
)

private fun VocabularyWord.toEntity() = VocabularyEntity(
    id = id,
    hskLevel = hskLevel,
    character = character,
    pinyin = pinyin,
    translation = translation,
    partOfSpeech = partOfSpeech,
    exampleChinese = exampleChinese,
    examplePinyin = examplePinyin,
    exampleEnglish = exampleEnglish,
    easeFactor = easeFactor,
    intervalDays = intervalDays,
    repetitionCount = repetitionCount,
    nextReviewDate = nextReviewDate,
    lastReviewedDate = lastReviewedDate,
    isIntroduced = if (isIntroduced) 1 else 0,
)
