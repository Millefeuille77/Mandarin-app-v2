# ARCHITECTURE.md — Mandarin Learn

> Single source of technical truth for the Developer agent. Every value here is locked. Do not deviate without an Architect re-spec.

- **Project package:** `com.mandarinlearn`
- **Application ID:** `com.mandarinlearn`
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **Java/Kotlin JVM target:** 17
- **App orientation:** portrait only (locked in manifest)
- **Build system:** Gradle 8.4 (Kotlin DSL only — `.gradle.kts`)

---

## 1. Dependency Manifest (PINNED — exact versions)

All dependencies are declared in the version catalog at `gradle/libs.versions.toml`. **No `latest`, no `+`, no dynamic ranges.** If a version must change, it changes here first and is reviewed.

### 1.1 Build plugins

```toml
[versions]
agp = "8.2.2"
kotlin = "1.9.22"
ksp = "1.9.22-1.0.17"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android       = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
ksp                  = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 1.2 Library versions

```toml
[versions]
# Kotlin / coroutines
kotlin              = "1.9.22"
coroutines          = "1.7.3"

# AndroidX core
core-ktx            = "1.12.0"
appcompat           = "1.6.1"
activity-compose    = "1.8.2"
lifecycle           = "2.7.0"
splashscreen        = "1.0.1"

# Compose
compose-bom         = "2024.02.00"   # BOM governs all compose-* artifacts
compose-compiler    = "1.5.8"        # MUST match Kotlin 1.9.22
navigation-compose  = "2.7.7"

# Room
room                = "2.6.1"

# DataStore (settings persistence — preferences flavor only)
datastore           = "1.0.0"

# JSON parsing
kotlinx-serialization-json = "1.6.2"

# Google Gemini SDK
generative-ai       = "0.2.2"        # com.google.ai.client.generativeai

# Audio recording amplitude helpers (AndroidX, no third-party)
# (none — MediaRecorder + MediaPlayer from android.media are sufficient)

# Testing
junit               = "4.13.2"
androidx-test-junit = "1.1.5"
espresso-core       = "3.5.1"
turbine             = "1.0.0"        # Flow testing
mockk               = "1.13.9"
robolectric         = "4.11.1"
```

### 1.3 Library coordinates

```toml
[libraries]
# Kotlin
kotlin-stdlib                  = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlinx-coroutines-android     = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test        = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json     = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization-json" }

# AndroidX
androidx-core-ktx              = { module = "androidx.core:core-ktx", version.ref = "core-ktx" }
androidx-appcompat             = { module = "androidx.appcompat:appcompat", version.ref = "appcompat" }
androidx-activity-compose      = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose   = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-splashscreen          = { module = "androidx.core:core-splashscreen", version.ref = "splashscreen" }

# Compose (versions resolved by BOM)
compose-bom                    = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui                     = { module = "androidx.compose.ui:ui" }
compose-ui-graphics            = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling             = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview     = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-test-junit4         = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest       = { module = "androidx.compose.ui:ui-test-manifest" }
compose-material3              = { module = "androidx.compose.material3:material3" }
compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
androidx-navigation-compose    = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }

# Room
room-runtime                   = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx                       = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler                  = { module = "androidx.room:room-compiler", version.ref = "room" }
room-testing                   = { module = "androidx.room:room-testing", version.ref = "room" }

# DataStore (Preferences)
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Gemini
google-generative-ai           = { module = "com.google.ai.client.generativeai:generativeai", version.ref = "generative-ai" }

# Test
junit                          = { module = "junit:junit", version.ref = "junit" }
androidx-test-ext-junit        = { module = "androidx.test.ext:junit", version.ref = "androidx-test-junit" }
androidx-test-espresso-core    = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso-core" }
turbine                        = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
mockk                          = { module = "io.mockk:mockk", version.ref = "mockk" }
robolectric                    = { module = "org.robolectric:robolectric", version.ref = "robolectric" }
```

**Total runtime dependencies:** 22 (excluding test).
**No DI framework** (manual constructor injection per CLAUDE.md rule).
**No image-loading library** (no remote images in app).
**No charting library** — the small line chart on `ProgressScreen` is drawn with Compose `Canvas` (see UX §10).

### 1.4 Manifest permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<!-- No external storage needed; export uses Storage Access Framework via ActivityResult API -->
```

---

## 2. Room Database Schema

- **Database name:** `mandarin_learn.db`
- **Class:** `MandarinLearnDatabase` (extends `RoomDatabase`)
- **Version:** `1`
- **Migrations:** none in v1; future migrations declared in `db/migrations/`
- **Type converters:** `Converters.kt` (LocalDate ↔ epoch-day Long; List<String> ↔ JSON; List<PinyinAnnotation> ↔ JSON)
- **All queries return Flow<…> or suspend** — never blocking.

### 2.1 Tables

#### `vocabulary`
Each row = one HSK word, with SM-2 spaced-repetition state.

| Column                | Type      | Constraints                      | Notes |
|-----------------------|-----------|----------------------------------|-------|
| `id`                  | TEXT      | PRIMARY KEY                      | e.g. `hsk1_001` (matches JSON id) |
| `hsk_level`           | INTEGER   | NOT NULL, INDEX                  | 1–5 |
| `character`           | TEXT      | NOT NULL                         | Hanzi |
| `pinyin`              | TEXT      | NOT NULL                         | tone-mark form |
| `translation`         | TEXT      | NOT NULL                         |  |
| `part_of_speech`      | TEXT      | NOT NULL                         |  |
| `example_chinese`     | TEXT      | NOT NULL                         | from `example_sentence.chinese` |
| `example_pinyin`      | TEXT      | NOT NULL                         | from `example_sentence.pinyin` |
| `example_english`     | TEXT      | NOT NULL                         | from `example_sentence.english` |
| `ease_factor`         | REAL      | NOT NULL, default `2.5`          | SM-2 |
| `interval_days`       | INTEGER   | NOT NULL, default `0`            | SM-2 |
| `repetition_count`    | INTEGER   | NOT NULL, default `0`            | SM-2 |
| `next_review_date`    | INTEGER   | NOT NULL, default `0`            | epoch day; `0` = never reviewed (treated as due) |
| `last_reviewed_date`  | INTEGER   | NULLABLE                         | epoch day |
| `is_introduced`       | INTEGER   | NOT NULL, default `0`            | 0/1 — has the user seen this card at least once |

Index: `idx_vocab_level_due` on `(hsk_level, next_review_date)`.

#### `reading_passages`

| Column                | Type    | Constraints     | Notes |
|-----------------------|---------|-----------------|-------|
| `id`                  | TEXT    | PRIMARY KEY     | `hsk1_reading_001` |
| `hsk_level`           | INTEGER | NOT NULL, INDEX |  |
| `title`               | TEXT    | NOT NULL        |  |
| `chinese_text`        | TEXT    | NOT NULL        |  |
| `pinyin_annotations`  | TEXT    | NOT NULL        | JSON-encoded `List<PinyinAnnotation>` (every char for HSK 1–3, key vocab only for 4–5) |
| `english_translation` | TEXT    | NOT NULL        |  |
| `vocabulary_highlights` | TEXT  | NOT NULL        | JSON `List<String>` |
| `word_count`          | INTEGER | NOT NULL        |  |
| `is_completed`        | INTEGER | NOT NULL, default `0` | 0/1 user has read |
| `completed_at`        | INTEGER | NULLABLE        | epoch day |

#### `conversation_phrases`
Source: `data/audio/conversation_phrases.json` (used by Speaking).

| Column          | Type    | Constraints     |
|-----------------|---------|-----------------|
| `id`            | TEXT    | PRIMARY KEY     |
| `hsk_level`     | INTEGER | NOT NULL, INDEX |
| `category`      | TEXT    | NOT NULL        |
| `chinese`       | TEXT    | NOT NULL        |
| `pinyin`        | TEXT    | NOT NULL        |
| `english`       | TEXT    | NOT NULL        |
| `usage_context` | TEXT    | NOT NULL        |

#### `tone_drills`
Source: `data/audio/tone_drills.json`.

| Column                | Type | Constraints   |
|-----------------------|------|---------------|
| `id`                  | TEXT | PRIMARY KEY   |
| `tone_pair`           | TEXT | NOT NULL      |
| `description`         | TEXT | NOT NULL      |
| `example_word`        | TEXT | NOT NULL      |
| `pinyin`              | TEXT | NOT NULL      |
| `translation`         | TEXT | NOT NULL      |
| `additional_examples` | TEXT | NOT NULL      | JSON list |

#### `exam_structures`
One row per HSK level. `sections_json` is variable-length JSON (HSK 1–2 = 2 sections, HSK 3–5 = 3 sections). The schema accommodates an arbitrary number of sections — see §2.3.

| Column                | Type    | Constraints  |
|-----------------------|---------|--------------|
| `hsk_level`           | INTEGER | PRIMARY KEY  |
| `total_duration_minutes` | INTEGER | NOT NULL  |
| `sections_json`       | TEXT    | NOT NULL     | JSON array of `ExamSection` (variable length) |
| `total_max_score`     | INTEGER | NOT NULL     | 200 (HSK 1–2) or 300 (HSK 3–5) |
| `total_passing_score` | INTEGER | NOT NULL     | 120 (HSK 1–2) or 180 (HSK 3–5) |
| `vocabulary_required` | INTEGER | NOT NULL     |
| `scoring_notes`       | TEXT    | NOT NULL     |

#### `sample_questions`

| Column               | Type    | Constraints     |
|----------------------|---------|-----------------|
| `id`                 | TEXT    | PRIMARY KEY     |
| `hsk_level`          | INTEGER | NOT NULL, INDEX |
| `section`            | TEXT    | NOT NULL, INDEX | listening / reading / writing |
| `question_type`      | TEXT    | NOT NULL        |
| `question_text`      | TEXT    | NOT NULL        |
| `audio_text_chinese` | TEXT    | NULLABLE        | for listening (TTS source) |
| `audio_text_pinyin`  | TEXT    | NULLABLE        |
| `options_json`       | TEXT    | NOT NULL        | JSON `List<String>` |
| `correct_answer`     | TEXT    | NOT NULL        | option key e.g. `"A"` |
| `explanation`        | TEXT    | NOT NULL        |

Composite index: `idx_q_level_section` on `(hsk_level, section)`.

#### `exam_results`
One row per attempted exam. Per-section scores stored as JSON to handle the variable section count cleanly.

| Column                | Type    | Constraints                |
|-----------------------|---------|----------------------------|
| `id`                  | INTEGER | PRIMARY KEY AUTOINCREMENT  |
| `hsk_level`           | INTEGER | NOT NULL, INDEX            |
| `started_at`          | INTEGER | NOT NULL                   | epoch millis |
| `finished_at`         | INTEGER | NOT NULL                   | epoch millis |
| `duration_seconds`    | INTEGER | NOT NULL                   |
| `section_scores_json` | TEXT    | NOT NULL                   | JSON `List<SectionScore>` (variable length matches `exam_structures.sections_json`) |
| `total_score`         | INTEGER | NOT NULL                   |
| `total_max_score`     | INTEGER | NOT NULL                   | 200 or 300 |
| `passing_score`       | INTEGER | NOT NULL                   | 120 or 180 |
| `passed`              | INTEGER | NOT NULL                   | 0/1 (precomputed: total_score ≥ passing_score) |
| `answers_json`        | TEXT    | NOT NULL                   | JSON `List<AnswerRecord>` for "Review mistakes" |

`SectionScore` schema (JSON):
```json
{ "name": "listening", "score": 78, "max_score": 100, "correct_count": 16, "question_count": 20 }
```
`AnswerRecord` schema (JSON):
```json
{ "question_id": "hsk1_listen_007", "user_answer": "B", "correct_answer": "B", "is_correct": true }
```

#### `audio_cache`
Caches Gemini TTS outputs to minimise API calls. Audio stored as Base64 BLOBs.

| Column         | Type    | Constraints |
|----------------|---------|-------------|
| `cache_key`    | TEXT    | PRIMARY KEY | hash of `text + voice + speed` (SHA-256, hex) |
| `text`         | TEXT    | NOT NULL    | original Chinese text |
| `voice`        | TEXT    | NOT NULL    | e.g. `"cmn-CN-Female-1"` |
| `speed`        | REAL    | NOT NULL    | 0.5–1.25 |
| `audio_bytes`  | BLOB    | NOT NULL    | raw audio (MP3/WAV bytes from Gemini) |
| `mime_type`    | TEXT    | NOT NULL    | `"audio/mpeg"` |
| `created_at`   | INTEGER | NOT NULL    | epoch millis |
| `last_used_at` | INTEGER | NOT NULL    | epoch millis |
| `byte_size`    | INTEGER | NOT NULL    | for cache eviction |

Eviction policy: LRU when total `byte_size > 50 MB` (run on app start, in worker scope).

#### `user_progress`
Per-section, per-HSK-level progress aggregated for the dashboard. One row per `(hsk_level, section)` pair. Section is one of `vocabulary | reading | listening | speaking | exam`.

| Column                | Type    | Constraints                                              |
|-----------------------|---------|----------------------------------------------------------|
| `hsk_level`           | INTEGER | NOT NULL                                                 |
| `section`             | TEXT    | NOT NULL                                                 |
| `total_items`         | INTEGER | NOT NULL                                                 |
| `completed_items`     | INTEGER | NOT NULL, default `0`                                    |
| `last_activity_date`  | INTEGER | NULLABLE                                                 | epoch day |
| PRIMARY KEY           |         | `(hsk_level, section)`                                   |

#### `streak`
Single-row table for daily learning streak.

| Column              | Type    | Constraints |
|---------------------|---------|-------------|
| `id`                | INTEGER | PRIMARY KEY (always 1) |
| `current_streak`    | INTEGER | NOT NULL, default 0 |
| `longest_streak`    | INTEGER | NOT NULL, default 0 |
| `last_active_date`  | INTEGER | NULLABLE | epoch day |

#### `data_version`
Tracks JSON-import version for first-launch and migration support.

| Column              | Type    | Constraints |
|---------------------|---------|-------------|
| `id`                | INTEGER | PRIMARY KEY (always 1) |
| `vocabulary_version` | INTEGER | NOT NULL, default 0 |
| `reading_version`    | INTEGER | NOT NULL, default 0 |
| `audio_version`      | INTEGER | NOT NULL, default 0 |
| `exam_version`       | INTEGER | NOT NULL, default 0 |
| `imported_at`        | INTEGER | NOT NULL |

Current import version (Phase 2): all `*_version` fields = `1`. Bump when shipped JSON content changes.

### 2.2 DAOs (one per entity, file-per-DAO)

- `VocabularyDao` — `getByLevel(hsk: Int): Flow<List<VocabularyEntity>>`, `getDueCards(today: Long, limit: Int): List<VocabularyEntity>`, `getNewCards(hsk: Int, limit: Int): List<VocabularyEntity>`, `update(card: VocabularyEntity)`, `searchByText(query: String): Flow<List<VocabularyEntity>>`, `count(): Flow<Int>`, `countMastered(hsk: Int): Flow<Int>` (mastered = `repetition_count >= 5 AND ease_factor >= 2.5`).
- `ReadingDao` — `getByLevel(hsk: Int): Flow<List<ReadingEntity>>`, `getById(id: String): Flow<ReadingEntity?>`, `markCompleted(id: String, date: Long)`.
- `ConversationPhraseDao` — `getByLevel(hsk: Int): Flow<List<ConversationPhraseEntity>>`, `getRandomPhrase(hsk: Int): ConversationPhraseEntity?`.
- `ToneDrillDao` — `getAll(): Flow<List<ToneDrillEntity>>`.
- `ExamStructureDao` — `getStructure(hsk: Int): Flow<ExamStructureEntity?>`.
- `SampleQuestionDao` — `getByLevelAndSection(hsk: Int, section: String): Flow<List<SampleQuestionEntity>>`, `getQuestionsForExam(hsk: Int, section: String, limit: Int): List<SampleQuestionEntity>`.
- `ExamResultDao` — `insert(result: ExamResultEntity): Long`, `getByLevel(hsk: Int): Flow<List<ExamResultEntity>>`, `getRecent(limit: Int): Flow<List<ExamResultEntity>>`, `getById(id: Long): Flow<ExamResultEntity?>`.
- `AudioCacheDao` — `get(key: String): AudioCacheEntity?`, `insert(entity: AudioCacheEntity)`, `touch(key: String, now: Long)`, `totalBytes(): Long`, `evictLruUntil(targetBytes: Long)`.
- `UserProgressDao` — `getAll(): Flow<List<UserProgressEntity>>`, `upsert(entity: UserProgressEntity)`.
- `StreakDao` — `get(): Flow<StreakEntity?>`, `upsert(entity: StreakEntity)`.
- `DataVersionDao` — `get(): DataVersionEntity?`, `upsert(entity: DataVersionEntity)`.

### 2.3 Variable section count — design note

Both `exam_structures.sections_json` and `exam_results.section_scores_json` are JSON arrays. The Kotlin types `List<ExamSection>` / `List<SectionScore>` are encoded with `kotlinx.serialization`. UI code iterates over these lists and renders one row per section dynamically, so the same screens render correctly for HSK 1–2 (2 sections) and HSK 3–5 (3 sections). No alter-table is ever needed if a future HSK level adds a fourth section.

---

## 3. Data Import Strategy (JSON → Room)

### 3.1 First-launch flow

```
App start
  └─► MainActivity → splash screen
        └─► AppContainer.bootstrap()
              ├─► open Room DB
              ├─► read data_version row
              │     ├─ if null OR any *_version < CURRENT_VERSION: run import
              │     └─ else: skip (warm start)
              └─► launch ImportLoadingScreen with progress flow
```

### 3.2 Import process — `JsonImporter.kt`

Lives at `data/local/import/JsonImporter.kt`. Public API:

```kotlin
class JsonImporter(
    private val context: Context,
    private val db: MandarinLearnDatabase,
    private val json: Json
) {
    /** Emits progress 0f..1f and a status string. Final emission is (1f, "Done"). */
    suspend fun importIfNeeded(): Flow<ImportProgress>
}

data class ImportProgress(val fraction: Float, val message: String)
```

**Bundled assets** live under `app/src/main/res/raw/`:
- `hsk1_vocab.json` … `hsk5_vocab.json`
- `hsk1_readings.json` … `hsk5_readings.json`
- `tone_drills.json`, `conversation_phrases.json`
- `hsk1_exam_structure.json` … `hsk5_exam_structure.json`
- `sample_questions.json`

**Algorithm:**
1. Open DB; query `data_version`. Decide which sub-imports to run.
2. For each missing/outdated category, in this order — vocabulary → reading → audio → exams. Each step:
   a. Open `resources.openRawResource(R.raw.<file>)`.
   b. Stream-decode with `kotlinx.serialization.json.Json.decodeFromStream`.
   c. Map DTO → Entity.
   d. Insert in chunks of 200 inside a single `withTransaction { … }`.
3. Initialise `user_progress` rows: one row per `(hsk_level, section)` with `total_items` populated from the just-imported counts. Sections seeded: `vocabulary, reading, listening, speaking, exam`.
4. Initialise `streak` row with zeros.
5. Update `data_version` to current versions.
6. Emit `ImportProgress(1f, "Done")`.

**Error handling:** if any category fails, the transaction rolls back, an error message is emitted, and `ImportLoadingScreen` shows a "Retry" button. The DB is left empty for that category, NOT half-populated.

### 3.3 Update strategy

When a future release ships updated JSON, bump the corresponding `*_version` constant in `JsonImporter.Companion.CURRENT_VERSION`. The next launch detects the gap and re-imports only the changed category. User progress in `vocabulary` (SM-2 fields) is preserved by upserting on `id` and only writing **content** columns (not SM-2 columns) for existing rows.

### 3.4 ImportLoadingScreen

Shown only on first launch (or version bump). Layout: app logo, "Setting up your lessons…" header (24 sp), an indeterminate-then-determinate `LinearProgressIndicator`, status text (18 sp), and "Retry" button on error. The screen blocks navigation. After completion, navigate to `HomeScreen` via `popUpTo` so back-press doesn't return.

---

## 4. Gemini API Integration

### 4.1 API key handling

- Gemini key lives in `local.properties` (gitignored) as `GEMINI_API_KEY=…`.
- `app/build.gradle.kts` reads it and exposes via:
  ```kotlin
  buildConfigField("String", "GEMINI_API_KEY", "\"${properties["GEMINI_API_KEY"] ?: ""}\"")
  ```
- Code reference is **only** `BuildConfig.GEMINI_API_KEY`. The string literal must NEVER appear in source files or version control.
- If the key is empty at runtime, `GeminiService` enters **degraded mode**: TTS and STT calls return `Result.failure(GeminiError.NoApiKey)` and the UI shows "AI features unavailable — set your API key in Settings → About" (Settings exposes a read-only field; key entry happens via local.properties at build time, not at runtime — single-device app).

### 4.2 Service layer — `GeminiService.kt`

Singleton, lifetime-bound to the application. Lives in `data/remote/`. Constructor-injected via `AppContainer`.

```kotlin
class GeminiService(
    private val apiKey: String,
    private val ioDispatcher: CoroutineDispatcher,
    private val networkMonitor: NetworkMonitor,
    private val audioCacheDao: AudioCacheDao,
) {
    suspend fun synthesize(text: String, speed: Float = 1.0f): Result<AudioBlob>
    suspend fun transcribeAndScore(audioFile: File, expectedText: String): Result<PronunciationResult>
    suspend fun chat(prompt: String, system: String? = null): Result<String>
}

data class AudioBlob(val bytes: ByteArray, val mimeType: String)
data class PronunciationResult(
    val transcribedText: String,
    val score: Int,           // 0..100
    val feedback: String,     // human-readable
    val phonemeIssues: List<String> // optional list, may be empty
)

sealed class GeminiError : Throwable() {
    data object NoApiKey : GeminiError()
    data object Offline : GeminiError()
    data class Timeout(override val message: String = "Request timed out") : GeminiError()
    data class RateLimited(val retryAfterMs: Long) : GeminiError()
    data class Server(val httpCode: Int, override val message: String) : GeminiError()
    data class Unknown(override val cause: Throwable?) : GeminiError()
}
```

**Models used:**
- TTS: Gemini SDK `GenerativeModel("gemini-1.5-flash")` with `responseMimeType = "audio/mpeg"` (where supported) or fallback to text-prompt-driven TTS. The wrapper hides the model choice; if the live SDK rejects audio output, the implementation falls back to Android's built-in `TextToSpeech` engine with `Locale.SIMPLIFIED_CHINESE` (still works offline, no API call).
- STT + scoring: `gemini-1.5-flash` with multipart input — audio + the expected Chinese text + a structured JSON-schema response asking for `{transcription, score, feedback}`.
- Chat / explanations: `gemini-1.5-flash`.

### 4.3 Behaviours

**TTS (`synthesize`):**
1. Compute `cacheKey = sha256("${text}|${voice}|${speed}")`.
2. Look up `AudioCacheDao.get(cacheKey)`. If hit → return `AudioBlob` and update `last_used_at`.
3. Else check `networkMonitor.isOnline()`. If offline → `Result.failure(Offline)`.
4. Else call Gemini with 30 s timeout (`withTimeout(30_000)`).
5. On success → insert into `audio_cache` and return.
6. On any failure → return appropriate `GeminiError`. The cache is NOT polluted on failure.

**STT (`transcribeAndScore`):**
1. Network check; offline → `Result.failure(Offline)` (no caching for user-recorded audio).
2. Upload audio file (max 10 s recording). 30 s timeout.
3. Parse structured response. Score is an integer 0–100. If parse fails, return `Unknown`.

**Chat (`chat`):**
1. Network check, 30 s timeout.
2. Used by `ExamResultScreen` "Explain this answer" feature and `FlashcardScreen` "Tell me more" optional button.

### 4.4 Error handling matrix

| Condition                       | Behaviour                                                                                     |
|---------------------------------|------------------------------------------------------------------------------------------------|
| No API key (`BuildConfig` blank)| Return `GeminiError.NoApiKey`. UI shows banner "AI features unavailable". App still navigable.|
| No network (`isOnline() == false`) | Return `GeminiError.Offline`. UI shows "Offline — using cached audio only" toast.          |
| Cached audio exists for TTS     | Use cache; never call API.                                                                     |
| `withTimeout(30_000)` fires     | Return `GeminiError.Timeout`. UI shows retry button.                                           |
| HTTP 429 rate-limit             | Return `RateLimited(retryAfterMs)`. **Implementation note:** Gemini SDK 0.2.2's `ServerException` does not surface response headers, so the `Retry-After` header cannot be parsed; `retryAfterMs` defaults to 1000 ms. The ViewModel's `BackoffPolicy` then drives the actual cadence (exponential backoff, 3 attempts, multiplier 2.0). When the SDK exposes headers in a future version, replace the default with the parsed value. Final failure shows "Try again in a moment". |
| HTTP 5xx                        | Return `Server`. UI shows generic "Server error — try again". No auto-retry.                   |
| Unknown exception               | Return `Unknown`. UI shows "Something went wrong" with retry.                                  |

### 4.5 NetworkMonitor

`util/NetworkMonitor.kt` wraps `ConnectivityManager.NetworkCallback` and exposes `Flow<Boolean>` plus a synchronous `isOnline(): Boolean` for fast pre-call checks.

### 4.6 AudioRepository — the fallback chain (authoritative contract)

`data/repository/AudioRepository.kt` is the **only** caller of `GeminiService.synthesize()`. UI and ViewModels never touch `GeminiService` directly for audio. `AudioRepository` is responsible for the cache → Gemini → on-device-TTS fallback chain. This is the single source of truth — `GeminiService.synthesize()`'s `Offline` failure is intentional and is handled by this repository, not by the UI.

```kotlin
class AudioRepository(
    private val gemini: GeminiService,
    private val audioCacheDao: AudioCacheDao,
    private val androidTts: AndroidTtsFallback,   // util/AndroidTtsFallback.kt
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun play(text: String, speed: Float = 1.0f): Flow<AudioPlaybackState>
}

sealed class AudioPlaybackState {
    data object Loading : AudioPlaybackState()
    data class Playing(val source: Source) : AudioPlaybackState()  // cache | gemini | androidTts
    data object Finished : AudioPlaybackState()
    data class Failed(val reason: String) : AudioPlaybackState()
    enum class Source { CACHE, GEMINI, ANDROID_TTS }
}
```

**`play(text, speed)` algorithm — must be implemented exactly:**

1. Emit `Loading`.
2. Compute `cacheKey = sha256("${text}|${voice}|${speed}")` (3-field key per §2.1 `audio_cache` schema; today `voice` is the constant `cmn-CN-Female-1`, single-voice).
3. Query `AudioCacheDao.get(cacheKey)`:
   - Hit → play via `MediaPlayer`, emit `Playing(CACHE)`, then `Finished`. Update `last_used_at`. Stop.
4. Cache miss → check `networkMonitor.isOnline()`:
   - Online → call `gemini.synthesize(text, speed)`:
     - `Result.success(blob)` → insert into `audio_cache`, play, emit `Playing(GEMINI)`, then `Finished`.
     - `Result.failure(NoApiKey | Offline | RateLimited | Server | Unknown)` → fall through to step 5.
     - `Result.failure(Timeout)` → fall through to step 5.
   - Offline → fall through to step 5.
5. Final fallback → `androidTts.speak(text, speed)`:
   - Success (TTS engine available, Simplified Chinese pack installed) → emit `Playing(ANDROID_TTS)`, then `Finished`. **Do NOT** insert into `audio_cache` (the on-device synthesis is per-device and per-engine; caching it across devices is incorrect).
   - Failure (no Chinese voice pack, engine unavailable) → emit `Failed(reason)`. UI shows snackbar "Audio not available — please install Chinese TTS voice in Android Settings → Languages → Text-to-speech."

**Offline-first guarantee:** if `text` has been played before (cache hit), the user gets audio with no network. If never played, on-device TTS fills in; the user always gets *some* audio for any non-empty `text` as long as a Chinese TTS engine is installed.

**`AndroidTtsFallback`** lives in `util/`. It wraps `android.speech.tts.TextToSpeech` with `Locale.SIMPLIFIED_CHINESE`, exposes `suspend fun speak(text: String, speed: Float)`, and reports availability via `isAvailable(): Boolean` (true iff the engine reports `LANG_AVAILABLE` for zh-CN). Speed maps to `setSpeechRate(speed)`.

---

## 5. Spaced Repetition Algorithm (SM-2 variant)

### 5.1 State per card

Stored in `vocabulary` row:
- `ease_factor` (REAL, initial `2.5`, floor `1.3`)
- `interval_days` (INTEGER, initial `0`)
- `repetition_count` (INTEGER, initial `0`)
- `next_review_date` (epoch day, initial `0`)
- `last_reviewed_date` (epoch day, nullable)

### 5.2 Quality ratings

User taps one of four buttons after seeing the back of a card:

| Button   | Quality `q` | Meaning                           |
|----------|-------------|-----------------------------------|
| Forgot   | 0           | "I had no idea"                   |
| Hard     | 1           | "I got it but it was a struggle"  |
| Good     | 2           | "I knew it"                       |
| Easy     | 3           | "Trivial"                         |

### 5.3 Update formula (called on every review)

```kotlin
fun review(card: VocabularyEntity, q: Int, today: Long): VocabularyEntity {
    require(q in 0..3)

    // 1. New ease factor (mapped from 0..3 to SM-2's 0..5 scale).
    val q5 = q * 5.0 / 3.0   // 0->0, 1->1.667, 2->3.333, 3->5
    var newEf = card.ease_factor + (0.1 - (5 - q5) * (0.08 + (5 - q5) * 0.02))
    if (newEf < 1.3) newEf = 1.3

    // 2. Reset on lapse.
    if (q == 0) {
        return card.copy(
            repetition_count = 0,
            interval_days    = 1,
            ease_factor      = newEf,
            next_review_date = today + 1,
            last_reviewed_date = today,
            is_introduced    = 1
        )
    }

    // 3. Successful review path.
    val newReps = card.repetition_count + 1
    val newInterval = when (newReps) {
        1    -> 1
        2    -> 6
        else -> Math.ceil(card.interval_days * newEf).toInt()
    }
    // 4. Hard-button penalty: shrink interval by 20%, min 1 day.
    val finalInterval = if (q == 1) maxOf(1, (newInterval * 0.8).toInt()) else newInterval

    return card.copy(
        repetition_count = newReps,
        interval_days    = finalInterval,
        ease_factor      = newEf,
        next_review_date = today + finalInterval,
        last_reviewed_date = today,
        is_introduced    = 1
    )
}
```

### 5.4 Daily session selection

`SrsScheduler.getSession(hsk: Int, newCardsLimit: Int): SrsSession`:

1. **Due cards:** all rows where `is_introduced = 1 AND next_review_date <= today AND hsk_level = hsk`. Ordered by `next_review_date ASC` then `id ASC`.
2. **New cards:** up to `newCardsLimit` rows where `is_introduced = 0 AND hsk_level = hsk`. Ordered by `id ASC` (preserves curriculum order).
3. Concatenate — due cards first, then new cards. UI shows total count up front.

Default `newCardsLimit = 10`, configurable in Settings (range 5–30).

### 5.5 "Mastered" definition (for ProgressScreen)

A card is **mastered** when `repetition_count >= 5 AND ease_factor >= 2.5 AND interval_days >= 21`. The dashboard's per-level progress bar displays `mastered / total_in_level`.

### 5.6 `today` semantics

`today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()`. All date math is epoch-day. The crossing-midnight problem doesn't apply because we only ever compare days.

---

## 6. Module / package layering

```
ui/  ──────► viewmodel/ ──► domain/ (use cases) ──► data/ (repos)
                                                       ├── local/   (Room)
                                                       └── remote/  (Gemini)
```

- `data/local/` knows nothing about Compose.
- `domain/` is pure Kotlin, uses Flows and suspending functions only.
- ViewModels expose `StateFlow<UiState>` (sealed class per screen).
- Screens are stateless Composables; state flows in via `collectAsStateWithLifecycle()`.
- Navigation is centralised in `navigation/AppNavigation.kt` with typed routes (`navigation/Routes.kt`).

---

## 7. Threading rules

- IO (DB, network, file I/O): `Dispatchers.IO`.
- CPU work (JSON parsing on import): `Dispatchers.Default`.
- UI: `Dispatchers.Main.immediate` (default for Compose collectors).
- ViewModels never touch `Dispatchers.Main` explicitly; they launch in `viewModelScope` and rely on repository functions to switch.
- No `runBlocking` in production code — flagged by lint rule.
