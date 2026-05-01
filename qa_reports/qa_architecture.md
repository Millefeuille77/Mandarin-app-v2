# QA Report: Architect Agent (Spec Validation)
**Date:** 2026-05-01T00:00:00Z
**Agent Under Review:** Architect
**Verdict:** PASS WITH WARNINGS

---

## Summary

All four spec files exist and are structurally sound. The 15 screens (11 named + 4 hub screens) are consistently cross-referenced between `UX_SPECIFICATION.md` and `FOLDER_STRUCTURE.md`. All 10 phases have acceptance criteria, all dependency versions are pinned, the SM-2 algorithm is fully specified, and accessibility requirements meet or exceed the qa.md mandate. Two MAJOR issues were found: a garbled acceptance-criteria sentence in Phase 8 that a developer cannot interpret without re-asking the architect, and a meaningful ambiguity in the offline TTS fallback path — the `ARCHITECTURE.md` contract for `GeminiService.synthesize()` says offline returns `Offline` failure, but three separate places (§4.2, `FOLDER_STRUCTURE.md` comment, Phase 5 notes) imply `AndroidTtsFallback` should produce audio offline; the layering at the `AudioRepository` level is never formally specified. Four hub screens (`PracticeHubScreen`, `ExamHubScreen`, `MeScreen`, `ImportLoadingScreen`) have no `UiState.kt` file in `FOLDER_STRUCTURE.md`, which is a minor gap.

---

## Statistics

- Files reviewed: 8 (4 spec files + 5 exam data JSON files + `prompts/qa.md`)
- Total checks: 28
- Passed: 25
- Blockers: 0
- Major issues: 2
- Minor issues: 3

---

## 🔴 Blockers (must fix before proceeding)

*(none)*

---

## 🟡 Major Issues (should fix)

- [ ] **Garbled Phase 8 acceptance criterion — `specs/IMPLEMENTATION_PLAN.md` line 406** — The bullet reads: *"Streak rollover logic from `ARCHITECTURE` does not produced in spec — but ARCHITECTURE referenced UX §5.2: implementation matches: …"* The phrase "does not produced in spec" is grammatically broken and semantically unclear (did the architect mean the streak logic was originally missing from ARCHITECTURE.md, or that it IS in UX §5.2 but not §5.2 of ARCHITECTURE?). A developer reading this verbatim cannot determine the authoritative source for the streak behaviour. **Fix:** replace the garbled opening with a clear statement, e.g. *"Streak rollover logic is defined in UX §5.2; ARCHITECTURE.md §6 defers to it. Implementation must match: …"*

- [ ] **Offline TTS path is ambiguous — `specs/ARCHITECTURE.md` §4.3 vs §4.2 vs `specs/IMPLEMENTATION_PLAN.md` Phase 5 Notes** — Three conflicting signals exist:
  1. `GeminiService.synthesize()` step 3 (ARCHITECTURE §4.3) says: *"If offline → `Result.failure(Offline)`"* — meaning the service always fails when network is absent.
  2. ARCHITECTURE §4.2 models note says: *"if the live SDK rejects audio output, the implementation falls back to Android's built-in `TextToSpeech` (still works offline, no API call)"* — implying offline audio IS possible via `AndroidTtsFallback`.
  3. Phase 5 notes say: *"AudioRepository: checks cache → Gemini → fallback to Android TTS"* — implying `AudioRepository` catches the `Offline` error and calls `AndroidTtsFallback`.
  
  A junior developer could implement this in at least three incompatible ways: (a) `synthesize()` itself calls `AndroidTtsFallback` before the network check; (b) `AudioRepository` catches `Offline` and calls `AndroidTtsFallback`; (c) `AndroidTtsFallback` is only used when the SDK audio format is rejected (not for network-absent scenarios). `ARCHITECTURE.md` never defines `AudioRepository`'s public API or its fallback contract.
  
  **Fix:** Add a formal `AudioRepository` API contract to ARCHITECTURE.md (or §4 of GeminiService section) that explicitly states: *"When `synthesize()` returns `Offline`, `AudioRepository.play()` falls back to `AndroidTtsFallback.speak(text)` as a last resort. The fallback is not cached. Phase 5 acceptance criterion must cover this path."*

---

## 🟢 Minor Issues (fix when convenient)

- [ ] **Hub screens missing `UiState.kt` — `specs/FOLDER_STRUCTURE.md`** — `PracticeHubScreen`, `ExamHubScreen`, `MeScreen`, and `ImportLoadingScreen` each have a `*Screen.kt` and `*ViewModel.kt` but no `*UiState.kt`. The other 11 named screens all have explicit `UiState.kt` files. Either add the four files to the tree (if these ViewModels will expose any state at all) or add a note explaining that these hub screens are stateless pass-throughs that emit no `UiState`. Leaving it ambiguous risks the developer creating ad-hoc state classes outside the defined file tree, violating the "no files outside this tree" rule.

- [ ] **`AndroidTtsFallback` not mentioned by name in `ARCHITECTURE.md`** — `FOLDER_STRUCTURE.md` lists `data/audio/AndroidTtsFallback.kt` with comment "built-in TextToSpeech (offline)" and Phase 5 references it by name. However, ARCHITECTURE.md §4 only refers to "Android's built-in `TextToSpeech` engine" without naming the wrapper class. The class name `AndroidTtsFallback` is a developer-facing contract detail that belongs in the architecture doc. **Fix:** add one sentence naming `AndroidTtsFallback` in §4.2.

- [ ] **Phase 5 acceptance criterion for `AndroidTtsFallback` offline path is absent** — The Phase 5 checklist covers cached-audio offline (✓) but has no acceptance criterion that verifies `AndroidTtsFallback` is invoked when the cache misses and the device is offline. If no test criterion exists, this path will not be exercised in the QA gate. **Fix:** add a criterion: *"Offline + no cache: `AudioRepository.play()` produces audio via `AndroidTtsFallback`; the UI does not show an error."*

---

## ✅ Checks Passed

- [x] **All 4 spec files exist** — `ARCHITECTURE.md` (609 lines), `UX_SPECIFICATION.md` (569 lines), `FOLDER_STRUCTURE.md` (350 lines), `IMPLEMENTATION_PLAN.md` (523 lines).
- [x] **Screen-to-file cross-reference (15 screens)** — All 15 screens extracted from `UX_SPECIFICATION.md` (ExamHubScreen, ExamResultScreen, ExamScreen, FlashcardScreen, HomeScreen, ImportLoadingScreen, ListeningScreen, MeScreen, PassageScreen, PracticeHubScreen, ProgressScreen, ReadingListScreen, SettingsScreen, SpeakingScreen, VocabularyScreen) have matching `*Screen.kt` + `*ViewModel.kt` in `FOLDER_STRUCTURE.md`. Perfect 15/15 match in both directions.
- [x] **Exactly 10 phases** — `IMPLEMENTATION_PLAN.md` contains exactly 10 `## Phase N:` sections.
- [x] **Acceptance criteria present on all 10 phases** — Every phase has a `### Acceptance criteria` section with at least one `- [ ]` checklist item.
- [x] **Dependency versions pinned** — No `latest` or `+` in actual version value strings in `ARCHITECTURE.md`. The line 18 grep false-positive was a comment *explaining* the policy, not a version declaration.
- [x] **Gemini SDK version 0.2.2 pinned exactly** — `generative-ai = "0.2.2"` in `ARCHITECTURE.md` §1.2 and `generativeai:0.2.2` in `IMPLEMENTATION_PLAN.md` Phase 5. No `0.2.1` anywhere.
- [x] **HSK grading data verified against exam files** — All 5 `data/exams/hsk{1-5}_exam_structure.json` files match the architect's specification: HSK 1–2 = 200/120, HSK 3–5 = 300/180. Data-backed, not assumption.
- [x] **Android TTS fallback for offline TTS documented** — `ARCHITECTURE.md` §4.2 states the fallback to `TextToSpeech` with `Locale.SIMPLIFIED_CHINESE` when the SDK rejects audio output. `FOLDER_STRUCTURE.md` lists `AndroidTtsFallback.kt` with correct comment.
- [x] **API key via `BuildConfig` only — not editable at runtime** — `ARCHITECTURE.md` §4.1 explicitly states key entry is via `local.properties` at build time, not at runtime. `UX_SPECIFICATION.md` Screen 11 (`SettingsScreen`) shows Gemini key as a read-only indicator with helper text "Set via local.properties at build time." No key-entry input field exists in the UX spec.
- [x] **No third-party charting library** — Neither `ARCHITECTURE.md` (dependency manifest) nor any spec references MPAndroidChart, Vico, or any other charting library. `ARCHITECTURE.md` §1.3 explicitly states "No charting library — the small line chart on `ProgressScreen` is drawn with Compose `Canvas`". `ExamScoresChart.kt` is listed in `FOLDER_STRUCTURE.md`.
- [x] **Hub screens in both UX and folder structure** — All 4 hub screens (`PracticeHubScreen`, `ExamHubScreen`, `MeScreen`, `ImportLoadingScreen`) appear in `UX_SPECIFICATION.md` navigation tree (§2) AND in `FOLDER_STRUCTURE.md` with matching `*Screen.kt` + `*ViewModel.kt`.
- [x] **SM-2 algorithm fully defined** — `ARCHITECTURE.md` §5 includes: initial ease factor 2.5, ease floor 1.3, quality scale 0–3 with exact mapping to SM-2's 0–5 scale, full `review()` Kotlin code with interval formula, reset-on-lapse (q==0), Hard penalty (0.8×), `getSession()` selection logic, mastered definition (`repetition_count ≥ 5 AND ease_factor ≥ 2.5 AND interval_days ≥ 21`), and `today` epoch-day semantics.
- [x] **No Gemini essay scoring at HSK 5 writing** — `IMPLEMENTATION_PLAN.md` Phase 7 Notes explicitly states: *"Out-of-scope: Gemini scoring of essays."* Grading for writing questions uses exact-match, lower-cased, whitespace-trimmed.
- [x] **Accessibility requirements specified** — `UX_SPECIFICATION.md` §1.5 mandates 56 dp touch targets (hard requirement). §1.3 sets body text at 18 sp as default. §1.2 verifies WCAG AAA (7:1 for body, 4.5:1 for large text). §1.8 explicitly prohibits swipe-only gestures and requires tap equivalents. §6 final checklist enumerates all requirements.
- [x] **HSK grading uses HSK standard, not percentages** — `ARCHITECTURE.md` tables specify 200/300 max scores and 120/180 passing scores. Cross-phase guarantees in `IMPLEMENTATION_PLAN.md` explicitly say "HSK grading uses 0–200 (HSK 1–2) or 0–300 (HSK 3–5) scale per the structure data — never percentages."
- [x] **Offline strategy documented** — `ARCHITECTURE.md` §4.3 and §4.4 document: cached audio used offline, `GeminiError.Offline` returned when no network, UI shows "Offline — using cached audio only" toast. `UX_SPECIFICATION.md` includes `OfflineBanner` component (§3.10) and per-screen offline states.
- [x] **Gemini API error handling fully specified** — `ARCHITECTURE.md` §4.4 error matrix covers: no API key, no network, cache hit, 30s timeout, HTTP 429 with exponential backoff (1s/2.0x/3 attempts), HTTP 5xx, and unknown exception. All 6 `GeminiError` subtypes have documented UI responses.
- [x] **Navigation depth ≤ 3 taps** — `UX_SPECIFICATION.md` §2 explicitly analyses and confirms the 3-tap constraint: FlashcardScreen is reachable in exactly 3 taps (Practice tab → VocabularyScreen → FlashcardScreen). Settings reachable in 2 taps.
- [x] **Data import strategy defined** — `ARCHITECTURE.md` §3 fully specifies: JSON from `res/raw/` via `openRawResource`, `JsonImporter.kt` with `importIfNeeded()` returning `Flow<ImportProgress>`, chunked inserts of 200 rows in transactions, error rollback with retry button, and version tracking via `data_version` table.
- [x] **API key via `BuildConfig`, never hardcoded** — `ARCHITECTURE.md` §4.1 states: `buildConfigField("String", "GEMINI_API_KEY", ...)`, "Code reference is **only** `BuildConfig.GEMINI_API_KEY`. The string literal must NEVER appear in source files or version control."
- [x] **`PracticeHubScreen` hub screen fully specified** — Appears in navigation tree (UX §2), in `FOLDER_STRUCTURE.md`, and Phase 3 includes its full implementation.
- [x] **`ExamHubScreen` hub screen fully specified** — Appears in navigation tree, folder structure, and Phase 7 includes full implementation.
- [x] **`MeScreen` hub screen fully specified** — Appears in navigation tree, folder structure, and Phase 8 includes full implementation.
- [x] **`ImportLoadingScreen` hub screen fully specified** — Appears in navigation tree, folder structure, `ARCHITECTURE.md` §3.4 with full layout specification, and Phase 2 includes full implementation.
- [x] **Module layering defined** — `ARCHITECTURE.md` §6 defines `ui → viewmodel → domain → data` layering with explicit rules (data/local knows nothing about Compose; domain is pure Kotlin; ViewModels expose `StateFlow`).
- [x] **Threading rules specified** — `ARCHITECTURE.md` §7 assigns IO/CPU/UI dispatchers and prohibits `runBlocking` in production code.

---

## Recommendation

**PROCEED**

The specs are coherent and implementation-ready for a developer. The two MAJOR issues should be fixed by the Architect before Phase 5 (TTS offline path) and before Phase 8 (streak AC wording) reach the developer. These can be patched as spec amendments without re-running the full architect pass. The three MINOR issues can be addressed at any point without blocking development.

**If the Architect chooses to patch before developer invocation:**
1. `IMPLEMENTATION_PLAN.md` Phase 8, first AC bullet: rewrite the garbled sentence to clarify that UX §5.2 is the authoritative streak spec.
2. `ARCHITECTURE.md` §4: add a formal `AudioRepository.play()` contract stating that when `GeminiService.synthesize()` returns `GeminiError.Offline` and the cache is empty, `AudioRepository` falls back to `AndroidTtsFallback.speak(text)`.
3. `FOLDER_STRUCTURE.md`: either add `PracticeHubUiState.kt`, `ExamHubUiState.kt`, `MeUiState.kt`, `ImportLoadingUiState.kt` to the file tree, or document that these hub screens are stateless pass-throughs.
