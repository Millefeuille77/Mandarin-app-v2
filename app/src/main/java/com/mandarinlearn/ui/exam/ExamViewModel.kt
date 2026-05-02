// ExamViewModel.kt — Mandarin Learn
// Full Phase 7 ViewModel for ExamScreen.
// UX_SPECIFICATION.md §4 Screen 8; IMPLEMENTATION_PLAN.md Phase 7 §B.

package com.mandarinlearn.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.domain.model.AnswerRecord
import com.mandarinlearn.domain.usecase.ExamSectionWithQuestions
import com.mandarinlearn.domain.usecase.StartExamUseCase
import com.mandarinlearn.domain.usecase.SubmitExamUseCase
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ExamViewModel"
private const val BREAK_DURATION_SECONDS = 30
private const val TIMER_TICK_MS = 1_000L

/** ViewModel for ExamScreen. NEVER touches DAOs — only use cases + repositories. */
class ExamViewModel(
    private val startExamUseCase: StartExamUseCase,
    private val submitExamUseCase: SubmitExamUseCase,
    private val audioControllerFactory: (kotlinx.coroutines.CoroutineScope) -> ExamAudioController,
    private val initialHsk: Int,
) : ViewModel() {

    private val audioController: ExamAudioController = audioControllerFactory(viewModelScope)
    fun replayCurrentAudio(): Int = audioController.replay()
    fun remainingReplays(): Int = audioController.remainingReplays()

    private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ExamEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ExamEvent> = _events.asSharedFlow()

    // In-memory answer tracking — questionId → user's selected option key
    private val answeredQuestions = mutableMapOf<String, String>()
    private var startedAtMillis: Long = 0L
    private var timerJob: Job? = null
    private var breakTimerJob: Job? = null

    init {
        loadExam(initialHsk)
    }

    // ---- Public actions ----

    fun selectAnswer(answer: String) {
        val state = _uiState.value as? ExamUiState.ActiveSection ?: return
        _uiState.value = state.copy(selectedAnswer = answer)
    }

    fun advance() {
        val state = _uiState.value as? ExamUiState.ActiveSection ?: return
        val question = state.currentQuestion ?: return
        val chosen = state.selectedAnswer ?: return

        // Record answer
        answeredQuestions[question.id] = chosen
        val newAnswered = answeredQuestions.toMap()

        if (!state.isLastQuestionInSection) {
            // Move to next question in same section
            val nextState = state.copy(
                currentQuestionIndex    = state.currentQuestionIndex + 1,
                selectedAnswer          = null,
                answeredQuestions       = newAnswered,
                totalQuestionsAnswered  = newAnswered.size,
            )
            _uiState.value = nextState
            nextState.currentQuestion?.let { audioController.playFor(it) }
        } else if (!state.isLastSection) {
            // Section complete — show break overlay
            timerJob?.cancel()
            _uiState.value = ExamUiState.SectionBreak(
                structure              = state.structure,
                allSections            = state.allSections,
                completedSectionIndex  = state.currentSectionIndex,
                breakSecondsRemaining  = BREAK_DURATION_SECONDS,
                answeredQuestions      = newAnswered,
                timerSecondsRemaining  = state.timerSecondsRemaining,
                startedAtMillis        = state.startedAtMillis,
            )
            startBreakTimer()
        } else {
            // Last question of last section — submit
            answeredQuestions[question.id] = chosen
            submitExam(state.structure, state.allSections, state.timerSecondsRemaining, state.startedAtMillis)
        }
    }

    fun skipBreak() { breakTimerJob?.cancel(); proceedToNextSection() }
    fun onBackPressed() {
        val state = _uiState.value as? ExamUiState.ActiveSection ?: return
        _uiState.value = state.copy(showQuitDialog = true)
    }
    fun dismissQuitDialog() {
        val state = _uiState.value as? ExamUiState.ActiveSection ?: return
        _uiState.value = state.copy(showQuitDialog = false)
    }
    fun confirmQuit() {
        timerJob?.cancel()
        breakTimerJob?.cancel()
        _events.tryEmit(ExamEvent.QuitExam)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        breakTimerJob?.cancel()
        audioController.stop()
    }

    // ---- Private helpers ----

    private fun loadExam(hsk: Int) {
        viewModelScope.launch {
            _uiState.value = ExamUiState.Loading
            startedAtMillis = System.currentTimeMillis()
            answeredQuestions.clear()
            startExamUseCase(hsk).fold(
                onSuccess = { result ->
                    val totalQ = result.orderedSections.sumOf { it.questions.size }
                    val totalSeconds = result.structure.totalDurationMinutes * 60
                    _uiState.value = ExamUiState.ActiveSection(
                        structure              = result.structure,
                        allSections            = result.orderedSections,
                        currentSectionIndex    = 0,
                        currentQuestionIndex   = 0,
                        selectedAnswer         = null,
                        answeredQuestions      = emptyMap(),
                        timerSecondsRemaining  = totalSeconds,
                        showQuitDialog         = false,
                        totalQuestionsAnswered = 0,
                        totalQuestions         = totalQ,
                        startedAtMillis        = startedAtMillis,
                    )
                    startTimer()
                    // Auto-play audio for the first question if it's a listening question
                    result.orderedSections.firstOrNull()?.questions?.firstOrNull()?.let {
                        audioController.playFor(it)
                    }
                },
                onFailure = { error ->
                    Logger.e(TAG, "Failed to load exam for HSK $hsk", error)
                    _uiState.value = ExamUiState.Error(
                        error.message ?: "Could not load exam. Please try again."
                    )
                }
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                val state = _uiState.value as? ExamUiState.ActiveSection ?: break
                val newSeconds = state.timerSecondsRemaining - 1
                if (newSeconds <= 0) {
                    // Timer expired — auto-submit
                    submitExam(
                        state.structure,
                        state.allSections,
                        0,
                        state.startedAtMillis,
                    )
                    break
                }
                // Announce at exact minute boundaries (accessibility: UX §1.8)
                if (newSeconds % 60 == 0) {
                    _events.tryEmit(ExamEvent.AnnounceTimer(newSeconds / 60))
                }
                _uiState.value = state.copy(timerSecondsRemaining = newSeconds)
            }
        }
    }

    private fun startBreakTimer() {
        breakTimerJob = viewModelScope.launch {
            repeat(BREAK_DURATION_SECONDS) {
                delay(TIMER_TICK_MS)
                val state = _uiState.value as? ExamUiState.SectionBreak ?: return@launch
                val newRemaining = state.breakSecondsRemaining - 1
                if (newRemaining <= 0) {
                    proceedToNextSection()
                    return@launch
                }
                _uiState.value = state.copy(breakSecondsRemaining = newRemaining)
            }
        }
    }

    private fun proceedToNextSection() {
        val state = _uiState.value as? ExamUiState.SectionBreak ?: return
        val nextSectionIndex = state.completedSectionIndex + 1
        _uiState.value = ExamUiState.ActiveSection(
            structure              = state.structure,
            allSections            = state.allSections,
            currentSectionIndex    = nextSectionIndex,
            currentQuestionIndex   = 0,
            selectedAnswer         = null,
            answeredQuestions      = state.answeredQuestions,
            timerSecondsRemaining  = state.timerSecondsRemaining,
            showQuitDialog         = false,
            totalQuestionsAnswered = state.answeredQuestions.size,
            totalQuestions         = state.allSections.sumOf { it.questions.size },
            startedAtMillis        = state.startedAtMillis,
        )
        startTimer()
    }

    private fun submitExam(
        structure: com.mandarinlearn.domain.model.ExamStructure,
        allSections: List<ExamSectionWithQuestions>,
        timerRemaining: Int,
        startedAt: Long,
    ) {
        timerJob?.cancel()
        _uiState.value = ExamUiState.Submitting
        viewModelScope.launch {
            val finishedAt = System.currentTimeMillis()

            // Build answer records and section tallies
            val answerRecords = mutableListOf<AnswerRecord>()
            val sectionTallies = mutableMapOf<String, Pair<Int, Int>>()
            for (sectionWithQ in allSections) {
                var correct = 0
                var presented = 0
                for (question in sectionWithQ.questions) {
                    val userAnswer = answeredQuestions[question.id] ?: ""
                    val isCorrect = userAnswer.equals(question.correctAnswer, ignoreCase = true)
                    answerRecords.add(AnswerRecord(question.id, userAnswer, question.correctAnswer, isCorrect))
                    presented++
                    if (isCorrect) correct++
                }
                sectionTallies[sectionWithQ.section.name] = Pair(correct, presented)
            }

            submitExamUseCase(structure, answerRecords, sectionTallies, startedAt, finishedAt)
                .fold(
                    onSuccess = { result ->
                        _uiState.value = ExamUiState.Done(result.id)
                    },
                    onFailure = { error ->
                        Logger.e(TAG, "Failed to submit exam", error)
                        _uiState.value = ExamUiState.Error(
                            error.message ?: "Could not save your exam result."
                        )
                    }
                )
        }
    }

    // ---- Factory ----

    companion object {
        fun factory(
            startExamUseCase: StartExamUseCase,
            submitExamUseCase: SubmitExamUseCase,
            audioRepository: com.mandarinlearn.data.repository.AudioRepository,
            hsk: Int,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ExamViewModel(
                startExamUseCase  = startExamUseCase,
                submitExamUseCase = submitExamUseCase,
                audioControllerFactory = { scope -> ExamAudioController(audioRepository, scope) },
                initialHsk        = hsk,
            ) as T
        }
    }
}

sealed class ExamEvent {
    data object QuitExam : ExamEvent()
    data class AnnounceTimer(val minutesRemaining: Int) : ExamEvent()
}
