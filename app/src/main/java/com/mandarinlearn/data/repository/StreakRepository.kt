// StreakRepository.kt — Mandarin Learn
// Full implementation. Streak update logic per UX_SPECIFICATION.md §5.2.
// recordActivity() is called from ViewModels whenever a learning event occurs.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.StreakDao
import com.mandarinlearn.data.local.entity.StreakEntity
import com.mandarinlearn.domain.model.Streak
import com.mandarinlearn.util.DateUtil
import com.mandarinlearn.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for daily streak management.
 *
 * Streak logic per UX_SPECIFICATION.md §5.2:
 * - Activity today + last_active == today → no-op (already counted).
 * - Activity today + last_active == today − 1 → streak++.
 * - Else (gap > 1 day or never active) → streak = 1.
 * - longest_streak updated if current_streak exceeds it.
 */
class StreakRepository(
    private val dao: StreakDao,
    private val dispatchers: DispatcherProvider,
) {

    /** Returns the current streak as a reactive Flow. */
    fun getStreak(): Flow<Streak?> =
        dao.get().map { entity ->
            entity?.let {
                Streak(it.currentStreak, it.longestStreak, it.lastActiveDate)
            }
        }

    /**
     * Records learning activity for today and updates the streak accordingly.
     * Safe to call multiple times on the same day (idempotent via no-op check).
     */
    suspend fun recordActivity() {
        withContext(dispatchers.io) {
            val today = DateUtil.today()
            // Read the current streak row (non-reactive via first() workaround below)
            // We use a direct DAO suspend query approach via a local helper
            val current = getStreakEntityOnce()
            val entity = current ?: StreakEntity()

            val lastActive = entity.lastActiveDate

            val newStreak = when {
                lastActive == today -> return@withContext // already counted today
                lastActive == today - 1 -> entity.currentStreak + 1 // consecutive day
                else -> 1 // gap — reset streak
            }

            val newLongest = maxOf(entity.longestStreak, newStreak)

            dao.upsert(
                entity.copy(
                    currentStreak = newStreak,
                    longestStreak = newLongest,
                    lastActiveDate = today,
                )
            )
        }
    }

    /** Resets the streak (Phase 9 reset). */
    suspend fun resetStreak() {
        withContext(dispatchers.io) { dao.resetStreak() }
    }

    /**
     * Helper to get the streak entity once without collecting the Flow.
     * Delegates to StreakDao.getOnce() which returns null if no row exists.
     */
    private suspend fun getStreakEntityOnce(): StreakEntity? = dao.getOnce()
}
