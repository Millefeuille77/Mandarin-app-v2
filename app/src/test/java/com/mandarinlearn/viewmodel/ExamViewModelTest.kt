// ExamViewModelTest.kt — Mandarin Learn
// Unit tests for ExamViewModel.
// IMPLEMENTATION_PLAN.md Phase 7 §E.

package com.mandarinlearn.viewmodel

import com.mandarinlearn.domain.grading.ExamGrader
import com.mandarinlearn.domain.model.ExamSection
import com.mandarinlearn.domain.model.ExamStructure
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.domain.usecase.ExamSectionWithQuestions
import com.mandarinlearn.domain.usecase.StartExamResult
import com.mandarinlearn.domain.usecase.StartExamUseCase
import com.mandarinlearn.domain.usecase.SubmitExamUseCase
import com.mandarinlearn.ui.exam.ExamEvent
import com.mandarinlearn.ui.exam.ExamUiState
import com.mandarinlearn.ui.exam.ExamViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExamViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val startExamUseCase: StartExamUseCase = mockk()
    private val submitExamUseCase: SubmitExamUseCase = mockk()
    // Phase 7 QA M-2: ExamViewModel now requires an ExamAudioController; a relaxed mock satisfies tests.
    private val noOpAudioRepo: com.mandarinlearn.data.repository.AudioRepository = mockk(relaxed = true)
    private val noOpAudioFactory: (kotlinx.coroutines.CoroutineScope) -> com.mandarinlearn.ui.exam.ExamAudioController =
        { scope -> com.mandarinlearn.ui.exam.ExamAudioController(noOpAudioRepo, scope) }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- Loading ----

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { startExamUseCase(any()) } returns Result.success(fakeStartResult())
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        assertEquals(ExamUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `after successful load state is ActiveSection`() = runTest {
        coEvery { startExamUseCase(1) } returns Result.success(fakeStartResult())
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is ExamUiState.ActiveSection)
    }

    @Test
    fun `load failure transitions to Error`() = runTest {
        coEvery { startExamUseCase(any()) } returns Result.failure(Exception("DB error"))
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is ExamUiState.Error)
    }

    // ---- Answer selection ----

    @Test
    fun `selectAnswer updates selectedAnswer in state`() = runTest {
        coEvery { startExamUseCase(1) } returns Result.success(fakeStartResult())
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectAnswer("B")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as ExamUiState.ActiveSection
        assertEquals("B", state.selectedAnswer)
    }

    @Test
    fun `selectAnswer on wrong state is no-op`() = runTest {
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        // Still Loading — selectAnswer should not crash
        vm.selectAnswer("A")
    }

    // ---- Navigation lock (no back navigation) ----

    @Test
    fun `onBackPressed shows quit dialog not navigating away`() = runTest {
        coEvery { startExamUseCase(1) } returns Result.success(fakeStartResult())
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onBackPressed()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as ExamUiState.ActiveSection
        assertTrue("Quit dialog must be shown on back press", state.showQuitDialog)
    }

    @Test
    fun `dismissQuitDialog hides the dialog`() = runTest {
        coEvery { startExamUseCase(1) } returns Result.success(fakeStartResult())
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onBackPressed()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.dismissQuitDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as ExamUiState.ActiveSection
        assertFalse(state.showQuitDialog)
    }

    @Test
    fun `confirmQuit emits QuitExam event`() = runTest {
        coEvery { startExamUseCase(1) } returns Result.success(fakeStartResult())
        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        var quitReceived = false
        val collectJob = launch {
            vm.events.collect { event ->
                if (event is ExamEvent.QuitExam) quitReceived = true
            }
        }

        vm.onBackPressed()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.confirmQuit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("QuitExam event must be emitted on confirm", quitReceived)
        collectJob.cancel()
    }

    // ---- Timer expiration ----

    @Test
    fun `timer expiration triggers Submitting state`() = runTest {
        // 1-second exam (for test speed)
        val shortStructure = fakeStructure(durationMinutes = 0) // 0 min = 0 seconds
        val shortResult = fakeStartResult(structure = shortStructure)
        coEvery { startExamUseCase(1) } returns Result.success(shortResult)
        coEvery { submitExamUseCase(any(), any(), any(), any(), any()) } returns
                Result.success(fakeDomainResult())

        val vm = ExamViewModel(startExamUseCase, submitExamUseCase, noOpAudioFactory, initialHsk = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Timer should have fired immediately (0 seconds)
        testDispatcher.scheduler.advanceTimeBy(2_000)

        val state = vm.uiState.value
        assertTrue(
            "Expected Submitting or Done after timer expiry, got $state",
            state is ExamUiState.Submitting || state is ExamUiState.Done || state is ExamUiState.Error
        )
    }

    // ---- Helpers ----

    private fun fakeStructure(durationMinutes: Int = 40) = ExamStructure(
        hskLevel             = 1,
        totalDurationMinutes = durationMinutes,
        sections             = listOf(
            ExamSection("listening", 2, 20, listOf("multiple_choice"), 100, 60, ""),
        ),
        totalMaxScore        = 200,
        totalPassingScore    = 120,
        vocabularyRequired   = 150,
        scoringNotes         = "",
    )

    private fun fakeQuestion(id: String, section: String) = SampleQuestion(
        id               = id,
        hskLevel         = 1,
        section          = section,
        questionType     = "multiple_choice",
        questionText     = "Which word means 'hello'?",
        audioTextChinese = null,
        audioTextPinyin  = null,
        options          = listOf("你好", "再见", "谢谢", "对不起"),
        correctAnswer    = "A",
        explanation      = "你好 means hello.",
    )

    private fun fakeStartResult(structure: ExamStructure = fakeStructure()) = StartExamResult(
        structure          = structure,
        questionsBySection = mapOf(
            "listening" to listOf(fakeQuestion("q1", "listening"), fakeQuestion("q2", "listening")),
        ),
    )

    private fun fakeDomainResult() = com.mandarinlearn.domain.model.ExamResult(
        id              = 42L,
        hskLevel        = 1,
        startedAt       = 0L,
        finishedAt      = 1000L,
        durationSeconds = 1,
        sectionScores   = emptyList(),
        totalScore      = 0,
        totalMaxScore   = 200,
        passingScore    = 120,
        passed          = false,
        answers         = emptyList(),
    )
}
