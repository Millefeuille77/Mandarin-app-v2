# Mandarin Learn

An Android app that takes complete beginners from zero Mandarin to HSK 5, designed specifically for 60-year-old learners with a minimalist interface, large touch targets, and TalkBack support. The app covers five learning sections — Vocabulary (spaced repetition flashcards), Reading (pinyin-annotated passages), Listening (Gemini TTS multiple-choice quiz), Speaking (Gemini STT pronunciation scoring), and full mock HSK 1–5 exams — all stored locally so content works offline once imported.

---

## Requirements

| Requirement | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 |
| Android SDK | API 34 (compile + target), API 26 minimum |
| Kotlin | 1.9.22 |
| Gradle | 8.2 (wrapper pinned in `gradle/wrapper/gradle-wrapper.properties`) |

---

## Setup

### 1. Open the project

1. Clone or download this repository.
2. Open Android Studio → **File → Open** → select the `mandarin_app_v2/` folder.
3. Let Gradle sync complete (this downloads all dependencies from Maven Central).

### 2. Add the Gemini API key

Create (or edit) `local.properties` in the project root (next to `build.gradle.kts`):

```properties
GEMINI_API_KEY=your_key_here
```

Obtain a free key at [aistudio.google.com](https://aistudio.google.com/). The key is read only at build time via `BuildConfig.GEMINI_API_KEY` and is never committed to version control (`.gitignore` excludes `local.properties`).

**If the key is omitted:** The app builds and runs normally. Vocabulary, Reading, and Exam sections are fully functional. The Listening section falls back to Android's built-in Chinese TTS (requires a Chinese TTS voice installed in Android Settings). The Speaking section's pronunciation scoring will return a neutral placeholder score.

### 3. Build and install

**From Android Studio:**
- Click the green **Run** button or press `Shift+F10` to build and deploy to a connected device or emulator.

**From the command line:**

```bash
# Debug APK
./gradlew assembleDebug

# Install directly to a connected device
./gradlew installDebug

# Release APK (unsigned — for distribution add signing config to app/build.gradle.kts)
./gradlew assembleRelease
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

---

## Running tests

### Unit tests (JVM, no device required)

```bash
./gradlew test
```

Covers: SM-2 spaced repetition (`SrsSchedulerTest`, 12 cases), exam grading (`ExamGraderTest`, 12 cases), readiness formula (`ReadinessCalculatorTest`, 13 cases), all ViewModel state machines, export/import round-trip.

### Instrumented tests (requires a device or emulator)

```bash
./gradlew connectedAndroidTest
```

Covers: Room database operations, JSON import, Compose UI smoke tests for all major screens, end-to-end navigation flows.

**Recommended emulator:** Pixel 5, API 26 or API 34 (both are tested targets).

---

## Known limitations

### Data coverage
- HSK 4 vocabulary: 310 of 600 standard words included (source limitation noted in `qa_reports/qa_research.md`).
- HSK 5 vocabulary: 300 of 1,300 standard words included (partial set).
- HSK 3 has 4 known duplicate word IDs in the source data; the app de-duplicates on import using `OnConflictStrategy.IGNORE`.
- Sample exam questions: HSK 3 reading/writing sections have 5–10 fewer questions than the real HSK format. The exam grader scales scores proportionally.

### Gemini SDK limitations
- Gemini SDK 0.2.2 does not support direct audio output (`responseMimeType = "audio/mpeg"` is not available). The Listening section uses Android's built-in `TextToSpeech` with `Locale.SIMPLIFIED_CHINESE` as the audio engine. When the SDK gains audio output support, `GeminiService.synthesize()` will return real audio bytes and the cache layer will activate.
- The `Retry-After` HTTP header from Gemini 429 responses cannot be parsed through SDK 0.2.2 — rate-limit retries use a fixed 1-second initial delay with exponential backoff (3 attempts maximum).
- Speaking pronunciation scoring requires a network connection to Gemini. Offline sessions return a neutral placeholder score of 50/100 with an explanatory message.

### First-launch import
- The first app launch imports all HSK data (vocabulary, readings, audio phrases, exam structures) from bundled JSON files into a local Room database. This takes up to 8 seconds on a Pixel 5. Subsequent launches skip the import (< 100 ms check).

### Exams
- Free-text writing questions (HSK 3+) are graded by exact-match after normalisation (lowercase, whitespace-trimmed). Partial-credit grading via Gemini is not implemented.

---

## Architecture

This app was built by a 4-agent AI pipeline (Research → Architect → Developer → QA). Full technical decisions, screen layouts, and the 10-phase build plan are documented in `specs/`:

- [`specs/ARCHITECTURE.md`](specs/ARCHITECTURE.md) — tech stack, data layer, Gemini integration
- [`specs/UX_SPECIFICATION.md`](specs/UX_SPECIFICATION.md) — all 11 screens, accessibility rules
- [`specs/IMPLEMENTATION_PLAN.md`](specs/IMPLEMENTATION_PLAN.md) — 10-phase build plan
- [`specs/FOLDER_STRUCTURE.md`](specs/FOLDER_STRUCTURE.md) — complete file tree

**Tech stack summary:**
- Language: Kotlin 1.9.22
- UI: Jetpack Compose + Material Design 3
- Architecture: MVVM + Repository (manual constructor DI — no Hilt/Dagger)
- State: `StateFlow<UiState>` only
- Persistence: Room 2.6.1 (SQLite)
- AI: Google Gemini SDK 0.2.2
- Min SDK: API 26 (Android 8.0+)
