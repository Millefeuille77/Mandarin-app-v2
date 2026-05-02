// ReadinessCalculator.kt — Mandarin Learn
// Pure domain function: computes readiness percentage per UX_SPECIFICATION.md §4 Screen 10.
// Phase 8: Progress & Dashboard.
// Formula: 0.4 * vocab_mastered_pct + 0.2 * reading_done_pct + 0.4 * best_exam_pct, clamped 0..100.

package com.mandarinlearn.domain.readiness

/**
 * Stateless readiness calculator for a single HSK level.
 *
 * Readiness gives the user a single "how ready am I for the real exam?" number.
 * Weights:
 * - Vocabulary mastery counts for 40% (direct recall of words).
 * - Reading completion counts for 20% (reading exposure, lower weight).
 * - Best exam score counts for 40% (actual timed performance = best predictor).
 *
 * All inputs are expected in the range 0.0..100.0 (percentage points, not fractions).
 * The result is also a percentage, clamped strictly to 0..100.
 *
 * Pure Kotlin — no Android imports, trivially unit-testable.
 */
object ReadinessCalculator {

    /**
     * Computes readiness percentage.
     *
     * @param vocabMasteredPct   Percentage of vocabulary words mastered (0..100).
     * @param readingDonePct     Percentage of reading passages completed (0..100).
     * @param bestExamPct        Best exam percentage for this level (0..100).
     * @return Readiness percentage clamped to 0..100.
     */
    fun calculate(
        vocabMasteredPct: Float,
        readingDonePct: Float,
        bestExamPct: Float,
    ): Float {
        // Inputs may come from Room counts that are out of expected range; clamp defensively.
        val vocab = vocabMasteredPct.coerceIn(0f, 100f)
        val reading = readingDonePct.coerceIn(0f, 100f)
        val exam = bestExamPct.coerceIn(0f, 100f)

        // Per UX spec §4 Screen 10 / IMPLEMENTATION_PLAN.md Phase 8 formula.
        val raw = 0.4f * vocab + 0.2f * reading + 0.4f * exam

        // Clamp the result to 0..100 (should be in range given clamped inputs, but be safe).
        return raw.coerceIn(0f, 100f)
    }
}
