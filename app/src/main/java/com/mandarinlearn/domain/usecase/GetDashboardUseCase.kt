// GetDashboardUseCase.kt — Mandarin Learn
// Aggregates all data needed for HomeScreen into a single reactive Flow.
// Phase 8: Progress & Dashboard (IMPLEMENTATION_PLAN.md Phase 8 §C / §D).

package com.mandarinlearn.domain.usecase

import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.data.repository.ProgressRepository
import com.mandarinlearn.data.repository.StreakRepository
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.domain.model.Streak
import com.mandarinlearn.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Aggregated dashboard data for a single HSK level.
 *
 * @param hskLevel          The HSK level (1–5).
 * @param masteredCount     Count of mastered vocabulary words.
 * @param totalVocabCount   Total vocabulary count for this level.
 * @param readingProgress   Progress row for the "reading" section (may be null if not seeded).
 * @param bestExamResult    Best exam result for this level (null if no exams taken).
 * @param totalMaxScore     Maximum score for this level's exam (from exam structure; 200 or 300).
 */
data class LevelDashboardData(
    val hskLevel: Int,
    val masteredCount: Int,
    val totalVocabCount: Int,
    val readingProgress: UserProgress?,
    val bestExamResult: ExamResult?,
    val totalMaxScore: Int,
)

/** Collected snapshot used by HomeViewModel and ProgressViewModel. */
data class DashboardData(
    val streak: Streak,
    val levels: List<LevelDashboardData>,
    /** HSK level with due cards — used by Home CTA. Null if no cards are due anywhere. */
    val focusLevel: Int?,
    /** Total vocabulary cards due today across all levels. */
    val totalDueCount: Int,
)

/**
 * Use case that combines streak, per-level vocab mastery, reading progress,
 * and exam history into a single observable [DashboardData] flow.
 *
 * Only repositories are touched — no DAOs accessed directly (ARCHITECTURE.md §6).
 */
class GetDashboardUseCase(
    private val streakRepository: StreakRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository,
    private val examRepository: ExamRepository,
) {

    /**
     * Returns a Flow that emits whenever any underlying data changes.
     * Used by [HomeViewModel] and [ProgressViewModel].
     */
    fun observe(): Flow<DashboardData> {
        // Build one reactive flow per HSK level (1–5) for mastered count.
        val masteredFlows = (1..5).map { hsk -> vocabularyRepository.getMasteredCount(hsk) }
        val progressFlow = progressRepository.getAllProgress()
        val streakFlow = streakRepository.getStreak()
        val dueCounts = (1..5).map { hsk -> vocabularyRepository.getDueCountForLevel(hsk) }

        // Combine: streak + all 5 mastered counts + progress + 5 due counts.
        // We use a nested combine approach (combine supports up to ~5 flows directly;
        // for more we nest pairs).
        val masteredCombined: Flow<List<Int>> = combine(
            masteredFlows[0], masteredFlows[1], masteredFlows[2],
            masteredFlows[3], masteredFlows[4],
        ) { counts -> counts.toList() }

        val dueCombined: Flow<List<Int>> = combine(
            dueCounts[0], dueCounts[1], dueCounts[2],
            dueCounts[3], dueCounts[4],
        ) { counts -> counts.toList() }

        return combine(
            streakFlow,
            masteredCombined,
            progressFlow,
            dueCombined,
        ) { streak, masteredCounts, allProgress, dueCountsList ->

            val progressByLevel = allProgress.groupBy { it.hskLevel }

            // Best exam results per level are queried synchronously from allProgress;
            // exam history data flows separately only on ProgressScreen.
            // For the dashboard we use the vocab/reading data — exam will be added
            // by ProgressViewModel via examRepository directly (keeps this use case lighter).
            val levels = (1..5).map { hsk ->
                val mastered = masteredCounts.getOrElse(hsk - 1) { 0 }
                val readingProgress = progressByLevel[hsk]?.find { it.section == "reading" }
                val vocabProgress = progressByLevel[hsk]?.find { it.section == "vocabulary" }
                val totalVocab = vocabProgress?.totalItems ?: 0

                LevelDashboardData(
                    hskLevel = hsk,
                    masteredCount = mastered,
                    totalVocabCount = totalVocab,
                    readingProgress = readingProgress,
                    bestExamResult = null, // Filled by ProgressViewModel for ProgressScreen
                    totalMaxScore = if (hsk <= 2) 200 else 300,
                )
            }

            // Focus level = lowest HSK level with any due cards.
            val focusLevel = dueCountsList.indexOfFirst { it > 0 }
                .takeIf { it >= 0 }
                ?.let { it + 1 } // convert 0-indexed to HSK level

            DashboardData(
                streak = streak ?: Streak(0, 0, null),
                levels = levels,
                focusLevel = focusLevel,
                totalDueCount = dueCountsList.sum(),
            )
        }
    }
}
