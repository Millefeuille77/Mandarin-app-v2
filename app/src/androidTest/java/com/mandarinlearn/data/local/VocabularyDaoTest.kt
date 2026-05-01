// VocabularyDaoTest.kt — Mandarin Learn
// Instrumented test for VocabularyDao. Verifies SM-2 queries, search, and mastered count.

package com.mandarinlearn.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VocabularyDaoTest {

    private lateinit var db: MandarinLearnDatabase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = MandarinLearnDatabase.create(context, inMemory = true)
    }

    @After
    fun tearDown() = db.close()

    private fun makeWord(
        id: String,
        hsk: Int = 1,
        char: String = "字",
        pinyin: String = "zì",
        isIntroduced: Int = 0,
        nextReviewDate: Long = 0L,
        repetitionCount: Int = 0,
        easeFactor: Double = 2.5,
        intervalDays: Int = 0,
    ) = VocabularyEntity(
        id = id, hskLevel = hsk, character = char, pinyin = pinyin,
        translation = "word", partOfSpeech = "noun",
        exampleChinese = "一字", examplePinyin = "yī zì", exampleEnglish = "one word",
        isIntroduced = isIntroduced, nextReviewDate = nextReviewDate,
        repetitionCount = repetitionCount, easeFactor = easeFactor, intervalDays = intervalDays,
    )

    @Test
    fun insertAll_withDuplicateIds_ignoresSecond() = runTest {
        val word1 = makeWord("hsk1_001", char = "我", pinyin = "wǒ")
        val word2 = makeWord("hsk1_001", char = "你", pinyin = "nǐ") // same id
        db.vocabularyDao().insertAll(listOf(word1, word2))
        val rows = db.vocabularyDao().getByLevel(1).first()
        assertEquals("Duplicate id should be ignored", 1, rows.size)
        assertEquals("First occurrence kept", "wǒ", rows[0].pinyin)
    }

    @Test
    fun getDueCards_returnsOnlyIntroducedAndDue() = runTest {
        val today = 19000L
        val due = makeWord("w1", isIntroduced = 1, nextReviewDate = today - 1)
        val future = makeWord("w2", isIntroduced = 1, nextReviewDate = today + 5)
        val newCard = makeWord("w3", isIntroduced = 0, nextReviewDate = 0L)
        db.vocabularyDao().insertAll(listOf(due, future, newCard))
        val result = db.vocabularyDao().getDueCards(1, today)
        assertEquals("Only 1 due card", 1, result.size)
        assertEquals("w1", result[0].id)
    }

    @Test
    fun getNewCards_returnsOnlyUnintroduced_limitApplied() = runTest {
        val words = (1..5).map { i -> makeWord("w$i", isIntroduced = 0) }
        db.vocabularyDao().insertAll(words)
        val result = db.vocabularyDao().getNewCards(1, 3)
        assertEquals("Limit of 3 new cards", 3, result.size)
    }

    @Test
    fun searchByText_matchesPinyinAndCharacter() = runTest {
        db.vocabularyDao().insertAll(listOf(
            makeWord("w1", char = "你好", pinyin = "nǐ hǎo"),
            makeWord("w2", char = "我", pinyin = "wǒ"),
        ))
        val results = db.vocabularyDao().searchByText("nǐ").first()
        assertEquals(1, results.size)
        assertEquals("w1", results[0].id)
    }

    @Test
    fun countMastered_countsCorrectly() = runTest {
        val mastered = makeWord("m1", repetitionCount = 5, easeFactor = 2.5, intervalDays = 21, isIntroduced = 1)
        val notMastered = makeWord("m2", repetitionCount = 2, easeFactor = 2.5, intervalDays = 5, isIntroduced = 1)
        db.vocabularyDao().insertAll(listOf(mastered, notMastered))
        val count = db.vocabularyDao().countMastered(1).first()
        assertEquals(1, count)
    }

    @Test
    fun toneMarks_surviveRoundTrip() = runTest {
        val pinyinWithTones = "ā á ǎ à wǒ nǐ tā"
        val word = makeWord("tone_test", pinyin = pinyinWithTones)
        db.vocabularyDao().insertAll(listOf(word))
        val retrieved = db.vocabularyDao().getByLevel(1).first().first()
        assertEquals("Tone marks must survive DB round-trip", pinyinWithTones, retrieved.pinyin)
    }

    @Test
    fun update_persistsSmTwoFields() = runTest {
        val word = makeWord("upd1")
        db.vocabularyDao().insertAll(listOf(word))
        val updated = word.copy(repetitionCount = 3, intervalDays = 6, easeFactor = 2.7, isIntroduced = 1)
        db.vocabularyDao().update(updated)
        val retrieved = db.vocabularyDao().getByLevel(1).first().first()
        assertEquals(3, retrieved.repetitionCount)
        assertEquals(6, retrieved.intervalDays)
        assertTrue(retrieved.easeFactor > 2.6)
    }
}
