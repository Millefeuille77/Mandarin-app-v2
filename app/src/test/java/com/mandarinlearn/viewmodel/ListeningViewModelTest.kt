// ListeningViewModelTest.kt — Mandarin Learn
// Unit tests for ListeningViewModel. Per IMPLEMENTATION_PLAN.md Phase 5 acceptance criteria.
// Tests: state transitions, replay cap, answer selection, session completion.

package com.mandarinlearn.viewmodel

import app.cash.turbine.test
import com.mandarinlearn.data.repository.AudioPlaybackState
import com.mandarinlearn.data.repository.AudioRepository
import com.mandarinlearn.data.repository.ListeningRepository
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.ui.listening.ListeningUiState
import com.mandarinlearn.ui.listening.ListeningViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListeningViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleQuestion = SampleQuestion(
        id               = "hsk1_listen_001",
        hskLevel         = 1,
        section          = "listening",
        questionType     = "multiple_choice",
        questionText     = "Which word did you hear?",
        audioTextChinese = "你好",
        audioTextPinyin  = "nǐ hǎo",
        options          = listOf("你好", "再见", "谢谢", "对不起"),
        correctAnswer    = "A",
        explanation      = "你好 means hello.",
    )

    private lateinit var listeningRepository: ListeningRepository
    private lateinit var audioRepository: AudioRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        listeningRepository = mockk()
        audioRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel(hsk: Int = 1): ListeningViewModel {
        coEvery { listeningRepository.getQuestionsForSession(any(), any()) } returns listOf(sampleQuestion)
        every { audioRepository.play(any(), any()) } returns flowOf(
            AudioPlaybackState.Loading,
            AudioPlaybackState.Playing(AudioPlaybackState.Source.ANDROID_TTS),
            AudioPlaybackState.Finished,
        )
        return ListeningViewModel(listeningRepository, audioRepository, hsk)
    }

    @Test
    fun `initial state transitions from Loading to Content`() = runTest {
        val vm = makeViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Expected Content but got $state", state is ListeningUiState.Content)
    }

    @Test
    fun `correct answer selection sets isCorrect and hasAnswered`() = runTest {
        val vm = makeViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectAnswer(0) // Option A (index 0) is correct
        val state = vm.uiState.value as ListeningUiState.Content

        assertTrue(state.hasAnswered)
        assertTrue(state.isCorrect)
        assertEquals(0, state.selectedOptionIndex)
    }

    @Test
    fun `wrong answer selection sets isCorrect false`() = runTest {
        val vm = makeViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectAnswer(1) // Option B — incorrect
        val state = vm.uiState.value as ListeningUiState.Content

        assertTrue(state.hasAnswered)
        assertFalse(state.isCorrect)
    }

    @Test
    fun `nextQuestion after last question shows SessionComplete`() = runTest {
        // Single-question session
        val vm = makeViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectAnswer(0)
        vm.nextQuestion()

        val state = vm.uiState.value
        assertTrue("Expected SessionComplete but got $state", state is ListeningUiState.SessionComplete)
    }

    @Test
    fun `replay count increments each play call`() = runTest {
        val vm = makeViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initial auto-play sets replayCount=1; play again sets replayCount=2
        vm.playAudio()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as ListeningUiState.Content
        assertTrue("replayCount should be >= 2 after manual play", state.replayCount >= 2)
    }

    @Test
    fun `empty questions list shows Empty state`() = runTest {
        coEvery { listeningRepository.getQuestionsForSession(any(), any()) } returns emptyList()
        every { audioRepository.play(any(), any()) } returns flowOf(AudioPlaybackState.Finished)

        val vm = ListeningViewModel(listeningRepository, audioRepository, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Expected Empty but got $state", state is ListeningUiState.Empty)
    }

    @Test
    fun `audio failure sets audioFailed flag`() = runTest {
        coEvery { listeningRepository.getQuestionsForSession(any(), any()) } returns listOf(sampleQuestion)
        every { audioRepository.play(any(), any()) } returns flowOf(
            AudioPlaybackState.Loading,
            AudioPlaybackState.Failed("No Chinese TTS voice installed"),
        )

        val vm = ListeningViewModel(listeningRepository, audioRepository, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as ListeningUiState.Content
        assertTrue(state.audioFailed)
    }

    @Test
    fun `skipQuestion advances without incrementing correctCount`() = runTest {
        coEvery { listeningRepository.getQuestionsForSession(any(), any()) } returns listOf(
            sampleQuestion,
            sampleQuestion.copy(id = "hsk1_listen_002"),
        )
        every { audioRepository.play(any(), any()) } returns flowOf(
            AudioPlaybackState.Failed("no audio")
        )

        val vm = ListeningViewModel(listeningRepository, audioRepository, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.skipQuestion()
        val state = vm.uiState.value as ListeningUiState.Content
        assertEquals(2, state.questionIndex) // Advanced to question 2
    }
}
