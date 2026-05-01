// UserProgress.kt — Mandarin Learn
// Domain model for per-section, per-level learning progress.

package com.mandarinlearn.domain.model

/**
 * Progress for one (hsk_level, section) pair.
 * [section] is one of: vocabulary, reading, listening, speaking, exam.
 */
data class UserProgress(
    val hskLevel: Int,
    val section: String,
    val totalItems: Int,
    val completedItems: Int,
    val lastActivityDate: Long?,
) {
    /** 0.0..1.0 completion percentage (0 if totalItems = 0 to avoid division by zero). */
    val completionFraction: Float
        get() = if (totalItems == 0) 0f else completedItems.toFloat() / totalItems.toFloat()
}
