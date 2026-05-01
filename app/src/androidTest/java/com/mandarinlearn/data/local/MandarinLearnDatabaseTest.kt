// MandarinLearnDatabaseTest.kt — Mandarin Learn
// Instrumented test: verifies MandarinLearnDatabase opens and all tables are accessible.
// Uses an in-memory database for speed and isolation.

package com.mandarinlearn.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.data.local.entity.StreakEntity
import com.mandarinlearn.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that MandarinLearnDatabase opens correctly on the device and
 * all DAO abstractions are accessible.
 */
@RunWith(AndroidJUnit4::class)
class MandarinLearnDatabaseTest {

    private lateinit var db: MandarinLearnDatabase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = MandarinLearnDatabase.create(context, inMemory = true)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun databaseOpens_andVocabularyDaoIsAccessible() = runTest {
        val count = db.vocabularyDao().count().first()
        assertEquals("Fresh DB should have 0 vocabulary rows", 0, count)
    }

    @Test
    fun insertVocabulary_andQueryByLevel() = runTest {
        val word = VocabularyEntity(
            id = "test_001",
            hskLevel = 1,
            character = "你",
            pinyin = "nǐ",
            translation = "you",
            partOfSpeech = "pronoun",
            exampleChinese = "你好",
            examplePinyin = "nǐ hǎo",
            exampleEnglish = "Hello",
        )
        db.vocabularyDao().insertAll(listOf(word))
        val rows = db.vocabularyDao().getByLevel(1).first()
        assertEquals(1, rows.size)
        assertEquals("nǐ", rows[0].pinyin) // tone marks must survive round-trip
    }

    @Test
    fun streakDao_isAccessible_andReturnsNullOnFreshDb() = runTest {
        val streak = db.streakDao().get().first()
        assertEquals("Streak table should start empty", null, streak)
    }

    @Test
    fun upsertStreak_andReadBack() = runTest {
        db.streakDao().upsert(StreakEntity(currentStreak = 5, longestStreak = 10))
        val streak = db.streakDao().get().first()
        assertNotNull(streak)
        assertEquals(5, streak?.currentStreak)
        assertEquals(10, streak?.longestStreak)
    }

    @Test
    fun dataVersionDao_returnsNull_beforeImport() = runTest {
        val version = db.dataVersionDao().get()
        assertEquals("data_version should be null before first import", null, version)
    }

    @Test
    fun allDaos_areAccessible() {
        // If any DAO is missing from the @Database annotation, this will crash
        assertNotNull(db.vocabularyDao())
        assertNotNull(db.readingDao())
        assertNotNull(db.conversationPhraseDao())
        assertNotNull(db.toneDrillDao())
        assertNotNull(db.examStructureDao())
        assertNotNull(db.sampleQuestionDao())
        assertNotNull(db.examResultDao())
        assertNotNull(db.audioCacheDao())
        assertNotNull(db.userProgressDao())
        assertNotNull(db.streakDao())
        assertNotNull(db.dataVersionDao())
    }
}
