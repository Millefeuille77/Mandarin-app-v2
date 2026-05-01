// VocabularyWord.kt — Mandarin Learn
// Domain model for a vocabulary word with SM-2 state.
// Mapped from VocabularyEntity; this layer knows nothing about Room.

package com.mandarinlearn.domain.model

/**
 * Domain representation of one vocabulary word.
 * SRS status is derived (not stored) — see [srsStatus].
 */
data class VocabularyWord(
    val id: String,
    val hskLevel: Int,
    val character: String,
    val pinyin: String,
    val translation: String,
    val partOfSpeech: String,
    val exampleChinese: String,
    val examplePinyin: String,
    val exampleEnglish: String,
    // SM-2 fields
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitionCount: Int,
    val nextReviewDate: Long,
    val lastReviewedDate: Long?,
    val isIntroduced: Boolean,
) {
    /**
     * Derived SRS status for display badges.
     * "Mastered" definition: repetitionCount >= 5 AND easeFactor >= 2.5 AND intervalDays >= 21
     * per ARCHITECTURE.md §5.5.
     */
    val srsStatus: SrsStatus
        get() = when {
            !isIntroduced -> SrsStatus.NEW
            repetitionCount >= 5 && easeFactor >= 2.5 && intervalDays >= 21 -> SrsStatus.MASTERED
            else -> SrsStatus.LEARNING
        }
}

enum class SrsStatus { NEW, LEARNING, MASTERED }
