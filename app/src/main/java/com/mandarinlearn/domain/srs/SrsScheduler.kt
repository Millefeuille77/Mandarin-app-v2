// SrsScheduler.kt — Mandarin Learn
// Pure SM-2 spaced repetition implementation per ARCHITECTURE.md §5.3.
// No Android imports — fully unit-testable.

package com.mandarinlearn.domain.srs

import com.mandarinlearn.domain.model.VocabularyWord
import kotlin.math.ceil
import kotlin.math.max

/**
 * Pure SM-2 implementation.
 *
 * The app uses a 0..3 quality scale (ARCHITECTURE.md §5.2), mapped to SM-2's 0..5 range
 * internally via: q5 = q * 5.0 / 3.0
 *
 * Ease factor floor: 1.3 (standard SM-2 minimum).
 * Initial ease factor: 2.5.
 */
object SrsScheduler {

    private const val EASE_FLOOR = 1.3
    private const val EASE_INITIAL = 2.5
    private const val HARD_PENALTY = 0.8

    /**
     * Applies one review event and returns the updated [VocabularyWord].
     *
     * Formula (ARCHITECTURE.md §5.3, implemented exactly):
     *  1. Map q (0..3) → q5 (0..5): q5 = q * 5.0 / 3.0
     *  2. newEf = easeFactor + (0.1 - (5 - q5) * (0.08 + (5 - q5) * 0.02))
     *  3. Clamp newEf to floor 1.3
     *  4. If q == 0 (Forgot): reset reps=0, interval=1, next_review=today+1
     *  5. Else: reps++, interval per rep count, Hard applies 0.8× penalty
     *
     * @param word The current card state.
     * @param quality The user's rating (FORGOT/HARD/GOOD/EASY).
     * @param today Today's epoch day (from DateUtil.today()).
     * @return Updated [VocabularyWord] with new SM-2 state.
     */
    fun review(word: VocabularyWord, quality: SrsQuality, today: Long): VocabularyWord {
        val q = quality.q

        // Step 1: map 0..3 → 0..5 scale for SM-2 ease-factor formula
        val q5 = q * 5.0 / 3.0

        // Step 2: compute new ease factor
        var newEf = word.easeFactor + (0.1 - (5 - q5) * (0.08 + (5 - q5) * 0.02))

        // Step 3: enforce ease floor
        if (newEf < EASE_FLOOR) newEf = EASE_FLOOR

        // Step 4: lapse path (Forgot)
        if (q == 0) {
            return word.copy(
                repetitionCount  = 0,
                intervalDays     = 1,
                easeFactor       = newEf,
                nextReviewDate   = today + 1,
                lastReviewedDate = today,
                isIntroduced     = true,
            )
        }

        // Step 5: successful review path
        val newReps = word.repetitionCount + 1
        val rawInterval = when (newReps) {
            1    -> 1
            2    -> 6
            else -> ceil(word.intervalDays * newEf).toInt()
        }

        // Hard-button 20% interval penalty, minimum 1 day
        val finalInterval = if (q == 1) max(1, (rawInterval * HARD_PENALTY).toInt())
                            else rawInterval

        return word.copy(
            repetitionCount  = newReps,
            intervalDays     = finalInterval,
            easeFactor       = newEf,
            nextReviewDate   = today + finalInterval,
            lastReviewedDate = today,
            isIntroduced     = true,
        )
    }

    /**
     * Previews the next interval for each quality rating without committing any state.
     * Used to populate the "next interval" labels on the rating buttons.
     *
     * @param word The current card state.
     * @param today Today's epoch day.
     * @return Map of [SrsQuality] to the number of days until next review.
     */
    fun previewNextIntervals(word: VocabularyWord, today: Long): Map<SrsQuality, Int> =
        SrsQuality.entries.associate { quality ->
            quality to review(word, quality, today).intervalDays
        }
}
