// PracticeHubViewModel.kt — Mandarin Learn
// ViewModel for PracticeHubScreen. Exposes StateFlow<PracticeHubUiState>.
// UX_SPECIFICATION.md §2 (Practice tab root). Phase 3 implementation.

package com.mandarinlearn.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.util.DateUtil
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "PracticeHubViewModel"

/**
 * ViewModel for PracticeHubScreen.
 * Shows the 4 practice sub-section entry points with a vocab due count for the selected HSK level.
 */
class PracticeHubViewModel(
    private val vocabularyRepository: VocabularyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PracticeHubUiState>(PracticeHubUiState.Loading)
    val uiState: StateFlow<PracticeHubUiState> = _uiState.asStateFlow()

    private val _selectedLevel = MutableStateFlow(1)

    init {
        loadDueCount(_selectedLevel.value)
    }

    /** Called when user selects a different HSK level chip on PracticeHubScreen. */
    fun selectLevel(level: Int) {
        _selectedLevel.value = level
        loadDueCount(level)
    }

    private fun loadDueCount(level: Int) {
        viewModelScope.launch {
            try {
                val today = DateUtil.today()
                val cards = vocabularyRepository.getDueAndNewCards(level, 0)
                val dueCount = cards.count { it.isIntroduced && it.nextReviewDate <= today }
                _uiState.value = PracticeHubUiState.Content(
                    selectedLevel  = level,
                    vocabDueCount  = dueCount,
                )
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load due count for HSK $level", e)
                _uiState.value = PracticeHubUiState.Error(e.message ?: "Could not load data")
            }
        }
    }

    companion object {
        fun factory(vocabularyRepository: VocabularyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PracticeHubViewModel(vocabularyRepository) as T
            }
    }
}
