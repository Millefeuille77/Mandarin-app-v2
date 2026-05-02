# QA Report — Developer Phase 6: Speaking Section + Gemini STT

**Date:** 2026-05-02
**Reviewer:** QA Agent (Sonnet)
**Verdict:** PASS WITH WARNINGS
**Counts:** B=0 M=2 m=1

---

## 1. Verdict

**PASS WITH WARNINGS** — All blockers clear. Two majors require developer attention before Phase 7 begins: (1) the fallback score is permanently 0, giving no variance for non-network sessions; (2) three `Icon()` calls in `SpeakingComponents.kt` omit `contentDescription` where a parent-semantics argument is not always airtight. One minor is noted (ViewModel comment references TODO for a non-Phase-6 feature — harmless but imprecise).

---

## 2. Statistics

| Category | Count |
|---|---|
| Files checked | 12 |
| Blockers (🔴) | 0 |
| Majors (🟡) | 2 |
| Minors (🟢) | 1 |
| Checks passed | 15 |

---

## 3. Checks Passed

| # | Check | Result |
|---|---|---|
| 1 | File length cap (all ≤ 300 lines; SpeakingComponents.kt == 300) | PASS |
| 2 | No `LiveData` / `MutableLiveData` imports in production code | PASS — comments only |
| 3 | No `!!` (non-null assertion) in any Kotlin source | PASS |
| 4 | ViewModels do NOT reference DAOs or GeminiService directly | PASS — comment in `ExamResultViewModel` is a TODO placeholder (Phase 7 scope), not an import |
| 5 | `RECORD_AUDIO` permission in `AndroidManifest.xml` | PASS |
| 6 | `transcribeAndScore()` exists in `GeminiService.kt` and is non-stub | PASS |
| 7 | `transcribeAndScore` follows ARCH §4.3 (network gate, 30s timeout, multipart audio, JSON parse) | PASS |
| 8 | `SpeakingRepository` replaces Phase 2 stub | PASS |
| 9 | Text-similarity fallback exists in `SpeakingRepository` | PASS (score=0 path — see Major #1) |
| 10 | No hardcoded user-facing strings in speaking Composables (`Text("Capital…")` pattern = 0 hits) | PASS |
| 11 | Score card pairs color with icon AND text label (color never sole indicator) | PASS — triple of (color, icon, label) at lines 199-201 |
| 12 | `Routes.SPEAKING` composable wired in `AppNavigation.kt` | PASS — line 231 + 246/251 |
| 13 | MicButtonSize = 96.dp (well above 56 dp minimum) | PASS |
| 14 | `MandarinPrimaryButton` reused for action buttons (inherits 56 dp min target) | PASS |
| 15 | `SpeakingViewModelTest.kt` exists (280 lines) | PASS |

---

## 4. Blockers (🔴) — None

---

## 5. Majors (🟡)

### M-1: Fallback score always returns 0 — no variance

**File:** `app/src/main/java/com/mandarinlearn/data/repository/SpeakingRepository.kt` lines 86-95
**Also:** `app/src/main/java/com/mandarinlearn/data/remote/GeminiService.kt` lines 207-215

Both the repository-level and service-level `computeFallbackScore()` implementations unconditionally return `score = 0`. The Phase 6 report's own "Open Questions" section flags this risk: *"if fallback always returns 50, users won't perceive variance"* — in fact the implementation returns 0, which is worse. During development or in regions with flaky connectivity, every practise session ends with a failing score. The `computeSimilarityScore()` helper exists in `GeminiService.kt` (lines 218-225) and correctly computes a character-overlap ratio, but it is never invoked from the fallback path; `computeFallbackScore` ignores it entirely.

**Fix:** Invoke `computeSimilarityScore("", expectedText)` (or better, a cached last-transcription) in the fallback path, or at minimum use a neutral encouragement score (e.g. 50) rather than 0 so users are not penalised when the network drops mid-session.

**Severity rationale:** Not a blocker because the app remains functional (result screen renders, user can try again), but the UX for the stated target audience (60-year-old learners) is punishing and misleading.

---

### M-2: Three `Icon()` calls in `SpeakingComponents.kt` use `contentDescription = null` without bulletproof parent semantics

**File:** `app/src/main/java/com/mandarinlearn/ui/speaking/SpeakingComponents.kt` lines 166, 220, 268

All three follow the "parent carries the description" pattern:
- **Line 166** (mic icon inside `IconButton`): the `IconButton` modifier at line 157 sets `contentDescription` via `Modifier.semantics { contentDescription = micDesc }`. Correct.
- **Line 220** (score icon in `ScoreCard`): the enclosing `Card` at line 208 sets `contentDescription = scoreDesc`. Correct.
- **Line 268** (mic icon in permission-denied block): the parent `Column` has no semantics node. TalkBack will announce *"Mic, Image"* or skip — depends on traversal strategy. This is the vulnerable one.

**Fix:** Add `contentDescription = stringResource(R.string.speaking_mic_icon_desc)` (or `contentDescription = null` is acceptable only if the `Column` has `Modifier.semantics(mergeDescendants = true)` with a description). The missing string resource is a one-liner fix.

---

## 6. Minors (🟢)

### m-1: `ExamResultViewModel.kt` comment references GeminiService injection as a Phase 7 TODO

**File:** `app/src/main/java/com/mandarinlearn/ui/exam/ExamResultViewModel.kt` line 14

The comment `// TODO(phase_7): Inject ExamRepository, GeminiService (for explain)` is technically correct (Phase 7 scope) but is flagged by the `grep -rn "GeminiService" ui/` check as a potential ViewModel boundary violation. A reviewer unfamiliar with the context could flag it incorrectly in a future gate. Rename the TODO to `// TODO(phase_8): Inject GeminiService for "Explain this answer"` (Phase 8 is where chat is implemented per the plan) or clarify the comment.

---

## 7. Architecture / Spec Compliance Notes

| Requirement | Status |
|---|---|
| `transcribeAndScore` is non-stub and follows ARCH §4.3 | Confirmed |
| `SpeakingRepository` replaces Phase 2 stub | Confirmed |
| Recording state machine (Idle→Recording→Processing→Result→Idle) | Confirmed in `SpeakingViewModel` |
| `RECORD_AUDIO` permission in manifest | Confirmed |
| Score display: color + icon + text label (color never sole indicator) | Confirmed |
| No LiveData anywhere | Confirmed |
| No `!!` operator | Confirmed |
| All files ≤ 300 lines | Confirmed |
| No hardcoded user-facing strings in Composables | Confirmed |
| Navigation route wired | Confirmed |
| MicButtonSize = 96 dp (> 56 dp minimum) | Confirmed |
| `SpeakingViewModelTest.kt` exists | Confirmed |
| `PermissionsHelper.kt` placed in `util/` (precedent from Phase 2) | Accepted |
| `SpeakingComponents.kt` at exactly 300 lines — future additions must split | Noted; flagged for next developer phase |
| `computeSimilarityScore()` exists but not used in fallback | See Major M-1 |

---

## 8. Open Questions Carried Forward

1. **Fallback variance (M-1):** `computeSimilarityScore()` is dead code in the fallback path. Either wire it or delete it; the Phase 6 report's intent was to use text similarity, not to hard-code 0.
2. **`SpeakingComponents.kt` at cap:** Phase 7 or later must split this file before adding any composable to it.
3. **Gemini SDK audio rejection:** The `GeminiError.Unknown` catch-all in `transcribeAndScore` means a Gemini SDK audio rejection silently activates the fallback. Consider adding a distinct `GeminiError.AudioNotSupported` variant so the UI can surface a more helpful message ("Audio scoring not supported on this device").

---

## 9. Recommendation

**PROCEED** — Phase 7 may begin. Developer must address M-1 (fallback score) and M-2 (permission-block icon contentDescription) as part of Phase 7's cross-phase guarantee sweep or as a targeted patch before Phase 7 QA gate. No blockers prevent forward progress.
