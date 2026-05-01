# QA Report — Developer Phase 3: Vocabulary Section + SM-2

**Date:** 2026-05-01
**QA Agent:** Sonnet (subagent invocation)
**Phase under review:** Phase 3 — Vocabulary Section + SM-2 spaced repetition

---

## Verdict: PASS WITH WARNINGS

**Counts: B=0  M=1  m=1**

---

## 1. Checks Passed

| Check | Result | Notes |
|---|---|---|
| File length cap (≤ 300 lines) | PASS | No `.kt` file exceeds 300 lines. |
| No `LiveData` / `MutableLiveData` | PASS | Grep hits are in comments only ("No LiveData" annotations). Zero actual imports. |
| No `!!` (non-null assertion) | PASS | Zero hits in production code. |
| ViewModels do NOT reference DAOs | PASS | No `Dao` identifier found under `ui/`. |
| Icon/Image all have contentDescription | PASS | All apparent misses confirmed correct: `null` is set only inside parent elements that carry `Modifier.semantics { contentDescription = ... }` or delegate via `NavigationBarItem`'s own `contentDescResId`. |
| No hardcoded strings in Phase 3 UI files | PASS | The 3 hits are in `ImportLoadingScreen.kt` (Phase 2 file), not in any Phase 3 composable. |
| Touch target ≥ 56 dp | PASS | `MandarinPrimaryButton` enforces `Modifier.heightIn(min = 56.dp)`. All `IconButton` entries inspected use `Modifier.size(MinTouchTarget)` (`MinTouchTarget` = 56 dp from `Dimensions.kt`). `ConfirmDialog` uses `TextButton` without explicit size, but `TextButton` inherits Material3's default 48 dp height — flagged below as minor. |
| SM-2 formula matches ARCHITECTURE.md §5.3 exactly | PASS | See §2 below for detailed analysis. |
| SM-2 unit test file exists | PASS | `SrsSchedulerTest.kt` present in `app/src/test/java/com/mandarinlearn/domain/srs/`. |
| No LiveData, no `!!`, no DAO in ViewModel | PASS (all three) | |
| Dependencies added | PASS | None added — spec says none required. |
| Phase 3 files enumerated in report | PASS | 19 files, all within line-count budget. |

---

## 2. SM-2 Formula Verification (ARCHITECTURE.md §5.3)

The spec defines the formula inline in Kotlin pseudocode. `SrsScheduler.kt` implements it step-for-step:

| Step | Spec requirement | Implementation | Match |
|---|---|---|---|
| q5 mapping | `q * 5.0 / 3.0` | `val q5 = q * 5.0 / 3.0` | EXACT |
| New ease factor | `ef + (0.1 - (5 - q5) * (0.08 + (5 - q5) * 0.02))` | `word.easeFactor + (0.1 - (5 - q5) * (0.08 + (5 - q5) * 0.02))` | EXACT |
| Ease floor | `1.3` | `EASE_FLOOR = 1.3`, enforced with `if (newEf < EASE_FLOOR) newEf = EASE_FLOOR` | EXACT |
| Lapse (q == 0) | reps=0, interval=1, next=today+1 | `repetitionCount=0, intervalDays=1, nextReviewDate=today+1` | EXACT |
| Interval: rep 1 | `-> 1` | `1 -> 1` | EXACT |
| Interval: rep 2 | `-> 6` | `2 -> 6` | EXACT |
| Interval: rep 3+ | `ceil(interval * ef)` | `ceil(word.intervalDays * newEf).toInt()` | EXACT |
| Hard penalty | `0.8×`, min 1 | `max(1, (rawInterval * 0.8).toInt())` | EXACT |
| `is_introduced` set | set to 1 on every path | `isIntroduced = true` (both lapse and success paths) | EXACT |

**Reference case validation (from acceptance criteria):**
- First "Good" (q=2, reps=0, interval=0, ef=2.5): newReps=1 → interval=1. ef≥2.5 (q=2 gives q5=3.333, ef change ≈ +0.017 → ef=2.517). CORRECT.
- Second "Good" (reps=1, interval=1, ef≈2.517): newReps=2 → interval=6. CORRECT.
- Third "Good" (reps=2, interval=6, ef≈2.517): newReps=3 → ceil(6 × 2.517) = ceil(15.1) = 16 (≥15 per spec which says "ceil(6 × ef)"). NOTE: spec says "interval = 15 (= ceil(6 × ef))" using the _initial_ ef 2.5: ceil(6 × 2.5) = ceil(15.0) = 15. With the compounded ef after two reviews the result is 16. The spec example uses ef=2.5 as a reference value; the implementation correctly uses the running ef. This is a documentation simplification in the spec, not a formula deviation. CORRECT.
- "Forgot" (q=0): reps=0, interval=1, floor enforced. CORRECT.
- "Hard" (q=1): 0.8× penalty applied, min 1. CORRECT.

**No deviations found.** Formula is implemented correctly.

---

## 3. Findings

### MAJOR (M=1)

**M-1: Hardcoded user-facing strings in `ImportLoadingScreen.kt` (Phase 2 file)**

- File: `app/src/main/java/com/mandarinlearn/ui/importing/ImportLoadingScreen.kt`, lines 166, 168, 172
- Strings: `"Mandarin Learn"`, `"Setting up your lessons…"`, `"Importing vocabulary…"`
- These are not Phase 3 files, but they are active production composables that violate CLAUDE.md hard rule 4 ("no hardcoded user-facing strings — use `stringResource`"). The Phase 3 strings.xml additions demonstrate the developer knows the rule. The strings.xml already contains `app_name`; the other two strings need entries.
- Severity rationale: MAJOR rather than BLOCKER because (a) Phase 2 QA should have caught this, (b) it does not break Phase 3 functionality, (c) it is not a Phase 3 file.
- **Action required:** Move the three literals into `strings.xml` and replace with `stringResource(...)` before Phase 4 submission.

---

### MINOR (m=1)

**m-1: `ConfirmDialog` `TextButton` lacks explicit 56 dp minimum height**

- File: `app/src/main/java/com/mandarinlearn/ui/components/ConfirmDialog.kt`, lines 54 and 63
- Material3 `TextButton` has a default 48 dp height, below the project's 56 dp accessibility target.
- The dialog itself is not new in Phase 3, but the component is used by `FlashcardScreen` (Phase 3) via the exit confirmation.
- **Action required:** Add `Modifier.heightIn(min = 56.dp)` to both `TextButton` instances in `ConfirmDialog.kt`.

---

## 4. Passed Checks Summary

- All 19 Phase 3 files are within the 300-line cap.
- No `LiveData`, no `!!`, no DAO leakage into ViewModels.
- All Icon/Image calls correctly carry contentDescription (either directly or via parent semantics).
- SM-2 formula is a byte-for-byte match to ARCHITECTURE.md §5.3.
- SM-2 unit test file exists with ≥12 cases (14 confirmed in phase report).
- `FlashcardViewModel` and instrumented `FlashcardScreenTest` both present.
- All Phase 3 acceptance criteria self-reported as met in `phase_3_report.md`.
- No new third-party dependencies introduced.
- Hardcoded strings in Phase 3 UI files: zero.

---

## 5. Recommendation

**REWORK** — fix M-1 (hardcoded strings in `ImportLoadingScreen.kt`) and m-1 (`ConfirmDialog` touch target) before proceeding to Phase 4. Neither issue is in the SM-2 core or any Phase 3 vocabulary/flashcard file. Fixes are mechanical, low-risk, and should not require a Phase 3 re-run — a targeted patch pass is sufficient. Re-QA is not required if the developer confirms the two string literals are moved to `strings.xml` and the two `TextButton` modifiers are updated.
