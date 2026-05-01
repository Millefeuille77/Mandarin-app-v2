// ExamResultDaoTest.kt — Mandarin Learn
// Instrumented test for ExamResultDao. Verifies insert returns auto-generated ID,
// level-filtered query, recent query, and JSON round-trip.

package com.mandarinlearn.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.data.local.entity.ExamResultEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExamResultDaoTest {

    private lateinit var db: MandarinLearnDatabase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = MandarinLearnDatabase.create(context, inMemory = true)
    }

    @After
    fun tearDown() = db.close()

    private fun makeResult(
        hsk: Int = 1,
        totalScore: Int = 140,
        passed: Boolean = true,
        sectionScoresJson: String = """[{"name":"listening","score":70,"max_score":100,"correct_count":14,"question_count":20}]""",
        answersJson: String = """[]""",
    ) = ExamResultEntity(
        hskLevel = hsk,
        startedAt = 1000L,
        finishedAt = 2000L,
        durationSeconds = 60,
        sectionScoresJson = sectionScoresJson,
        totalScore = totalScore,
        totalMaxScore = 200,
        passingScore = 120,
        passed = if (passed) 1 else 0,
        answersJson = answersJson,
    )

    @Test
    fun insert_returnsNonZeroId() = runTest {
        val id = db.examResultDao().insert(makeResult())
        assertNotEquals("Auto-generated id should not be 0", 0L, id)
    }

    @Test
    fun getByLevel_returnsMatchingResults() = runTest {
        db.examResultDao().insert(makeResult(hsk = 1))
        db.examResultDao().insert(makeResult(hsk = 2))
        val level1 = db.examResultDao().getByLevel(1).first()
        assertEquals(1, level1.size)
        assertEquals(1, level1[0].hskLevel)
    }

    @Test
    fun getRecent_returnsNewestFirst() = runTest {
        db.examResultDao().insert(makeResult().copy(finishedAt = 1000L))
        db.examResultDao().insert(makeResult().copy(finishedAt = 3000L))
        val recent = db.examResultDao().getRecent(10).first()
        assertEquals(2, recent.size)
        assertTrue("Newest first", recent[0].finishedAt > recent[1].finishedAt)
    }

    @Test
    fun sectionScoresJson_survivesRoundTrip() = runTest {
        val json = """[{"name":"listening","score":70,"max_score":100,"correct_count":14,"question_count":20},{"name":"reading","score":80,"max_score":100,"correct_count":16,"question_count":20}]"""
        val id = db.examResultDao().insert(makeResult(sectionScoresJson = json))
        val retrieved = db.examResultDao().getById(id).first()
        assertEquals("section_scores_json must survive DB round-trip", json, retrieved?.sectionScoresJson)
    }

    @Test
    fun deleteAll_clearsTable() = runTest {
        db.examResultDao().insert(makeResult())
        db.examResultDao().insert(makeResult())
        db.examResultDao().deleteAll()
        val results = db.examResultDao().getRecent(100).first()
        assertEquals(0, results.size)
    }
}
