// SpeakingViewModelTest.kt — Mandarin Learn
// Unit tests for SpeakingViewModel state transitions.
// Per IMPLEMENTATION_PLAN.md Phase 6 acceptance criteria.
// Tests: state transitions, denied permission path, successful score path.

package com.mandarinlearn.viewmodel

import android.content.Context
import app.cash.turbine.test
import com.mandarinlearn.data.audio.AudioRecorder
import com.mandarinlearn.data.audio.RecordError
import com.mandarinlearn.data.remote.GeminiError
import com.mandarinlearn.data.repository.SpeakingRepository
import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.domain.model.PronunciationResult
import com.mandarinlearn.domain.usecase.ScorePronunciationUseCase
import com.mandarinlearn.ui.speaking.SpeakingEvent
import com.mandarinlearn.ui.speaking.SpeakingUiState
import com.mandarinlearn.ui.speaking.SpeakingViewModel
import com.mandarinlearn.util.NetworkMonitor
import com.mandarinlearn.util.PermissionsHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SpeakingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val samplePhrase = ConversationPhrase(
        id           = "cp_hsk1_001",
        hskLevel     = 1,
        category     = "Greetings",
        chinese      = "你好",
        pinyin       = "nǐ hǎo",
        english      = "Hello",
        usageContext = "Standard greeting",
    )

    private val sampleResult = PronunciationResult(
        transcribedText = "你好",
        score           = 87,
        feedback        = "Great tone on nǐ! Clear pronunciation.",
        phonemeIssues   = emptyList(),
    )

    private lateinit var speakingRepository: SpeakingRepository
    private lateinit var scorePronunciationUseCase: ScorePronunciationUseCase
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        speakingRepository     = mockk()
        audioRecorder          = mockk()
        networkMonitor         = mockk()
        context                = mockk(relaxed = true)

        // Default: network online and permission granted
        every { networkMonitor.isOnline() } returns true
        mockkObject(PermissionsHelper)
        every { PermissionsHelper.hasRecordAudioPermission(any()) } returns true

        scorePronunciationUseCase = ScorePronunciationUseCase(speakingRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(PermissionsHelper)
    }

    private fun createViewModel(hsk: Int = 1): SpeakingViewModel {
        coEvery { speakingRepository.getRandomPhrase(any()) } returns samplePhrase
        return SpeakingViewModel(
            speakingRepository        = speakingRepository,
            scorePronunciationUseCase = scorePronunciationUseCase,
            audioRecorder             = audioRecorder,
            networkMonitor            = networkMonitor,
            context                   = context,
            initialHsk                = hsk,
        )
    }

    // ---- State transition: Loading → Idle ----

    @Test
    fun `initial state is Loading then transitions to Idle after phrase loaded`() = runTest {
        val vm = createViewModel()
        vm.uiState.test {
            // First emission is Loading (set synchronously in init)
            val first = awaitItem()
            assertTrue("Expected Loading, got $first", first is SpeakingUiState.Loading)

            // After coroutine runs, transitions to Idle
            testDispatcher.scheduler.advanceUntilIdle()
            val second = awaitItem()
            assertTrue("Expected Idle, got $second", second is SpeakingUiState.Idle)
            assertEquals(samplePhrase, (second as SpeakingUiState.Idle).phrase)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- State transition: No phrase → Error ----

    @Test
    fun `when no phrase available shows error state`() = runTest {
        coEvery { speakingRepository.getRandomPhrase(any()) } returns null
        val vm = SpeakingViewModel(
            speakingRepository        = speakingRepository,
            scorePronunciationUseCase = scorePronunciationUseCase,
            audioRecorder             = audioRecorder,
            networkMonitor            = networkMonitor,
            context                   = context,
            initialHsk                = 1,
        )
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue("Expected Error, got $state", state is SpeakingUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- Permission denied path ----

    @Test
    fun `when permission denied onMicTapped emits PermissionDenied state`() = runTest {
        every { PermissionsHelper.hasRecordAudioPermission(any()) } returns false
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle() // reach Idle

        vm.uiState.test {
            awaitItem() // consume Idle
            vm.onMicTapped()
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue("Expected PermissionDenied, got $state", state is SpeakingUiState.PermissionDenied)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPermissionResult granted true reloads phrase`() = runTest {
        every { PermissionsHelper.hasRecordAudioPermission(any()) } returns false
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Now grant permission
        every { PermissionsHelper.hasRecordAudioPermission(any()) } returns true
        vm.onPermissionResult(granted = true, shouldShowRationale = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Expected Idle after permission granted, got $state", state is SpeakingUiState.Idle)
    }

    @Test
    fun `onPermissionResult granted false with no rationale shows permanently denied`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onPermissionResult(granted = false, shouldShowRationale = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SpeakingUiState.PermissionDenied)
        assertTrue((state as SpeakingUiState.PermissionDenied).isPermanentlyDenied)
    }

    // ---- Offline path ----

    @Test
    fun `when offline onMicTapped shows error state`() = runTest {
        every { networkMonitor.isOnline() } returns false
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onMicTapped()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Expected Error for offline, got $state", state is SpeakingUiState.Error)
    }

    // ---- Score success path ----

    @Test
    fun `successful score transitions Recording → Processing → Result`() = runTest {
        val fakeFile = File.createTempFile("test_audio", ".m4a").also { it.deleteOnExit() }
        coEvery { audioRecorder.record(any()) } returns Result.success(fakeFile)
        coEvery { speakingRepository.scoreRecording(any(), any()) } returns Result.success(sampleResult)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle() // reach Idle

        vm.onMicTapped()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Expected Result after scoring, got $state", state is SpeakingUiState.Result)
        assertEquals(sampleResult, (state as SpeakingUiState.Result).result)
    }

    // ---- Try again ----

    @Test
    fun `tryAgain resets to Idle with same phrase`() = runTest {
        val fakeFile = File.createTempFile("test_audio2", ".m4a").also { it.deleteOnExit() }
        coEvery { audioRecorder.record(any()) } returns Result.success(fakeFile)
        coEvery { speakingRepository.scoreRecording(any(), any()) } returns Result.success(sampleResult)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onMicTapped()
        testDispatcher.scheduler.advanceUntilIdle()
        // Should be Result now
        assertTrue(vm.uiState.value is SpeakingUiState.Result)

        vm.tryAgain()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Expected Idle after tryAgain, got $state", state is SpeakingUiState.Idle)
        assertEquals(samplePhrase, (state as SpeakingUiState.Idle).phrase)
    }

    // ---- Recording failure path ----

    @Test
    fun `recording failure shows error state`() = runTest {
        coEvery { audioRecorder.record(any()) } returns
            Result.failure(RecordError.RecordingFailed(RuntimeException("mic error")))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onMicTapped()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Expected Error after recording failure, got $state", state is SpeakingUiState.Error)
    }

    // ---- Level selection reloads phrase ----

    @Test
    fun `selectLevel loads phrase for new HSK level`() = runTest {
        val hsk2Phrase = samplePhrase.copy(id = "cp_hsk2_001", hskLevel = 2)
        coEvery { speakingRepository.getRandomPhrase(2) } returns hsk2Phrase

        val vm = createViewModel(hsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectLevel(2)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SpeakingUiState.Idle)
        assertEquals(2, (state as SpeakingUiState.Idle).selectedHsk)
    }
}
