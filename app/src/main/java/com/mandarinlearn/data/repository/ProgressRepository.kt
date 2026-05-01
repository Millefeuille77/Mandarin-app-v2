// ProgressRepository.kt — Mandarin Learn
// Full stub for Phase 2. Provides user_progress CRUD.
// Phase 8 will add the ReadinessCalculator integration.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.UserProgressDao
import com.mandarinlearn.data.local.entity.UserProgressEntity
import com.mandarinlearn.domain.model.UserProgress
import com.mandarinlearn.util.DateUtil
import com.mandarinlearn.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for per-section, per-level progress rows.
 * Updated by domain events in Phase 3–7 (card mastered, passage read, etc.).
 */
class ProgressRepository(
    private val dao: UserProgressDao,
    private val dispatchers: DispatcherProvider,
) {

    /** Returns all progress rows as a reactive Flow. */
    fun getAllProgress(): Flow<List<UserProgress>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    /** Returns progress for a specific level and section — reactive. */
    fun getProgress(hsk: Int, section: String): Flow<UserProgress?> =
        dao.getByLevelAndSection(hsk, section).map { it?.toDomain() }

    /**
     * Records one completed item in a given section.
     * Increments completed_items by 1 and updates last_activity_date.
     * Called from vocabulary/reading/listening/speaking/exam ViewModels in Phases 3–7.
     */
    suspend fun recordCompletion(hsk: Int, section: String) {
        withContext(dispatchers.io) {
            val existing = dao.getByLevelAndSectionOnce(hsk, section) ?: return@withContext
            dao.upsert(
                existing.copy(
                    completedItems = (existing.completedItems + 1).coerceAtMost(existing.totalItems),
                    lastActivityDate = com.mandarinlearn.util.DateUtil.today(),
                )
            )
        }
    }

    /** Resets all completed_items to 0 (Phase 9 reset). */
    suspend fun resetAllProgress() {
        withContext(dispatchers.io) { dao.resetAllProgress() }
    }

    // ---- Mapper ----

    private fun UserProgressEntity.toDomain() = UserProgress(
        hskLevel = hskLevel,
        section = section,
        totalItems = totalItems,
        completedItems = completedItems,
        lastActivityDate = lastActivityDate,
    )
}
