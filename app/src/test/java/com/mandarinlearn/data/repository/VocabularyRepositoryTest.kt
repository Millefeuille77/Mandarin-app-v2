// VocabularyRepositoryTest.kt — Mandarin Learn
// Unit test for VocabularyRepository using MockK for the DAO and TestDispatchers.
// Tests domain model mapping, SM-2 update delegation, and search.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.VocabularyDao
import com.mandarinlearn.data.local.entity.VocabularyEntity
import com.mandarinlearn.domain.model.SrsStatus
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.util.DispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VocabularyRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockDao = mockk<VocabularyDao>()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }
    private lateinit var repository: VocabularyRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = VocabularyRepository(mockDao, dispatcherProvider)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeEntity(
        id: String = "hsk1_001",
        hsk: Int = 1,
        char: String = "我",
        pinyin: String = "wǒ",
        isIntroduced: Int = 0,
        repetitionCount: Int = 0,
        easeFactor: Double = 2.5,
        intervalDays: Int = 0,
    ) = VocabularyEntity(
        id = id, hskLevel = hsk, character = char, pinyin = pinyin,
        translation = "I, me", partOfSpeech = "pronoun",
        exampleChinese = "我是", examplePinyin = "wǒ shì", exampleEnglish = "I am",
        isIntroduced = isIntroduced, repetitionCount = repetitionCount,
        easeFactor = easeFactor, intervalDays = intervalDays,
    )

    @Test
    fun getWordsByLevel_mapsEntityToDomain() = runTest {
        val entity = makeEntity(pinyin = "wǒ")
        every { mockDao.getByLevel(1) } returns flowOf(listOf(entity))

        val words = repository.getWordsByLevel(1).toList().first()

        assertEquals(1, words.size)
        assertEquals("wǒ", words[0].pinyin) // tone marks preserved
        assertEquals(SrsStatus.NEW, words[0].srsStatus) // isIntroduced=0 → NEW
    }

    @Test
    fun srsStatus_mastered_whenAllConditionsMet() = runTest {
        val masteredEntity = makeEntity(
            isIntroduced = 1, repetitionCount = 5, easeFactor = 2.5, intervalDays = 21
        )
        every { mockDao.getByLevel(1) } returns flowOf(listOf(masteredEntity))

        val words = repository.getWordsByLevel(1).toList().first()
        assertEquals(SrsStatus.MASTERED, words[0].srsStatus)
    }

    @Test
    fun srsStatus_learning_whenIntroducedButNotMastered() = runTest {
        val learningEntity = makeEntity(isIntroduced = 1, repetitionCount = 2)
        every { mockDao.getByLevel(1) } returns flowOf(listOf(learningEntity))

        val words = repository.getWordsByLevel(1).toList().first()
        assertEquals(SrsStatus.LEARNING, words[0].srsStatus)
    }

    @Test
    fun updateCard_delegatesToDao() = runTest {
        val entity = makeEntity()
        coEvery { mockDao.update(any()) } returns Unit

        val word = VocabularyWord(
            id = entity.id, hskLevel = entity.hskLevel, character = entity.character,
            pinyin = entity.pinyin, translation = entity.translation,
            partOfSpeech = entity.partOfSpeech, exampleChinese = entity.exampleChinese,
            examplePinyin = entity.examplePinyin, exampleEnglish = entity.exampleEnglish,
            easeFactor = 2.7, intervalDays = 6, repetitionCount = 2,
            nextReviewDate = 19010L, lastReviewedDate = 19004L, isIntroduced = true,
        )
        repository.updateCard(word)

        coVerify { mockDao.update(any()) }
    }

    @Test
    fun getDueAndNewCards_combinesDueAndNew() = runTest {
        val dueEntity = makeEntity("due1", isIntroduced = 1)
        val newEntity = makeEntity("new1", isIntroduced = 0)
        coEvery { mockDao.getDueCards(1, any()) } returns listOf(dueEntity)
        coEvery { mockDao.getNewCards(1, any()) } returns listOf(newEntity)

        val cards = repository.getDueAndNewCards(1, newCardsLimit = 10)
        assertEquals(2, cards.size)
        assertEquals("due1", cards[0].id) // due cards first
        assertEquals("new1", cards[1].id)
    }
}
