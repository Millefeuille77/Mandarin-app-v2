// MeViewModel.kt — Mandarin Learn
// ViewModel for MeScreen. Phase 8: Progress & Dashboard.
// UX_SPECIFICATION.md §2: Me tab root linking to Progress and Settings.

package com.mandarinlearn.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.StreakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for [MeScreen].
 *
 * Observes the current streak for the Me tab streak display.
 * Never accesses DAOs directly — only StreakRepository (ARCHITECTURE.md §6).
 */
class MeViewModel(
    private val streakRepository: StreakRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MeUiState>(MeUiState.Loading)
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            streakRepository.getStreak()
                .catch { e ->
                    _uiState.value = MeUiState.Error(e.message ?: "Failed to load streak")
                }
                .collect { streak ->
                    _uiState.value = MeUiState.Content(
                        currentStreak = streak?.currentStreak ?: 0,
                        longestStreak = streak?.longestStreak ?: 0,
                    )
                }
        }
    }

    // ---- Factory ----

    companion object {
        fun factory(streakRepository: StreakRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MeViewModel(streakRepository) as T
            }
    }
}
