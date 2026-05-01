// ReviewVocabularyUseCase.kt — Mandarin Learn
// Use case that applies an SM-2 review to a vocabulary card and persists the result.
// ARCHITECTURE.md §6: domain layer sits between ViewModels and repositories.

package com.mandarinlearn.domain.usecase

import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.domain.srs.SrsQuality
import com.mandarinlearn.domain.srs.SrsScheduler
import com.mandarinlearn.util.DateUtil

/**
 * Applies one SM-2 review to [word] and saves the updated state.
 * The SM-2 computation is pure (in [SrsScheduler]); persistence happens via [VocabularyRepository].
 *
 * Called by [FlashcardViewModel] after the user taps a rating button.
 */
class ReviewVocabularyUseCase(
    private val vocabularyRepository: VocabularyRepository,
) {
    /**
     * @param word The card to review.
     * @param quality The user's self-rating.
     * @return The updated [VocabularyWord] (already persisted to Room).
     */
    suspend operator fun invoke(word: VocabularyWord, quality: SrsQuality): VocabularyWord {
        val today = DateUtil.today()
        val updated = SrsScheduler.review(word, quality, today)
        vocabularyRepository.updateCard(updated)
        return updated
    }
}
