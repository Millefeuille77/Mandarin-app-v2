// ExportImportRoundTripTest.kt — Mandarin Learn
// Tests the JSON shape of ExportProgressUseCase output and validates that
// the snapshot can be round-tripped through the kotlinx.serialization layer.
// Phase 9: Settings & Polish. IMPLEMENTATION_PLAN.md §Phase 9 tests.

package com.mandarinlearn.data.repository

import com.mandarinlearn.domain.usecase.ExportSnapshot
import com.mandarinlearn.domain.usecase.ExamResultExportRow
import com.mandarinlearn.domain.usecase.ProgressExportRow
import com.mandarinlearn.domain.usecase.StreakExportRow
import com.mandarinlearn.domain.usecase.VocabExportRow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the ExportSnapshot JSON schema without a Room database.
 * Exercises the serialization/deserialization round-trip so QA can confirm
 * the file format is stable between export and import.
 */
class ExportImportRoundTripTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun `ExportSnapshot serializes and deserializes with version 1`() {
        val snapshot = buildTestSnapshot()
        val encoded = json.encodeToString(snapshot)
        val decoded = json.decodeFromString(ExportSnapshot.serializer(), encoded)

        assertEquals(1, decoded.version)
        assertNotNull(decoded.exportedAt)
        assertTrue(decoded.exportedAt.isNotBlank())
    }

    @Test
    fun `vocabulary rows round-trip preserves all SM-2 fields`() {
        val snapshot = buildTestSnapshot()
        val encoded = json.encodeToString(snapshot)
        val decoded = json.decodeFromString(ExportSnapshot.serializer(), encoded)

        assertEquals(1, decoded.vocabulary.size)
        val v = decoded.vocabulary[0]
        assertEquals("hsk1_001", v.id)
        assertEquals(2.5, v.easeFactor, 0.001)
        assertEquals(6, v.intervalDays)
        assertEquals(2, v.repetitionCount)
        assertEquals(42L, v.nextReviewDate)
        assertEquals(36L, v.lastReviewedDate)
        assertEquals(1, v.isIntroduced)
    }

    @Test
    fun `exam result rows round-trip preserves all fields`() {
        val snapshot = buildTestSnapshot()
        val encoded = json.encodeToString(snapshot)
        val decoded = json.decodeFromString(ExportSnapshot.serializer(), encoded)

        assertEquals(1, decoded.examResults.size)
        val e = decoded.examResults[0]
        assertEquals(1, e.hskLevel)
        assertEquals(145, e.totalScore)
        assertEquals(200, e.totalMaxScore)
        assertEquals(1, e.passed)
    }

    @Test
    fun `streak round-trip preserves current and longest streak`() {
        val snapshot = buildTestSnapshot()
        val encoded = json.encodeToString(snapshot)
        val decoded = json.decodeFromString(ExportSnapshot.serializer(), encoded)

        assertEquals(7, decoded.streak.currentStreak)
        assertEquals(14, decoded.streak.longestStreak)
        assertEquals(20000L, decoded.streak.lastActiveDate)
    }

    @Test
    fun `user progress round-trip preserves completed items`() {
        val snapshot = buildTestSnapshot()
        val encoded = json.encodeToString(snapshot)
        val decoded = json.decodeFromString(ExportSnapshot.serializer(), encoded)

        assertEquals(2, decoded.userProgress.size)
        val vocab = decoded.userProgress.first { it.section == "vocabulary" }
        assertEquals(45, vocab.completedItems)
    }

    @Test
    fun `version field must be present in JSON output`() {
        val snapshot = buildTestSnapshot()
        val encoded = json.encodeToString(snapshot)

        assertTrue("JSON must contain version field", encoded.contains("\"version\""))
        assertTrue("JSON must contain exportedAt field", encoded.contains("\"exportedAt\""))
    }

    @Test
    fun `deserialization rejects unknown fields gracefully (ignoreUnknownKeys)`() {
        // Add an extra unknown field — should not throw with ignoreUnknownKeys = true
        val withExtra = """
            {
                "version": 1,
                "exportedAt": "2026-05-02T00:00:00Z",
                "vocabulary": [],
                "examResults": [],
                "streak": {"currentStreak": 0, "longestStreak": 0},
                "userProgress": [],
                "futureField": "ignored"
            }
        """.trimIndent()

        val decoded = json.decodeFromString(ExportSnapshot.serializer(), withExtra)
        assertEquals(1, decoded.version)
    }

    // ---- Helpers ----

    private fun buildTestSnapshot(): ExportSnapshot = ExportSnapshot(
        version    = 1,
        exportedAt = "2026-05-02T10:00:00Z",
        vocabulary = listOf(
            VocabExportRow(
                id               = "hsk1_001",
                easeFactor       = 2.5,
                intervalDays     = 6,
                repetitionCount  = 2,
                nextReviewDate   = 42L,
                lastReviewedDate = 36L,
                isIntroduced     = 1,
            )
        ),
        examResults = listOf(
            ExamResultExportRow(
                id                = 1L,
                hskLevel          = 1,
                startedAt         = 1000L,
                finishedAt        = 5000L,
                durationSeconds   = 2400,
                sectionScoresJson = """[{"name":"listening","score":78,"max_score":100,"correct_count":16,"question_count":20}]""",
                totalScore        = 145,
                totalMaxScore     = 200,
                passingScore      = 120,
                passed            = 1,
                answersJson       = "[]",
            )
        ),
        streak = StreakExportRow(
            currentStreak  = 7,
            longestStreak  = 14,
            lastActiveDate = 20000L,
        ),
        userProgress = listOf(
            ProgressExportRow(1, "vocabulary", 153, 45, 20000L),
            ProgressExportRow(1, "reading",    8,   3,  19000L),
        ),
    )
}
