// Streak.kt — Mandarin Learn
// Domain model for the user's daily learning streak.

package com.mandarinlearn.domain.model

/**
 * Current streak state. Streak update logic lives in StreakRepository.
 */
data class Streak(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long?,
)
