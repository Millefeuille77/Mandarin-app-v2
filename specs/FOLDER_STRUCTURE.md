# FOLDER_STRUCTURE.md — Mandarin Learn

> Complete file tree the Developer agent must produce. **Every file the project will ever contain is listed here.** Do not create files outside this tree without an Architect re-spec.

The 11 screens spec'd in `UX_SPECIFICATION.md` (HomeScreen, VocabularyScreen, FlashcardScreen, ReadingListScreen, PassageScreen, SpeakingScreen, ListeningScreen, ExamScreen, ExamResultScreen, ProgressScreen, SettingsScreen) each appear as both a `<Name>Screen.kt` Composable file and a `<Name>ViewModel.kt`. The four hub/host screens that organise navigation (PracticeHubScreen, ExamHubScreen, MeScreen, ImportLoadingScreen) also appear here.

---

## Top-level

```
mandarin_app_v2/
├── CLAUDE.md
├── build.gradle.kts                 # root project build
├── settings.gradle.kts
├── gradle.properties
├── local.properties                 # gitignored, holds GEMINI_API_KEY
├── .gitignore
├── README.md                        # created in Phase 10
├── gradle/
│   ├── libs.versions.toml           # version catalog (single source for all dep versions)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── data/                            # Research-agent JSON (already exists)
├── specs/                           # Architect-agent output (this folder)
├── reports/                         # Developer phase reports
├── qa_reports/                      # QA reports
└── app/                             # Android module (everything below)
```

---

## App module

```
app/
├── build.gradle.kts
├── proguard-rules.pro
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/mandarinlearn/
    │   │   ├── MainActivity.kt
    │   │   ├── MandarinLearnApp.kt                  # Application subclass; owns AppContainer
    │   │   ├── di/
    │   │   │   └── AppContainer.kt                  # Manual DI: DB, repos, Gemini, dispatchers
    │   │   ├── navigation/
    │   │   │   ├── AppNavigation.kt                 # NavHost graph
    │   │   │   ├── Routes.kt                        # Sealed class with typed routes & arg builders
    │   │   │   └── NavTransitions.kt                # Shared enter/exit animations
    │   │   ├── ui/
    │   │   │   ├── MainScaffold.kt                  # Bottom-nav scaffold, hosts the 4 tabs
    │   │   │   ├── theme/
    │   │   │   │   ├── Color.kt
    │   │   │   │   ├── Type.kt
    │   │   │   │   ├── Shapes.kt
    │   │   │   │   ├── Theme.kt                     # MandarinLearnTheme + MaterialTheme wrap
    │   │   │   │   └── Dimensions.kt                # Spacing, touch-target, elevation tokens
    │   │   │   ├── components/
    │   │   │   │   ├── MandarinTopBar.kt
    │   │   │   │   ├── MandarinBottomNav.kt
    │   │   │   │   ├── MandarinPrimaryButton.kt
    │   │   │   │   ├── MandarinSecondaryButton.kt
    │   │   │   │   ├── HskLevelChip.kt
    │   │   │   │   ├── HskLevelChipRow.kt           # row of 5 chips with state hoist
    │   │   │   │   ├── PinyinText.kt                # ruby-style pinyin/hanzi composable
    │   │   │   │   ├── PinyinAnnotationModels.kt    # PinyinAnnotation data class
    │   │   │   │   ├── LoadingState.kt
    │   │   │   │   ├── EmptyState.kt
    │   │   │   │   ├── ErrorState.kt
    │   │   │   │   ├── OfflineBanner.kt
    │   │   │   │   ├── CharacterDefinitionSheet.kt  # bottom sheet for tap-to-define
    │   │   │   │   ├── ScoreBadge.kt
    │   │   │   │   ├── ProgressBarLabeled.kt
    │   │   │   │   └── ConfirmDialog.kt
    │   │   │   ├── home/
    │   │   │   │   ├── HomeScreen.kt
    │   │   │   │   ├── HomeViewModel.kt
    │   │   │   │   └── HomeUiState.kt
    │   │   │   ├── practice/
    │   │   │   │   ├── PracticeHubScreen.kt
    │   │   │   │   ├── PracticeHubViewModel.kt
    │   │   │   │   └── PracticeHubUiState.kt
    │   │   │   ├── vocabulary/
    │   │   │   │   ├── VocabularyScreen.kt
    │   │   │   │   ├── VocabularyViewModel.kt
    │   │   │   │   ├── VocabularyUiState.kt
    │   │   │   │   ├── FlashcardScreen.kt
    │   │   │   │   ├── FlashcardViewModel.kt
    │   │   │   │   └── FlashcardUiState.kt
    │   │   │   ├── reading/
    │   │   │   │   ├── ReadingListScreen.kt
    │   │   │   │   ├── ReadingListViewModel.kt
    │   │   │   │   ├── ReadingListUiState.kt
    │   │   │   │   ├── PassageScreen.kt
    │   │   │   │   ├── PassageViewModel.kt
    │   │   │   │   └── PassageUiState.kt
    │   │   │   ├── listening/
    │   │   │   │   ├── ListeningScreen.kt
    │   │   │   │   ├── ListeningViewModel.kt
    │   │   │   │   └── ListeningUiState.kt
    │   │   │   ├── speaking/
    │   │   │   │   ├── SpeakingScreen.kt
    │   │   │   │   ├── SpeakingViewModel.kt
    │   │   │   │   └── SpeakingUiState.kt
    │   │   │   ├── exam/
    │   │   │   │   ├── ExamHubScreen.kt
    │   │   │   │   ├── ExamHubViewModel.kt
    │   │   │   │   ├── ExamHubUiState.kt
    │   │   │   │   ├── ExamScreen.kt
    │   │   │   │   ├── ExamViewModel.kt
    │   │   │   │   ├── ExamUiState.kt
    │   │   │   │   ├── ExamResultScreen.kt
    │   │   │   │   ├── ExamResultViewModel.kt
    │   │   │   │   └── ExamResultUiState.kt
    │   │   │   ├── me/
    │   │   │   │   ├── MeScreen.kt
    │   │   │   │   ├── MeViewModel.kt
    │   │   │   │   └── MeUiState.kt
    │   │   │   ├── progress/
    │   │   │   │   ├── ProgressScreen.kt
    │   │   │   │   ├── ProgressViewModel.kt
    │   │   │   │   ├── ProgressUiState.kt
    │   │   │   │   └── ExamScoresChart.kt           # Compose Canvas line chart
    │   │   │   ├── settings/
    │   │   │   │   ├── SettingsScreen.kt
    │   │   │   │   ├── SettingsViewModel.kt
    │   │   │   │   └── SettingsUiState.kt
    │   │   │   └── importing/
    │   │   │       ├── ImportLoadingScreen.kt
    │   │   │       ├── ImportLoadingViewModel.kt
    │   │   │       └── ImportLoadingUiState.kt
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── HskLevel.kt
    │   │   │   │   ├── VocabularyWord.kt
    │   │   │   │   ├── ReadingPassage.kt
    │   │   │   │   ├── ConversationPhrase.kt
    │   │   │   │   ├── ToneDrill.kt
    │   │   │   │   ├── ExamStructure.kt
    │   │   │   │   ├── ExamSection.kt
    │   │   │   │   ├── SectionScore.kt
    │   │   │   │   ├── SampleQuestion.kt
    │   │   │   │   ├── ExamResult.kt
    │   │   │   │   ├── AnswerRecord.kt
    │   │   │   │   ├── PronunciationResult.kt
    │   │   │   │   ├── PinyinAnnotation.kt
    │   │   │   │   ├── UserProgress.kt
    │   │   │   │   ├── Streak.kt
    │   │   │   │   └── SrsSession.kt
    │   │   │   ├── srs/
    │   │   │   │   ├── SrsScheduler.kt              # SM-2 implementation
    │   │   │   │   └── SrsQuality.kt                # enum: FORGOT, HARD, GOOD, EASY
    │   │   │   ├── grading/
    │   │   │   │   └── ExamGrader.kt                # tally per-section + total + pass/fail
    │   │   │   ├── readiness/
    │   │   │   │   └── ReadinessCalculator.kt
    │   │   │   └── usecase/
    │   │   │       ├── GetDashboardUseCase.kt
    │   │   │       ├── ReviewVocabularyUseCase.kt
    │   │   │       ├── StartExamUseCase.kt
    │   │   │       ├── SubmitExamUseCase.kt
    │   │   │       ├── ScorePronunciationUseCase.kt
    │   │   │       ├── PlayChineseAudioUseCase.kt
    │   │   │       ├── ExportProgressUseCase.kt
    │   │   │       ├── ImportProgressUseCase.kt
    │   │   │       └── ResetProgressUseCase.kt
    │   │   ├── data/
    │   │   │   ├── local/
    │   │   │   │   ├── MandarinLearnDatabase.kt
    │   │   │   │   ├── Converters.kt
    │   │   │   │   ├── entity/
    │   │   │   │   │   ├── VocabularyEntity.kt
    │   │   │   │   │   ├── ReadingEntity.kt
    │   │   │   │   │   ├── ConversationPhraseEntity.kt
    │   │   │   │   │   ├── ToneDrillEntity.kt
    │   │   │   │   │   ├── ExamStructureEntity.kt
    │   │   │   │   │   ├── SampleQuestionEntity.kt
    │   │   │   │   │   ├── ExamResultEntity.kt
    │   │   │   │   │   ├── AudioCacheEntity.kt
    │   │   │   │   │   ├── UserProgressEntity.kt
    │   │   │   │   │   ├── StreakEntity.kt
    │   │   │   │   │   └── DataVersionEntity.kt
    │   │   │   │   ├── dao/
    │   │   │   │   │   ├── VocabularyDao.kt
    │   │   │   │   │   ├── ReadingDao.kt
    │   │   │   │   │   ├── ConversationPhraseDao.kt
    │   │   │   │   │   ├── ToneDrillDao.kt
    │   │   │   │   │   ├── ExamStructureDao.kt
    │   │   │   │   │   ├── SampleQuestionDao.kt
    │   │   │   │   │   ├── ExamResultDao.kt
    │   │   │   │   │   ├── AudioCacheDao.kt
    │   │   │   │   │   ├── UserProgressDao.kt
    │   │   │   │   │   ├── StreakDao.kt
    │   │   │   │   │   └── DataVersionDao.kt
    │   │   │   │   ├── import/
    │   │   │   │   │   ├── JsonImporter.kt
    │   │   │   │   │   ├── ImportProgress.kt
    │   │   │   │   │   └── dto/
    │   │   │   │   │       ├── VocabularyDto.kt
    │   │   │   │   │       ├── ReadingDto.kt
    │   │   │   │   │       ├── ConversationPhraseDto.kt
    │   │   │   │   │       ├── ToneDrillDto.kt
    │   │   │   │   │       ├── ExamStructureDto.kt
    │   │   │   │   │       └── SampleQuestionDto.kt
    │   │   │   │   └── migrations/
    │   │   │   │       └── MIGRATIONS.kt            # placeholder for future versions
    │   │   │   ├── remote/
    │   │   │   │   ├── GeminiService.kt
    │   │   │   │   ├── GeminiError.kt
    │   │   │   │   ├── GeminiPrompts.kt             # canned system prompts
    │   │   │   │   ├── AudioBlob.kt
    │   │   │   │   └── BackoffPolicy.kt             # rate-limit retry helper
    │   │   │   ├── audio/
    │   │   │   │   ├── AudioRecorder.kt             # MediaRecorder wrapper
    │   │   │   │   ├── AudioPlayer.kt               # MediaPlayer wrapper
    │   │   │   │   └── AndroidTtsFallback.kt        # built-in TextToSpeech (offline)
    │   │   │   ├── preferences/
    │   │   │   │   ├── PreferencesKeys.kt
    │   │   │   │   └── UserPreferencesRepository.kt # DataStore-backed
    │   │   │   └── repository/
    │   │   │       ├── VocabularyRepository.kt
    │   │   │       ├── ReadingRepository.kt
    │   │   │       ├── AudioRepository.kt
    │   │   │       ├── SpeakingRepository.kt
    │   │   │       ├── ListeningRepository.kt
    │   │   │       ├── ExamRepository.kt
    │   │   │       ├── ProgressRepository.kt
    │   │   │       ├── StreakRepository.kt
    │   │   │       └── SettingsRepository.kt
    │   │   ├── util/
    │   │   │   ├── NetworkMonitor.kt
    │   │   │   ├── DateUtil.kt                      # epoch-day helpers, today()
    │   │   │   ├── HashUtil.kt                      # sha256 for cache key
    │   │   │   ├── Result.kt                        # alias / Either-like
    │   │   │   ├── Logger.kt                        # thin wrapper around android.util.Log
    │   │   │   ├── PermissionsHelper.kt
    │   │   │   ├── FileExportHelper.kt              # Storage Access Framework helper
    │   │   │   └── DispatcherProvider.kt
    │   │   └── res/
    │   │       ├── drawable/
    │   │       │   ├── ic_launcher_background.xml
    │   │       │   ├── ic_launcher_foreground.xml
    │   │       │   ├── ic_streak_flame.xml
    │   │       │   ├── ic_check.xml
    │   │       │   ├── ic_close.xml
    │   │       │   ├── ic_volume_up.xml
    │   │       │   ├── ic_mic.xml
    │   │       │   ├── ic_book.xml
    │   │       │   ├── ic_home.xml
    │   │       │   ├── ic_edit.xml
    │   │       │   └── ic_person.xml
    │   │       ├── mipmap-anydpi-v26/
    │   │       │   ├── ic_launcher.xml
    │   │       │   └── ic_launcher_round.xml
    │   │       ├── mipmap-mdpi/                     # plus -hdpi, -xhdpi, -xxhdpi, -xxxhdpi
    │   │       │   └── ic_launcher.png
    │   │       ├── values/
    │   │       │   ├── strings.xml                  # ALL user-facing strings
    │   │       │   ├── colors.xml                   # raw hex (referenced by Theme.kt)
    │   │       │   ├── themes.xml                   # AppTheme parent for splash
    │   │       │   ├── dimens.xml
    │   │       │   └── styles.xml
    │   │       ├── values-night/
    │   │       │   ├── colors.xml
    │   │       │   └── themes.xml
    │   │       ├── xml/
    │   │       │   ├── backup_rules.xml
    │   │       │   └── data_extraction_rules.xml
    │   │       └── raw/
    │   │           ├── hsk1_vocab.json              # copied from /data/vocabulary/
    │   │           ├── hsk2_vocab.json
    │   │           ├── hsk3_vocab.json
    │   │           ├── hsk4_vocab.json
    │   │           ├── hsk5_vocab.json
    │   │           ├── hsk1_readings.json
    │   │           ├── hsk2_readings.json
    │   │           ├── hsk3_readings.json
    │   │           ├── hsk4_readings.json
    │   │           ├── hsk5_readings.json
    │   │           ├── tone_drills.json
    │   │           ├── conversation_phrases.json
    │   │           ├── hsk1_exam_structure.json
    │   │           ├── hsk2_exam_structure.json
    │   │           ├── hsk3_exam_structure.json
    │   │           ├── hsk4_exam_structure.json
    │   │           ├── hsk5_exam_structure.json
    │   │           ├── sample_questions.json
    │   │           └── licenses.json                # third-party license metadata for SettingsScreen
    │
    ├── test/
    │   └── java/com/mandarinlearn/
    │       ├── domain/
    │       │   ├── srs/SrsSchedulerTest.kt
    │       │   ├── grading/ExamGraderTest.kt
    │       │   └── readiness/ReadinessCalculatorTest.kt
    │       ├── data/
    │       │   ├── repository/VocabularyRepositoryTest.kt
    │       │   ├── repository/ExamRepositoryTest.kt
    │       │   └── remote/BackoffPolicyTest.kt
    │       └── viewmodel/
    │           ├── HomeViewModelTest.kt
    │           ├── FlashcardViewModelTest.kt
    │           ├── ExamViewModelTest.kt
    │           └── SettingsViewModelTest.kt
    │
    └── androidTest/
        └── java/com/mandarinlearn/
            ├── data/local/
            │   ├── MandarinLearnDatabaseTest.kt
            │   ├── VocabularyDaoTest.kt
            │   └── ExamResultDaoTest.kt
            ├── data/local/import/
            │   └── JsonImporterTest.kt
            └── ui/
                ├── HomeScreenTest.kt
                ├── FlashcardScreenTest.kt
                └── ExamScreenTest.kt
```

---

## Notes on file conventions

- **One class per file.** ViewModels and their `UiState` sealed classes are split into separate files.
- **Every file ≤ 300 lines** (per CLAUDE.md hard rule). If a file approaches that, split it before submission.
- **No `LiveData`** anywhere. UiState is `StateFlow<...UiState>`.
- **No DI framework files**. Manual constructor injection through `di/AppContainer.kt`.
- **All user-facing strings** live in `res/values/strings.xml`. Never hard-code.
- **Resource ids** in raw/ match the JSON filenames exactly (`R.raw.hsk1_vocab` etc.).
- **Tests:** unit tests in `test/`, instrumented tests in `androidTest/`. Compose UI tests use `compose-ui-test-junit4`.

---

## Screen-to-file cross-reference (for QA)

| UX_SPECIFICATION screen   | Screen file                                              | ViewModel file                                            |
|---------------------------|----------------------------------------------------------|-----------------------------------------------------------|
| HomeScreen                | `ui/home/HomeScreen.kt`                                  | `ui/home/HomeViewModel.kt`                                |
| VocabularyScreen          | `ui/vocabulary/VocabularyScreen.kt`                      | `ui/vocabulary/VocabularyViewModel.kt`                    |
| FlashcardScreen           | `ui/vocabulary/FlashcardScreen.kt`                       | `ui/vocabulary/FlashcardViewModel.kt`                     |
| ReadingListScreen         | `ui/reading/ReadingListScreen.kt`                        | `ui/reading/ReadingListViewModel.kt`                      |
| PassageScreen             | `ui/reading/PassageScreen.kt`                            | `ui/reading/PassageViewModel.kt`                          |
| SpeakingScreen            | `ui/speaking/SpeakingScreen.kt`                          | `ui/speaking/SpeakingViewModel.kt`                        |
| ListeningScreen           | `ui/listening/ListeningScreen.kt`                        | `ui/listening/ListeningViewModel.kt`                      |
| ExamScreen                | `ui/exam/ExamScreen.kt`                                  | `ui/exam/ExamViewModel.kt`                                |
| ExamResultScreen          | `ui/exam/ExamResultScreen.kt`                            | `ui/exam/ExamResultViewModel.kt`                          |
| ProgressScreen            | `ui/progress/ProgressScreen.kt`                          | `ui/progress/ProgressViewModel.kt`                        |
| SettingsScreen            | `ui/settings/SettingsScreen.kt`                          | `ui/settings/SettingsViewModel.kt`                        |
| (host) PracticeHubScreen  | `ui/practice/PracticeHubScreen.kt`                       | `ui/practice/PracticeHubViewModel.kt`                     |
| (host) ExamHubScreen      | `ui/exam/ExamHubScreen.kt`                               | `ui/exam/ExamHubViewModel.kt`                             |
| (host) MeScreen           | `ui/me/MeScreen.kt`                                      | `ui/me/MeViewModel.kt`                                    |
| (host) ImportLoadingScreen| `ui/importing/ImportLoadingScreen.kt`                    | `ui/importing/ImportLoadingViewModel.kt`                  |
