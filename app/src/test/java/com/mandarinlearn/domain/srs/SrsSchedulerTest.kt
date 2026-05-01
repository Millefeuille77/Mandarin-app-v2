// SrsSchedulerTest.kt — Mandarin Learn
// Unit tests for SrsScheduler covering all quality ratings, lapse, interval growth, ease floor.
// IMPLEMENTATION_PLAN.md Phase 3: ≥ 12 test cases.

package com.mandarinlearn.domain.srs

import com.mandarinlearn.domain.model.SrsStatus
import com.mandarinlearn.domain.model.VocabularyWord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsSchedulerTest {

    private val today = 19000L // arbitrary epoch day

    /** Builds a fresh card with SM-2 defaults. */
    private fun newCard(
        repetitionCount: Int = 0,
        intervalDays: Int = 0,
        easeFactor: Double = 2.5,
        isIntroduced: Boolean = false,
    ) = VocabularyWord(
        id               = "test_001",
        hskLevel         = 1,
        character        = "你",
        pinyin           = "nǐ",
        translation      = "You",
        partOfSpeech     = "pronoun",
        exampleChinese   = "你好",
        examplePinyin    = "nǐ hǎo",
        exampleEnglish   = "Hello",
        easeFactor       = easeFactor,
        intervalDays     = intervalDays,
        repetitionCount  = repetitionCount,
        nextReviewDate   = 0L,
        lastReviewedDate = null,
        isIntroduced     = isIntroduced,
    )

    // ── Test 1: First "Good" review on a new card ──────────────────────────────
    @Test
    fun `first Good review sets interval to 1 reps to 1 ef stays at 2_5`() {
        val card = newCard()
        val result = SrsScheduler.review(card, SrsQuality.GOOD, today)

        assertEquals(1, result.intervalDays)
        assertEquals(1, result.repetitionCount)
        assertTrue("EF should stay ≥ 2.5 on first Good", result.easeFactor >= 2.5)
        assertEquals(today + 1, result.nextReviewDate)
        assertTrue(result.isIntroduced)
    }

    // ── Test 2: Second "Good" review → interval 6 ─────────────────────────────
    @Test
    fun `second Good review sets interval to 6 reps to 2`() {
        val card = newCard(repetitionCount = 1, intervalDays = 1, isIntroduced = true)
        val result = SrsScheduler.review(card, SrsQuality.GOOD, today)

        assertEquals(6, result.intervalDays)
        assertEquals(2, result.repetitionCount)
    }

    // ── Test 3: Third "Good" review → interval = ceil(6 × ef), reps = 3 ───────
    @Test
    fun `third Good review sets interval to ceil(6 times ef) reps to 3 ef increases`() {
        val ef = 2.5
        val card = newCard(repetitionCount = 2, intervalDays = 6, easeFactor = ef, isIntroduced = true)
        val result = SrsScheduler.review(card, SrsQuality.GOOD, today)

        val expectedInterval = Math.ceil(6 * ef).toInt() // = 15
        assertEquals(3, result.repetitionCount)
        assertEquals(expectedInterval, result.intervalDays) // 15
        assertTrue("EF should increase on Good", result.easeFactor > ef)
    }

    // ── Test 4: "Forgot" resets reps and interval ─────────────────────────────
    @Test
    fun `Forgot resets reps to 0 and interval to 1`() {
        val card = newCard(repetitionCount = 3, intervalDays = 21, easeFactor = 2.5, isIntroduced = true)
        val result = SrsScheduler.review(card, SrsQuality.FORGOT, today)

        assertEquals(0, result.repetitionCount)
        assertEquals(1, result.intervalDays)
        assertEquals(today + 1, result.nextReviewDate)
        assertTrue(result.isIntroduced)
    }

    // ── Test 5: Ease floor honoured after repeated Forgot ─────────────────────
    @Test
    fun `ease factor floor is 1_3 and never goes below`() {
        // Start at near-floor EF and Forgot repeatedly
        var card = newCard(repetitionCount = 5, intervalDays = 10, easeFactor = 1.31)
        repeat(10) { card = SrsScheduler.review(card, SrsQuality.FORGOT, today) }
        assertTrue("EF must not go below 1.3, was ${card.easeFactor}", card.easeFactor >= 1.3)
    }

    // ── Test 6: "Hard" applies 0.8× interval penalty ─────────────────────────
    @Test
    fun `Hard button applies 0_8x interval penalty to third review`() {
        val card = newCard(repetitionCount = 2, intervalDays = 6, easeFactor = 2.5, isIntroduced = true)
        val result = SrsScheduler.review(card, SrsQuality.HARD, today)

        // Without penalty: ceil(6 × 2.5) = 15; with 0.8× → floor(15 × 0.8) = 12
        val rawInterval = Math.ceil(6 * 2.5).toInt() // 15
        val expected = maxOf(1, (rawInterval * 0.8).toInt()) // 12
        assertEquals(expected, result.intervalDays)
    }

    // ── Test 7: "Hard" minimum interval is 1 day ─────────────────────────────
    @Test
    fun `Hard on first review gives minimum 1 day`() {
        val card = newCard(repetitionCount = 0, intervalDays = 0)
        val result = SrsScheduler.review(card, SrsQuality.HARD, today)
        assertTrue("Hard interval must be at least 1", result.intervalDays >= 1)
    }

    // ── Test 8: "Easy" increases ease factor the most ─────────────────────────
    @Test
    fun `Easy increases ease factor more than Good`() {
        val card = newCard(repetitionCount = 2, intervalDays = 6, easeFactor = 2.5, isIntroduced = true)
        val goodResult = SrsScheduler.review(card, SrsQuality.GOOD, today)
        val easyResult = SrsScheduler.review(card, SrsQuality.EASY, today)
        assertTrue("Easy EF should be higher than Good EF", easyResult.easeFactor > goodResult.easeFactor)
    }

    // ── Test 9: "Easy" gives longer interval than "Good" ─────────────────────
    @Test
    fun `Easy gives longer interval than Good`() {
        val card = newCard(repetitionCount = 2, intervalDays = 6, easeFactor = 2.5, isIntroduced = true)
        val goodInterval = SrsScheduler.review(card, SrsQuality.GOOD, today).intervalDays
        val easyInterval = SrsScheduler.review(card, SrsQuality.EASY, today).intervalDays
        assertTrue("Easy interval must be >= Good interval", easyInterval >= goodInterval)
    }

    // ── Test 10: last_reviewed_date is set to today on every review ───────────
    @Test
    fun `last_reviewed_date is set to today on every review`() {
        SrsQuality.entries.forEach { quality ->
            val card = newCard()
            val result = SrsScheduler.review(card, quality, today)
            assertEquals("lastReviewedDate must equal today for $quality", today, result.lastReviewedDate)
        }
    }

    // ── Test 11: is_introduced set to true after first review ─────────────────
    @Test
    fun `is_introduced becomes true after any review`() {
        SrsQuality.entries.forEach { quality ->
            val card = newCard(isIntroduced = false)
            val result = SrsScheduler.review(card, quality, today)
            assertTrue("isIntroduced must be true after $quality review", result.isIntroduced)
        }
    }

    // ── Test 12: previewNextIntervals returns correct keys ───────────────────
    @Test
    fun `previewNextIntervals returns all four qualities`() {
        val card = newCard(repetitionCount = 2, intervalDays = 6, easeFactor = 2.5)
        val previews = SrsScheduler.previewNextIntervals(card, today)
        assertEquals(4, previews.size)
        SrsQuality.entries.forEach { q ->
            assertTrue("preview must contain $q", previews.containsKey(q))
        }
    }

    // ── Test 13: Forgot on new card also works correctly ─────────────────────
    @Test
    fun `Forgot on brand new card resets cleanly and marks introduced`() {
        val card = newCard(repetitionCount = 0, intervalDays = 0, isIntroduced = false)
        val result = SrsScheduler.review(card, SrsQuality.FORGOT, today)
        assertEquals(0, result.repetitionCount)
        assertEquals(1, result.intervalDays)
        assertTrue(result.isIntroduced)
    }

    // ── Test 14: Interval grows after multiple Good reviews ───────────────────
    @Test
    fun `interval grows with successive Good reviews`() {
        var card = newCard()
        val intervals = mutableListOf<Int>()
        repeat(5) {
            card = SrsScheduler.review(card, SrsQuality.GOOD, today)
            intervals.add(card.intervalDays)
        }
        // Intervals after 5 Good reviews: 1, 6, growing...
        assertTrue("Intervals should grow over time", intervals.last() > intervals.first())
    }
}
