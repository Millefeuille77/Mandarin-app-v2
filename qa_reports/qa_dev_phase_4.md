# QA Report — Developer Phase 4: Reading Section + Nav Wiring

**Date:** 2026-05-01
**QA Agent:** Sonnet (Claude Code subagent)
**Phase under review:** Phase 4 — Reading Section + carried-over navigation wiring
**Prior verdict:** Phase 3 PASS WITH WARNINGS (B=0 M=1 m=1) — both issues confirmed resolved.

---

## 1. Summary

| Check | Result |
|---|---|
| File length ≤ 300 lines (all Phase 4 files) | PASS |
| No `LiveData` / `MutableLiveData` | PASS |
| No `!!` operator in production code | PASS |
| ViewModels do not reference DAOs directly | PASS |
| Icon/Image contentDescription in Phase 4 files | PASS |
| Hardcoded user-facing strings in Phase 4 UI | PASS |
| Touch targets ≥ 56 dp on interactive elements | PASS |
| Nav routes (Vocab/Flashcard/ReadingList/Passage) use real ViewModels | PASS |
| PinyinText: punctuation not tappable, contentDescription empty | PASS |
| PinyinText: pinyin toggle hides row | PASS |
| PassageScreen: font slider present | PASS |
| PassageScreen: pinyin toggle present | PASS |
| HSK 1–3 vs 4–5 annotation handling in `toAnnotatedCharacters` | PASS |
| Phase 3 QA issues resolved (M-1 hardcoded strings, m-1 touch target) | PASS |

---

## 2. Blocker Issues (🔴)

**None found.**

---

## 3. Major Issues (🟡)

**None found.**

---

## 4. Minor Issues (🟢)

### m-1 — `PassageControls.kt` not enumerated in `FOLDER_STRUCTURE.md`

**File:** `app/src/main/java/com/mandarinlearn/ui/reading/PassageControls.kt`

**Observation:** This helper file was created to keep `PassageScreen.kt` under 300 lines (it reached 419 lines without the split). The file is not listed in `specs/FOLDER_STRUCTURE.md`. The developer's rationale is sound — CLAUDE.md's 300-line hard rule overrides omissions in the folder structure spec — and the placement in `ui/reading/` is architecturally correct. No functional impact.

**Recommendation:** Architect agent should add `PassageControls.kt` (and `NoDefinitionSheet.kt`) to `specs/FOLDER_STRUCTURE.md` in a housekeeping pass. No developer action required before Phase 5.

### m-2 — `NoDefinitionSheet.kt` not enumerated in `FOLDER_STRUCTURE.md`

**File:** `app/src/main/java/com/mandarinlearn/ui/components/NoDefinitionSheet.kt`

**Observation:** Added to satisfy the Phase 4 acceptance criterion for the "character not in vocabulary" graceful fallback. Placement in `ui/components/` alongside `CharacterDefinitionSheet.kt` is correct. Not listed in `FOLDER_STRUCTURE.md`. Same housekeeping note as m-1.

### m-3 — `PlayChineseAudioUseCase` "Play all" button is a no-op stub

**File:** `app/src/main/java/com/mandarinlearn/ui/reading/PassageControls.kt` line 118

**Observation:** The IconButton's `onClick` is a `/* TODO(phase_5) */` comment with no action. This is explicitly approved in the Phase 4 spec notes ("the implementation can return `NotAvailable` until Phase 5 wires Gemini"). However, there is **no user-visible feedback** when the button is tapped — it silently does nothing. Per CLAUDE.md product goals and UX spec, 60-year-old users pressing a button that gives no response is confusing.

**Recommendation:** Add a lightweight Snackbar message (e.g., `R.string.audio_coming_soon` or reuse an existing "Audio coming soon" string) when the stub button is tapped, consistent with the pattern already implemented in `FlashcardScreen` for the flashcard audio button. This is a Phase 5 pick-up, not a blocker for Phase 4, but should be captured as a Phase 5 TODO.

---

## 5. Detailed Findings

### 5.1 File Length (300-line rule)

Checked via `find … wc -l | awk '$1>300'` — **no output produced**. All production `.kt` files are within the limit:

| File | Lines |
|---|---|
| `PassageScreen.kt` | 267 |
| `ReadingListScreen.kt` | 279 |
| `PassageViewModel.kt` | 169 |
| `ReadingListViewModel.kt` | 84 |
| `PassageControls.kt` | 183 |
| `AppNavigation.kt` | 263 |
| `PinyinText.kt` | 179 |
| `NoDefinitionSheet.kt` | 84 |
| `PinyinAnnotationModels.kt` | 43 |

### 5.2 LiveData / `!!` / DAO-in-ViewModel

- `grep LiveData|MutableLiveData` — zero production imports. Comments in `PassageUiState.kt`, `ReadingListViewModel.kt`, and others are "No LiveData" reminders — correct.
- `grep "!!"` in production code — **zero hits**.
- `grep "Dao"` in `ui/` package — **zero hits**. ViewModels access only repositories.

### 5.3 Icon / Image contentDescription Audit

The grep on Phase 4 files returned three `Icon(` call sites in `PassageControls.kt`:

- **Line 94** (`TextDecrease`): `contentDescription = stringResource(R.string.reading_font_decrease)` — compliant.
- **Line 109** (`TextIncrease`): `contentDescription = stringResource(R.string.reading_font_increase)` — compliant.
- **Line 123** (`VolumeUp`): `contentDescription = null` — correct because the parent `IconButton` sets `Modifier.semantics { contentDescription = playDesc }` which carries the accessibility label. TalkBack reads the button, not the icon inside. Compliant.

### 5.4 Hardcoded User-Facing Strings

Python regex scan across all Phase 4 UI files — **0 hits**. All visible strings route through `stringResource(...)`. The report confirms 22 new entries added to `res/values/strings.xml`.

### 5.5 Touch Targets

- `PassageControls.kt` line 117–128: `IconButton(modifier = Modifier.size(MinTouchTarget))` where `MinTouchTarget` is the 56 dp constant from `Dimensions.kt`. Compliant.
- `PassageControls.kt` line 153–158: `TextButton(modifier = Modifier.fillMaxWidth().height(MinTouchTarget))` — compliant (height = 56 dp).
- `PinyinCharCell`: `Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 56.dp)` — meets the 56 dp vertical target. Width is 48 dp per the spec note ("48.dp width" in Phase 4 notes). Compliant.
- `MandarinPrimaryButton` at `PassageScreen.kt:193` — this is the shared component which has enforced 56 dp from Phase 1. Compliant.

### 5.6 AppNavigation — ViewModel Wiring

Confirmed in `AppNavigation.kt` (263 lines):

- **`VOCABULARY` route (lines 100–121):** When `appContainer != null`, creates `VocabularyViewModel` via `VocabularyViewModel.factory(appContainer.vocabularyRepository, hsk)`. Real ViewModel wired. Legacy fallback kept for preview/test.
- **`FLASHCARDS` route (lines 123–147):** When `appContainer != null`, creates `FlashcardViewModel` via factory with `vocabularyRepository`, `audioRepository`, `reviewVocabularyUseCase`, and `hsk`. Real ViewModel wired.
- **`READING_LIST` route (lines 151–171):** `ReadingListViewModel.factory(appContainer.readingRepository, hsk)`. Real ViewModel wired.
- **`PASSAGE` route (lines 174–197):** `PassageViewModel.factory(passageId, appContainer.readingRepository, appContainer.vocabularyRepository)`. Real ViewModel wired.
- Phase 1 placeholder fallbacks (no-arg overloads) retained correctly for preview contexts.

### 5.7 PinyinText Correctness

Full read of `PinyinText.kt` (179 lines) and `PinyinAnnotationModels.kt` (43 lines):

- **Every character rendered:** `FlowRow` iterates all `AnnotatedCharacter` entries — no filtering or skipping.
- **Punctuation handling:** `isTappable = annotation.pinyin.isNotEmpty()` in `toAnnotatedCharacters()`. When `pinyin == ""`, `isTappable = false`, `clickable` modifier is not applied, and `cellDesc = ""` (empty contentDescription for TalkBack). The pinyin `Text` renders an empty string rather than being absent — preserves row height for visual alignment. Correct per spec.
- **Pinyin toggle:** The `if (showPinyin)` block wraps the pinyin `Text` — hiding it removes it from composition completely, not just invisible. Instant toggle. Correct.
- **Font slider:** `fontScale` multiplied on both `PinyinStyle.fontSize.value` and `HanziLargeStyle.fontSize.value`. Live-updates on recompose. Correct.
- **HSK 1–3 vs 4–5:** The composable itself is annotation-agnostic — it renders what it receives. The repository layer is responsible for providing full vs key-only annotations, which is correctly validated by `ReadingRepositoryTest.kt`. The UI layer handles both gracefully.

### 5.8 PassageScreen Feature Completeness

- **Pinyin toggle:** `PassageViewModel.togglePinyin()` flips `showPinyin` in the `PassageContent` UiState — wired to `PassageControlsBar`.
- **Font slider:** `onFontScale` lambda wired from `PassageControlsBar` Slider through `PassageViewModel.setFontScale()` into the UI state.
- **Mark as read button:** Present in `PassageScreen.kt:193` as `MandarinPrimaryButton` calling `onMarkRead`.
- **Character tap → definition sheet:** `onCharacterTapped` in `PassageViewModel` sets `selectedCharacter`; `PassageScreen` shows `CharacterDefinitionSheet` or `NoDefinitionSheet` based on vocab lookup result.
- **Loading / Error states:** `PassageUiState.Loading` → `LoadingState` composable; `PassageUiState.Error` → `ErrorState` composable.

### 5.9 Phase 3 QA Regression Check

Both Phase 3 WARNINGS confirmed resolved per developer report and no grep evidence of recurrence:
- M-1 hardcoded strings in `ImportLoadingScreen.kt` — no hits in current hardcoded-string scan.
- m-1 `ConfirmDialog TextButton` 56 dp target — `defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)` applied; outside Phase 4 scope but confirmed not reverted.

---

## 6. Tests Assessment

Phase 4 delivered:
- `ReadingRepositoryTest.kt` (197 lines, 8 unit tests) — covers pinyin annotation parse for HSK 1 vs HSK 4, mark-completed, loading states.
- `HomeScreenTest.kt` (58 lines, 2 instrumented smoke tests) — placeholder as specified; Phase 8 will extend.

Test coverage is appropriate to the phase scope. No unit tests for `PinyinText` composable itself (Compose UI tests were not specified for this phase). Minor observation only.

---

## 7. Spec Compliance Summary

| Spec requirement | Status |
|---|---|
| PinyinText stacks pinyin for HSK 1–3; key vocab only for HSK 4–5 | PASS |
| Character tap → CharacterDefinitionSheet (or NoDefinitionSheet) | PASS |
| Punctuation not tappable, TalkBack semantics empty | PASS |
| Font slider 0.8×–1.6× live updates | PASS |
| Pinyin toggle instant | PASS |
| Mark as read toggles `is_completed` | PASS |
| Loading + Error states render | PASS |
| FlowRow wraps without overflow | PASS |
| Tests present and passing (reported by developer) | PASS |

---

## 8. Verdict

**Verdict: PASS WITH WARNINGS**
**Counts: B=0 M=0 m=3**

The three minor issues (two folder-structure doc gaps and one silent no-op button) do not block Phase 5. The Reading section implementation is complete and spec-compliant. Nav wiring for all four screens is correctly implemented with real ViewModels.

**PROCEED — Phase 5 (Listening Section / Gemini TTS) may begin; carry forward m-3 (add Snackbar to "Play all" stub) as the first fix in Phase 5.**
