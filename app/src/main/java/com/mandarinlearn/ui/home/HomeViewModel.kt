// HomeViewModel.kt — Mandarin Learn
// ViewModel for HomeScreen. Phase 8: Progress & Dashboard.
// UX_SPECIFICATION.md §4 Screen 1; ARCHITECTURE.md §6 (MVVM, no DAO access).
// Manual DI via companion-object factory (no Hilt/Dagger).

package com.mandarinlearn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.domain.usecase.GetDashboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for [HomeScreen].
 *
 * Observes [GetDashboardUseCase.observe] and maps the result to [HomeUiState].
 * Never accesses DAOs or repositories directly — ARCHITECTURE.md §6.
 */
class HomeViewModel(
    private val getDashboardUseCase: GetDashboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /** Observable UI state. Never LiveData. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    /** Starts collecting the dashboard flow; emits Loading → Content or Error. */
    private fun loadDashboard() {
        viewModelScope.launch {
            getDashboardUseCase.observe()
                .catch { e ->
                    _uiState.value = HomeUiState.Error(
                        e.message ?: "Failed to load dashboard"
                    )
                }
                .collect { data ->
                    val rows = data.levels.map { levelData ->
                        LevelProgressRow(
                            hskLevel     = levelData.hskLevel,
                            masteredCount = levelData.masteredCount,
                            totalCount   = levelData.totalVocabCount,
                        )
                    }
                    _uiState.value = HomeUiState.Content(
                        currentStreak = data.streak.currentStreak,
                        longestStreak = data.streak.longestStreak,
                        totalDueCount = data.totalDueCount,
                        levelRows     = rows,
                        focusLevel    = data.focusLevel ?: 1,
                    )
                }
        }
    }

    /** Retry after an error — re-subscribes to the use case. */
    fun retry() {
        _uiState.value = HomeUiState.Loading
        loadDashboard()
    }

    // ---- Factory ----

    companion object {
        fun factory(getDashboardUseCase: GetDashboardUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(getDashboardUseCase) as T
            }
    }
}
