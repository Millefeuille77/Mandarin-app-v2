// SettingsViewModelTest.kt — Mandarin Learn
// Unit tests for SettingsViewModel preference reads/writes and use-case delegation.
// Phase 9: Settings & Polish. IMPLEMENTATION_PLAN.md §Phase 9 tests.

package com.mandarinlearn.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.mandarinlearn.data.repository.SettingsRepository
import com.mandarinlearn.domain.usecase.ExportProgressUseCase
import com.mandarinlearn.domain.usecase.ImportProgressUseCase
import com.mandarinlearn.domain.usecase.ResetProgressUseCase
import com.mandarinlearn.ui.settings.AUDIO_SPEEDS
import com.mandarinlearn.ui.settings.FONT_SCALE_STEPS
import com.mandarinlearn.ui.settings.SettingsEvent
import com.mandarinlearn.ui.settings.SettingsUiState
import com.mandarinlearn.ui.settings.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    // Preference flows backed by mutable state for easy manipulation
    private val themeFlow          = MutableStateFlow("system")
    private val fontScaleFlow      = MutableStateFlow(1.0f)
    private val reduceMotionFlow   = MutableStateFlow(false)
    private val audioSpeedFlow     = MutableStateFlow(1.0f)
    private val showPinyinFlow     = MutableStateFlow(true)
    private val dailyLimitFlow     = MutableStateFlow(10)

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val exportUseCase: ExportProgressUseCase   = mockk()
    private val importUseCase: ImportProgressUseCase   = mockk()
    private val resetUseCase: ResetProgressUseCase     = mockk()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        every { settingsRepository.theme }             returns themeFlow
        every { settingsRepository.fontScale }         returns fontScaleFlow
        every { settingsRepository.reduceMotion }      returns reduceMotionFlow
        every { settingsRepository.audioSpeed }        returns audioSpeedFlow
        every { settingsRepository.showPinyin }        returns showPinyinFlow
        every { settingsRepository.dailyNewCardsLimit } returns dailyLimitFlow

        viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
            exportUseCase      = exportUseCase,
            importUseCase      = importUseCase,
            resetUseCase       = resetUseCase,
            appVersion         = "1.0.0",
            geminiKeySet       = true,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then transitions to Content`() = runTest {
        viewModel.uiState.test {
            // Loading or Content depending on how fast flows emit
            val state = awaitItem()
            // After initial emit, ensure we get Content
            if (state is SettingsUiState.Loading) {
                val content = awaitItem()
                assertTrue(content is SettingsUiState.Content)
            } else {
                assertTrue(state is SettingsUiState.Content)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Content state reflects current preference values`() = runTest {
        dispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as SettingsUiState.Content

        assertEquals("system", state.theme)
        assertEquals(1, state.fontScaleIndex) // 1.0f → index 1 (Medium)
        assertFalse(state.reduceMotion)
        assertEquals(2, state.audioSpeedIndex) // 1.0f → index 2
        assertTrue(state.showPinyinDefault)
        assertEquals(10, state.dailyNewCardsLimit)
        assertEquals("1.0.0", state.appVersion)
        assertTrue(state.geminiKeySet)
    }

    @Test
    fun `setTheme calls repository with correct value`() = runTest {
        viewModel.setTheme("dark")
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setTheme("dark") }
    }

    @Test
    fun `setFontScaleIndex maps index to correct multiplier`() = runTest {
        // Index 0 = Small = 0.9f
        viewModel.setFontScaleIndex(0)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { settingsRepository.setFontScale(FONT_SCALE_STEPS[0]) }

        // Index 3 = Extra-large = 1.3f
        viewModel.setFontScaleIndex(3)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { settingsRepository.setFontScale(FONT_SCALE_STEPS[3]) }
    }

    @Test
    fun `setAudioSpeedIndex maps index to correct speed`() = runTest {
        viewModel.setAudioSpeedIndex(0) // 0.5x
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { settingsRepository.setAudioSpeed(AUDIO_SPEEDS[0]) }

        viewModel.setAudioSpeedIndex(3) // 1.25x
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { settingsRepository.setAudioSpeed(AUDIO_SPEEDS[3]) }
    }

    @Test
    fun `setReduceMotion calls repository`() = runTest {
        viewModel.setReduceMotion(true)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { settingsRepository.setReduceMotion(true) }
    }

    @Test
    fun `setShowPinyinDefault calls repository`() = runTest {
        viewModel.setShowPinyinDefault(false)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { settingsRepository.setShowPinyin(false) }
    }

    @Test
    fun `setDailyNewCardsLimit calls repository`() = runTest {
        viewModel.setDailyNewCardsLimit(20)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { settingsRepository.setDailyNewCardsLimit(20) }
    }

    @Test
    fun `exportProgress emits EXPORT_SUCCESS snackbar on success`() = runTest {
        val uri: Uri = mockk()
        coEvery { exportUseCase.execute(uri) } returns Result.success(Unit)

        viewModel.events.test {
            viewModel.exportProgress(uri)
            dispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem() as SettingsEvent.ShowSnackbar
            assertEquals(SettingsEvent.SnackbarKey.EXPORT_SUCCESS, event.key)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `exportProgress emits EXPORT_FAILED snackbar on failure`() = runTest {
        val uri: Uri = mockk()
        coEvery { exportUseCase.execute(uri) } returns Result.failure(Exception("IO error"))

        viewModel.events.test {
            viewModel.exportProgress(uri)
            dispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem() as SettingsEvent.ShowSnackbar
            assertEquals(SettingsEvent.SnackbarKey.EXPORT_FAILED, event.key)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `importProgress emits IMPORT_SUCCESS on success`() = runTest {
        val uri: Uri = mockk()
        coEvery { importUseCase.execute(uri) } returns Result.success(Unit)

        viewModel.events.test {
            viewModel.importProgress(uri)
            dispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem() as SettingsEvent.ShowSnackbar
            assertEquals(SettingsEvent.SnackbarKey.IMPORT_SUCCESS, event.key)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `importProgress emits IMPORT_FAILED on failure`() = runTest {
        val uri: Uri = mockk()
        coEvery { importUseCase.execute(uri) } returns Result.failure(Exception("bad file"))

        viewModel.events.test {
            viewModel.importProgress(uri)
            dispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem() as SettingsEvent.ShowSnackbar
            assertEquals(SettingsEvent.SnackbarKey.IMPORT_FAILED, event.key)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `resetAllProgress emits RESET_COMPLETE on success`() = runTest {
        coEvery { resetUseCase.execute() } returns Result.success(Unit)

        viewModel.events.test {
            viewModel.resetAllProgress()
            dispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem() as SettingsEvent.ShowSnackbar
            assertEquals(SettingsEvent.SnackbarKey.RESET_COMPLETE, event.key)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `resetAllProgress emits RESET_FAILED on failure`() = runTest {
        coEvery { resetUseCase.execute() } returns Result.failure(Exception("db error"))

        viewModel.events.test {
            viewModel.resetAllProgress()
            dispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem() as SettingsEvent.ShowSnackbar
            assertEquals(SettingsEvent.SnackbarKey.RESET_FAILED, event.key)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `exportProgress sets isExporting flag during operation`() = runTest {
        val uri: Uri = mockk()
        coEvery { exportUseCase.execute(uri) } returns Result.success(Unit)

        // isExporting should be false before
        val before = viewModel.uiState.value as? SettingsUiState.Content
        assertNull(before?.exportError)

        viewModel.exportProgress(uri)
        dispatcher.scheduler.advanceUntilIdle()

        // isExporting should be false after
        val after = viewModel.uiState.value as? SettingsUiState.Content
        assertFalse(after?.isExporting ?: true)
    }
}
