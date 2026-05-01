// ReadingListViewModel.kt — Mandarin Learn
// ViewModel for ReadingListScreen. Full Phase 4 implementation.
// UX_SPECIFICATION.md §4 Screen 4. ARCHITECTURE.md §6 (MVVM + Repository).

package com.mandarinlearn.ui.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.ReadingRepository
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "ReadingListViewModel"

/**
 * ViewModel for [ReadingListScreen].
 * Loads reading passages for the selected HSK level reactively via a Room Flow.
 * Exposes [StateFlow]<[ReadingListUiState]> — never LiveData.
 *
 * @param readingRepository Repository that owns all ReadingPassage data.
 * @param initialLevel      HSK level to load on init (passed from navigation argument).
 */
class ReadingListViewModel(
    private val readingRepository: ReadingRepository,
    initialLevel: Int = 1,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReadingListUiState>(ReadingListUiState.Loading)
    val uiState: StateFlow<ReadingListUiState> = _uiState.asStateFlow()

    private val _selectedLevel = MutableStateFlow(initialLevel)

    init {
        observePassages()
    }

    /** Called when user taps an HskLevelChip. Re-loads passages for the new level. */
    fun selectLevel(level: Int) {
        _selectedLevel.value = level
    }

    private fun observePassages() {
        viewModelScope.launch {
            _selectedLevel.collectLatest { level ->
                _uiState.value = ReadingListUiState.Loading
                readingRepository.getPassagesByLevel(level)
                    .catch { e ->
                        Logger.e(TAG, "Failed to load passages for HSK $level", e)
                        emit(emptyList())
                        _uiState.value = ReadingListUiState.Error(
                            e.message ?: "Could not load reading passages"
                        )
                    }
                    .collect { passages ->
                        // Only update state if we haven't already set an Error above
                        if (_uiState.value !is ReadingListUiState.Error) {
                            _uiState.value = ReadingListUiState.Content(
                                passages      = passages,
                                selectedLevel = level,
                            )
                        }
                    }
            }
        }
    }

    companion object {
        /** Factory for AppContainer-based constructor injection. */
        fun factory(
            readingRepository: ReadingRepository,
            initialLevel: Int = 1,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReadingListViewModel(readingRepository, initialLevel) as T
        }
    }
}
