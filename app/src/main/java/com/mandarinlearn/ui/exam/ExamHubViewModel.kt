// ExamHubViewModel.kt — Mandarin Learn
// ViewModel for ExamHubScreen.
// UX_SPECIFICATION.md §2 (ExamHubScreen); IMPLEMENTATION_PLAN.md Phase 7 §A.

package com.mandarinlearn.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val TAG = "ExamHubViewModel"

/**
 * ViewModel for [ExamHubScreen].
 *
 * Loads per-level exam history from [ExamRepository] and maps it to
 * [ExamHubUiState.Content] with [ExamLevelSummary] for each of HSK 1–5.
 *
 * NEVER touches DAOs directly — only calls the repository.
 */
class ExamHubViewModel(
    private val examRepository: ExamRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamHubUiState>(ExamHubUiState.Loading)
    val uiState: StateFlow<ExamHubUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                // Combine flows for all 5 HSK levels simultaneously.
                val level1Flow = examRepository.getResultsByLevel(1)
                val level2Flow = examRepository.getResultsByLevel(2)
                val level3Flow = examRepository.getResultsByLevel(3)
                val level4Flow = examRepository.getResultsByLevel(4)
                val level5Flow = examRepository.getResultsByLevel(5)

                combine(
                    level1Flow, level2Flow, level3Flow, level4Flow, level5Flow
                ) { l1, l2, l3, l4, l5 ->
                    listOf(l1, l2, l3, l4, l5).mapIndexed { idx, results ->
                        val hsk = idx + 1
                        // Best result: most recent (DAOs return newest-first per ARCHITECTURE.md).
                        val best = results.firstOrNull()
                        ExamLevelSummary(
                            hskLevel     = hsk,
                            bestResult   = best,
                            attemptCount = results.size,
                        )
                    }
                }.collect { summaries ->
                    _uiState.value = ExamHubUiState.Content(summaries)
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load exam history", e)
                _uiState.value = ExamHubUiState.Error(
                    e.message ?: "Could not load exam history"
                )
            }
        }
    }

    // ---- Factory ----

    companion object {
        fun factory(examRepository: ExamRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ExamHubViewModel(examRepository) as T
            }
    }
}
