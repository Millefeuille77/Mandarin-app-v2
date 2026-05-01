# IMPLEMENTATION_PLAN.md — Mandarin Learn

> Build the app in **exactly 10 phases**, in order. Each phase is its own QA gate. The Developer agent must produce one phase per invocation, verify acceptance criteria, then submit a `reports/phase_{N}_report.md`.

All file paths reference `specs/FOLDER_STRUCTURE.md`. All technical choices reference `specs/ARCHITECTURE.md`. All UI elements reference `specs/UX_SPECIFICATION.md`.

---

## Phase 1: Project Setup

### Files to create
- `build.gradle.kts` (root)
- `settings.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `.gitignore` (Android-standard, plus `local.properties`)
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mandarinlearn/MainActivity.kt`
- `app/src/main/java/com/mandarinlearn/MandarinLearnApp.kt`
- `app/src/main/java/com/mandarinlearn/di/AppContainer.kt` (skeleton, to be filled in later phases)
- `app/src/main/java/com/mandarinlearn/navigation/AppNavigation.kt`
- `app/src/main/java/com/mandarinlearn/navigation/Routes.kt`
- `app/src/main/java/com/mandarinlearn/navigation/NavTransitions.kt`
- `app/src/main/java/com/mandarinlearn/ui/MainScaffold.kt`
- `app/src/main/java/com/mandarinlearn/ui/theme/Color.kt`
- `app/src/main/java/com/mandarinlearn/ui/theme/Type.kt`
- `app/src/main/java/com/mandarinlearn/ui/theme/Shapes.kt`
- `app/src/main/java/com/mandarinlearn/ui/theme/Theme.kt`
- `app/src/main/java/com/mandarinlearn/ui/theme/Dimensions.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/MandarinTopBar.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/MandarinBottomNav.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/MandarinPrimaryButton.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/MandarinSecondaryButton.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/HskLevelChip.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/HskLevelChipRow.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/LoadingState.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/EmptyState.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/ErrorState.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/OfflineBanner.kt`
- `app/src/main/java/com/mandarinlearn/ui/components/ConfirmDialog.kt`
- Empty placeholder Composables (each just shows screen name, will be implemented in later phases):
  - `ui/home/HomeScreen.kt`
  - `ui/practice/PracticeHubScreen.kt`
  - `ui/vocabulary/VocabularyScreen.kt`
  - `ui/vocabulary/FlashcardScreen.kt`
  - `ui/reading/ReadingListScreen.kt`
  - `ui/reading/PassageScreen.kt`
  - `ui/listening/ListeningScreen.kt`
  - `ui/speaking/SpeakingScreen.kt`
  - `ui/exam/ExamHubScreen.kt`
  - `ui/exam/ExamScreen.kt`
  - `ui/exam/ExamResultScreen.kt`
  - `ui/me/MeScreen.kt`
  - `ui/progress/ProgressScreen.kt`
  - `ui/settings/SettingsScreen.kt`
  - `ui/importing/ImportLoadingScreen.kt`
- `app/src/main/res/values/strings.xml` (initial set: app name, tab labels, common buttons)
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values/dimens.xml`
- `app/src/main/res/values/styles.xml`
- `app/src/main/res/values-night/colors.xml`
- `app/src/main/res/values-night/themes.xml`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_*.xml` (home, book, edit, person, mic, volume_up, check, close, streak_flame)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/mipmap-*/ic_launcher.png` (5 densities — placeholder asset OK)

### Dependencies needed
Per `ARCHITECTURE.md` §1: kotlin, coroutines, AndroidX core/lifecycle/activity-compose, splashscreen, compose-bom, navigation-compose, material-icons-extended, and test artifacts. No Room or Gemini yet (added in Phases 2 / 5).

### Acceptance criteria
- [ ] `./gradlew assembleDebug` builds with zero warnings (treat warnings as errors via `-Werror` already configured).
- [ ] App installs on an Android 8.0 emulator and launches `MainActivity`.
- [ ] Bottom-nav with 4 tabs (Learn / Practice / Exam / Me) is visible and switching works.
- [ ] Each tab renders its placeholder screen showing only the screen title.
- [ ] No `LiveData` imports anywhere.
- [ ] All bottom-nav items, top-bar back arrows, and any visible buttons are ≥ 56 dp tall, verified via `Modifier.heightIn(min = 56.dp)`.
- [ ] All hex colors come only from `Theme.kt`/`colors.xml` — no inline hex literals.
- [ ] Light + dark themes both render correctly (manual eyeball + automated `composeTestRule.setContent { … }` smoke test).
- [ ] All user-visible strings live in `strings.xml`.
- [ ] `gradle/libs.versions.toml` matches `ARCHITECTURE.md` §1 exactly — version drift fails QA.

### Notes for developer
- The Compose compiler version must be `1.5.8` to match Kotlin `1.9.22`. Mismatch → build failure.
- Configure `composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }` in `app/build.gradle.kts`.
- Lock orientation in manifest: `android:screenOrientation="portrait"` on `MainActivity`.
- Add `-Werror` to Kotlin compile options to keep warnings tight from day one.
- Add `buildFeatures { compose = true; buildConfig = true }`.
- The placeholder screens are fully replaced in later phases — keep them minimal so refactor diffs stay clean.

---

## Phase 2: Data Layer

### Files to create
- All entity files under `data/local/entity/`
- All DAO files under `data/local/dao/`
- `data/local/MandarinLearnDatabase.kt`
- `data/local/Converters.kt`
- `data/local/migrations/MIGRATIONS.kt` (empty list, ready for v2)
- `data/local/import/JsonImporter.kt`
- `data/local/import/ImportProgress.kt`
- All DTO files under `data/local/import/dto/`
- All domain models under `domain/model/`
- All repositories under `data/repository/` (only the ones not requiring Gemini: VocabularyRepository, ReadingRepository, ExamRepository, ProgressRepository, StreakRepository, plus stubs for AudioRepository / SpeakingRepository / ListeningRepository to be filled in Phases 5–6)
- `data/preferences/PreferencesKeys.kt`
- `data/preferences/UserPreferencesRepository.kt`
- `util/DateUtil.kt`
- `util/HashUtil.kt`
- `util/DispatcherProvider.kt`
- `util/Logger.kt`
- `ui/importing/ImportLoadingScreen.kt` (full implementation)
- `ui/importing/ImportLoadingViewModel.kt`
- Update `di/AppContainer.kt` to construct DB, repositories, JsonImporter.
- Update `MainActivity.kt` / `AppNavigation.kt` to gate on import status.
- Copy all 18 JSON files from `data/**` into `app/src/main/res/raw/` (filenames must match `ARCHITECTURE.md` §3.2 list).
- Tests:
  - `androidTest/data/local/MandarinLearnDatabaseTest.kt`
  - `androidTest/data/local/VocabularyDaoTest.kt`
  - `androidTest/data/local/ExamResultDaoTest.kt`
  - `androidTest/data/local/import/JsonImporterTest.kt`

### Dependencies needed
- `room-runtime`, `room-ktx`, `room-compiler` (KSP), `kotlinx-serialization-json`, `androidx-datastore-preferences`. Apply the `kotlinx-serialization` plugin to `app/build.gradle.kts`.

### Acceptance criteria
- [ ] All Room entities compile and `MandarinLearnDatabase` opens on a fresh device.
- [ ] `JsonImporter.importIfNeeded()` populates all tables from `res/raw/` on first launch.
- [ ] Counts per table match the JSON inputs (HSK 1: 153 vocab; HSK 1: 8 readings; tone drills: 20; conversation phrases: 100; sample questions: 115; exam structures: 5).
- [ ] Variable section count is preserved: querying `exam_structures.sections_json` for HSK 2 returns 2 sections; for HSK 5 returns 3 sections.
- [ ] Pinyin tone marks survive the round-trip — randomly sample 10 vocab rows and assert each pinyin field matches the source JSON byte-for-byte.
- [ ] `ImportLoadingScreen` shows a progress bar and finishes within 8 seconds on a Pixel 5 emulator.
- [ ] On second launch, `data_version` check skips re-import in < 100 ms.
- [ ] Retry button works if a forced exception is thrown mid-import.
- [ ] Unit + instrumented tests pass.
- [ ] No `runBlocking` in production code.

### Notes for developer
- Use `Json { ignoreUnknownKeys = true; explicitNulls = false }` so future schema additions don't break parsing.
- `Json.decodeFromStream` requires `kotlinx-serialization-json` 1.6+ — pinned at 1.6.2.
- The HSK 3 vocab file has 4 known duplicate IDs per `qa_research.md`. Treat them as duplicates: `OnConflictStrategy.IGNORE` keeps the first occurrence, which matches the source order. Log a warning.
- Per QA: HSK 4 has 310 / 600 entries, HSK 5 has 300 / 1300 entries. The schema is unaffected; UI correctly displays whatever counts are imported. No special-casing.
- `data_version` is stored as four counters (vocabulary / reading / audio / exam) — Phase 2 sets all to `1`.
- On first launch, also seed `user_progress` rows for every (hsk_level 1–5) × (section in {vocabulary, reading, listening, speaking, exam}) — `total_items` = corresponding count, `completed_items` = 0. Without this, dashboard divisions are nonsense.

---

## Phase 3: Vocabulary Section

### Files to create
- `domain/srs/SrsScheduler.kt` (SM-2 implementation per `ARCHITECTURE.md` §5.3)
- `domain/srs/SrsQuality.kt`
- `domain/usecase/ReviewVocabularyUseCase.kt`
- `data/repository/VocabularyRepository.kt` (full implementation, replacing Phase 2 stub if applicable)
- `ui/vocabulary/VocabularyScreen.kt` (full implementation)
- `ui/vocabulary/VocabularyViewModel.kt`
- `ui/vocabulary/VocabularyUiState.kt`
- `ui/vocabulary/FlashcardScreen.kt` (full implementation)
- `ui/vocabulary/FlashcardViewModel.kt`
- `ui/vocabulary/FlashcardUiState.kt`
- `ui/practice/PracticeHubScreen.kt` (full implementation — link cards to Vocabulary / Reading / Listening / Speaking)
- `ui/practice/PracticeHubViewModel.kt`
- `ui/components/CharacterDefinitionSheet.kt` (used in vocab detail bottom sheet)
- Tests:
  - `test/domain/srs/SrsSchedulerTest.kt` (≥ 12 cases covering all q values, lapse, interval growth, ease-floor)
  - `test/data/repository/VocabularyRepositoryTest.kt`
  - `test/viewmodel/FlashcardViewModelTest.kt`
  - `androidTest/ui/FlashcardScreenTest.kt`

### Dependencies needed
None (uses only Phase 1 + 2 artifacts).

### Acceptance criteria
- [ ] `SrsScheduler.review(...)` matches the formula in `ARCHITECTURE.md` §5.3 byte-for-byte. Reference cases:
  - First "Good" review on a new card → interval = 1, reps = 1, ef ≥ 2.5.
  - Second "Good" → interval = 6, reps = 2.
  - Third "Good" → interval = 15 (= ceil(6 × ef)), reps = 3, ef increases.
  - "Forgot" on any card → reps reset to 0, interval = 1, ef floor 1.3 honoured.
  - "Hard" applies the 0.8× penalty.
- [ ] `VocabularyScreen` displays all words for the selected level, search debounces, status badges (New/Learning/Mastered) align with the §5.5 definition.
- [ ] `FlashcardScreen` shows due cards first, then up to `newCardsLimit` new cards (default 10).
- [ ] Card flip is achievable via tap on card AND via "Show answer" button (no swipe-only).
- [ ] After rating a card, the row in Room is updated correctly (verified by re-querying).
- [ ] End-of-session screen appears when the queue is empty.
- [ ] All four rating buttons show the "next interval" label.
- [ ] TalkBack reads back-of-card content automatically when revealed.
- [ ] All tests pass.

### Notes for developer
- The SrsScheduler is **pure** — no Android imports. This makes unit tests trivial.
- Card flip animation: use `graphicsLayer { rotationY = … }` with two halves; respect Settings → "Reduce motion" by skipping rotation and crossfading instead.
- Audio play button on flashcards calls `AudioRepository.play(text)` — but `AudioRepository` is a stub returning `Result.failure(GeminiError.NoApiKey)` until Phase 5. The flashcard screen must handle that failure silently (button does nothing, snackbar "Audio coming soon" shown).
- Search uses Room `LIKE '%query%'` against `character`, `pinyin`, and `translation`. Index those columns if performance is poor on HSK 5 (300 rows — likely fine without).

---

## Phase 4: Reading Section

### Files to create
- `domain/usecase/PlayChineseAudioUseCase.kt` (TTS-or-cache; in this phase the implementation can return `NotAvailable` until Phase 5 wires Gemini — alternatively, fall back to Android `TextToSpeech` from the start to avoid a dead button)
- `data/repository/ReadingRepository.kt` (full)
- `ui/reading/ReadingListScreen.kt` (full)
- `ui/reading/ReadingListViewModel.kt`
- `ui/reading/ReadingListUiState.kt`
- `ui/reading/PassageScreen.kt` (full)
- `ui/reading/PassageViewModel.kt`
- `ui/reading/PassageUiState.kt`
- `ui/components/PinyinText.kt` (the ruby-text composable per UX §3.6)
- `ui/components/PinyinAnnotationModels.kt`
- Tests:
  - `test/data/repository/ReadingRepositoryTest.kt` (verify pinyin_annotations parse correctly for HSK 1 vs HSK 4)
  - `androidTest/ui/HomeScreenTest.kt` (smoke — actually tests only Home placeholder until Phase 8; spawn it now since the file is enumerated in FolderStructure)

### Dependencies needed
None.

### Acceptance criteria
- [ ] `PinyinText` correctly stacks pinyin above each hanzi for HSK 1–3 passages (every char has pinyin) and only above key vocab for HSK 4–5.
- [ ] Tapping a hanzi opens `CharacterDefinitionSheet`. If the character is in `vocabulary`, full info shows; otherwise a graceful "No local definition" message appears.
- [ ] Punctuation characters are rendered but not tappable; their TalkBack semantics is empty.
- [ ] Font size slider live-updates rendering between 0.8× and 1.6×.
- [ ] Pinyin toggle hides/shows the pinyin row instantly.
- [ ] "Mark as read" toggles `is_completed` and updates `user_progress`.
- [ ] Loading and error states render correctly.
- [ ] FlowRow wraps long passages without overflow on a 360 dp wide screen.
- [ ] Tests pass.

### Notes for developer
- Use `androidx.compose.foundation.layout.FlowRow` (Compose 1.4+ stable as of BOM 2024.02). The lazy column wraps each "line" of FlowRow; for very long passages, manually break on `\n` or punctuation+space sequences.
- The pinyin/hanzi pair is a `Column { Text(pinyin); Text(hanzi) }` with `Modifier.minSize(width=48.dp, height=56.dp)` to satisfy touch-target rule.
- TalkBack: set `Modifier.semantics { contentDescription = chinese_text }` on the outer container so the entire passage can be read in one swipe — and use `Modifier.semantics(mergeDescendants = false)` on each character cell for tap navigation.
- "Reduce motion" affects nothing on this screen — no animations beyond the bottom-sheet default.

---

## Phase 5: Listening Section (Gemini TTS integration)

### Files to create
- `data/remote/GeminiService.kt` (full implementation — TTS + chat methods; STT stub thrown in this phase, fully wired in Phase 6)
- `data/remote/GeminiError.kt`
- `data/remote/GeminiPrompts.kt`
- `data/remote/AudioBlob.kt`
- `data/remote/BackoffPolicy.kt`
- `data/audio/AudioPlayer.kt`
- `data/audio/AndroidTtsFallback.kt`
- `data/repository/AudioRepository.kt` (full — checks cache → Gemini → fallback to Android TTS)
- `data/repository/ListeningRepository.kt` (full)
- `util/NetworkMonitor.kt`
- `ui/listening/ListeningScreen.kt` (full)
- `ui/listening/ListeningViewModel.kt`
- `ui/listening/ListeningUiState.kt`
- Wire flashcard audio button to the now-real `AudioRepository`.
- Add `INTERNET` and `ACCESS_NETWORK_STATE` permissions to `AndroidManifest.xml` (if not added in Phase 1).
- Add `BuildConfig.GEMINI_API_KEY` field in `app/build.gradle.kts`.
- Tests:
  - `test/data/remote/BackoffPolicyTest.kt`
  - `androidTest/data/local/AudioCacheDaoTest.kt` (or merge into existing DAO test file)
  - Mocked test for `AudioRepository`: cache hit returns instantly; cache miss with online calls Gemini; offline returns failure.

### Dependencies needed
- `google-generative-ai` (`com.google.ai.client.generativeai:generativeai:0.2.2`)

### Acceptance criteria
- [ ] `BuildConfig.GEMINI_API_KEY` is wired via `local.properties`. If `local.properties` is absent or the key is empty, the app builds but `GeminiService.synthesize` returns `Result.failure(GeminiError.NoApiKey)`.
- [ ] TTS output is cached in `audio_cache`. Repeated calls for the same text don't hit the network — verified by counting Gemini calls in a mocked test.
- [ ] Cache eviction runs on app start when `totalBytes() > 50 MB`.
- [ ] Network timeout is exactly 30 s.
- [ ] HTTP 429 triggers exponential backoff: initial 1 s, multiplier 2, max 3 attempts.
- [ ] Offline mode: ListeningScreen still works for cached audio; uncached questions show the "Audio unavailable" inline warning and allow Skip.
- [ ] Multiple-choice quiz: 4 options, one selectable, immediate feedback overlay (color + icon + text).
- [ ] Replay limit per question is 3.
- [ ] All TalkBack announcements specified in UX §4 Screen 7 are implemented.
- [ ] No API key string appears anywhere in source code.

### Notes for developer
- The Google AI SDK's TTS support has been gated; if at integration time the `responseMimeType = "audio/mpeg"` route is unavailable, fall through to `AndroidTtsFallback` (uses `android.speech.tts.TextToSpeech` with `Locale.SIMPLIFIED_CHINESE`). The user never knows; the app still speaks Chinese.
- `AudioRepository.play(text)` returns `Flow<AudioPlaybackState>` so the UI can show "loading → playing → finished".
- `BackoffPolicy.retry(maxAttempts = 3, initialDelay = 1.seconds, multiplier = 2.0)` is reusable for STT later.
- Cache key is `sha256("$text|$voice|${"%.2f".format(speed)}")` — the speed value is rounded to 2 decimals to keep keys stable across slider stops.
- Listening questions are pulled from `sample_questions` filtered by `(hsk_level, section = "listening")`. For HSK 1 the existing 10 questions are sufficient; bigger sets come from QA's known data gaps and are not in scope here.

---

## Phase 6: Speaking Section (Gemini STT)

### Files to create
- `data/audio/AudioRecorder.kt`
- `data/repository/SpeakingRepository.kt` (full)
- `data/repository/AudioRepository.kt` — extend with `transcribeAndScore`.
- `domain/usecase/ScorePronunciationUseCase.kt`
- `util/PermissionsHelper.kt`
- Update `GeminiService.kt`: implement `transcribeAndScore`.
- `ui/speaking/SpeakingScreen.kt` (full)
- `ui/speaking/SpeakingViewModel.kt`
- `ui/speaking/SpeakingUiState.kt`
- Tests:
  - `test/viewmodel/SpeakingViewModelTest.kt` (mocked Gemini)
  - Manual / instrumented test for permission flow.

### Dependencies needed
- `RECORD_AUDIO` permission added to `AndroidManifest.xml`.

### Acceptance criteria
- [ ] First entry to `SpeakingScreen` triggers the custom permission rationale dialog (UX §5.4) before requesting `RECORD_AUDIO`.
- [ ] Recording uses `MediaRecorder` with format `AAC_LC`, 16 kHz, mono, max 10 seconds.
- [ ] Recording auto-stops at 10 seconds with a snackbar "Recording stopped — 10 second limit".
- [ ] Tap the mic again to stop early.
- [ ] After stop, audio file is sent to `GeminiService.transcribeAndScore`. UI shows "Processing…" until response or 30 s timeout.
- [ ] Score 0–100 is displayed in display-large; color band is correct (≥85 success, 70–84 primary, <70 warning); feedback in body-large.
- [ ] Try-again button erases the last attempt and re-arms the mic.
- [ ] Next-phrase pulls a new conversation phrase at the same HSK level.
- [ ] Offline → ErrorState card "You're offline. Speaking practice needs internet."
- [ ] No audio file persists on disk after evaluation (cleaned up in `viewModelScope` `finally`).
- [ ] All TalkBack announcements specified in UX §4 Screen 6 are implemented.

### Notes for developer
- Per the Gemini SDK at version 0.2.2, audio uploads are via the multimodal `generateContent` route (`Content.Builder { add(BlobPart(...)) }`). Wrap this so that if the SDK rejects the audio mime, the failure surfaces as `GeminiError.Unknown`.
- For pronunciation scoring, the system prompt asks Gemini to compare the transcription to the expected text and return strict JSON: `{"transcription": "...", "score": 0..100, "feedback": "..."}`. Use response-MIME-type JSON if the SDK supports it; otherwise parse the text response leniently.
- Microphone permission denial path: render a persistent inline message and a "Open settings" button that fires `ACTION_APPLICATION_DETAILS_SETTINGS` intent.
- The audio file is created in `context.cacheDir` and deleted after the API response (success or failure).

---

## Phase 7: Exam Section

### Files to create
- `domain/grading/ExamGrader.kt` (per-section + total + pass/fail; honors HSK 1–2 = 200/120 vs HSK 3–5 = 300/180 by reading from `exam_structures` row)
- `domain/usecase/StartExamUseCase.kt`
- `domain/usecase/SubmitExamUseCase.kt`
- `data/repository/ExamRepository.kt` (full)
- `ui/exam/ExamHubScreen.kt` (full — HSK level selector with start buttons)
- `ui/exam/ExamHubViewModel.kt`
- `ui/exam/ExamScreen.kt` (full)
- `ui/exam/ExamViewModel.kt`
- `ui/exam/ExamUiState.kt`
- `ui/exam/ExamResultScreen.kt` (full)
- `ui/exam/ExamResultViewModel.kt`
- `ui/exam/ExamResultUiState.kt`
- `ui/components/ScoreBadge.kt`
- `ui/components/ProgressBarLabeled.kt`
- Tests:
  - `test/domain/grading/ExamGraderTest.kt` (fixtures for HSK 1 and HSK 5 with full and partial answer sets)
  - `test/data/repository/ExamRepositoryTest.kt`
  - `test/viewmodel/ExamViewModelTest.kt`
  - `androidTest/data/local/ExamResultDaoTest.kt` (already in Phase 2; extend if needed)
  - `androidTest/ui/ExamScreenTest.kt`

### Dependencies needed
None.

### Acceptance criteria
- [ ] `ExamGrader` correctly tallies per-section scores and totals. For HSK 1: max 200, pass 120; for HSK 3: max 300, pass 180. The schema-driven approach means no level-specific code paths.
- [ ] Variable section count (2 sections HSK 1–2 vs 3 sections HSK 3–5) renders without UI hard-codes — `ExamScreen` iterates `sections_json` from the structure row.
- [ ] Timer counts down correctly using a `flow { emit; delay(1.seconds) }` loop. Stops on submit / quit / timeout.
- [ ] At < 5 minutes timer goes warning color; at < 1 minute goes error color; at 0 the exam auto-submits.
- [ ] Cannot navigate to previous question (verified by `BackHandler` showing the confirm-quit dialog).
- [ ] Section transitions show a 30-second break overlay with a "Continue" button to skip wait.
- [ ] On submit, `exam_results` row is inserted with full per-section JSON and answers JSON.
- [ ] `ExamResultScreen` correctly shows pass/fail, per-section bars, mistakes count, and history. The Review-mistakes list reads `answers_json` to flag wrong answers.
- [ ] "Try again" on result screen navigates to a fresh `ExamScreen` (fresh `started_at`).
- [ ] All ListeningScreen audio in exam-listening mode reuses the cache from Phase 5 — no duplicate cache entries for the same text.
- [ ] All tests pass.

### Notes for developer
- `ExamScreen` listening questions: reuse the `AudioRepository.play(text)` from Phase 5. The 2-replay limit is enforced by the ViewModel.
- For HSK 3+ writing questions where the format is "fill in the pinyin" or "reorder words", render a simple `OutlinedTextField` for free-text answers (graded on exact-match, lower-cased, whitespace-trimmed). Out-of-scope: Gemini scoring of essays.
- QA flagged 15 sample-question shortfalls (HSK 3 reading -5, HSK 3 writing -5, HSK 4 writing -3, HSK 5 writing -2). The exam runner gracefully handles fewer-than-spec'd questions: `getQuestionsForExam(level, section, limit)` returns up to `limit`, and the ExamScreen renders only what it gets. The total_score scales proportionally to the number of questions presented (`grader.scaleSection(correct, presented, max=100)`).
- Persist the in-progress exam to a `SavedStateHandle` so a process death mid-exam doesn't lose answers (best-effort; if it fails, that's acceptable).

---

## Phase 8: Progress & Dashboard

### Files to create
- `domain/readiness/ReadinessCalculator.kt`
- `domain/usecase/GetDashboardUseCase.kt`
- `data/repository/ProgressRepository.kt` (full — replaces Phase 2 stub)
- `data/repository/StreakRepository.kt` (full)
- `ui/home/HomeScreen.kt` (full implementation, replacing placeholder)
- `ui/home/HomeViewModel.kt`
- `ui/home/HomeUiState.kt`
- `ui/me/MeScreen.kt` (full — links to Progress + Settings)
- `ui/me/MeViewModel.kt`
- `ui/progress/ProgressScreen.kt` (full)
- `ui/progress/ProgressViewModel.kt`
- `ui/progress/ProgressUiState.kt`
- `ui/progress/ExamScoresChart.kt` (Compose Canvas)
- Tests:
  - `test/domain/readiness/ReadinessCalculatorTest.kt`
  - `test/viewmodel/HomeViewModelTest.kt`
  - `androidTest/ui/HomeScreenTest.kt` (extend Phase 4 file)

### Dependencies needed
None.

### Acceptance criteria
- [ ] `HomeScreen` displays correct streak, due count, per-HSK progress bars sourced from real Room queries.
- [ ] Streak rollover logic — implementation follows UX §5.2 (the authoritative source) and matches:
  - Activity today + last_active==today → no-op.
  - Activity today + last_active==today−1 → streak++.
  - Else → streak = 1. `longest_streak` updates accordingly.
- [ ] "Today's review" CTA correctly routes to `FlashcardScreen` for the user's current focus level (default the lowest HSK level with any due card).
- [ ] `ProgressScreen` per-level cards show vocab mastered % (matches §5.5 mastered-card definition), reading completed %, best-exam %, and a readiness percentage.
- [ ] `ReadinessCalculator` matches the formula in UX §4 Screen 10:
  `readiness = 0.4 * vocab_mastered_pct + 0.2 * reading_done_pct + 0.4 * best_exam_pct`, clamped 0..100.
- [ ] Exam scores chart draws correctly with 0, 1, 2, and 5+ data points. Empty state shows the placeholder text.
- [ ] Chart points are tappable and route to the corresponding `ExamResultScreen`.
- [ ] All TalkBack annotations in UX §4 Screens 1 and 10 are implemented.

### Notes for developer
- The chart is plain Canvas. Don't pull in MPAndroidChart; we have ≤ ~30 points to draw.
- For the readiness "best_exam_pct" component: best_exam_pct = (best_total_score_for_level / total_max_score_for_level) × 100. If no exam taken at that level → 0.
- Streak update is centralised in `StreakRepository.recordActivity(today)` and called from: FlashcardViewModel after rating any card; PassageViewModel after marking read; ListeningViewModel after answering any question; SpeakingViewModel after a successful score; ExamViewModel after submit.
- Home's "lowest level with due cards" lookup: query `vocabulary` for `min(hsk_level)` where `next_review_date <= today AND is_introduced = 1`. If none, default to HSK 1 with new-cards-only mode.

---

## Phase 9: Settings & Polish

### Files to create
- `ui/settings/SettingsScreen.kt` (full)
- `ui/settings/SettingsViewModel.kt`
- `ui/settings/SettingsUiState.kt`
- `data/repository/SettingsRepository.kt` (DataStore-backed, full)
- `domain/usecase/ExportProgressUseCase.kt`
- `domain/usecase/ImportProgressUseCase.kt`
- `domain/usecase/ResetProgressUseCase.kt`
- `util/FileExportHelper.kt`
- Tests:
  - `test/viewmodel/SettingsViewModelTest.kt`
  - `test/data/repository/ExportImportRoundTripTest.kt` (export → wipe → import → assert equality)

### Dependencies needed
None.

### Acceptance criteria
- [ ] Theme switch (System / Light / Dark) immediately re-themes the app.
- [ ] Font multiplier slider applies live across all screens (use a `LocalFontScale` `CompositionLocal` plumbed through `MandarinLearnTheme`).
- [ ] Audio playback speed chips persist via DataStore and are honored by `AudioRepository.play`.
- [ ] Pinyin default switch persists and is read by `PassageViewModel.init`.
- [ ] Daily new cards limit slider persists and is read by `FlashcardViewModel.init`.
- [ ] Export writes a valid JSON file via Storage Access Framework; reading it back produces identical SRS state and exam history.
- [ ] Import shows confirm dialog, validates schema, applies cleanly. On invalid file, snackbar "File could not be read".
- [ ] Reset all progress: confirm dialog, on confirm, all `vocabulary.is_introduced` set to 0, SM-2 fields defaults, `exam_results` truncated, `streak` reset, `user_progress.completed_items` zeroed. Content tables (vocab content, readings, exam structures, sample questions) untouched.
- [ ] About section shows app version (read from `BuildConfig.VERSION_NAME`) and Gemini key set/unset indicator.
- [ ] Final accessibility audit per UX §6 passes on all 11 screens — every checklist item ticked in `qa_reports/qa_dev_phase_9.md`.

### Notes for developer
- Plumb font scale via a `CompositionLocal<Float>` rather than a recompose-the-world state: place it on `MandarinLearnTheme` and multiply at the `Type.kt` level.
- Reduce-motion preference also lives here; consume via `LocalReduceMotion`.
- "Reset all progress" calls a transactional Room operation that touches only the user-state columns. It does NOT delete content rows. After reset, `data_version` is left untouched (so we don't trigger re-import).
- File export schema:
  ```json
  {
    "version": 1,
    "exported_at": "2026-05-01T...",
    "vocabulary": [{"id":"hsk1_001","ease":2.5,...}, ...],
    "exam_results": [...],
    "streak": {...},
    "user_progress": [...]
  }
  ```
- Validate `version == 1` on import; reject otherwise with a clear message.

---

## Phase 10: Integration & Testing

### Files to create
- `README.md` (root) — setup instructions, how to run, how to set the API key, target Android version, third-party deps with versions.
- `androidTest/ui/HomeScreenTest.kt` (extended)
- `androidTest/ui/FlashcardScreenTest.kt` (extended)
- `androidTest/ui/ExamScreenTest.kt` (extended)
- `androidTest/integration/EndToEndFlowTest.kt` (smoke flow: import → review one card → take HSK 1 mock with 4 questions stubbed → see result)
- Crash/edge-case patches as needed across earlier files. Document each patch in `reports/phase_10_report.md`.

### Dependencies needed
- `mockk`, `turbine`, `robolectric`, `compose-ui-test-junit4`, `compose-ui-test-manifest` — should already be in the catalog from Phase 1; ensure they're applied in `app/build.gradle.kts` `androidTestImplementation`/`testImplementation` configs.

### Acceptance criteria
- [ ] All unit + instrumented tests pass on a Pixel 5 API 26 emulator AND on API 34. Test coverage of `domain/` is ≥ 80 % (line).
- [ ] End-to-end flow test passes: cold start → import → home → vocab → flashcard rate → home shows updated streak.
- [ ] All accessibility checks from UX §6 pass on every screen — verified manually with TalkBack.
- [ ] No crashes during a 5-minute exploratory session. Logcat shows no `WARN`/`ERROR` from app code in normal operation.
- [ ] Cold start to interactive Home: ≤ 2 seconds on Pixel 5 emulator (post-import).
- [ ] First-launch import completes in ≤ 8 seconds.
- [ ] Memory: heap stays under 200 MB during typical usage.
- [ ] APK size: release build < 25 MB.
- [ ] README documents:
  1. Required environment (Android Studio Hedgehog or newer, JDK 17, SDK 34).
  2. How to populate `local.properties` with `GEMINI_API_KEY`.
  3. How to run unit tests (`./gradlew test`) and instrumented (`./gradlew connectedAndroidTest`).
  4. Known data limitations from `qa_research.md`.
  5. Project tech stack matching `ARCHITECTURE.md` §1.
- [ ] Lint: `./gradlew lintDebug` reports zero errors and ≤ 20 warnings (no warnings in Kotlin code).
- [ ] All every-file ≤ 300 lines rule verified by a CI script — no exceptions.

### Notes for developer
- This phase is "everything that was deferred". Common pickups: empty-state polish, error-message rewording, animation tweaks, accessibility traversal-order fixes.
- The end-to-end test uses `Robolectric` for fast iteration — use real Room (in-memory) and a `FakeGeminiService` returning canned responses.
- Run `lint` early in the phase; it commonly flags non-null `contentDescription` issues which we want to surface.
- Bump `versionName = "1.0.0"` and `versionCode = 1` in `app/build.gradle.kts` when the phase completes.

---

## Cross-phase guarantees (every phase QA gate must verify)

- All dependency versions match `ARCHITECTURE.md` §1 exactly. No `latest`, no `+`.
- Every screen named in `UX_SPECIFICATION.md` has the matching files in `FOLDER_STRUCTURE.md`.
- 56 dp touch targets, 18 sp body text, no `LiveData`, no hardcoded strings, no hardcoded API key.
- HSK grading uses 0–200 (HSK 1–2) or 0–300 (HSK 3–5) scale per the structure data — never percentages.
- Navigation depth ≤ 3 taps from any bottom-nav root to content.
- SM-2 implementation matches `ARCHITECTURE.md` §5.3 exactly.
- Gemini API: 30 s timeout, offline-cache fallback, rate-limit retry, `BuildConfig.GEMINI_API_KEY` only.
- Every file ≤ 300 lines.
