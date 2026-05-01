// VocabularyViewModel.kt — Mandarin Learn
// ViewModel for VocabularyScreen. Exposes StateFlow<VocabularyUiState>.
// UX_SPECIFICATION.md §4 Screen 2. ARCHITECTURE.md §6 (MVVM + Repository).

package com.mandarinlearn.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.util.DateUtil
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 300L

/**
 * ViewModel for VocabularyScreen.
 * Loads words for the selected HSK level, handles search with 300 ms debounce,
 * and computes stats (total / mastered / due / new).
 *
 * Uses constructor injection; obtain via [factory].
 */
class VocabularyViewModel(
    private val vocabularyRepository: VocabularyRepository,
    initialLevel: Int = 1,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VocabularyUiState>(VocabularyUiState.Loading)
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    private val _selectedLevel = MutableStateFlow(initialLevel)
    private val _searchQuery = MutableStateFlow("")

    init {
        observeWords()
    }

    /** Called when the user taps an HskLevelChip. */
    fun selectLevel(level: Int) {
        _selectedLevel.value = level
    }

    /** Called when the search text changes. Debounce is applied in [observeWords]. */
    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class)
    private fun observeWords() {
        viewModelScope.launch {
            _selectedLevel.collectLatest { level ->
                _uiState.value = VocabularyUiState.Loading
                // Combine the debounced search query with the word list
                val wordsFlow = vocabularyRepository.getWordsByLevel(level)
                val masteredFlow = vocabularyRepository.getMasteredCount(level)
                val searchFlow = _searchQuery.debounce(SEARCH_DEBOUNCE_MS)

                combine(wordsFlow, masteredFlow, searchFlow) { words, mastered, query ->
                    val today = DateUtil.today()
                    val filtered = if (query.isBlank()) words
                    else words.filter { w ->
                        w.character.contains(query, ignoreCase = true) ||
                        w.pinyin.contains(query, ignoreCase = true) ||
                        w.translation.contains(query, ignoreCase = true)
                    }
                    val due = words.count { w ->
                        w.isIntroduced && w.nextReviewDate <= today
                    }
                    val new = words.count { w -> !w.isIntroduced }

                    VocabularyUiState.Content(
                        words         = filtered,
                        selectedLevel = level,
                        searchQuery   = query,
                        totalCount    = words.size,
                        masteredCount = mastered,
                        dueCount      = due,
                        newCount      = new,
                    )
                }
                .catch { e ->
                    emit(VocabularyUiState.Error(e.message ?: "Unknown error"))
                }
                .collect { state ->
                    _uiState.value = state
                }
            }
        }
    }

    companion object {
        /** Factory for AppContainer-based constructor injection. */
        fun factory(
            vocabularyRepository: VocabularyRepository,
            initialLevel: Int = 1,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                VocabularyViewModel(vocabularyRepository, initialLevel) as T
        }
    }
}
