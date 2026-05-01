// FlashcardViewModelTest.kt — Mandarin Learn
// Unit tests for FlashcardViewModel state transitions.
// Phase 3: session loading, flip, rating, session complete, audio stub handling.

package com.mandarinlearn.viewmodel

import com.mandarinlearn.data.repository.AudioPlaybackState
import com.mandarinlearn.data.repository.AudioRepository
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.domain.srs.SrsQuality
import com.mandarinlearn.domain.usecase.ReviewVocabularyUseCase
import com.mandarinlearn.ui.vocabulary.FlashcardEvent
import com.mandarinlearn.ui.vocabulary.FlashcardUiState
import com.mandarinlearn.ui.vocabulary.FlashcardViewModel
import com.mandarinlearn.util.DispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockVocabRepo = mockk<VocabularyRepository>()
    private val mockAudioRepo = mockk<AudioRepository>()
    private val mockReviewUseCase = mockk<ReviewVocabularyUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeWord(
        id: String = "hsk1_001",
        isIntroduced: Boolean = false,
        repetitionCount: Int = 0,
        intervalDays: Int = 0,
        easeFactor: Double = 2.5,
    ) = VocabularyWord(
        id = id, hskLevel = 1, character = "我", pinyin = "wǒ",
        translation = "I, me", partOfSpeech = "pronoun",
        exampleChinese = "我是学生", examplePinyin = "wǒ shì xuéshēng",
        exampleEnglish = "I am a student",
        easeFactor = easeFactor, intervalDays = intervalDays,
        repetitionCount = repetitionCount, nextReviewDate = 0L,
        lastReviewedDate = null, isIntroduced = isIntroduced,
    )

    private fun makeViewModel(cards: List<VocabularyWord>): FlashcardViewModel {
        coEvery { mockVocabRepo.getDueAndNewCards(any(), any()) } returns cards
        coEvery { mockReviewUseCase(any(), any()) } returns cards.first()
        return FlashcardViewModel(
            vocabularyRepository = mockVocabRepo,
            audioRepository      = mockAudioRepo,
            reviewUseCase        = mockReviewUseCase,
            hsk                  = 1,
            newCardsLimit        = 10,
        )
    }

    // ── Test 1: Session loads a card and shows Reviewing state ─────────────────
    @Test
    fun `loads session and transitions to Reviewing`() = runTest {
        val vm = makeViewModel(listOf(makeWord()))
        val state = vm.uiState.value
        assertTrue("Should be Reviewing after load", state is FlashcardUiState.Reviewing)
    }

    // ── Test 2: Empty queue shows Empty state ─────────────────────────────────
    @Test
    fun `empty queue shows Empty state`() = runTest {
        coEvery { mockVocabRepo.getDueAndNewCards(any(), any()) } returns emptyList()
        val vm = FlashcardViewModel(mockVocabRepo, mockAudioRepo, mockReviewUseCase, 1, 10)
        assertTrue("Should be Empty", vm.uiState.value is FlashcardUiState.Empty)
    }

    // ── Test 3: flipCard transitions isFlipped from false → true ─────────────
    @Test
    fun `flipCard sets isFlipped to true`() = runTest {
        val vm = makeViewModel(listOf(makeWord()))
        val initial = vm.uiState.value as FlashcardUiState.Reviewing
        assertTrue("Initially not flipped", !initial.isFlipped)

        vm.flipCard()

        val flipped = vm.uiState.value as FlashcardUiState.Reviewing
        assertTrue("Should be flipped after flipCard", flipped.isFlipped)
    }

    // ── Test 4: Rating single card → SessionComplete ───────────────────────────
    @Test
    fun `rating the only card transitions to SessionComplete`() = runTest {
        val word = makeWord()
        coEvery { mockVocabRepo.getDueAndNewCards(any(), any()) } returnsMany
            listOf(listOf(word), emptyList())
        coEvery { mockReviewUseCase(any(), any()) } returns word

        val vm = FlashcardViewModel(mockVocabRepo, mockAudioRepo, mockReviewUseCase, 1, 10)
        vm.rateCard(SrsQuality.GOOD)

        // After rating, queue empty → SessionComplete
        assertTrue("Should be SessionComplete", vm.uiState.value is FlashcardUiState.SessionComplete)
    }

    // ── Test 5: ReviewVocabularyUseCase is called with the correct quality ─────
    @Test
    fun `rateCard calls review use case with correct quality`() = runTest {
        val word = makeWord()
        coEvery { mockVocabRepo.getDueAndNewCards(any(), any()) } returnsMany
            listOf(listOf(word), emptyList())
        coEvery { mockReviewUseCase(any(), any()) } returns word

        val vm = FlashcardViewModel(mockVocabRepo, mockAudioRepo, mockReviewUseCase, 1, 10)
        vm.rateCard(SrsQuality.EASY)

        coVerify { mockReviewUseCase(word, SrsQuality.EASY) }
    }

    // ── Test 6: Audio stub failure emits ShowSnackbar event ───────────────────
    @Test
    fun `playAudio emits ShowSnackbar when audio repository fails`() = runTest {
        val word = makeWord()
        coEvery { mockVocabRepo.getDueAndNewCards(any(), any()) } returns listOf(word)
        every { mockAudioRepo.play(any(), any()) } returns flowOf(
            AudioPlaybackState.Loading,
            AudioPlaybackState.Failed("Audio available in a future update"),
        )

        val vm = FlashcardViewModel(mockVocabRepo, mockAudioRepo, mockReviewUseCase, 1, 10)
        val events = mutableListOf<FlashcardEvent>()
        val job = kotlinx.coroutines.launch(testDispatcher) { vm.events.collect { events.add(it) } }

        vm.playAudio()

        assertTrue("Should have emitted a snackbar event", events.isNotEmpty())
        assertTrue("Event should be ShowSnackbar",
            events.first() is FlashcardEvent.ShowSnackbar)
        job.cancel()
    }

    // ── Test 7: Session counts reviewed and new cards correctly ───────────────
    @Test
    fun `session stats are correct after reviewing new card`() = runTest {
        val newWord = makeWord(isIntroduced = false)
        coEvery { mockVocabRepo.getDueAndNewCards(any(), any()) } returnsMany
            listOf(listOf(newWord), emptyList())
        coEvery { mockReviewUseCase(any(), any()) } returns newWord

        val vm = FlashcardViewModel(mockVocabRepo, mockAudioRepo, mockReviewUseCase, 1, 10)
        vm.rateCard(SrsQuality.GOOD)

        val state = vm.uiState.value as FlashcardUiState.SessionComplete
        assertEquals(1, state.reviewedCount)
        assertEquals(1, state.newCount) // new card counted
    }

    // ── Test 8: Multiple cards → index advances correctly ─────────────────────
    @Test
    fun `reviewing multiple cards advances index`() = runTest {
        val cards = listOf(makeWord("c1", isIntroduced = true), makeWord("c2", isIntroduced = false))
        coEvery { mockVocabRepo.getDueAndNewCards(any(), any()) } returnsMany
            listOf(cards, emptyList())
        coEvery { mockReviewUseCase(any(), any()) } returns cards[0]

        val vm = FlashcardViewModel(mockVocabRepo, mockAudioRepo, mockReviewUseCase, 1, 10)
        val initial = vm.uiState.value as FlashcardUiState.Reviewing
        assertEquals(0, initial.currentIndex)

        vm.rateCard(SrsQuality.GOOD)

        val next = vm.uiState.value as FlashcardUiState.Reviewing
        assertEquals(1, next.currentIndex)
    }
}
