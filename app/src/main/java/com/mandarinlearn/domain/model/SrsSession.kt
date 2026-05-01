// SrsSession.kt — Mandarin Learn
// Domain model for a spaced-repetition flashcard session.
// Constructed by SrsScheduler.getSession() in Phase 3.

package com.mandarinlearn.domain.model

/**
 * A study session: due cards first, then new cards up to newCardsLimit.
 * [dueCards] are already-introduced cards that need review today.
 * [newCards] are cards being shown for the first time.
 */
data class SrsSession(
    val hskLevel: Int,
    val dueCards: List<VocabularyWord>,
    val newCards: List<VocabularyWord>,
) {
    val totalCards: Int get() = dueCards.size + newCards.size
    val allCards: List<VocabularyWord> get() = dueCards + newCards
    val isEmpty: Boolean get() = totalCards == 0
}
