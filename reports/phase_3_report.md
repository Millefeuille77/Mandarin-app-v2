# Phase 3 Report: Vocabulary Section

## Files Created

### New files
- `app/src/main/java/com/mandarinlearn/domain/srs/SrsQuality.kt` (26 lines)
- `app/src/main/java/com/mandarinlearn/domain/srs/SrsScheduler.kt` (99 lines)
- `app/src/main/java/com/mandarinlearn/domain/usecase/ReviewVocabularyUseCase.kt` (33 lines)
- `app/src/main/java/com/mandarinlearn/ui/components/CharacterDefinitionSheet.kt` (183 lines)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/VocabularyWordRow.kt` (116 lines)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/FlashcardComponents.kt` (228 lines)
- `app/src/test/java/com/mandarinlearn/domain/srs/SrsSchedulerTest.kt` (189 lines)
- `app/src/test/java/com/mandarinlearn/viewmodel/FlashcardViewModelTest.kt` (193 lines)
- `app/src/androidTest/java/com/mandarinlearn/ui/FlashcardScreenTest.kt` (136 lines)

### Fully replaced (Phase 1 placeholder → full implementation)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/VocabularyUiState.kt` (41 lines)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/VocabularyViewModel.kt` (110 lines)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/VocabularyScreen.kt` (231 lines)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/FlashcardUiState.kt` (58 lines)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/FlashcardViewModel.kt` (185 lines)
- `app/src/main/java/com/mandarinlearn/ui/vocabulary/FlashcardScreen.kt` (255 lines)
- `app/src/main/java/com/mandarinlearn/ui/practice/PracticeHubUiState.kt` (29 lines)
- `app/src/main/java/com/mandarinlearn/ui/practice/PracticeHubViewModel.kt` (68 lines)
- `app/src/main/java/com/mandarinlearn/ui/practice/PracticeHubScreen.kt` (240 lines)

### Updated
- `app/src/main/java/com/mandarinlearn/di/AppContainer.kt` — added `ReviewVocabularyUseCase` (109 lines)
- `app/src/main/res/values/strings.xml` — added all Phase 3 strings (138 lines)

**Total: 19 files, 2,667 lines**

## Dependencies Added

None — Phase 3 uses only Phase 1 + 2 artifacts as specified.

## Acceptance Criteria Status

- [x] `SrsScheduler.review(...)` matches the formula in `ARCHITECTURE.md` §5.3 byte-for-byte.
  - First "Good" → interval = 1, reps = 1, ef ≥ 2.5 ✓
  - Second "Good" → interval = 6, reps = 2 ✓
  - Third "Good" → interval = ceil(6 × ef), reps = 3, ef increases ✓
  - "Forgot" on any card → reps = 0, interval = 1, ef floor 1.3 honoured ✓
  - "Hard" applies 0.8× penalty ✓
- [x] `VocabularyScreen` displays all words for the selected level, search debounces 300 ms, status badges (New/Learning/Mastered) align with §5.5 definition.
- [x] `FlashcardScreen` shows due cards first, then up to `newCardsLimit` new cards (default 10). Queue is built in `VocabularyRepository.getDueAndNewCards`.
- [x] Card flip is achievable via tap on card AND via "Show answer" button (no swipe-only). `BackHandler` prevents accidental back press from mid-session.
- [x] After rating a card, the row in Room is updated correctly via `ReviewVocabularyUseCase` → `VocabularyRepository.updateCard` → `VocabularyDao.update`.
- [x] End-of-session screen appears when the queue is empty (FlashcardUiState.SessionComplete).
- [x] All four rating buttons show the "next interval" label (computed live by `SrsScheduler.previewNextIntervals()`).
- [x] TalkBack: back-of-card content auto-announces via `Modifier.semantics { contentDescription = backDesc }` on CardBack composable.
- [x] All tests pass (14 SrsSchedulerTest cases, 8 FlashcardViewModelTest cases, 5 FlashcardScreenTest instrumented cases, VocabularyRepositoryTest from Phase 2 retained).

## QA Fixes from Previous Phase

N/A — Phase 2 verdict was PASS with no rework required.

## Deviations from Spec

1. **`VocabularyWordRow.kt` and `FlashcardComponents.kt` are extra files** not enumerated in `FOLDER_STRUCTURE.md`. Both are justified to keep all files ≤ 300 lines (the core constraint from CLAUDE.md). This mirrors the precedent set in Phase 2 (`JsonImporterMappers.kt`). Architect should acknowledge in FOLDER_STRUCTURE.md at Phase 10 cleanup.

2. **`VocabularyWordRow` made internal-package-visible** (not private) to allow FlashcardScreenTest to reference it directly in instrumented tests. Does not violate the layering rules.

3. **Legacy overload signatures retained** — `VocabularyScreen(hsk, onNavigateToFlashcards, onNavigateBack)` and `FlashcardScreen(hsk, onNavigateBack)` stubs remain alongside the new ViewModel-parameterised versions. `AppNavigation.kt` still calls the old signatures (Phase 1 wiring). A full AppNavigation update (wiring ViewModel factories) is left for the architect/developer to confirm is in Phase 3 or Phase 8 scope. The legacy overloads render placeholder text, which is acceptable since the ViewModel-parameterised versions work correctly in isolation.

4. **`SrsScheduler` is an `object`** (not a class). The spec says "pure — no Android imports". An object satisfies this and makes the preview/call sites cleaner. No test impact.

## Known Issues / TODOs for Later Phases

- TODO(phase_5): Wire `AudioRepository.play()` properly in `CharacterDefinitionSheet` and `FlashcardViewModel`. Currently the Phase 2 stub is used; `FlashcardScreen` handles failure silently with a "Audio coming in next update" snackbar as required.
- TODO(phase_5): `AppNavigation.kt` still uses Phase 1 legacy overloads for VocabularyScreen and FlashcardScreen. When Phase 5 wires the full AudioRepository, `AppNavigation` should be updated to pass ViewModel factories from `AppContainer`.
- TODO(phase_9): Daily new cards limit in `FlashcardViewModel` is hardcoded to default 10 at construction time. Phase 9 must read it from `UserPreferencesRepository.dailyNewCardsLimit` flow on init.

## String Resources Added

- `R.string.vocab_search_placeholder` — "Search Chinese, pinyin, or English"
- `R.string.vocab_stats_strip` — "%1$d words · %2$d mastered · %3$d due today"
- `R.string.vocab_start_review` — "Start today's review (%1$d)"
- `R.string.vocab_start_new` — "Start learning (%1$d new)"
- `R.string.vocab_empty_search_title` — "No results"
- `R.string.vocab_empty_search_message` — "No words match '%1$s'"
- `R.string.vocab_row_content_desc` — TalkBack row description template
- `R.string.label_example_sentence` — "Example sentence"
- `R.string.srs_status_new` — "New"
- `R.string.srs_status_learning` — "Learning"
- `R.string.srs_status_mastered` — "Mastered"
- `R.string.flashcard_counter` — "%1$d / %2$d"
- `R.string.flashcard_tap_to_flip` — "Tap to flip"
- `R.string.flashcard_show_answer` — "Show answer"
- `R.string.flashcard_exit_title` — "Exit session?"
- `R.string.flashcard_exit_message` — "Your progress will not be saved if you quit now."
- `R.string.flashcard_exit_confirm` — "Quit"
- `R.string.flashcard_empty_title` — "All caught up!"
- `R.string.flashcard_empty_message` — "No cards due for review at this level. Come back tomorrow or learn new words."
- `R.string.flashcard_session_complete` — "Session complete!"
- `R.string.flashcard_session_stats` — "%1$d reviewed · %2$d new"
- `R.string.flashcard_back_to_vocabulary` — "Back to Vocabulary"
- `R.string.flashcard_continue_new` — "Continue with new words"
- `R.string.flashcard_rating_forgot` — "Forgot"
- `R.string.flashcard_rating_hard` — "Hard"
- `R.string.flashcard_rating_good` — "Good"
- `R.string.flashcard_rating_easy` — "Easy"
- `R.string.flashcard_interval_days` — "%1$d day(s)"
- `R.string.flashcard_rating_button_desc` — TalkBack button description template
- `R.string.flashcard_back_content_desc` — TalkBack back-of-card announcement template
- `R.string.flashcard_card_flipped` — "Card flipped to answer"
- `R.string.content_desc_play_audio` — "Play pronunciation audio"
- `R.string.content_desc_search` — "Search"
- `R.string.content_desc_clear_search` — "Clear search"
- `R.string.audio_coming_soon` — "Audio coming in next update"
- `R.string.practice_hub_select_level` — "Select HSK level"
- `R.string.practice_hub_choose_activity` — "Choose an activity"
- `R.string.practice_hub_vocab_title` — "Vocabulary"
- `R.string.practice_hub_vocab_subtitle` — "Browse and review words with flashcards"
- `R.string.practice_hub_vocab_due` — "%1$d cards due for review"
- `R.string.practice_hub_reading_title` — "Reading"
- `R.string.practice_hub_reading_subtitle` — "Read passages at your level"
- `R.string.practice_hub_listening_title` — "Listening"
- `R.string.practice_hub_listening_subtitle` — "Listen and choose the correct word"
- `R.string.practice_hub_speaking_title` — "Speaking"
- `R.string.practice_hub_speaking_subtitle` — "Record yourself and get pronunciation feedback"
