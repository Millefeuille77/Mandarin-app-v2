// PassageViewModel.kt — Mandarin Learn
// ViewModel for PassageScreen. Full Phase 4 implementation replacing skeleton.
// UX_SPECIFICATION.md §4 Screen 5. ARCHITECTURE.md §6 (MVVM + Repository).
// Phase 9: pinyin default reads from UserPreferencesRepository; audio speed honours preference.

package com.mandarinlearn.ui.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.preferences.UserPreferencesRepository
import com.mandarinlearn.data.repository.AudioPlaybackState
import com.mandarinlearn.data.repository.AudioRepository
import com.mandarinlearn.data.repository.ReadingRepository
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "PassageViewModel"

// Font scale constraints (UX spec §4 Screen 5)
private const val FONT_SCALE_MIN = 0.8f
private const val FONT_SCALE_MAX = 1.6f

/**
 * ViewModel for [PassageScreen].
 * Loads the passage by ID, exposes pinyin toggle + font scale, handles tap-to-define,
 * and wires the "Play all" button to AudioRepository (Phase 5).
 *
 * @param passageId            The unique ID of the passage to load.
 * @param readingRepository    Source for reading passage data.
 * @param vocabularyRepository Source for character definition lookup.
 * @param audioRepository      For "Play all" TTS playback (Phase 5).
 */
class PassageViewModel(
    private val passageId: String,
    private val readingRepository: ReadingRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val audioRepository: AudioRepository? = null,
    /** Phase 9: reads pinyin default and audio speed from DataStore. */
    private val preferencesRepository: UserPreferencesRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PassageUiState>(PassageUiState.Loading)
    val uiState: StateFlow<PassageUiState> = _uiState.asStateFlow()

    /** One-time snackbar/announcement events. */
    private val _events = MutableSharedFlow<PassageEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PassageEvent> = _events.asSharedFlow()

    init {
        loadPassage()
    }

    private fun loadPassage() {
        viewModelScope.launch {
            readingRepository.getPassageById(passageId)
                .catch { e ->
                    Logger.e(TAG, "Failed to load passage $passageId", e)
                    _uiState.value = PassageUiState.Error(
                        e.message ?: "Could not load passage"
                    )
                }
                .collectLatest { passage ->
                    if (passage == null) {
                        _uiState.value = PassageUiState.Error("Passage not found")
                        return@collectLatest
                    }
                    // Default pinyin ON for HSK 1–3, OFF for HSK 4–5 per UX spec §4 Screen 5
                    val defaultShowPinyin = passage.hskLevel <= 3
                    val current = _uiState.value
                    // Phase 9: if a preference exists, use it as the default; otherwise
                    // fall back to the HSK-level rule (≤3 → ON, 4–5 → OFF).
                    val effectiveDefault = preferencesRepository?.let {
                        try { it.showPinyin.first() } catch (e: Exception) { defaultShowPinyin }
                    } ?: defaultShowPinyin

                    // Preserve UI state (showPinyin, fontScale) across Room Flow emissions
                    _uiState.value = when (current) {
                        is PassageUiState.Content -> current.copy(passage = passage)
                        else -> PassageUiState.Content(
                            passage         = passage,
                            showPinyin      = effectiveDefault,
                            fontScale       = 1.0f,
                            selectedWord    = null,
                            noDefinition    = false,
                            tappedCharacter = "",
                            isMarkingRead   = false,
                            showTranslation = false,
                        )
                    }
                }
        }
    }

    /** Toggle pinyin visibility (UX spec §4 Screen 5: controls bar switch). */
    fun togglePinyin() {
        val current = _uiState.value as? PassageUiState.Content ?: return
        _uiState.value = current.copy(showPinyin = !current.showPinyin)
    }

    /** Update font scale from slider (clamped 0.8–1.6). */
    fun setFontScale(scale: Float) {
        val current = _uiState.value as? PassageUiState.Content ?: return
        _uiState.value = current.copy(
            fontScale = scale.coerceIn(FONT_SCALE_MIN, FONT_SCALE_MAX)
        )
    }

    /** Toggle English translation panel expansion. */
    fun toggleTranslation() {
        val current = _uiState.value as? PassageUiState.Content ?: return
        _uiState.value = current.copy(showTranslation = !current.showTranslation)
    }

    /**
     * Called when the user taps a character.
     * Looks up the character in the vocabulary table. Populates [selectedWord] if found,
     * sets [noDefinition] = true if not found. UX spec §4 Screen 5 popup rules.
     */
    fun onCharacterTapped(character: String) {
        val current = _uiState.value as? PassageUiState.Content ?: return
        viewModelScope.launch {
            try {
                val word = vocabularyRepository.findByCharacter(character)
                _uiState.value = current.copy(
                    selectedWord    = word,
                    noDefinition    = word == null,
                    tappedCharacter = character,
                )
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to look up character $character", e)
                _uiState.value = current.copy(
                    selectedWord    = null,
                    noDefinition    = true,
                    tappedCharacter = character,
                )
            }
        }
    }

    /**
     * Plays the full passage text via AudioRepository (Phase 5 "Play all" button).
     * Per QA m-3: previously silent no-op. Now wired to AudioRepository.
     * On failure shows snackbar "Audio coming soon" (reuse existing string).
     */
    fun playAll() {
        val current = _uiState.value as? PassageUiState.Content ?: return
        val repo = audioRepository ?: run {
            _events.tryEmit(PassageEvent.ShowSnackbar("audio_coming_soon"))
            return
        }
        viewModelScope.launch {
            // Phase 9: honour audio speed preference for "Play all".
            val speed = preferencesRepository?.let {
                try { it.audioSpeed.first() } catch (e: Exception) { 1.0f }
            } ?: 1.0f
            repo.play(current.passage.chineseText, speed).collect { state ->
                when (state) {
                    is AudioPlaybackState.Loading ->
                        _uiState.value = current.copy(isPlayingAll = true, playAllFailed = false)
                    is AudioPlaybackState.Playing ->
                        _uiState.value = (_uiState.value as? PassageUiState.Content)
                            ?.copy(isPlayingAll = true, playAllFailed = false)
                            ?: _uiState.value
                    is AudioPlaybackState.Finished ->
                        _uiState.value = (_uiState.value as? PassageUiState.Content)
                            ?.copy(isPlayingAll = false, playAllFailed = false)
                            ?: _uiState.value
                    is AudioPlaybackState.Failed -> {
                        Logger.w(TAG, "Play all failed: ${state.reason}")
                        _uiState.value = (_uiState.value as? PassageUiState.Content)
                            ?.copy(isPlayingAll = false, playAllFailed = true)
                            ?: _uiState.value
                        _events.tryEmit(PassageEvent.ShowSnackbar("audio_coming_soon"))
                    }
                }
            }
        }
    }

    /** Dismiss the character definition sheet. */
    fun dismissDefinition() {
        val current = _uiState.value as? PassageUiState.Content ?: return
        _uiState.value = current.copy(
            selectedWord    = null,
            noDefinition    = false,
            tappedCharacter = "",
        )
    }

    /**
     * Mark the passage as read. Calls [onMarked] on success so the screen can show a snackbar
     * and back-navigate per UX spec §4 Screen 5 footer behaviour.
     */
    fun markAsRead(onMarked: () -> Unit) {
        val current = _uiState.value as? PassageUiState.Content ?: return
        if (current.isMarkingRead) return // Guard against double-tap
        _uiState.value = current.copy(isMarkingRead = true)
        viewModelScope.launch {
            try {
                readingRepository.markCompleted(passageId)
                onMarked()
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to mark passage $passageId as read", e)
                _uiState.value = current.copy(isMarkingRead = false)
            }
        }
    }

    companion object {
        /** Factory for AppContainer-based constructor injection. */
        fun factory(
            passageId: String,
            readingRepository: ReadingRepository,
            vocabularyRepository: VocabularyRepository,
            audioRepository: AudioRepository? = null,
            preferencesRepository: UserPreferencesRepository? = null,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PassageViewModel(
                    passageId, readingRepository, vocabularyRepository,
                    audioRepository, preferencesRepository
                ) as T
        }
    }
}

/** One-time events emitted by [PassageViewModel] to the screen. */
sealed class PassageEvent {
    data class ShowSnackbar(val stringKey: String) : PassageEvent()
}
