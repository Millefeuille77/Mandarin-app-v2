# Phase 4 Report: Reading Section

## Files Created

### New files (Phase 4)
- `app/src/main/java/com/mandarinlearn/ui/components/PinyinAnnotationModels.kt` (43 lines)
- `app/src/main/java/com/mandarinlearn/ui/components/PinyinText.kt` (179 lines)
- `app/src/main/java/com/mandarinlearn/ui/components/NoDefinitionSheet.kt` (84 lines)
- `app/src/main/java/com/mandarinlearn/domain/usecase/PlayChineseAudioUseCase.kt` (33 lines)
- `app/src/test/java/com/mandarinlearn/data/repository/ReadingRepositoryTest.kt` (197 lines)
- `app/src/androidTest/java/com/mandarinlearn/ui/HomeScreenTest.kt` (58 lines)
- `app/src/main/java/com/mandarinlearn/ui/reading/PassageControls.kt` (183 lines) — extracted from PassageScreen for 300-line compliance

### Files replaced / fully implemented (were Phase 1 placeholders)
- `app/src/main/java/com/mandarinlearn/ui/reading/ReadingListUiState.kt` (34 lines)
- `app/src/main/java/com/mandarinlearn/ui/reading/ReadingListViewModel.kt` (84 lines)
- `app/src/main/java/com/mandarinlearn/ui/reading/ReadingListScreen.kt` (279 lines)
- `app/src/main/java/com/mandarinlearn/ui/reading/PassageUiState.kt` (47 lines)
- `app/src/main/java/com/mandarinlearn/ui/reading/PassageViewModel.kt` (169 lines)
- `app/src/main/java/com/mandarinlearn/ui/reading/PassageScreen.kt` (267 lines)

### Files updated (carried-over nav wiring task + minor addenda)
- `app/src/main/java/com/mandarinlearn/navigation/AppNavigation.kt` (263 lines) — added `appContainer` param, wired ViewModels for Vocabulary/Flashcard/ReadingList/Passage routes
- `app/src/main/java/com/mandarinlearn/ui/MainScaffold.kt` (128 lines) — PracticeHubScreen now receives real ViewModel from AppContainer
- `app/src/main/java/com/mandarinlearn/MainActivity.kt` — passes `appContainer` to AppNavigation
- `app/src/main/java/com/mandarinlearn/di/AppContainer.kt` — added `PlayChineseAudioUseCase`
- `app/src/main/java/com/mandarinlearn/data/local/dao/VocabularyDao.kt` — added `findByCharacter` query
- `app/src/main/java/com/mandarinlearn/data/repository/VocabularyRepository.kt` — added `findByCharacter` method
- `app/src/main/res/values/strings.xml` — 22 new string resources for Phase 4

**Total new/modified production files: 16**
**Total lines in Phase 4 files: 2,048 (all ≤ 300 per file)**

## Dependencies Added

None — Phase 4 uses only Phase 1 + 2 + 3 artifacts. `FlowRow` is from Compose BOM 2024.02.00 which was already locked in Phase 1.

## Acceptance Criteria Status

- [x] `PinyinText` correctly stacks pinyin above each hanzi for HSK 1–3 passages (every char annotated) and only above key vocab for HSK 4–5 — implemented in `PinyinText.kt` + `PinyinAnnotationModels.kt`; verified in `ReadingRepositoryTest.kt`.
- [x] Tapping a hanzi opens `CharacterDefinitionSheet` — implemented in `PassageViewModel.onCharacterTapped()` + `PassageScreen`.
- [x] If character not in vocabulary, graceful "Word not in HSK 1–5 vocabulary list" message via `NoDefinitionSheet` — `noDefinition` branch in `PassageContent`.
- [x] Punctuation characters rendered but not tappable — `isTappable = annotation.pinyin.isNotEmpty()` in `toAnnotatedCharacters()`; TalkBack `contentDescription = ""` for punctuation cells.
- [x] Font size slider live-updates rendering between 0.8× and 1.6× — `PassageControlsBar` Slider + `fontScale` applied to each `Text` sp value in `PinyinText`.
- [x] Pinyin toggle hides/shows the pinyin row instantly — `showPinyin` state in `PassageViewModel.togglePinyin()`.
- [x] "Mark as read" toggles `is_completed` and updates via `ReadingRepository.markCompleted()` — footer button in `PassageContent`.
- [x] Loading and error states render correctly — `PassageUiState.Loading` → `LoadingState`; `PassageUiState.Error` → `ErrorState`.
- [x] FlowRow wraps long passages without overflow — `PinyinText` uses `@OptIn(ExperimentalLayoutApi::class) FlowRow`.
- [x] Tests pass — `ReadingRepositoryTest.kt` (8 unit tests) and `HomeScreenTest.kt` (2 instrumented smoke tests).

## QA Fixes from Previous Phase

Phase 3 QA Verdict was PASS WITH WARNINGS (B=0, M=1, m=1). Both issues were patched manually before Phase 4 started per the task brief:

- [x] **M-1 fixed:** Hardcoded strings in `ImportLoadingScreen.kt` moved to `strings.xml` and replaced with `stringResource(...)`. The `strings.xml` already contains `import_setting_up` and `import_importing_vocabulary` entries added in Phase 2 strings; the Phase 3 QA caught that the screen was using raw literals — confirmed fixed before Phase 4.
- [x] **m-1 fixed:** `ConfirmDialog` `TextButton` instances now have `Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)` — confirmed in the current `ConfirmDialog.kt`.

## Carried-Over Phase 3 Navigation Fixes (completed in this phase)

The task brief specified wiring working screens into `AppNavigation.kt`:

1. **`AppNavigation.kt`** — added `appContainer: AppContainer?` parameter. When non-null, creates ViewModels via factory for `VocabularyScreen`, `FlashcardScreen`, `ReadingListScreen`, and `PassageScreen`. Legacy no-arg overloads are kept as fallbacks for preview environments.
2. **`MainScaffold.kt`** — added `appContainer: AppContainer?` parameter. When non-null, creates `PracticeHubViewModel` via factory so PracticeHub shows accurate vocab-due counts. Falls back to the legacy overload (which shows static content) when container is absent.
3. **`MainActivity.kt`** — passes `appContainer = container` to `AppNavigation`.
4. **`VocabularyScreen`** — the Phase 3 `VocabularyScreen(viewModel, ...)` overload is now the active route; `VocabularyScreen(hsk, ...)` legacy overload is kept as fallback.
5. **`FlashcardScreen`** — same pattern; real `FlashcardViewModel` with `audioRepository` + `reviewVocabularyUseCase` now wired.
6. **`ReadingListScreen`** → `PassageScreen` route now uses `ReadingListViewModel` and `PassageViewModel` with real repositories.

## Deviations from Spec

1. **`PassageControls.kt` added (not in FOLDER_STRUCTURE.md)** — `PassageScreen.kt` reached 419 lines before split. CLAUDE.md mandates ≤ 300 lines per file; `PassageControls.kt` was created to hold the controls bar and translation card, bringing `PassageScreen.kt` to 267 lines. The file is within the `ui/reading/` package which is the correct home. The architect spec does not prohibit helper files within a package; the 300-line rule is the overriding constraint.

2. **`NoDefinitionSheet.kt` added (not in FOLDER_STRUCTURE.md)** — required by Phase 4 acceptance criterion: "If the character isn't in the vocabulary table (rare), show 'No definition available'". Placed in `ui/components/` alongside `CharacterDefinitionSheet.kt` — the logical home for reusable bottom-sheet components.

3. **`PlayChineseAudioUseCase` returns stub `AudioPlaybackState.Failed`** — per IMPLEMENTATION_PLAN.md Phase 4 note: "in this phase the implementation can return `NotAvailable` until Phase 5 wires Gemini." The PassageScreen "Play all" button is present in `PassageControlsBar` with a `// TODO(phase_5)` comment and does nothing on click.

## Known Issues / TODOs for Later Phases

- `TODO(phase_5)`: Wire `PlayChineseAudioUseCase` to real Gemini TTS for "Play all" button in `PassageControlsBar`.
- `TODO(phase_5)`: "Ask Gemini" button on the `NoDefinitionSheet` is not yet implemented (Phase 5 adds `GeminiService.chat`).
- `TODO(phase_8)`: `HomeScreenTest.kt` — extend with full Home → Reading flow assertions once `HomeScreen` has real data.
- `TODO(phase_10)`: Add full end-to-end flow test with `FakeGeminiService`.

## String Resources Added

- `R.string.reading_pinyin_note` — "HSK 1–3 passages show pinyin above every character. HSK 4–5 show pinyin only on key vocabulary."
- `R.string.reading_empty_title` — "No passages available"
- `R.string.reading_empty_body` — "Reading passages will appear here once imported."
- `R.string.reading_status_read` — "Read"
- `R.string.reading_status_not_read` — "Not yet read"
- `R.string.reading_word_count` — "%1$d characters"
- `R.string.reading_card_content_desc` — "%1$s. %2$d characters. %3$s."
- `R.string.reading_pinyin_toggle_label` — "Pinyin"
- `R.string.reading_pinyin_shown` — "Pinyin shown"
- `R.string.reading_pinyin_hidden` — "Pinyin hidden"
- `R.string.reading_font_scale_desc` — "Font size %1$.1f times"
- `R.string.reading_font_decrease` — "Decrease font size"
- `R.string.reading_font_increase` — "Increase font size"
- `R.string.reading_show_translation` — "Show English translation"
- `R.string.reading_hide_translation` — "Hide English translation"
- `R.string.reading_mark_as_read` — "Mark as read"
- `R.string.reading_already_read` — "Already read"
- `R.string.reading_marked_as_read` — "Marked as read"
- `R.string.reading_no_definition` — "Word not in HSK 1–5 vocabulary list."
- `R.string.preview_passage_title_1` — "Self Introduction"
- `R.string.preview_passage_title_2` — "My Family"
