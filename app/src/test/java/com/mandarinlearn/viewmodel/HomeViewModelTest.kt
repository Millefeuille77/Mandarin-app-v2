// HomeViewModelTest.kt — Mandarin Learn
// Unit tests for HomeViewModel.
// IMPLEMENTATION_PLAN.md Phase 8 §E: state shape, due-count zero case.

package com.mandarinlearn.viewmodel

import com.mandarinlearn.domain.model.Streak
import com.mandarinlearn.domain.usecase.DashboardData
import com.mandarinlearn.domain.usecase.GetDashboardUseCase
import com.mandarinlearn.domain.usecase.LevelDashboardData
import com.mandarinlearn.ui.home.HomeUiState
import com.mandarinlearn.ui.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getDashboardUseCase: GetDashboardUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getDashboardUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- Helper ----

    private fun buildDashboardData(
        currentStreak: Int = 0,
        longestStreak: Int = 0,
        dueCounts: List<Int> = listOf(0, 0, 0, 0, 0),
        masteredCounts: List<Int> = listOf(0, 0, 0, 0, 0),
        totalCounts: List<Int> = listOf(153, 150, 300, 310, 300),
    ): DashboardData {
        val levels = (1..5).map { hsk ->
            LevelDashboardData(
                hskLevel      = hsk,
                masteredCount = masteredCounts.getOrElse(hsk - 1) { 0 },
                totalVocabCount = totalCounts.getOrElse(hsk - 1) { 0 },
                readingProgress = null,
                bestExamResult  = null,
                totalMaxScore   = if (hsk <= 2) 200 else 300,
            )
        }
        return DashboardData(
            streak        = Streak(currentStreak, longestStreak, null),
            levels        = levels,
            focusLevel    = dueCounts.indexOfFirst { it > 0 }.takeIf { it >= 0 }?.let { it + 1 },
            totalDueCount = dueCounts.sum(),
        )
    }

    // ---- Tests ----

    @Test
    fun `initial state is Loading`() = runTest {
        every { getDashboardUseCase.observe() } returns flowOf()
        val vm = HomeViewModel(getDashboardUseCase)
        assertTrue("Initial state should be Loading", vm.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `emits Content after dashboard data arrives`() = runTest {
        val data = buildDashboardData(currentStreak = 5, longestStreak = 12)
        every { getDashboardUseCase.observe() } returns flowOf(data)

        val vm = HomeViewModel(getDashboardUseCase)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("State should be Content", state is HomeUiState.Content)
        state as HomeUiState.Content
        assertEquals(5, state.currentStreak)
        assertEquals(12, state.longestStreak)
    }

    @Test
    fun `due count zero case shows all caught up state`() = runTest {
        val data = buildDashboardData(dueCounts = listOf(0, 0, 0, 0, 0))
        every { getDashboardUseCase.observe() } returns flowOf(data)

        val vm = HomeViewModel(getDashboardUseCase)
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Content
        assertEquals(0, state.totalDueCount)
        // Focus level defaults to 1 when nothing is due
        assertEquals(1, state.focusLevel)
    }

    @Test
    fun `due count nonzero sets correct focus level`() = runTest {
        // HSK 2 has 5 due cards; HSK 1 has 0
        val data = buildDashboardData(dueCounts = listOf(0, 5, 0, 0, 0))
        every { getDashboardUseCase.observe() } returns flowOf(data)

        val vm = HomeViewModel(getDashboardUseCase)
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Content
        assertEquals(5, state.totalDueCount)
        assertEquals(2, state.focusLevel) // lowest level with due cards = HSK 2
    }

    @Test
    fun `level rows have correct mastered fractions`() = runTest {
        val data = buildDashboardData(
            masteredCounts = listOf(45, 0, 0, 0, 0),
            totalCounts    = listOf(153, 150, 300, 310, 300),
        )
        every { getDashboardUseCase.observe() } returns flowOf(data)

        val vm = HomeViewModel(getDashboardUseCase)
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Content
        assertEquals(5, state.levelRows.size)
        val hsk1Row = state.levelRows.first()
        assertEquals(45, hsk1Row.masteredCount)
        assertEquals(153, hsk1Row.totalCount)
        val expectedFraction = 45f / 153f
        assertEquals(expectedFraction, hsk1Row.masteredFraction, 0.001f)
    }

    @Test
    fun `emits Error when use case throws`() = runTest {
        every { getDashboardUseCase.observe() } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException("DB failure")
        }

        val vm = HomeViewModel(getDashboardUseCase)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("State should be Error on exception", state is HomeUiState.Error)
    }

    @Test
    fun `retry transitions back to Loading then Content`() = runTest {
        val data = buildDashboardData(currentStreak = 3)
        every { getDashboardUseCase.observe() } returns flowOf(data)

        val vm = HomeViewModel(getDashboardUseCase)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is HomeUiState.Content)

        vm.retry()
        // After retry() state resets to Loading; then re-emits Content
        advanceUntilIdle()
        assertTrue(vm.uiState.value is HomeUiState.Content)
    }
}
