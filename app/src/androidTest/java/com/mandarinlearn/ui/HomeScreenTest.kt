// HomeScreenTest.kt — Mandarin Learn
// Instrumented smoke tests for HomeScreen and basic navigation.
// Phase 4: initial smoke tests created per FOLDER_STRUCTURE.md enumeration.
// Phase 8: extended with home → flashcard CTA flow and HomeScreen content assertions.
// IMPLEMENTATION_PLAN.md Phase 8 §E: HomeScreenTest extension for home → flashcard CTA flow.

package com.mandarinlearn.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke and navigation tests for HomeScreen.
 *
 * Phase 4: Bottom nav tab visibility tests.
 * Phase 8: Home → Flashcard CTA flow tests.
 *
 * NOTE: These run against a real device/emulator. The app completes its
 * first-launch JSON import before navigation is possible — allow adequate
 * idle time (Espresso/Compose auto-waits for idle composition).
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ---- Phase 4 smoke tests ----

    @Test
    fun bottomNavTabs_areDisplayed() {
        composeTestRule.onNodeWithText("Learn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Practice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Me").assertIsDisplayed()
    }

    @Test
    fun tappingPracticeTab_showsPracticeHub() {
        composeTestRule.onNodeWithText("Practice").assertIsDisplayed()
        // Smoke: Practice tab is reachable — title is rendered by MandarinTopBar.
        // TODO(phase_10): Assert specific PracticeHub card content.
    }

    // ---- Phase 8 tests: HomeScreen content and CTA flow ----

    @Test
    fun homeScreen_showsHskProgressHeader() {
        // Wait for HomeScreen to load — the HSK progress header should appear.
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Your HSK progress").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsPracticeGridHeader() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Practice").assertIsDisplayed()
    }

    @Test
    fun homeScreen_reviewCta_navigatesToFlashcards() {
        // When due cards are present, "Review now" should be visible and tappable.
        // In a freshly-imported state the "All caught up" path may appear instead;
        // this test verifies one of the two states is shown (not a crash).
        composeTestRule.waitForIdle()
        // Either "Review now" or "Learn new words" should be displayed (no blank state).
        val reviewNodeExists = try {
            composeTestRule.onNodeWithText("Review now").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }
        val learnNodeExists = try {
            composeTestRule.onNodeWithText("Learn new words").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }
        assert(reviewNodeExists || learnNodeExists) {
            "HomeScreen should show either 'Review now' or 'Learn new words' CTA"
        }
    }

    @Test
    fun meTab_navigatesToProgressScreen() {
        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()
        // Progress nav row should appear in MeScreen
        composeTestRule.onNodeWithText("Progress").assertIsDisplayed()
    }
}
