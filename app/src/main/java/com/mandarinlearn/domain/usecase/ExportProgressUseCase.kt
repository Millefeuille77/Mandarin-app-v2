// ExportProgressUseCase.kt — Mandarin Learn
// Domain use case for exporting user progress to JSON.
// Phase 9: Settings & Polish. IMPLEMENTATION_PLAN.md §Phase 9.
// Export schema follows IMPLEMENTATION_PLAN.md §Phase 9 Notes exactly.

package com.mandarinlearn.domain.usecase

import android.content.Context
import android.net.Uri
import com.mandarinlearn.data.local.MandarinLearnDatabase
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

private const val TAG = "ExportProgressUseCase"
private const val EXPORT_VERSION = 1

/**
 * Exports the user's SRS state, exam results, streak, and progress to a JSON file
 * written to a URI provided by the Storage Access Framework.
 *
 * Export schema (IMPLEMENTATION_PLAN.md §Phase 9 Notes):
 * ```json
 * {
 *   "version": 1,
 *   "exported_at": "2026-05-01T...",
 *   "vocabulary": [...],
 *   "exam_results": [...],
 *   "streak": {...},
 *   "user_progress": [...]
 * }
 * ```
 *
 * ViewModels MUST NOT call Room directly — this use case handles all data access.
 */
class ExportProgressUseCase(
    private val context: Context,
    private val database: MandarinLearnDatabase,
    private val json: Json,
) {

    /**
     * Reads all user-state data from Room and writes the JSON export to [uri].
     *
     * @param uri SAF URI obtained from [Intent.ACTION_CREATE_DOCUMENT] picker.
     * @return [Result.success] if written OK; [Result.failure] with an Exception on error.
     */
    suspend fun execute(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Gather vocabulary SRS state
            val vocabRows = database.vocabularyDao().getAllOnce()
            val vocabExport = vocabRows.map { v ->
                VocabExportRow(
                    id              = v.id,
                    easeFactor      = v.easeFactor,
                    intervalDays    = v.intervalDays,
                    repetitionCount = v.repetitionCount,
                    nextReviewDate  = v.nextReviewDate,
                    lastReviewedDate = v.lastReviewedDate,
                    isIntroduced    = v.isIntroduced,
                )
            }

            // Gather exam results
            val examRows = database.examResultDao().getAllOnce()
            val examExport = examRows.map { e ->
                ExamResultExportRow(
                    id                = e.id,
                    hskLevel          = e.hskLevel,
                    startedAt         = e.startedAt,
                    finishedAt        = e.finishedAt,
                    durationSeconds   = e.durationSeconds,
                    sectionScoresJson = e.sectionScoresJson,
                    totalScore        = e.totalScore,
                    totalMaxScore     = e.totalMaxScore,
                    passingScore      = e.passingScore,
                    passed            = e.passed,
                    answersJson       = e.answersJson,
                )
            }

            // Gather streak
            val streakRow = database.streakDao().getOnce()
            val streakExport = StreakExportRow(
                currentStreak  = streakRow?.currentStreak ?: 0,
                longestStreak  = streakRow?.longestStreak ?: 0,
                lastActiveDate = streakRow?.lastActiveDate,
            )

            // Gather user progress
            val progressRows = database.userProgressDao().getAllOnce()
            val progressExport = progressRows.map { p ->
                ProgressExportRow(
                    hskLevel        = p.hskLevel,
                    section         = p.section,
                    totalItems      = p.totalItems,
                    completedItems  = p.completedItems,
                    lastActivityDate = p.lastActivityDate,
                )
            }

            val exportData = ExportSnapshot(
                version     = EXPORT_VERSION,
                exportedAt  = Instant.now().toString(),
                vocabulary  = vocabExport,
                examResults = examExport,
                streak      = streakExport,
                userProgress = progressExport,
            )

            val jsonString = json.encodeToString(exportData)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonString.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Logger.d(TAG, "Export complete: ${jsonString.length} chars written to $uri")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Export failed", e)
            Result.failure(e)
        }
    }
}

// ---- Export DTOs (serialized into the JSON file) ----

@Serializable
data class ExportSnapshot(
    val version: Int,
    val exportedAt: String,
    val vocabulary: List<VocabExportRow>,
    val examResults: List<ExamResultExportRow>,
    val streak: StreakExportRow,
    val userProgress: List<ProgressExportRow>,
)

@Serializable
data class VocabExportRow(
    val id: String,
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitionCount: Int,
    val nextReviewDate: Long,
    val lastReviewedDate: Long?,
    val isIntroduced: Int,
)

@Serializable
data class ExamResultExportRow(
    val id: Long,
    val hskLevel: Int,
    val startedAt: Long,
    val finishedAt: Long,
    val durationSeconds: Int,
    val sectionScoresJson: String,
    val totalScore: Int,
    val totalMaxScore: Int,
    val passingScore: Int,
    val passed: Int,
    val answersJson: String,
)

@Serializable
data class StreakExportRow(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long?,
)

@Serializable
data class ProgressExportRow(
    val hskLevel: Int,
    val section: String,
    val totalItems: Int,
    val completedItems: Int,
    val lastActivityDate: Long?,
)
