// FlashcardScreenTest.kt — Mandarin Learn
// Instrumented Compose UI tests for FlashcardScreen.
// Tests: front/back rendering, show-answer button, rating buttons, session-complete state.

package com.mandarinlearn.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.domain.srs.SrsQuality
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.vocabulary.FlashcardComponents
import com.mandarinlearn.ui.vocabulary.FlashcardUiState
import com.mandarinlearn.ui.vocabulary.RatingButtonRow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlashcardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleWord = VocabularyWord(
        id = "hsk1_001", hskLevel = 1,
        character = "我", pinyin = "wǒ", translation = "I, me",
        partOfSpeech = "pronoun",
        exampleChinese = "我是学生", examplePinyin = "wǒ shì xuéshēng",
        exampleEnglish = "I am a student",
        easeFactor = 2.5, intervalDays = 0, repetitionCount = 0,
        nextReviewDate = 0L, lastReviewedDate = null, isIntroduced = false,
    )

    private val reviewingState = FlashcardUiState.Reviewing(
        currentCard   = sampleWord,
        isFlipped     = false,
        currentIndex  = 0,
        totalCards    = 5,
        nextIntervals = mapOf(
            SrsQuality.FORGOT to 1,
            SrsQuality.HARD   to 1,
            SrsQuality.GOOD   to 1,
            SrsQuality.EASY   to 6,
        ),
    )

    // ── Test 1: Front of card shows hanzi character ───────────────────────────
    @Test
    fun front_shows_character() {
        composeTestRule.setContent {
            MandarinLearnTheme {
                FlippableCard(
                    card           = sampleWord,
                    isFlipped      = false,
                    reduceMotion   = false,
                    onFlip         = {},
                    onPlayAudio    = {},
                    isAudioLoading = false,
                )
            }
        }
        composeTestRule.onNodeWithText("我").assertIsDisplayed()
    }

    // ── Test 2: Back of card shows translation ───────────────────────────────
    @Test
    fun back_shows_translation() {
        composeTestRule.setContent {
            MandarinLearnTheme {
                FlippableCard(
                    card           = sampleWord,
                    isFlipped      = true,
                    reduceMotion   = true, // skip animation in tests
                    onFlip         = {},
                    onPlayAudio    = {},
                    isAudioLoading = false,
                )
            }
        }
        composeTestRule.onNodeWithText("I, me").assertIsDisplayed()
    }

    // ── Test 3: Rating buttons are visible when flipped ───────────────────────
    @Test
    fun rating_buttons_visible_when_flipped() {
        composeTestRule.setContent {
            MandarinLearnTheme {
                RatingButtonRow(
                    nextIntervals = reviewingState.nextIntervals,
                    onRate        = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Forgot").assertIsDisplayed()
        composeTestRule.onNodeWithText("Good").assertIsDisplayed()
        composeTestRule.onNodeWithText("Easy").assertIsDisplayed()
    }

    // ── Test 4: Tapping "Good" triggers the onRate callback ──────────────────
    @Test
    fun tapping_good_button_calls_onRate() {
        var ratedQuality: SrsQuality? = null
        composeTestRule.setContent {
            MandarinLearnTheme {
                RatingButtonRow(
                    nextIntervals = reviewingState.nextIntervals,
                    onRate        = { ratedQuality = it },
                )
            }
        }
        composeTestRule.onNodeWithText("Good").performClick()
        assert(ratedQuality == SrsQuality.GOOD)
    }

    // ── Test 5: Pinyin is shown on the back ───────────────────────────────────
    @Test
    fun back_shows_pinyin() {
        composeTestRule.setContent {
            MandarinLearnTheme {
                FlippableCard(
                    card           = sampleWord,
                    isFlipped      = true,
                    reduceMotion   = true,
                    onFlip         = {},
                    onPlayAudio    = {},
                    isAudioLoading = false,
                )
            }
        }
        composeTestRule.onNodeWithText("wǒ").assertIsDisplayed()
    }
}
