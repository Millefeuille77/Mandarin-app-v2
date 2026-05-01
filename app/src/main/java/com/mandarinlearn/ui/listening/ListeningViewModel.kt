// ListeningViewModel.kt — Mandarin Learn
// Full Phase 5 ViewModel for ListeningScreen.
// UX_SPECIFICATION.md §4 Screen 7. ARCHITECTURE.md §4.6 (AudioRepository contract).

package com.mandarinlearn.ui.listening

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.AudioPlaybackState
import com.mandarinlearn.data.repository.AudioRepository
import com.mandarinlearn.data.repository.ListeningRepository
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ListeningViewModel"

/** Maximum number of replays per question per UX spec §4 Screen 7. */
private const val MAX_REPLAYS = 3

/** Number of questions per listening session. */
private const val SESSION_SIZE = 10

/**
 * ViewModel for [ListeningScreen].
 * Manages: session loading, HSK level selection, audio playback (via AudioRepository),
 * answer selection with immediate feedback, replay cap, and session completion.
 */
class ListeningViewModel(
    private val listeningRepository: ListeningRepository,
    private val audioRepository: AudioRepository,
    private val initialHsk: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListeningUiState>(ListeningUiState.Loading)
    val uiState: StateFlow<ListeningUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ListeningEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ListeningEvent> = _events.asSharedFlow()

    // Session state
    private var allQuestions: List<SampleQuestion> = emptyList()
    private var currentQuestionIndex: Int = 0
    private var correctCount: Int = 0
    private var currentHsk: Int = initialHsk

    init {
        loadSession(initialHsk)
    }

    /** Called when the user selects a different HSK level chip. */
    fun selectLevel(hsk: Int) {
        currentHsk = hsk
        loadSession(hsk)
    }

    private fun loadSession(hsk: Int) {
        viewModelScope.launch {
            _uiState.value = ListeningUiState.Loading
            currentQuestionIndex = 0
            correctCount = 0
            try {
                allQuestions = listeningRepository.getQuestionsForSession(hsk, SESSION_SIZE)
                if (allQuestions.isEmpty()) {
                    _uiState.value = ListeningUiState.Empty(hsk)
                } else {
                    showQuestion(0, hsk)
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load listening session for HSK $hsk", e)
                _uiState.value = ListeningUiState.Error(
                    e.message ?: "Could not load questions"
                )
            }
        }
    }

    /**
     * User tapped the Play button. Starts audio for the current question.
     * Increments replay count (capped at [MAX_REPLAYS]).
     */
    fun playAudio() {
        val state = _uiState.value as? ListeningUiState.Content ?: return
        if (state.replayCount >= MAX_REPLAYS && state.replayCount > 0) return

        val audioText = state.currentQuestion.audioTextChinese
            ?: state.currentQuestion.questionText

        viewModelScope.launch {
            val newReplayCount = if (state.isAudioPlaying) state.replayCount
            else state.replayCount + 1

            audioRepository.play(audioText).collect { playbackState ->
                when (playbackState) {
                    is AudioPlaybackState.Loading ->
                        _uiState.value = state.copy(
                            isAudioPlaying = true,
                            audioFailed = false,
                            replayCount = newReplayCount,
                        )

                    is AudioPlaybackState.Playing ->
                        _uiState.value = (_uiState.value as? ListeningUiState.Content)
                            ?.copy(isAudioPlaying = true, audioFailed = false)
                            ?: _uiState.value

                    is AudioPlaybackState.Finished ->
                        _uiState.value = (_uiState.value as? ListeningUiState.Content)
                            ?.copy(isAudioPlaying = false)
                            ?: _uiState.value

                    is AudioPlaybackState.Failed -> {
                        Logger.w(TAG, "Audio playback failed: ${playbackState.reason}")
                        _uiState.value = (_uiState.value as? ListeningUiState.Content)
                            ?.copy(isAudioPlaying = false, audioFailed = true)
                            ?: _uiState.value
                    }
                }
            }
        }
    }

    /**
     * User tapped an answer option.
     * Shows immediate feedback (correct / incorrect).
     * Triggers TalkBack announcement via [ListeningEvent].
     */
    fun selectAnswer(optionIndex: Int) {
        val state = _uiState.value as? ListeningUiState.Content ?: return
        if (state.hasAnswered) return // Already answered — ignore double-taps

        val isCorrect = optionIndex == state.correctOptionIndex
        if (isCorrect) correctCount++

        _uiState.value = state.copy(
            selectedOptionIndex = optionIndex,
            hasAnswered         = true,
            isCorrect           = isCorrect,
        )

        // TalkBack announcement
        val announcement = if (isCorrect) "Correct answer" else {
            val correctText = state.options.getOrNull(state.correctOptionIndex) ?: ""
            "Incorrect. The answer was $correctText"
        }
        _events.tryEmit(ListeningEvent.AnnounceAnswer(announcement))
    }

    /** User tapped "Skip" after audio failed — advance without counting this question. */
    fun skipQuestion() {
        advanceToNextQuestion()
    }

    /** User tapped "Next" after seeing feedback. */
    fun nextQuestion() {
        advanceToNextQuestion()
    }

    /** Restart session at the same HSK level. */
    fun retrySession() {
        loadSession(currentHsk)
    }

    private fun advanceToNextQuestion() {
        currentQuestionIndex++
        val hsk = currentHsk
        if (currentQuestionIndex >= allQuestions.size) {
            _uiState.value = ListeningUiState.SessionComplete(
                correctCount   = correctCount,
                totalQuestions = allQuestions.size,
                hskLevel       = hsk,
            )
        } else {
            showQuestion(currentQuestionIndex, hsk)
        }
    }

    private fun showQuestion(index: Int, hsk: Int) {
        val question = allQuestions.getOrNull(index) ?: return
        val (options, correctIndex) = buildOptions(question)
        _uiState.value = ListeningUiState.Content(
            currentQuestion  = question,
            options          = options,
            correctOptionIndex = correctIndex,
            questionIndex    = index + 1,
            totalQuestions   = allQuestions.size,
            hskLevel         = hsk,
        )
        // Auto-play audio when question appears
        playAudio()
    }

    /**
     * Builds the 4 display options and identifies which index is correct.
     * The question already has options in [SampleQuestion.options] — we use them directly
     * and find which one matches [SampleQuestion.correctAnswer] (e.g. "A", "B", "C", "D").
     */
    private fun buildOptions(question: SampleQuestion): Pair<List<String>, Int> {
        val options = question.options
        if (options.isEmpty()) return Pair(listOf("—", "—", "—", "—"), 0)

        // Correct answer key is "A", "B", "C", or "D" — map to 0-based index
        val correctIndex = when (question.correctAnswer.uppercase().trim()) {
            "A" -> 0
            "B" -> 1
            "C" -> 2
            "D" -> 3
            else -> 0
        }.coerceIn(0, options.size - 1)

        // Pad to 4 if fewer options provided
        val paddedOptions = options + List(maxOf(0, 4 - options.size)) { "—" }
        return Pair(paddedOptions.take(4), correctIndex)
    }

    companion object {
        fun factory(
            listeningRepository: ListeningRepository,
            audioRepository: AudioRepository,
            hsk: Int,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ListeningViewModel(listeningRepository, audioRepository, hsk) as T
        }
    }
}

/** One-time events emitted by [ListeningViewModel] to the screen. */
sealed class ListeningEvent {
    data class AnnounceAnswer(val message: String) : ListeningEvent()
}
