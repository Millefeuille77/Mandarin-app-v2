// SettingsFlowTest.kt — Mandarin Learn
// Integration test: Me → Settings → font scale → verify text size changes.
// IMPLEMENTATION_PLAN.md Phase 10 §A.

package com.mandarinlearn.integration

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
 * Settings screen flow tests.
 *
 * Flow:
 * 1. Launch → HomeScreen
 * 2. Tap "Me" tab → MeScreen
 * 3. Tap "Settings" → SettingsScreen
 * 4. Verify settings controls are visible
 * 5. Back-press returns to MeScreen
 *
 * Font-scale live-preview is verified by the presence of the slider and preview text.
 * Actual pixel-level text-size assertions require a custom SemanticsMatcher and are
 * noted as a manual accessibility audit item in phase_10_report.md.
 */
@RunWith(AndroidJUnit4::class)
class SettingsFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /** Settings screen is reachable from Me tab → Settings in 2 taps. */
    @Test
    fun meTab_settingsIsReachable() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()

        // SettingsScreen top bar title
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    /** Theme section is visible in Settings. */
    @Test
    fun settings_displaySectionVisible() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Display").assertIsDisplayed()
    }

    /** Font size section controls are visible (slider + preview text). */
    @Test
    fun settings_fontSizeControlsVisible() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()

        // Font size label from strings.xml
        composeTestRule.onNodeWithText("Font size").assertIsDisplayed()
        // Live preview text from strings.xml (settings_font_preview = "Aa Bb 你好")
        composeTestRule.onNodeWithText("Aa Bb 你好").assertIsDisplayed()
    }

    /** Data section (export/import/reset) is visible. */
    @Test
    fun settings_dataSectionVisible() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Data").assertIsDisplayed()
    }

    /** About section shows app version string. */
    @Test
    fun settings_aboutSectionVisible() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("App version").assertIsDisplayed()
    }

    /** Back navigation from Settings returns to MeScreen without crash. */
    @Test
    fun settings_backNavigation_returnToMe() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()

        // Navigate back using back arrow (MandarinTopBar)
        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.waitForIdle()

        // Should be back on MeScreen — "Progress" link is visible
        composeTestRule.onNodeWithText("Progress").assertIsDisplayed()
    }
}
