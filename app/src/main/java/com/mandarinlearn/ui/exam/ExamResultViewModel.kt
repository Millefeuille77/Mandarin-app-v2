// ExamResultViewModel.kt — Mandarin Learn
// Full Phase 7 ViewModel for ExamResultScreen.
// UX_SPECIFICATION.md §4 Screen 9; IMPLEMENTATION_PLAN.md Phase 7 §C.

package com.mandarinlearn.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "ExamResultViewModel"
private const val HISTORY_LIMIT = 5

/**
 * ViewModel for [ExamResultScreen].
 *
 * Loads a single exam result by [resultId] and the recent history for that HSK level.
 * The "Review mistakes" toggle is managed locally — no network needed.
 * The "Explain" button (Gemini chat) is TODO(phase_10): deferred per the plan.
 *
 * NEVER touches DAOs directly — calls [ExamRepository] only.
 */
class ExamResultViewModel(
    private val examRepository: ExamRepository,
    private val resultId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamResultUiState>(ExamResultUiState.Loading)
    val uiState: StateFlow<ExamResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    // ---- Public actions ----

    fun toggleMistakes() {
        val state = _uiState.value as? ExamResultUiState.Content ?: return
        _uiState.value = state.copy(showMistakesExpanded = !state.showMistakesExpanded)
    }

    // ---- Private helpers ----

    private fun loadResult() {
        viewModelScope.launch {
            try {
                // Collect the result first; then combine with history.
                val result = examRepository.getResultById(resultId)
                    .filterNotNull()
                    .first()

                // History for this level (newest-first); limit to 5.
                combine(
                    examRepository.getResultById(resultId),
                    examRepository.getResultsByLevel(result.hskLevel),
                ) { current, history ->
                    if (current == null) return@combine null
                    buildContent(current, history.take(HISTORY_LIMIT))
                }.collect { content ->
                    if (content != null) _uiState.value = content
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load exam result $resultId", e)
                _uiState.value = ExamResultUiState.Error(
                    e.message ?: "Could not load result."
                )
            }
        }
    }

    private fun buildContent(
        result: ExamResult,
        history: List<ExamResult>,
    ): ExamResultUiState.Content {
        // Build wrong-answer details from the answer records.
        val wrongAnswers = result.answers
            .filter { !it.isCorrect }
            .map { record ->
                WrongAnswerDetail(
                    questionText  = record.questionId,   // Phase 10: enrich with full text from DB
                    userAnswer    = record.userAnswer,
                    correctAnswer = record.correctAnswer,
                    explanation   = "",                   // Phase 10: load from SampleQuestion
                    questionId    = record.questionId,
                )
            }

        return ExamResultUiState.Content(
            result                = result,
            sectionScores         = result.sectionScores,
            wrongAnswers          = wrongAnswers,
            history               = history,
            showMistakesExpanded  = false,
        )
    }

    // ---- Factory ----

    companion object {
        fun factory(
            examRepository: ExamRepository,
            resultId: Long,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExamResultViewModel(examRepository, resultId) as T
        }
    }
}
