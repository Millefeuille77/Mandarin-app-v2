// ExamGraderTest.kt — Mandarin Learn
// Unit tests for ExamGrader.
// IMPLEMENTATION_PLAN.md Phase 7 §E: ExamGraderTest fixtures for HSK 1 and HSK 5.

package com.mandarinlearn.domain.grading

import com.mandarinlearn.domain.model.ExamSection
import com.mandarinlearn.domain.model.ExamStructure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [ExamGrader].
 *
 * Covers:
 * - scaleSection: all-correct, all-wrong, partial, zero denominator
 * - gradeExam: HSK 1 (2 sections, 200/120), HSK 5 (3 sections, 300/180)
 * - Partial section shortfall (QA-flagged: fewer questions presented than spec'd)
 * - Pass/fail threshold is DATA-DRIVEN (no hardcoded values tested)
 */
class ExamGraderTest {

    private val grader = ExamGrader()

    // ---- scaleSection ----

    @Test fun `scaleSection all-correct returns maxScore`() {
        assertEquals(100, grader.scaleSection(10, 10, 100))
    }

    @Test fun `scaleSection all-wrong returns 0`() {
        assertEquals(0, grader.scaleSection(0, 10, 100))
    }

    @Test fun `scaleSection 50 percent correct returns half maxScore`() {
        assertEquals(50, grader.scaleSection(5, 10, 100))
    }

    @Test fun `scaleSection 0 presented returns 0`() {
        assertEquals(0, grader.scaleSection(0, 0, 100))
    }

    @Test fun `scaleSection partial shortfall HSK3 writing 4 of 5 presented`() {
        // QA-flagged: HSK 3 writing has 5 questions vs 10 spec'd
        // 4 correct out of 5 presented → 80/100
        assertEquals(80, grader.scaleSection(4, 5, 100))
    }

    @Test fun `scaleSection result is clamped to maxScore`() {
        // Edge case: correctCount == presentedCount
        assertEquals(100, grader.scaleSection(20, 20, 100))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `scaleSection correctCount greater than presentedCount throws`() {
        grader.scaleSection(11, 10, 100)
    }

    // ---- gradeExam: HSK 1 (2 sections, 200/120) ----

    @Test fun `HSK1 all-correct full exam passes`() {
        val structure = hsk1Structure()
        val tallies = mapOf(
            "listening" to (20 to 20),
            "reading"   to (20 to 20),
        )
        val result = grader.gradeExam(structure, tallies)
        assertEquals(200, result.totalScore)
        assertEquals(200, result.totalMaxScore)
        assertEquals(120, result.passingScore)
        assertTrue(result.passed)
        assertEquals(2, result.sectionScores.size)
    }

    @Test fun `HSK1 all-wrong fails`() {
        val result = grader.gradeExam(hsk1Structure(), mapOf(
            "listening" to (0 to 20),
            "reading"   to (0 to 20),
        ))
        assertEquals(0, result.totalScore)
        assertFalse(result.passed)
    }

    @Test fun `HSK1 exactly passing score passes`() {
        // listening: 60/100, reading: 60/100 → total 120/200 = exactly pass
        val result = grader.gradeExam(hsk1Structure(), mapOf(
            "listening" to (12 to 20),  // 60%
            "reading"   to (12 to 20),  // 60%
        ))
        assertEquals(120, result.totalScore)
        assertTrue("Score 120 should pass with threshold 120", result.passed)
    }

    @Test fun `HSK1 one below passing score fails`() {
        // listening 55+reading 60 = 115 < 120
        val result = grader.gradeExam(hsk1Structure(), mapOf(
            "listening" to (11 to 20),  // 55
            "reading"   to (12 to 20),  // 60
        ))
        assertEquals(115, result.totalScore)
        assertFalse(result.passed)
    }

    // ---- gradeExam: HSK 5 (3 sections, 300/180) ----

    @Test fun `HSK5 all-correct full exam passes`() {
        val result = grader.gradeExam(hsk5Structure(), mapOf(
            "listening" to (20 to 20),
            "reading"   to (20 to 20),
            "writing"   to (10 to 10),
        ))
        assertEquals(300, result.totalScore)
        assertEquals(300, result.totalMaxScore)
        assertEquals(180, result.passingScore)
        assertTrue(result.passed)
        assertEquals(3, result.sectionScores.size)
    }

    @Test fun `HSK5 writing shortfall still grades proportionally`() {
        // HSK 5 writing: only 8 questions presented vs 10 spec'd
        val result = grader.gradeExam(hsk5Structure(), mapOf(
            "listening" to (15 to 20),  // 75
            "reading"   to (16 to 20),  // 80
            "writing"   to (6 to 8),    // 75
        ))
        assertEquals(75 + 80 + 75, result.totalScore)
        assertTrue(result.passed)   // 230 >= 180
    }

    @Test fun `HSK5 missing section treated as 0`() {
        // No writing answers → writing score = 0
        val result = grader.gradeExam(hsk5Structure(), mapOf(
            "listening" to (20 to 20),
            "reading"   to (20 to 20),
            // writing missing
        ))
        assertEquals(200, result.totalScore)
        assertTrue(result.passed)  // 200 >= 180
    }

    @Test fun `HSK5 all-wrong fails`() {
        val result = grader.gradeExam(hsk5Structure(), mapOf(
            "listening" to (0 to 20),
            "reading"   to (0 to 20),
            "writing"   to (0 to 10),
        ))
        assertEquals(0, result.totalScore)
        assertFalse(result.passed)
    }

    // ---- Helpers ----

    private fun hsk1Structure() = ExamStructure(
        hskLevel             = 1,
        totalDurationMinutes = 40,
        sections             = listOf(
            ExamSection("listening", 20, 20, listOf("multiple_choice"), 100, 60, ""),
            ExamSection("reading",   20, 20, listOf("multiple_choice"), 100, 60, ""),
        ),
        totalMaxScore        = 200,
        totalPassingScore    = 120,
        vocabularyRequired   = 150,
        scoringNotes         = "",
    )

    private fun hsk5Structure() = ExamStructure(
        hskLevel             = 5,
        totalDurationMinutes = 120,
        sections             = listOf(
            ExamSection("listening", 20, 30, listOf("multiple_choice"), 100, 60, ""),
            ExamSection("reading",   20, 50, listOf("multiple_choice"), 100, 60, ""),
            ExamSection("writing",   10, 40, listOf("essay"),           100, 60, ""),
        ),
        totalMaxScore        = 300,
        totalPassingScore    = 180,
        vocabularyRequired   = 2500,
        scoringNotes         = "",
    )
}
