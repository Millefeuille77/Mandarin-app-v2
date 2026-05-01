# Phase 5 Report: Listening Section + Gemini TTS

## Files Created (new)

| File | Lines |
|---|---|
| `app/src/main/java/com/mandarinlearn/data/remote/GeminiService.kt` | 129 |
| `app/src/main/java/com/mandarinlearn/data/remote/GeminiError.kt` | 49 |
| `app/src/main/java/com/mandarinlearn/data/remote/GeminiPrompts.kt` | 55 |
| `app/src/main/java/com/mandarinlearn/data/remote/AudioBlob.kt` | 29 |
| `app/src/main/java/com/mandarinlearn/data/remote/BackoffPolicy.kt` | 65 |
| `app/src/main/java/com/mandarinlearn/data/audio/AndroidTtsFallback.kt` | 121 |
| `app/src/main/java/com/mandarinlearn/data/audio/AudioPlayer.kt` | 78 |
| `app/src/main/java/com/mandarinlearn/util/NetworkMonitor.kt` | 82 |
| `app/src/main/java/com/mandarinlearn/ui/listening/ListeningUiState.kt` | 56 |
| `app/src/main/java/com/mandarinlearn/ui/listening/ListeningViewModel.kt` | 239 |
| `app/src/main/java/com/mandarinlearn/ui/listening/ListeningComponents.kt` | 295 |
| `app/src/test/java/com/mandarinlearn/data/remote/BackoffPolicyTest.kt` | 127 |
| `app/src/test/java/com/mandarinlearn/data/remote/GeminiServiceTest.kt` | 112 |
| `app/src/test/java/com/mandarinlearn/data/repository/AudioRepositoryTest.kt` | 179 |
| `app/src/test/java/com/mandarinlearn/viewmodel/ListeningViewModelTest.kt` | 178 |
| `app/src/androidTest/java/com/mandarinlearn/data/local/AudioCacheDaoTest.kt` | 128 |

**Total new: 16 files, ~1,922 lines**

## Files Replaced / Updated

| File | Action | Lines |
|---|---|---|
| `app/src/main/java/com/mandarinlearn/data/repository/AudioRepository.kt` | **REPLACED** Phase 2 stub with full §4.6 algorithm | 209 |
| `app/src/main/java/com/mandarinlearn/data/repository/ListeningRepository.kt` | **REPLACED** Phase 2 stub with SampleQuestionDao queries | 91 |
| `app/src/main/java/com/mandarinlearn/ui/listening/ListeningScreen.kt` | **REPLACED** placeholder with full Screen 7 implementation | 230 |
| `app/src/main/java/com/mandarinlearn/ui/listening/ListeningViewModel.kt` | **REPLACED** skeleton with full Phase 5 ViewModel | 239 |
| `app/src/main/java/com/mandarinlearn/di/AppContainer.kt` | **UPDATED** — wired GeminiService, NetworkMonitor, AndroidTtsFallback, AudioRepository (full), ListeningRepository (full) | 163 |
| `app/src/main/java/com/mandarinlearn/navigation/AppNavigation.kt` | **UPDATED** — wired ListeningViewModel factory; added audioRepository to PassageViewModel factory | 279 |
| `app/src/main/java/com/mandarinlearn/ui/reading/PassageViewModel.kt` | **UPDATED** — added audioRepository param, `playAll()` method, PassageEvent sealed class | 225 |
| `app/src/main/java/com/mandarinlearn/ui/reading/PassageUiState.kt` | **UPDATED** — added `isPlayingAll`, `playAllFailed` fields to Content | 50 |
| `app/src/main/java/com/mandarinlearn/ui/reading/PassageScreen.kt` | **UPDATED** — wired `onPlayAll`, LaunchedEffect for PassageEvent, onPlayAll param | 284 |
| `app/src/main/java/com/mandarinlearn/ui/reading/PassageControls.kt` | **UPDATED** — Play all button wired to real `onPlayAll`, shows spinner while playing | 199 |
| `app/src/main/java/com/mandarinlearn/ui/vocabulary/FlashcardViewModel.kt` | **UPDATED** — improved audio failure snackbar message (Phase 5 AudioRepository failure reason) | 186 |
| `app/build.gradle.kts` | **UPDATED** — added `google-generative-ai` dependency | 138 |
| `app/src/main/res/values/strings.xml` | **UPDATED** — added 21 new string resources for Phase 5 | 183 |

**Total updated: 13 files**

## Dependencies Added

- `com.google.ai.client.generativeai:generativeai:0.2.2` — pinned per ARCHITECTURE.md §1.2. Already declared in `gradle/libs.versions.toml` (from Phase 1 planning). Added `implementation(libs.google.generative.ai)` to `app/build.gradle.kts`.

## QA Fixes from Phase 4

- [x] **m-3 fixed** — `PassageControls.kt` "Play all" button was a silent no-op. Now wired to `AudioRepository.play(passage.chineseText)` via `PassageViewModel.playAll()`. Shows `CircularProgressIndicator` while playing; on `AudioPlaybackState.Failed`, emits `PassageEvent.ShowSnackbar` which triggers snackbar "Audio coming soon" in `PassageScreen.kt`. This matches the QA recommendation exactly.

## Acceptance Criteria Status

- [x] `BuildConfig.GEMINI_API_KEY` wired via `local.properties`. Blank key → `GeminiError.NoApiKey` (degraded mode). — `GeminiService.kt` line 47.
- [x] TTS output cached in `audio_cache`. Cache hits bypass Gemini. — `AudioRepository.kt` steps 2–3.
- [x] Cache eviction runs when `totalBytes() > 50 MB`. — `AudioRepository.evictCacheIfNeeded()`.
- [x] Network timeout exactly 30 s (`withTimeout(30_000)`). — `GeminiService.kt` line 72.
- [x] HTTP 429 triggers exponential backoff: initial 1 s, multiplier 2, max 3 attempts. — `BackoffPolicy.kt`.
- [x] Offline mode: ListeningScreen works for cached audio; uncached falls to AndroidTtsFallback. — `AudioRepository.kt` steps 4–5.
- [x] Multiple-choice quiz: 4 options, one selectable, immediate feedback (color + icon + text). — `ListeningComponents.kt` `FeedbackCard`.
- [x] Replay limit per question is 3. — `ListeningViewModel.kt` `MAX_REPLAYS = 3`.
- [x] TalkBack announcements per UX §4 Screen 7: Play button desc, option descs, feedback announce. — `ListeningComponents.kt` `semantics { contentDescription }`.
- [x] No API key string in source code — only `BuildConfig.GEMINI_API_KEY`. — verified with grep.

## Deviations from Spec

1. **GeminiService.synthesize() always returns failure** — Gemini SDK 0.2.2 does not support `responseMimeType = "audio/mpeg"` for audio output via the standard `generateContent` route. As noted in IMPLEMENTATION_PLAN §Phase 5 spec note: "if at integration time the `responseMimeType = 'audio/mpeg'` route is unavailable, fall through to `AndroidTtsFallback`." The implementation correctly falls through to `AndroidTtsFallback` every time (which speaks real Simplified Chinese audio). The cache-hit path will work correctly when real audio bytes are provided (e.g., by a future SDK upgrade). The `TODO(phase_6)` comment in `GeminiService.kt` marks the upgrade point.

2. **ListeningComponents.kt not in FOLDER_STRUCTURE.md** — Created to comply with the 300-line hard rule (ListeningScreen.kt reached 538 lines without the split). Placement in `ui/listening/` is architecturally correct. Same pattern as `PassageControls.kt` (Phase 4 QA m-1). Architect housekeeping pass should add it.

3. **Cache key uses `sha256("text|speed")` instead of `sha256("text|voice|speed")`** — ARCHITECTURE.md §4.6 specifies `sha256("${text}|${speed}")` (2-field key). `HashUtil.audioCacheKey()` uses a 3-field key including `voice`. `AudioRepository` uses the 2-field spec (`sha256("$text|${"%.2f".format(speed)}")`). The `HashUtil.audioCacheKey()` helper is available for future phases that use multiple voices. No functional conflict; each is used in its own context.

4. **ViewModel snackbar messages not resolved from stringResource** — The ViewModel-level audio failure message is passed as a raw string from `AudioRepository` (the `reason` field of `AudioPlaybackState.Failed`). This is a known acceptable pattern in this project — ViewModels don't hold Context. The FlashcardScreen displays this string as-is. The string value ("Audio not available — please install Chinese TTS voice in Android Settings") is the canonical UX spec message. UI-layer strings are all in `strings.xml`; the ViewModel's internal error propagation follows the existing project pattern.

## Known Issues / TODOs for Later Phases

- `TODO(phase_6)`: `GeminiService.transcribeAndScore()` stub → full STT implementation.
- `TODO(phase_8)`: `GeminiService.chat()` stub → full chat implementation.
- `TODO(phase_6)`: Upgrade GeminiService.synthesize() when SDK supports audio output.
- `TODO(phase_9)`: Audio playback speed read from DataStore `UserPreferencesRepository` and passed to `AudioRepository.play(speed = ...)`.

## String Resources Added (21 new entries)

| Key | English text |
|---|---|
| `R.string.listening_question_counter` | "Question %1$d of %2$d" |
| `R.string.listening_tap_to_play` | "Tap to play" |
| `R.string.listening_play_button_desc` | "Play audio. Question %1$d of %2$d" |
| `R.string.listening_replay_desc` | "Replay audio" |
| `R.string.listening_replays_remaining` | "%1$d replays remaining" |
| `R.string.listening_audio_failed` | "Could not play audio — try again or skip" |
| `R.string.listening_skip` | "Skip" |
| `R.string.listening_option_desc` | "Option %1$s: %2$s" |
| `R.string.listening_correct` | "Correct!" |
| `R.string.listening_incorrect` | "Not quite." |
| `R.string.listening_correct_answer` | "The answer was: %1$s" |
| `R.string.listening_feedback_correct_desc` | "Correct answer" |
| `R.string.listening_feedback_incorrect_desc` | "Incorrect. The answer was %1$s" |
| `R.string.listening_session_complete` | "Session complete!" |
| `R.string.listening_session_score` | "%1$d of %2$d correct" |
| `R.string.listening_try_again` | "Try again" |
| `R.string.listening_empty_title` | "No questions available" |
| `R.string.listening_empty_body` | "Listening questions will appear here once imported." |
| `R.string.audio_not_available` | "Audio not available — please install Chinese TTS voice in Android Settings" |
| `R.string.audio_play_all` | "Play all" |
| `R.string.audio_coming_soon` | "Audio coming soon" |
