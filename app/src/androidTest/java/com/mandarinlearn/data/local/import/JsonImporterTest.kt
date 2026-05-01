// JsonImporterTest.kt — Mandarin Learn
// Instrumented test for JsonImporter.
// Verifies counts per table match JSON inputs and pinyin tone marks survive the round-trip.
// NOTE: This test reads real res/raw/ files, so it must run as an instrumented test.

package com.mandarinlearn.data.local.import

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.data.local.MandarinLearnDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonImporterTest {

    private lateinit var db: MandarinLearnDatabase
    private lateinit var importer: JsonImporter
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = MandarinLearnDatabase.create(context, inMemory = true)
        importer = JsonImporter(context, db, json)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun importIfNeeded_completesWithoutError() = runTest {
        val events = importer.importIfNeeded().toList()
        val lastEvent = events.last()
        assertFalse("Import should not end in error", lastEvent.isError)
        assertEquals(1f, lastEvent.fraction, 0.01f)
    }

    @Test
    fun importIfNeeded_populatesVocabularyTable() = runTest {
        importer.importIfNeeded().toList() // consume full flow
        val count = db.vocabularyDao().count().first()
        assertTrue("Should have imported vocabulary rows", count > 0)
        // HSK 1 spec: 153 words
        val hsk1Count = db.vocabularyDao().countByLevel(1)
        assertEquals("HSK 1 vocab should have 153 rows", 153, hsk1Count)
    }

    @Test
    fun importIfNeeded_populatesReadingTable() = runTest {
        importer.importIfNeeded().toList()
        // HSK 1 spec: 8 readings
        val hsk1ReadingCount = db.readingDao().countByLevel(1)
        assertEquals("HSK 1 should have 8 reading passages", 8, hsk1ReadingCount)
    }

    @Test
    fun importIfNeeded_populatesToneDrills() = runTest {
        importer.importIfNeeded().toList()
        val drillCount = db.toneDrillDao().count()
        assertEquals("Should have 20 tone drills", 20, drillCount)
    }

    @Test
    fun importIfNeeded_populatesConversationPhrases() = runTest {
        importer.importIfNeeded().toList()
        // Total across all levels should be 100
        var total = 0
        for (level in 1..5) total += db.conversationPhraseDao().countByLevel(level)
        assertEquals("Should have 100 conversation phrases total", 100, total)
    }

    @Test
    fun importIfNeeded_populatesExamStructures() = runTest {
        importer.importIfNeeded().toList()
        val structures = db.examStructureDao().getAll().first()
        assertEquals("Should have 5 exam structures (one per level)", 5, structures.size)
    }

    @Test
    fun importIfNeeded_hsk2HasTwoSections() = runTest {
        importer.importIfNeeded().toList()
        val hsk2 = db.examStructureDao().getStructure(2).first()
        // sections_json should encode 2 sections for HSK 2
        val sectionCount = hsk2?.sectionsJson?.split("\"name\"")?.size?.minus(1) ?: 0
        assertEquals("HSK 2 should have 2 sections", 2, sectionCount)
    }

    @Test
    fun importIfNeeded_hsk5HasThreeSections() = runTest {
        importer.importIfNeeded().toList()
        val hsk5 = db.examStructureDao().getStructure(5).first()
        val sectionCount = hsk5?.sectionsJson?.split("\"name\"")?.size?.minus(1) ?: 0
        assertEquals("HSK 5 should have 3 sections", 3, sectionCount)
    }

    @Test
    fun pinyinToneMarks_surviveRoundTrip() = runTest {
        importer.importIfNeeded().toList()
        // wǒ (我) should be in HSK 1 with correct tone mark
        val hsk1Words = db.vocabularyDao().getByLevel(1).first()
        val wo = hsk1Words.find { it.character == "我" }
        assertTrue("我 should be in HSK 1", wo != null)
        assertEquals("Pinyin for 我 must retain tone mark", "wǒ", wo?.pinyin)
    }

    @Test
    fun importIfNeeded_secondCall_skipsImport() = runTest {
        // First import
        importer.importIfNeeded().toList()
        // Second call should detect versions are up-to-date and emit (1f, "Up to date")
        val events = importer.importIfNeeded().toList()
        assertEquals("Second call should emit 'Up to date'", "Up to date", events.last().message)
        assertEquals(1f, events.last().fraction, 0.01f)
    }

    @Test
    fun userProgress_seeded_afterImport() = runTest {
        importer.importIfNeeded().toList()
        val progress = db.userProgressDao().getAll().first()
        // 5 levels × 5 sections = 25 rows
        assertEquals("Should have 25 user_progress rows (5 levels × 5 sections)", 25, progress.size)
        // All should start with 0 completed
        assertTrue("All completed_items should be 0", progress.all { it.completedItems == 0 })
    }

    @Test
    fun streak_seeded_afterImport() = runTest {
        importer.importIfNeeded().toList()
        val streak = db.streakDao().get().first()
        assertTrue("Streak row should exist after import", streak != null)
        assertEquals("Initial streak should be 0", 0, streak?.currentStreak)
    }
}
