// SettingsViewModel.kt — Mandarin Learn
// Full Phase 9 ViewModel for SettingsScreen.
// UX_SPECIFICATION.md §4 Screen 11. IMPLEMENTATION_PLAN.md §Phase 9.
// Reads/writes DataStore via SettingsRepository; delegates export/import/reset to use cases.

package com.mandarinlearn.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.SettingsRepository
import com.mandarinlearn.domain.usecase.ExportProgressUseCase
import com.mandarinlearn.domain.usecase.ImportProgressUseCase
import com.mandarinlearn.domain.usecase.ResetProgressUseCase
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val TAG = "SettingsViewModel"

/**
 * ViewModel for SettingsScreen. Owns all preference reads/writes and delegates
 * heavy operations (export, import, reset) to injected use cases.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exportUseCase: ExportProgressUseCase,
    private val importUseCase: ImportProgressUseCase,
    private val resetUseCase: ResetProgressUseCase,
    private val appVersion: String,
    private val geminiKeySet: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /** One-time snackbar events (export success, reset complete, etc.). */
    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        observePreferences()
    }

    // ---- Preference observation ----

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                settingsRepository.theme,
                settingsRepository.fontScale,
                settingsRepository.reduceMotion,
                settingsRepository.audioSpeed,
                settingsRepository.showPinyin,
            ) { theme, fontScale, reduceMotion, audioSpeed, showPinyin ->
                // Build a partial Content object; dailyNewCardsLimit merged below
                theme to Triple(fontScale, reduceMotion, audioSpeed to showPinyin)
            }.combine(settingsRepository.dailyNewCardsLimit) { partial, limit ->
                val (theme, rest) = partial
                val (fontScale, reduceMotion, speedAndPinyin) = rest
                val (audioSpeed, showPinyin) = speedAndPinyin

                val current = _uiState.value as? SettingsUiState.Content ?: SettingsUiState.Content()
                current.copy(
                    theme               = theme,
                    fontScaleIndex      = indexForFontScale(fontScale),
                    reduceMotion        = reduceMotion,
                    audioSpeedIndex     = indexForAudioSpeed(audioSpeed),
                    showPinyinDefault   = showPinyin,
                    dailyNewCardsLimit  = limit,
                    appVersion          = appVersion,
                    geminiKeySet        = geminiKeySet,
                )
            }.collect { content ->
                _uiState.value = content
            }
        }
    }

    // ---- Preference setters ----

    fun setTheme(theme: String) {
        viewModelScope.launch {
            try { settingsRepository.setTheme(theme) }
            catch (e: Exception) { Logger.e(TAG, "Failed to save theme", e) }
        }
    }

    fun setFontScaleIndex(index: Int) {
        viewModelScope.launch {
            try { settingsRepository.setFontScale(fontScaleForIndex(index)) }
            catch (e: Exception) { Logger.e(TAG, "Failed to save font scale", e) }
        }
    }

    fun setReduceMotion(reduce: Boolean) {
        viewModelScope.launch {
            try { settingsRepository.setReduceMotion(reduce) }
            catch (e: Exception) { Logger.e(TAG, "Failed to save reduce-motion", e) }
        }
    }

    fun setAudioSpeedIndex(index: Int) {
        viewModelScope.launch {
            try { settingsRepository.setAudioSpeed(audioSpeedForIndex(index)) }
            catch (e: Exception) { Logger.e(TAG, "Failed to save audio speed", e) }
        }
    }

    fun setShowPinyinDefault(show: Boolean) {
        viewModelScope.launch {
            try { settingsRepository.setShowPinyin(show) }
            catch (e: Exception) { Logger.e(TAG, "Failed to save pinyin default", e) }
        }
    }

    fun setDailyNewCardsLimit(limit: Int) {
        viewModelScope.launch {
            try { settingsRepository.setDailyNewCardsLimit(limit) }
            catch (e: Exception) { Logger.e(TAG, "Failed to save daily limit", e) }
        }
    }

    // ---- Export ----

    fun exportProgress(uri: Uri) {
        val current = _uiState.value as? SettingsUiState.Content ?: return
        if (current.isExporting) return
        _uiState.value = current.copy(isExporting = true, exportError = null)
        viewModelScope.launch {
            val result = exportUseCase.execute(uri)
            val updated = (_uiState.value as? SettingsUiState.Content) ?: return@launch
            if (result.isSuccess) {
                _uiState.value = updated.copy(isExporting = false)
                _events.emit(SettingsEvent.ShowSnackbar(SettingsEvent.SnackbarKey.EXPORT_SUCCESS))
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Export failed"
                _uiState.value = updated.copy(isExporting = false, exportError = msg)
                _events.emit(SettingsEvent.ShowSnackbar(SettingsEvent.SnackbarKey.EXPORT_FAILED))
            }
        }
    }

    // ---- Import ----

    fun importProgress(uri: Uri) {
        val current = _uiState.value as? SettingsUiState.Content ?: return
        if (current.isImporting) return
        _uiState.value = current.copy(isImporting = true, importError = null)
        viewModelScope.launch {
            val result = importUseCase.execute(uri)
            val updated = (_uiState.value as? SettingsUiState.Content) ?: return@launch
            if (result.isSuccess) {
                _uiState.value = updated.copy(isImporting = false)
                _events.emit(SettingsEvent.ShowSnackbar(SettingsEvent.SnackbarKey.IMPORT_SUCCESS))
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Import failed"
                _uiState.value = updated.copy(isImporting = false, importError = msg)
                _events.emit(SettingsEvent.ShowSnackbar(SettingsEvent.SnackbarKey.IMPORT_FAILED))
            }
        }
    }

    // ---- Reset ----

    fun resetAllProgress() {
        val current = _uiState.value as? SettingsUiState.Content ?: return
        if (current.isResetting) return
        _uiState.value = current.copy(isResetting = true)
        viewModelScope.launch {
            val result = resetUseCase.execute()
            val updated = (_uiState.value as? SettingsUiState.Content) ?: return@launch
            _uiState.value = updated.copy(isResetting = false)
            if (result.isSuccess) {
                _events.emit(SettingsEvent.ShowSnackbar(SettingsEvent.SnackbarKey.RESET_COMPLETE))
            } else {
                _events.emit(SettingsEvent.ShowSnackbar(SettingsEvent.SnackbarKey.RESET_FAILED))
            }
        }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            exportUseCase: ExportProgressUseCase,
            importUseCase: ImportProgressUseCase,
            resetUseCase: ResetProgressUseCase,
            appVersion: String,
            geminiKeySet: Boolean,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(
                    settingsRepository, exportUseCase, importUseCase,
                    resetUseCase, appVersion, geminiKeySet
                ) as T
        }
    }
}

/** One-time events emitted by [SettingsViewModel]. */
sealed class SettingsEvent {
    enum class SnackbarKey { EXPORT_SUCCESS, EXPORT_FAILED, IMPORT_SUCCESS, IMPORT_FAILED, RESET_COMPLETE, RESET_FAILED }
    data class ShowSnackbar(val key: SnackbarKey) : SettingsEvent()
}
