# QA Report — Developer Phase 7: Exam Section + Grading
**Date:** 2026-05-02
**Validator:** QA Agent (claude-sonnet-4-6)
**Phase:** 7 — Exam Section + Grading
**Source report:** `reports/phase_7_report.md`

---

## Verdict

**PASS WITH WARNINGS**

---

## Statistics

| Category | Count |
|---|---|
| Blockers (🔴) | 0 |
| Majors (🟡) | 2 |
| Minors (🟢) | 1 |
| Checks passed | 15 |

---

## Blockers — 0

None.

---

## Majors — 2

### M-1: `ExamViewModel.kt` is exactly 296 lines — at the boundary, needs monitoring
**File:** `app/src/main/java/com/mandarinlearn/ui/exam/ExamViewModel.kt` (296 lines)

The file is within the 300-line cap but leaves only 4 lines of headroom. Any future bugfix or Phase 10 wiring (AudioRepository, StreakRepository) that adds even a few lines will push it over the limit. The developer should proactively split timer logic or event emission into a helper class before that phase to prevent a future blocker.

Severity: 🟡 MAJOR (not a current violation; preventive action required before Phase 10 wiring).

### M-2: In-exam audio for listening questions not wired (deferred gap)
**Files:** `app/src/main/java/com/mandarinlearn/ui/exam/ExamScreen.kt`, `ExamViewModel.kt`

The Phase 7 spec acceptance criterion states: "All ListeningScreen audio in exam-listening mode reuses `AudioRepository.play(text)` from Phase 5." The developer has deferred this to `TODO(phase_10)`. The criterion does not say "deferred is acceptable" — it says the feature must be present. The workaround (rendering question text instead of playing audio) is functional for grading but breaks the listening exam experience for the target user.

Severity: 🟡 MAJOR (functionality gap vs. acceptance criterion; explicitly acknowledged in report deviation section).

---

## Minors — 1

### m-1: `WrongAnswerDetail.questionText` shows questionId, not question text
**File:** `app/src/main/java/com/mandarinlearn/ui/exam/ExamResultScreen.kt`

The review-mistakes panel shows the `questionId` string instead of the full question text. The developer acknowledged this as a `TODO(phase_10)` deviation. For a 60-year-old target user this could be confusing, but correct/wrong answer strings are always shown and the questionId is a reference. Low impact for Phase 7.

Severity: 🟢 MINOR.

---

## Checks Passed — 15

1. **File length cap (≤ 300 lines):** All 14 new and modified production `.kt` files are within the 300-line limit. Largest: `ExamViewModel.kt` at 296, `ExamScreen.kt` at 277, `ExamResultScreen.kt` at 283, `ExamHubScreen.kt` at 229. `ExamGrader.kt` at 128 lines.
2. **No LiveData:** Zero `LiveData` or `MutableLiveData` instantiations in production code. Comment-only references in older files confirmed to be documentation.
3. **No `!!` null-assertion operator:** Zero occurrences in exam package or domain grading package (checked across all production `.kt` files).
4. **ViewModels do not reference DAOs or GeminiService:** `grep` of `ui/exam/*.kt` for `Dao` and `GeminiService` returned zero hits. Architecture boundary respected.
5. **No hardcoded grading constants:** `ExamGrader.kt` and all exam UI files contain no bare `= 200`, `= 120`, `= 300`, or `= 180` numeric literals. All thresholds flow through `ExamStructure.totalMaxScore` and `ExamStructure.totalPassingScore`.
6. **Grading is data-driven from JSON:** Confirmed via `data/exams/hsk1_exam_structure.json` (`total_max_score: 200`, `total_passing_score: 120`) and `data/exams/hsk3_exam_structure.json` (`total_max_score: 300`, `total_passing_score: 180`). `ExamGrader.gradeExam()` reads these fields from the `ExamStructure` model parameter — no level-specific code paths.
7. **No hardcoded user-facing strings in Composables:** Python regex scan of all `ui/exam/*.kt` files for `Text("` with uppercase-initial content found 0 matches. All user-facing text uses `stringResource(...)`.
8. **Icons have contentDescription:** The grep flag at `ExamHubScreen.kt:199` was a false positive — `contentDescription = passLabel` is set on line 201 (multi-line argument). `ScoreBadge.kt` also correctly sets `contentDescription = iconDesc` on its Icon.
9. **Touch targets ≥ 56 dp:** The `clickable` modifier on the option card in `ExamComponents.kt` (line 135) is paired with `defaultMinSize(minHeight = 64.dp)` on line 136 — exceeds the 56 dp requirement. `MandarinSecondaryButton` inherits the standard minimum from the design system. No violations found.
10. **Pass/fail uses icon + text + color (never color alone):** `ScoreBadge.kt` explicitly shows `Icons.Filled.Check`/`Icons.Filled.Close` (icon) + label string (text) + `containerColor` (color). Comment in file header explicitly calls out CLAUDE.md rule. `ExamResultScreen.kt` history row also uses text label alongside color.
11. **AppNavigation wires real exam screens:** `AppNavigation.kt` imports `ExamResultScreen` and wires `ExamHubScreen`, `ExamScreen`, and `ExamResultScreen` with real ViewModels — not Phase 1 placeholders. Confirmed by grep output showing routes at lines 272, 274, 284, 286.
12. **ExamGrader algorithm correctness:** `scaleSection` uses proportional scaling with `coerceIn(0, maxScore)`. `gradeExam` sums section scores and `coerceAtMost(structure.totalMaxScore)`. Pass/fail evaluated as `totalScore >= structure.totalPassingScore`. No off-by-one; the require guard rejects invalid inputs.
13. **Accessibility — semantics on option cards:** Option card in `ExamComponents.kt` uses `.semantics { contentDescription = itemDesc; selected = isSelected; role = Role.RadioButton }` — fully TalkBack-complete.
14. **String resources complete:** 28 new exam string resources verified in `strings.xml` as listed in the phase report. All use format args (`%1$d`, `%1$s`) correctly.
15. **Tests created:** `ExamGraderTest.kt` (181 lines, 12 cases), `ExamViewModelTest.kt` (224 lines, 8 cases), `ExamRepositoryTest.kt` (195 lines, 7 cases), `ExamScreenTest.kt` (78 lines, 3 smoke cases) — all within 300-line cap. Coverage of the most safety-critical path (`ExamGrader`) is adequate.

---

## QA Fixes Required Before Phase 8

### Fix for M-2 (in-exam audio)
Wire `AudioRepository.play(text)` into `ExamViewModel` for listening-section questions before Phase 8 begins, or formally defer to Phase 8 with a spec amendment. The current `TODO(phase_10)` defers too late — the listening exam is unusable without audio.

### Preventive for M-1 (ExamViewModel size)
Extract `ExamTimerManager` or `ExamEventEmitter` as a separate class from `ExamViewModel.kt` before Phase 10 wiring to keep the file under 300 lines.

---

## Recommendation

**REWORK M-2 before Phase 8.** The in-exam audio gap (M-2) must be resolved or explicitly deferred with a spec amendment approved at the human gate — the listening exam section is non-functional without audio playback. All other Phase 7 acceptance criteria are met. The grading algorithm is correct, data-driven, and well-tested. Architecture boundaries are clean. Accessibility is properly implemented. Once M-2 is resolved (or formally deferred with sign-off), Phase 7 is clear to proceed.
