// ReadingRepository.kt — Mandarin Learn
// Full implementation. Maps ReadingEntity ↔ ReadingPassage domain model.
// Decodes JSON pinyin_annotations and vocabulary_highlights from the entity.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.ReadingDao
import com.mandarinlearn.data.local.entity.ReadingEntity
import com.mandarinlearn.data.local.import.dto.PinyinAnnotationDto
import com.mandarinlearn.domain.model.PinyinAnnotation
import com.mandarinlearn.domain.model.ReadingPassage
import com.mandarinlearn.util.DateUtil
import com.mandarinlearn.util.DispatcherProvider
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private const val TAG = "ReadingRepository"

/**
 * Repository for reading passages.
 * Decodes JSON columns into typed domain objects before returning to callers.
 */
class ReadingRepository(
    private val dao: ReadingDao,
    private val dispatchers: DispatcherProvider,
) {

    // Json instance for decoding stored JSON columns — lenient to survive minor schema changes
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    /** Returns all passages for a level as a reactive Flow with decoded annotations. */
    fun getPassagesByLevel(hsk: Int): Flow<List<ReadingPassage>> =
        dao.getByLevel(hsk).map { list -> list.mapNotNull { it.toDomainSafe() } }

    /** Returns a single passage by id as a reactive Flow. */
    fun getPassageById(id: String): Flow<ReadingPassage?> =
        dao.getById(id).map { entity -> entity?.toDomainSafe() }

    /** Marks a passage as read and updates user_progress in Phase 8. */
    suspend fun markCompleted(id: String) {
        withContext(dispatchers.io) {
            try {
                dao.markCompleted(id, DateUtil.today())
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to mark passage $id as completed", e)
                throw e
            }
        }
    }

    /** Count of completed passages per level — reactive, for ProgressScreen. */
    fun getCompletedCount(hsk: Int): Flow<Int> = dao.countCompletedByLevel(hsk)

    /** Resets all read progress (ResetProgressUseCase in Phase 9). */
    suspend fun resetAllProgress() {
        withContext(dispatchers.io) { dao.resetAllProgress() }
    }

    private fun ReadingEntity.toDomainSafe(): ReadingPassage? {
        return try {
            val annotations = json.decodeFromString(
                ListSerializer(PinyinAnnotationDto.serializer()),
                pinyinAnnotations
            ).map { PinyinAnnotation(it.character, it.pinyin) }

            val highlights = json.decodeFromString(
                ListSerializer(String.serializer()),
                vocabularyHighlights
            )

            ReadingPassage(
                id = id,
                hskLevel = hskLevel,
                title = title,
                chineseText = chineseText,
                pinyinAnnotations = annotations,
                englishTranslation = englishTranslation,
                vocabularyHighlights = highlights,
                wordCount = wordCount,
                isCompleted = isCompleted == 1,
                completedAt = completedAt,
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decode passage $id", e)
            null
        }
    }
}
