// ImportLoadingViewModel.kt — Mandarin Learn
// ViewModel for ImportLoadingScreen. Phase 2: full implementation.
// Collects ImportProgress flow from JsonImporter and drives UI state.

package com.mandarinlearn.ui.importing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.local.import.JsonImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for [ImportLoadingScreen].
 * Drives the import process and exposes [uiState] for the screen to observe.
 * The [retry] function re-triggers the import flow on error.
 */
class ImportLoadingViewModel(
    private val importer: JsonImporter,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportLoadingUiState>(ImportLoadingUiState.Idle)
    val uiState: StateFlow<ImportLoadingUiState> = _uiState.asStateFlow()

    init {
        startImport()
    }

    fun retry() {
        startImport()
    }

    private fun startImport() {
        viewModelScope.launch {
            importer.importIfNeeded().collect { progress ->
                _uiState.value = when {
                    progress.isError -> ImportLoadingUiState.Error(progress.message)
                    progress.fraction >= 1f -> ImportLoadingUiState.Done
                    else -> ImportLoadingUiState.Progress(progress.fraction, progress.message)
                }
            }
        }
    }

    companion object {
        /** Factory for ViewModelProvider — receives AppContainer-provided JsonImporter. */
        fun factory(importer: JsonImporter): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ImportLoadingViewModel(importer) as T
            }
    }
}
