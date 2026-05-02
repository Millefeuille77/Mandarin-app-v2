// ProgressViewModel.kt — Mandarin Learn
// ViewModel for ProgressScreen. Phase 8: Progress & Dashboard.
// UX_SPECIFICATION.md §4 Screen 10; ARCHITECTURE.md §6 (MVVM).
// Manual DI via companion-object factory (no Hilt/Dagger).

package com.mandarinlearn.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.data.repository.ProgressRepository
import com.mandarinlearn.data.repository.StreakRepository
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.domain.model.UserProgress
import com.mandarinlearn.domain.readiness.ReadinessCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * ViewModel for [ProgressScreen].
 *
 * Aggregates: streak, per-level vocab mastery, reading progress, and best exam scores.
 * Computes readiness via [ReadinessCalculator].
 * Never accesses DAOs directly — only through repositories (ARCHITECTURE.md §6).
 */
class ProgressViewModel(
    private val streakRepository: StreakRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository,
    private val examRepository: ExamRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _chartFilterLevel = MutableStateFlow<Int?>(null)

    init {
        loadProgress()
    }

    /** Changes the exam chart level filter. */
    fun setChartFilter(level: Int?) {
        _chartFilterLevel.value = level
        loadProgress()
    }

    /** Retries after an error. */
    fun retry() {
        _uiState.value = ProgressUiState.Loading
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val masteredFlows = (1..5).map { hsk -> vocabularyRepository.getMasteredCount(hsk) }
            val masteredCombined = combine(
                masteredFlows[0], masteredFlows[1], masteredFlows[2],
                masteredFlows[3], masteredFlows[4],
            ) { counts -> counts.toList() }

            combine(
                streakRepository.getStreak(),
                progressRepository.getAllProgress(),
                masteredCombined,
                examRepository.getRecentResults(50),
                _chartFilterLevel,
            ) { streak, allProgress, masteredCounts, recentResults, filterLevel ->

                val progressByLevel = allProgress.groupBy { it.hskLevel }

                val levelCards = (1..5).map { hsk ->
                    buildLevelCard(hsk, progressByLevel, masteredCounts, recentResults)
                }

                // Compute this-week activity from streak last_active or progress dates.
                val activeWeekDays = computeActiveWeekDays(allProgress)

                val chartResults = if (filterLevel == null) {
                    recentResults.sortedBy { it.finishedAt }
                } else {
                    recentResults.filter { it.hskLevel == filterLevel }.sortedBy { it.finishedAt }
                }

                ProgressUiState.Content(
                    currentStreak    = streak?.currentStreak ?: 0,
                    longestStreak    = streak?.longestStreak ?: 0,
                    activeWeekDays   = activeWeekDays,
                    levelCards       = levelCards,
                    chartResults     = chartResults,
                    chartFilterLevel = filterLevel,
                )
            }.catch { e ->
                _uiState.value = ProgressUiState.Error(e.message ?: "Failed to load progress")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun buildLevelCard(
        hsk: Int,
        progressByLevel: Map<Int, List<UserProgress>>,
        masteredCounts: List<Int>,
        recentResults: List<ExamResult>,
    ): LevelProgressCard {
        val mastered = masteredCounts.getOrElse(hsk - 1) { 0 }
        val vocabProgress = progressByLevel[hsk]?.find { it.section == "vocabulary" }
        val readingProgress = progressByLevel[hsk]?.find { it.section == "reading" }

        val totalVocab = vocabProgress?.totalItems ?: 0
        val readingCompleted = readingProgress?.completedItems ?: 0
        val readingTotal = readingProgress?.totalItems ?: 0
        val totalMaxScore = if (hsk <= 2) 200 else 300

        val bestExam = recentResults
            .filter { it.hskLevel == hsk }
            .maxByOrNull { it.totalScore }

        val vocabPct = if (totalVocab == 0) 0f
                       else (mastered.toFloat() / totalVocab * 100f).coerceIn(0f, 100f)
        val readPct = if (readingTotal == 0) 0f
                      else (readingCompleted.toFloat() / readingTotal * 100f).coerceIn(0f, 100f)
        val examPct = if (bestExam == null || totalMaxScore == 0) 0f
                      else (bestExam.totalScore.toFloat() / totalMaxScore * 100f).coerceIn(0f, 100f)

        val readiness = ReadinessCalculator.calculate(vocabPct, readPct, examPct)

        return LevelProgressCard(
            hskLevel        = hsk,
            masteredCount   = mastered,
            totalVocabCount = totalVocab,
            readingCompleted = readingCompleted,
            readingTotal    = readingTotal,
            bestExamResult  = bestExam,
            totalMaxScore   = totalMaxScore,
            readinessPct    = readiness,
        )
    }

    /** Returns a set of weekday indices (0=Sun..6=Sat) that had activity this week. */
    private fun computeActiveWeekDays(allProgress: List<UserProgress>): Set<Int> {
        val today = LocalDate.now(ZoneId.systemDefault())
        val dayOfWeek = today.dayOfWeek.value % 7 // 0=Sun..6=Sat
        val weekStart = today.minusDays(dayOfWeek.toLong())

        return allProgress
            .mapNotNull { it.lastActivityDate }
            .filter { epochDay ->
                val date = LocalDate.ofEpochDay(epochDay)
                !date.isBefore(weekStart) && !date.isAfter(today)
            }
            .map { epochDay ->
                val date = LocalDate.ofEpochDay(epochDay)
                date.dayOfWeek.value % 7
            }
            .toSet()
    }

    // ---- Factory ----

    companion object {
        fun factory(
            streakRepository: StreakRepository,
            vocabularyRepository: VocabularyRepository,
            progressRepository: ProgressRepository,
            examRepository: ExamRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProgressViewModel(
                    streakRepository, vocabularyRepository, progressRepository, examRepository
                ) as T
        }
    }
}
