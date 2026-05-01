// FlashcardViewModel.kt — Mandarin Learn
// ViewModel for FlashcardScreen. Manages SM-2 session queue and card state.
// UX_SPECIFICATION.md §4 Screen 3. ARCHITECTURE.md §5 (SM-2) and §4.6 (AudioRepository).

package com.mandarinlearn.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.AudioRepository
import com.mandarinlearn.data.repository.AudioPlaybackState
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.domain.srs.SrsQuality
import com.mandarinlearn.domain.srs.SrsScheduler
import com.mandarinlearn.domain.usecase.ReviewVocabularyUseCase
import com.mandarinlearn.util.DateUtil
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "FlashcardViewModel"

/**
 * ViewModel for FlashcardScreen.
 * Queues due cards first, then up to [newCardsLimit] new cards (ARCHITECTURE.md §5.4).
 * Applies SM-2 via [ReviewVocabularyUseCase] after each rating.
 */
class FlashcardViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val audioRepository: AudioRepository,
    private val reviewUseCase: ReviewVocabularyUseCase,
    private val hsk: Int,
    private val newCardsLimit: Int = 10,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlashcardUiState>(FlashcardUiState.Loading)
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    /** One-time events (snackbar messages). Collected by the screen via LaunchedEffect. */
    private val _events = MutableSharedFlow<FlashcardEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<FlashcardEvent> = _events.asSharedFlow()

    // Session queue — mutable list consumed in order
    private val queue = mutableListOf<VocabularyWord>()
    private var reviewedCount = 0
    private var newCardsSeen = 0

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.value = FlashcardUiState.Loading
            try {
                val cards = vocabularyRepository.getDueAndNewCards(hsk, newCardsLimit)
                if (cards.isEmpty()) {
                    _uiState.value = FlashcardUiState.Empty
                    return@launch
                }
                queue.clear()
                queue.addAll(cards)
                showCurrentCard()
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load session for HSK $hsk", e)
                _uiState.value = FlashcardUiState.Error(e.message ?: "Could not load cards")
            }
        }
    }

    /** User tapped the card or "Show answer" button — reveals the back. */
    fun flipCard() {
        val current = currentContent() ?: return
        _uiState.value = current.copy(isFlipped = true)
    }

    /**
     * User tapped a rating button.
     * Persists the SM-2 update via [ReviewVocabularyUseCase], then advances to the next card.
     */
    fun rateCard(quality: SrsQuality) {
        val current = currentContent() ?: return
        viewModelScope.launch {
            try {
                val wasNew = !current.currentCard.isIntroduced
                reviewUseCase(current.currentCard, quality)
                reviewedCount++
                if (wasNew) newCardsSeen++
                advanceQueue()
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to save review", e)
                _events.tryEmit(FlashcardEvent.ShowSnackbar("Could not save your rating — please try again"))
            }
        }
    }

    /** User tapped the audio play button. Stub plays via AudioRepository (Phase 5 will wire TTS). */
    fun playAudio() {
        val current = currentContent() ?: return
        viewModelScope.launch {
            audioRepository.play(current.currentCard.character).collect { state ->
                when (state) {
                    is AudioPlaybackState.Loading ->
                        _uiState.value = current.copy(isAudioLoading = true)
                    is AudioPlaybackState.Playing ->
                        _uiState.value = current.copy(isAudioLoading = false, audioError = null)
                    is AudioPlaybackState.Finished ->
                        _uiState.value = current.copy(isAudioLoading = false)
                    is AudioPlaybackState.Failed -> {
                        // Phase 5: AudioRepository now shows real reason.
                        // Per spec note: on Failed, show snackbar "Audio not available — please
                        // install Chinese TTS voice in Android Settings → Languages → Text-to-speech."
                        _uiState.value = current.copy(isAudioLoading = false)
                        _events.tryEmit(
                            FlashcardEvent.ShowSnackbar(
                                if (state.reason.contains("TTS", ignoreCase = true) ||
                                    state.reason.contains("install", ignoreCase = true)
                                ) {
                                    state.reason
                                } else {
                                    "Audio not available — please install Chinese TTS voice in Android Settings"
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    private fun advanceQueue() {
        if (queue.isNotEmpty()) queue.removeAt(0)
        if (queue.isEmpty()) {
            finishSession()
        } else {
            showCurrentCard()
        }
    }

    private fun showCurrentCard() {
        val card = queue.firstOrNull() ?: return
        val today = DateUtil.today()
        _uiState.value = FlashcardUiState.Reviewing(
            currentCard    = card,
            isFlipped      = false,
            currentIndex   = reviewedCount,
            totalCards     = reviewedCount + queue.size,
            nextIntervals  = SrsScheduler.previewNextIntervals(card, today),
        )
    }

    private fun finishSession() {
        viewModelScope.launch {
            // Check if there are more new cards beyond the limit we loaded
            val moreNew = try {
                vocabularyRepository.getDueAndNewCards(hsk, 1).any { !it.isIntroduced }
            } catch (e: Exception) {
                false
            }
            _uiState.value = FlashcardUiState.SessionComplete(
                reviewedCount = reviewedCount,
                newCount      = newCardsSeen,
                hasMoreNew    = moreNew,
            )
        }
    }

    private fun currentContent(): FlashcardUiState.Reviewing? =
        _uiState.value as? FlashcardUiState.Reviewing

    companion object {
        fun factory(
            vocabularyRepository: VocabularyRepository,
            audioRepository: AudioRepository,
            reviewUseCase: ReviewVocabularyUseCase,
            hsk: Int,
            newCardsLimit: Int = 10,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FlashcardViewModel(
                    vocabularyRepository, audioRepository, reviewUseCase, hsk, newCardsLimit
                ) as T
        }
    }
}

/** One-time events emitted by [FlashcardViewModel] to the screen. */
sealed class FlashcardEvent {
    data class ShowSnackbar(val message: String) : FlashcardEvent()
}
