// SrsQuality.kt — Mandarin Learn
// Enum representing the four rating buttons on FlashcardScreen.
// Maps to quality values 0..3 used in the SM-2 formula (ARCHITECTURE.md §5.2).

package com.mandarinlearn.domain.srs

/**
 * Quality rating the user gives after seeing a flashcard answer.
 * Maps to SM-2 quality integer (q) 0..3.
 * The spec uses 0–3 (not 0–5) and scales internally to the 0–5 range in the formula.
 */
enum class SrsQuality(val q: Int) {
    /** "I had no idea" — resets the card. */
    FORGOT(0),
    /** "I got it but it was a struggle" — applies 0.8× interval penalty. */
    HARD(1),
    /** "I knew it" — standard successful review. */
    GOOD(2),
    /** "Trivial" — confident, boosts ease factor. */
    EASY(3);

    companion object {
        /** Returns the [SrsQuality] for the given integer quality value. */
        fun fromQ(q: Int): SrsQuality = entries.first { it.q == q }
    }
}
