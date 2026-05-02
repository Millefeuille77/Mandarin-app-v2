// ImportProgressUseCase.kt — Mandarin Learn
// Domain use case for importing a JSON progress snapshot from a SAF URI.
// Phase 9: Settings & Polish. IMPLEMENTATION_PLAN.md §Phase 9.
// Validates version == 1; rejects otherwise with a clear failure.

package com.mandarinlearn.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.mandarinlearn.data.local.MandarinLearnDatabase
import com.mandarinlearn.data.local.entity.ExamResultEntity
import com.mandarinlearn.data.local.entity.StreakEntity
import com.mandarinlearn.data.local.entity.UserProgressEntity
import com.mandarinlearn.data.local.entity.VocabularyEntity
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val TAG = "ImportProgressUseCase"

/**
 * Reads a JSON progress snapshot written by [ExportProgressUseCase] and replaces
 * the current user-state in Room. Content tables (vocab content, readings, questions) are
 * NOT touched — only SRS fields and exam history are replaced.
 *
 * @throws ImportValidationException if the file cannot be parsed or version != 1.
 */
class ImportProgressUseCase(
    private val context: Context,
    private val database: MandarinLearnDatabase,
    private val json: Json,
) {

    /**
     * Parses the JSON at [uri] and applies it to Room.
     *
     * @return [Result.success] on clean import; [Result.failure] with an Exception on any error.
     */
    suspend fun execute(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: return@withContext Result.failure(
                ImportValidationException("Could not open the selected file")
            )

            val snapshot = try {
                json.decodeFromString(ExportSnapshot.serializer(), jsonString)
            } catch (e: Exception) {
                Logger.w(TAG, "JSON parse failed", e)
                return@withContext Result.failure(
                    ImportValidationException("File could not be read — format not recognised")
                )
            }

            if (snapshot.version != 1) {
                return@withContext Result.failure(
                    ImportValidationException(
                        "Unsupported file version ${snapshot.version} — only version 1 is supported"
                    )
                )
            }

            // Apply all data inside one transaction for atomicity.
            // withTransaction (suspend version) is required because the DAO methods are suspend.
            database.withTransaction {
                // 1. Apply vocabulary SRS state to existing vocabulary rows
                //    Content columns (character, pinyin, etc.) are NOT overwritten.
                snapshot.vocabulary.forEach { row ->
                    database.vocabularyDao().updateSrsFields(
                        id               = row.id,
                        easeFactor       = row.easeFactor,
                        intervalDays     = row.intervalDays,
                        repetitionCount  = row.repetitionCount,
                        nextReviewDate   = row.nextReviewDate,
                        lastReviewedDate = row.lastReviewedDate,
                        isIntroduced     = row.isIntroduced,
                    )
                }

                // 2. Replace exam results
                database.examResultDao().deleteAll()
                snapshot.examResults.forEach { row ->
                    database.examResultDao().insert(
                        ExamResultEntity(
                            id                = 0L, // autoGenerate resets id
                            hskLevel          = row.hskLevel,
                            startedAt         = row.startedAt,
                            finishedAt        = row.finishedAt,
                            durationSeconds   = row.durationSeconds,
                            sectionScoresJson = row.sectionScoresJson,
                            totalScore        = row.totalScore,
                            totalMaxScore     = row.totalMaxScore,
                            passingScore      = row.passingScore,
                            passed            = row.passed,
                            answersJson       = row.answersJson,
                        )
                    )
                }

                // 3. Replace streak
                database.streakDao().upsert(
                    StreakEntity(
                        id            = 1,
                        currentStreak = snapshot.streak.currentStreak,
                        longestStreak = snapshot.streak.longestStreak,
                        lastActiveDate = snapshot.streak.lastActiveDate,
                    )
                )

                // 4. Replace user progress (preserve total_items from existing rows)
                snapshot.userProgress.forEach { row ->
                    val existing = database.userProgressDao()
                        .getByLevelAndSectionSync(row.hskLevel, row.section)
                    database.userProgressDao().upsert(
                        UserProgressEntity(
                            hskLevel         = row.hskLevel,
                            section          = row.section,
                            totalItems       = existing?.totalItems ?: row.totalItems,
                            completedItems   = row.completedItems,
                            lastActivityDate = row.lastActivityDate,
                        )
                    )
                }
            }

            Logger.d(TAG, "Import complete from $uri")
            Result.success(Unit)
        } catch (e: ImportValidationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Logger.e(TAG, "Import failed unexpectedly", e)
            Result.failure(Exception("File could not be read"))
        }
    }
}

/** Thrown when the import JSON is invalid or has an unsupported version. */
class ImportValidationException(message: String) : Exception(message)
