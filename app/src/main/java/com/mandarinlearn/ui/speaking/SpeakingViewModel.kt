// SpeakingViewModel.kt — Mandarin Learn
// ViewModel for SpeakingScreen. Per UX_SPECIFICATION.md §4 Screen 6.
// Phase 6: full implementation — recording state machine, Gemini STT, permission handling.

package com.mandarinlearn.ui.speaking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mandarinlearn.data.audio.AudioRecorder
import com.mandarinlearn.data.remote.GeminiError
import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.domain.usecase.ScorePronunciationUseCase
import com.mandarinlearn.data.repository.SpeakingRepository
import com.mandarinlearn.util.Logger
import com.mandarinlearn.util.NetworkMonitor
import com.mandarinlearn.util.PermissionsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "SpeakingViewModel"
private const val MAX_RECORD_SECONDS = 10
private const val TIMER_TICK_MS = 1_000L

/**
 * ViewModel for [SpeakingScreen].
 *
 * State machine: Loading → Idle → Recording → Processing → Result
 *                         ↑___________Try-again____________|
 *
 * NEVER touches DAOs or GeminiService directly — uses repositories and use cases only.
 */
class SpeakingViewModel(
    private val speakingRepository: SpeakingRepository,
    private val scorePronunciationUseCase: ScorePronunciationUseCase,
    private val audioRecorder: AudioRecorder,
    private val networkMonitor: NetworkMonitor,
    private val context: Context,
    initialHsk: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpeakingUiState>(SpeakingUiState.Loading)
    val uiState: StateFlow<SpeakingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SpeakingEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SpeakingEvent> = _events.asSharedFlow()

    private var recordingJob: Job? = null
    private var lastAudioFile: File? = null
    private var currentPhrase: ConversationPhrase? = null

    init {
        loadPhrase(initialHsk)
    }

    // ---- Public actions (called from SpeakingScreen) ----

    /** Called when the user taps a different HSK level chip. */
    fun selectLevel(hsk: Int) {
        stopAnyRecording()
        loadPhrase(hsk)
    }

    /**
     * Called when the user taps the mic button.
     * - If idle → check permission, start recording.
     * - If recording → stop early.
     */
    fun onMicTapped() {
        when (val state = _uiState.value) {
            is SpeakingUiState.Idle -> startRecording(state.phrase, state.selectedHsk)
            is SpeakingUiState.Recording -> stopAnyRecording()
            else -> Unit // Tap ignored while Processing/Result/Error
        }
    }

    /**
     * Called by the screen after the system permission dialog resolves.
     * [granted] is true if RECORD_AUDIO was just granted.
     * [shouldShowRationale] reflects whether the system will show a rationale again.
     */
    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        if (granted) {
            // Re-attempt: load phrase and transition to Idle
            val hsk = currentHsk()
            loadPhrase(hsk)
        } else {
            _uiState.value = SpeakingUiState.PermissionDenied(
                isPermanentlyDenied = !shouldShowRationale
            )
        }
    }

    /** Called when the user taps "Try again" — resets to Idle with the same phrase. */
    fun tryAgain() {
        val phrase = currentPhrase ?: run { loadPhrase(currentHsk()); return }
        _uiState.value = SpeakingUiState.Idle(phrase, currentHsk())
    }

    /** Called when the user taps "Next phrase" — loads a new random phrase. */
    fun nextPhrase() {
        loadPhrase(currentHsk())
    }

    override fun onCleared() {
        super.onCleared()
        stopAnyRecording()
    }

    // ---- Private helpers ----

    private fun loadPhrase(hsk: Int) {
        _uiState.value = SpeakingUiState.Loading
        viewModelScope.launch {
            val phrase = speakingRepository.getRandomPhrase(hsk)
            if (phrase == null) {
                _uiState.value = SpeakingUiState.Error("No speaking phrases found for HSK $hsk.")
            } else {
                currentPhrase = phrase
                _uiState.value = SpeakingUiState.Idle(phrase, hsk)
            }
        }
    }

    private fun startRecording(phrase: ConversationPhrase, hsk: Int) {
        if (!PermissionsHelper.hasRecordAudioPermission(context)) {
            _uiState.value = SpeakingUiState.PermissionDenied(isPermanentlyDenied = false)
            return
        }
        if (!networkMonitor.isOnline()) {
            _uiState.value = SpeakingUiState.Error(
                "You’re offline. Speaking practice needs internet."
            )
            return
        }

        recordingJob = viewModelScope.launch {
            _uiState.value = SpeakingUiState.Recording(phrase, hsk, elapsedSeconds = 0)

            // Run timer ticks in parallel with the actual recording
            val timerJob = launch {
                repeat(MAX_RECORD_SECONDS) { second ->
                    delay(TIMER_TICK_MS)
                    // Only update if still recording
                    if (_uiState.value is SpeakingUiState.Recording) {
                        _uiState.value = SpeakingUiState.Recording(phrase, hsk, second + 1)
                    }
                }
                // Auto-stop at limit
                if (_uiState.value is SpeakingUiState.Recording) {
                    audioRecorder.stopRecording()
                    _events.tryEmit(SpeakingEvent.RecordingLimitReached)
                }
            }

            val recordResult = audioRecorder.record(MAX_RECORD_SECONDS)
            timerJob.cancel()

            recordResult.fold(
                onSuccess = { file ->
                    lastAudioFile = file
                    scoreAudio(file, phrase, hsk)
                },
                onFailure = { error ->
                    Logger.e(TAG, "Recording failed", error)
                    _uiState.value = SpeakingUiState.Error("Could not start recording. Please try again.")
                }
            )
        }
    }

    private suspend fun scoreAudio(audioFile: File, phrase: ConversationPhrase, hsk: Int) {
        _uiState.value = SpeakingUiState.Processing(phrase, hsk)
        try {
            val result = scorePronunciationUseCase(audioFile, phrase.chinese)
            result.fold(
                onSuccess = { pronunciation ->
                    _uiState.value = SpeakingUiState.Result(phrase, hsk, pronunciation)
                },
                onFailure = { error ->
                    val message = when (error) {
                        is GeminiError.Offline -> "You’re offline. Speaking practice needs internet."
                        is GeminiError.NoApiKey -> "AI features unavailable — set your API key in Settings."
                        is GeminiError.Timeout -> "Scoring timed out. Please try again."
                        else -> "Could not score pronunciation. Please try again."
                    }
                    _uiState.value = SpeakingUiState.Error(message)
                }
            )
        } finally {
            // Always delete the audio file after evaluation (acceptance criterion)
            audioFile.delete()
            lastAudioFile = null
        }
    }

    private fun stopAnyRecording() {
        audioRecorder.stopRecording()
        recordingJob?.cancel()
        recordingJob = null
    }

    private fun currentHsk(): Int = when (val s = _uiState.value) {
        is SpeakingUiState.Idle -> s.selectedHsk
        is SpeakingUiState.Recording -> s.selectedHsk
        is SpeakingUiState.Processing -> s.selectedHsk
        is SpeakingUiState.Result -> s.selectedHsk
        else -> 1
    }

    // ---- Factory ----

    companion object {
        fun factory(
            speakingRepository: SpeakingRepository,
            scorePronunciationUseCase: ScorePronunciationUseCase,
            audioRecorder: AudioRecorder,
            networkMonitor: NetworkMonitor,
            context: Context,
            hsk: Int,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SpeakingViewModel(
                    speakingRepository        = speakingRepository,
                    scorePronunciationUseCase = scorePronunciationUseCase,
                    audioRecorder             = audioRecorder,
                    networkMonitor            = networkMonitor,
                    context                   = context,
                    initialHsk                = hsk,
                ) as T
        }
    }
}
