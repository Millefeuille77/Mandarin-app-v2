// ResetProgressUseCase.kt — Mandarin Learn
// Domain use case for resetting all user progress.
// Phase 9: Settings & Polish. IMPLEMENTATION_PLAN.md §Phase 9.
// Content tables (vocab content, readings, exam structures, sample questions) are NOT touched.

package com.mandarinlearn.domain.usecase

import com.mandarinlearn.data.local.MandarinLearnDatabase
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ResetProgressUseCase"

/**
 * Resets all user-state data in Room inside a single transaction.
 *
 * Per IMPLEMENTATION_PLAN.md §Phase 9 Notes, the reset:
 * - Sets `vocabulary.is_introduced = 0` and SM-2 fields to defaults.
 * - Deletes all `exam_results`.
 * - Resets the `streak` row (current_streak = 0, last_active_date = NULL).
 * - Zeroes `user_progress.completed_items` for all rows.
 * - Does NOT delete content rows (vocab content, readings, questions, etc.).
 * - Does NOT touch `data_version` (no re-import triggered).
 *
 * ViewModels never call Room directly; they call this use case through SettingsViewModel.
 */
class ResetProgressUseCase(
    private val database: MandarinLearnDatabase,
) {

    /**
     * Executes the full reset as a single atomic Room transaction.
     *
     * @return [Result.success] on completion; [Result.failure] on any exception.
     */
    suspend fun execute(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.runInTransaction {
                // 1. Reset SRS state on all vocabulary rows (content columns untouched)
                database.vocabularyDao().resetAllProgress()

                // 2. Wipe all exam results
                database.examResultDao().deleteAll()

                // 3. Reset streak to zero
                database.streakDao().resetStreak()

                // 4. Zero out all user_progress completed_items
                database.userProgressDao().resetAllProgress()
            }
            Logger.d(TAG, "Full progress reset complete")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Progress reset failed", e)
            Result.failure(e)
        }
    }
}
