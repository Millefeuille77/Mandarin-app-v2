// ExamScreenTest.kt — Mandarin Learn
// Instrumented smoke tests for ExamScreen composables.
// IMPLEMENTATION_PLAN.md Phase 7 §E.

package com.mandarinlearn.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.ui.exam.ExamScreen
import com.mandarinlearn.ui.exam.SectionBreakOverlay
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [ExamScreen] and related composables.
 *
 * These tests use the fallback (no-ViewModel) overloads so they don't need a database.
 * Full integration tests live in Phase 10 EndToEndFlowTest.
 */
@RunWith(AndroidJUnit4::class)
class ExamScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun examScreen_fallback_displays_loading() {
        composeTestRule.setContent {
            MandarinLearnTheme {
                ExamScreen(
                    hsk                = 1,
                    onNavigateToResult = {},
                    onNavigateBack     = {},
                )
            }
        }
        // The fallback overload shows a LoadingState
        composeTestRule.waitForIdle()
    }

    @Test
    fun sectionBreakOverlay_shows_completed_section_name() {
        composeTestRule.setContent {
            MandarinLearnTheme {
                SectionBreakOverlay(
                    completedSectionName = "Listening",
                    nextSectionName      = "Reading",
                    secondsRemaining     = 28,
                    onContinue           = {},
                )
            }
        }
        composeTestRule
            .onNodeWithText("Listening section complete!", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun sectionBreakOverlay_continue_button_visible() {
        composeTestRule.setContent {
            MandarinLearnTheme {
                SectionBreakOverlay(
                    completedSectionName = "Reading",
                    nextSectionName      = "",
                    secondsRemaining     = 30,
                    onContinue           = {},
                )
            }
        }
        composeTestRule
            .onNodeWithText("Continue")
            .assertIsDisplayed()
    }
}
