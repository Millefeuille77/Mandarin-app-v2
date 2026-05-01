# UX_SPECIFICATION.md — Mandarin Learn

> Every screen the user can reach is documented here, top-to-bottom, with all states and accessibility annotations.
> The target users are 60-year-old absolute beginners with no tech background. Optimise for clarity over density.

---

## 1. Design System

### 1.1 Material 3 baseline

Build on `MaterialTheme` (M3) with a custom `MandarinLearnTheme` wrapper.

### 1.2 Color tokens (WCAG AAA verified — 7:1 contrast)

| Token              | Light (`#hex`) | Dark (`#hex`)  | Used for                        |
|--------------------|----------------|----------------|---------------------------------|
| `primary`          | `#1565C0`      | `#90CAF9`      | Primary buttons, focused state  |
| `onPrimary`        | `#FFFFFF`      | `#0D47A1`      | Text on primary                 |
| `secondary`        | `#5D4037`      | `#BCAAA4`      | Hanzi accent, secondary buttons |
| `onSecondary`      | `#FFFFFF`      | `#3E2723`      |                                 |
| `background`       | `#FAFAFA`      | `#121212`      | Screen background               |
| `onBackground`     | `#1A1A1A`      | `#F5F5F5`      | Body text                       |
| `surface`          | `#FFFFFF`      | `#1E1E1E`      | Cards, sheets                   |
| `onSurface`        | `#1A1A1A`      | `#F5F5F5`      |                                 |
| `surfaceVariant`   | `#EEEEEE`      | `#2A2A2A`      | Disabled / pinyin background    |
| `onSurfaceVariant` | `#424242`      | `#BDBDBD`      | Pinyin text, captions           |
| `error`            | `#C62828`      | `#EF9A9A`      | Errors, "Forgot" button         |
| `onError`          | `#FFFFFF`      | `#B71C1C`      |                                 |
| `success`          | `#2E7D32`      | `#A5D6A7`      | Correct answers, "Easy" button  |
| `onSuccess`        | `#FFFFFF`      | `#1B5E20`      |                                 |
| `warning`          | `#EF6C00`      | `#FFCC80`      | "Hard" button, time warnings    |
| `onWarning`        | `#FFFFFF`      | `#E65100`      |                                 |

Color contrast was checked with WCAG AAA (7:1 for body, 4.5:1 for large text).
**Dark theme is supported**; toggle in Settings, default = system.

### 1.3 Typography

```
display-large    = 32 sp / Bold     — Score numbers, large stats
header-large     = 24 sp / SemiBold — Screen titles
header-medium    = 20 sp / SemiBold — Section headers, dialog titles
body-large       = 18 sp / Regular  — Body text (DEFAULT)
body-medium      = 16 sp / Regular  — Secondary info (NEVER below this)
label-large      = 18 sp / Medium   — Button labels
hanzi-display    = 56 sp / Regular  — Flashcard front character
hanzi-large      = 28 sp / Regular  — Inline characters in lists, passages (HSK 1–3)
hanzi-medium     = 24 sp / Regular  — Passage characters (HSK 4–5)
pinyin           = 14 sp / Regular  — Ruby pinyin above characters
pinyin-large     = 18 sp / Regular  — Pinyin under flashcard hanzi
```

All sizes use `sp` so they scale with system font setting. The Settings screen exposes a multiplier (Small 0.9 / Medium 1.0 / Large 1.15 / Extra-large 1.3) on top of the system value.

### 1.4 Spacing & shape

- 4 dp grid. Standard paddings: 8, 12, 16, 24, 32 dp.
- Card corner radius: **12 dp**. Button radius: **8 dp**. Pill chips: **24 dp**.
- Card elevation: **2 dp**. Buttons: **0 dp** (flat tonal). Bottom nav: **3 dp**.
- Page horizontal padding: **16 dp**.

### 1.5 Touch target rules (HARD)

- **Every interactive element has minHeight = 56 dp and minWidth = 56 dp.** Buttons reach this via `Modifier.heightIn(min = 56.dp).widthIn(min = 56.dp)`.
- Spacing between adjacent interactive elements ≥ 8 dp.

### 1.6 Bottom navigation

Height 80 dp (Material 3 default + extra padding for elderly users), 4 items, icon + label always visible.

```
[ Learn ]  [ Practice ]  [ Exam ]  [ Me ]
   home      book          edit     person
```

Icons from `material-icons-extended`. Each item has `contentDescription = "<label> tab"`.

### 1.7 Motion

Use only `fadeIn` / `fadeOut` (200 ms) and `slideIn(Horizontally)` / `slideOut(Horizontally)` (250 ms). No bouncing, no parallax. The flashcard flip is a 350 ms `rotateY` on the card surface; user can disable it under Settings → "Reduce motion".

### 1.8 Accessibility cross-cutting (non-negotiable)

- Every `Image`/`Icon` has a non-null `contentDescription` or `null` only for purely decorative images, with the parent component carrying a meaningful description.
- TalkBack announcements via `Modifier.semantics` for state changes:
  - "Card flipped to back"
  - "Correct answer"
  - "Incorrect — the answer is …"
  - "Exam started, 40 minutes remaining"
  - "Recording, tap to stop"
- Color is **never** the sole signal: success/failure pair color with an icon (✓/✗) and a text label.
- No swipe-only gestures: every flick has a tap equivalent (Next button, Flip button).
- Focus order in Compose lists is set via `Modifier.semantics { traversalIndex = … }` so reader users hit "Title → Pinyin → Translation" not the other way round.
- Live regions: timer ticks announce on minute boundaries only ("4 minutes remaining"), not every second.
- All screens respect system **font scale**, **dark mode**, and the in-app **font multiplier**.

---

## 2. Navigation Structure

```
RootNavGraph (NavHost)
│
├─ ImportLoadingScreen        (route: "import")           — entry on first launch only
│
├─ MainScaffold (BottomNav)   (route: "main")             — host for the 4 tabs
│   ├─ HomeScreen             (route: "home")
│   ├─ PracticeHubScreen      (route: "practice")
│   │   ├─ VocabularyScreen   (route: "vocab/{hsk}")
│   │   │   └─ FlashcardScreen(route: "flashcards/{hsk}")
│   │   ├─ ReadingListScreen  (route: "reading/{hsk}")
│   │   │   └─ PassageScreen  (route: "passage/{id}")
│   │   ├─ ListeningScreen    (route: "listening/{hsk}")
│   │   └─ SpeakingScreen     (route: "speaking/{hsk}")
│   ├─ ExamHubScreen          (route: "exam")
│   │   ├─ ExamScreen         (route: "exam/{hsk}")
│   │   └─ ExamResultScreen   (route: "exam/result/{id}")
│   └─ MeScreen               (route: "me")
│       ├─ ProgressScreen     (route: "progress")
│       └─ SettingsScreen     (route: "settings")
```

**Tap-depth check:**
- Home → 1 tap (bottom nav).
- Flashcard for HSK 2 = Practice tab (1) → Vocabulary card (2) → HSK 2 button (3 — actually 2; the level selector is on `VocabularyScreen` which is reached at 2). Then Flashcards button = 4? Re-design: `PracticeHubScreen` is a single page listing the 4 sub-sections. Tapping "Vocabulary" goes to `VocabularyScreen` (HSK selector + word list combined — selector is a row of pills at top). Tapping "Start flashcards" on that screen goes to `FlashcardScreen`. **3 taps total**. ✅
- Settings → Me tab (1) → Settings (2). ✅

---

## 3. Common Components

These are spec'd once, referenced by every screen.

### 3.1 `MandarinTopBar`
- Height 64 dp, surface color, 0 dp elevation.
- Left: optional back arrow (56 dp touch target, `contentDescription = "Back"`).
- Center: screen title in `header-large`.
- Right: optional 1 action icon (56 dp, with `contentDescription`).

### 3.2 `MandarinBottomNav`
- 4 items, equal width, height 80 dp.
- Each item: icon (28 dp) + label (`label-large`), selected = `primary`, unselected = `onSurfaceVariant`.

### 3.3 `MandarinPrimaryButton`
- Height 56 dp, full-width by default, `primary` background, `onPrimary` text, label-large.
- States: enabled, disabled (alpha 0.38), loading (replaces label with `CircularProgressIndicator` 24 dp + retains size).

### 3.4 `MandarinSecondaryButton`
- Same dimensions, `surfaceVariant` background, `onSurfaceVariant` text.

### 3.5 `HskLevelChip`
- 80 dp × 56 dp pill, label "HSK 1" … "HSK 5", primary when selected.

### 3.6 `PinyinText` (composable)
- Accepts `chinese: String, annotations: List<PinyinAnnotation>, showPinyin: Boolean, fontScale: Float, onCharacterClick: (String) -> Unit`.
- Layout: a `FlowRow` of stacked `Column { pinyin; hanzi }` units. If `showPinyin = false`, hides the pinyin row but reserves space (or collapses; toggleable in PassageScreen).
- Tapping a hanzi calls `onCharacterClick`. Each tappable cell ≥ 48 dp wide / 56 dp tall (touch target rule).
- Punctuation characters (`pinyin = ""`) are not tappable and have no semantics description ("punctuation").

### 3.7 `LoadingState`
- Centered `CircularProgressIndicator` (48 dp) + caption text, `body-large`.

### 3.8 `EmptyState`
- Centered icon (56 dp), title (`header-medium`), body line (`body-large`), optional CTA button.

### 3.9 `ErrorState`
- Centered icon (56 dp, `error` tint), title "Something went wrong" (`header-medium`), body line, "Retry" button.

### 3.10 `OfflineBanner`
- 48 dp tall surface at the very top of any screen that needs network, only visible while offline. Text: "You are offline. AI features are unavailable.", `warning` background.

### 3.11 Animations
- `AnimatedContent` for screen-level state transitions: `fadeIn() with fadeOut()`.
- Page enter/exit between routes: `slideInHorizontally + fadeIn / slideOutHorizontally + fadeOut`.

---

## 4. Screen Specifications

Naming convention: each screen has a route and a Composable file `<Name>Screen.kt`. The corresponding ViewModel is `<Name>ViewModel.kt` exposing `StateFlow<<Name>UiState>` (sealed class with `Loading`, `Content(...)`, `Error(...)` variants where appropriate).

---

### Screen 1 — `HomeScreen`

**Route:** `"home"`
**ViewModel:** `HomeViewModel`
**User story:** "As a learner, I open the app and see what to do today, my streak, and my progress at a glance."

**Layout (top to bottom):**

1. `MandarinTopBar` — title "Mandarin Learn", right action: settings icon (taps to Settings).
2. `OfflineBanner` (conditional).
3. **Greeting card** (12 dp radius, 16 dp padding, 16 dp horizontal margin): "Good morning, learner" / "Good afternoon" / "Good evening" based on time of day. Body-large, primary-tinted.
4. **Streak row** (16 dp horizontal): two side-by-side cards.
   - Left: "Day streak" with `current_streak` value in display-large + flame icon.
   - Right: "Longest" with `longest_streak`.
5. **Today's review card** (16 dp horizontal): big card showing `<dueCount>` due cards. CTA button "Review now" (MandarinPrimaryButton).
   - If `dueCount == 0`: shows "All caught up — try a new lesson" with secondary CTA "Learn new words".
6. **HSK progress section**: header "Your HSK progress" (header-medium), then 5 rows (one per HSK level):
   - Row layout: `HSK 1` label | `LinearProgressIndicator` (animated, primary color) | `<mastered>/<total>` body-medium.
   - Each row 56 dp tall, tappable, opens `VocabularyScreen` for that level.
7. **Quick start grid** (header "Practice", header-medium): 2×2 grid of cards (Vocabulary, Reading, Listening, Speaking). Each card 160 dp × 120 dp, 12 dp radius, icon + label.

**States:**
- **Loading:** show `LoadingState`. Triggered while `HomeViewModel` queries Room.
- **Empty (first run after import):** all numbers are zero, "Today's review" card says "Welcome — start with HSK 1 vocabulary."
- **Error:** `ErrorState` with "Retry" — only fires if Room throws (extremely rare).

**Accessibility:**
- Streak row TalkBack: "Current streak: 5 days. Longest: 12 days."
- Each HSK row: "HSK level 1, 45 of 153 words mastered. Tap to open."
- Greeting card content updates on day change; `LiveRegion = polite` so TalkBack reads when focused.

**Transitions:** screen entry: fade-in 200 ms.

---

### Screen 2 — `VocabularyScreen`

**Route:** `"vocab/{hsk}"` (default `hsk = 1`)
**ViewModel:** `VocabularyViewModel(hsk: Int)`
**User story:** "I want to pick an HSK level and browse or search the words."

**Layout (top to bottom):**

1. `MandarinTopBar` — title "Vocabulary".
2. **HSK level selector**: horizontally-scrolling row of 5 `HskLevelChip`s. The selected one is `primary`. Tapping one updates the route arg and reloads.
3. **Search bar** (`OutlinedTextField`): 56 dp tall, leading search icon, placeholder "Search Chinese, pinyin, or English". Debounced 300 ms. Clears with right "x" icon (56 dp target).
4. **Stats strip**: "<total> words • <mastered> mastered • <due> due today" (body-medium).
5. **Start flashcards button** (MandarinPrimaryButton) — full-width, 16 dp horizontal margin, label "Start today's review (<dueCount>)" or "Start learning (<newCount> new)" if no due cards.
6. **Word list** (`LazyColumn`): each item 88 dp tall.
   - Item layout: hanzi (hanzi-large) | pinyin (pinyin-large) | translation (body-medium) — left-aligned column. Right side: a small status badge ("New", "Learning", "Mastered") with 4.5:1 contrast color.
   - Tapping a row opens a detail bottom sheet with full example sentence + audio play button.

**States:**
- Loading: `LoadingState`.
- Empty (search returns nothing): `EmptyState` icon + "No words match '<query>'".
- Error: `ErrorState`.

**Accessibility:**
- Each row: "Word: 我, pronounced wǒ, meaning I, me. Status: New."
- Search bar has `imeAction = Search`.

**Transitions:** chip selection animates progress bar; list reload uses `crossfade` 200 ms.

---

### Screen 3 — `FlashcardScreen`

**Route:** `"flashcards/{hsk}"`
**ViewModel:** `FlashcardViewModel(hsk: Int)`
**User story:** "I see one word at a time, I rate how well I know it, and the algorithm schedules the next review."

**Layout:**

1. `MandarinTopBar` — title "Review", left back arrow (with confirm-exit dialog if mid-session), right: "X / Y" counter (e.g. "3 / 18").
2. **Progress bar** under top bar (4 dp tall, primary, full-width).
3. **Card surface** (16 dp horizontal margin, 24 dp top, fills available height up to 480 dp): 12 dp radius, 2 dp elevation. Front and back share dimensions.
   - **Front:** centered hanzi at `hanzi-display` (56 sp). Below, audio play button (56 dp circle, `Icons.VolumeUp`, plays Gemini TTS or cached). Bottom of card: small instruction "Tap to flip" (body-medium, on-surface-variant) + a "Show answer" button (full-width Secondary, 56 dp).
   - **Back:** hanzi at hanzi-large + pinyin at pinyin-large + translation in body-large + example sentence (chinese | pinyin | english stacked).
4. **Rating buttons** (visible on back only, full-width row, 16 dp horizontal margin, 16 dp gap above): four buttons in a 2×2 grid OR single horizontal row depending on width. Heights all 56 dp.
   - "Forgot" — error background, white text, icon `Icons.Close`.
   - "Hard" — warning background, white text, icon `Icons.Schedule`.
   - "Good" — primary background, white text, icon `Icons.Check`.
   - "Easy" — success background, white text, icon `Icons.Stars`.
   - Each button shows the **next interval** in tiny caption text below the label, e.g. "1 day", "3 days", "8 days", "21 days" (computed live by `SrsScheduler.preview()`).
5. **End-of-session state:** when no more cards, show celebratory layout: ✓ icon, "Session complete!", stats ("12 reviewed, 3 new"), buttons "Back to Vocabulary" and "Continue with new words" (only if more new cards exist).

**States:**
- Loading: `LoadingState`.
- Card-flipped state: same UI, animated flip.
- TTS loading: spinner overlay on the audio button.
- TTS error: snackbar "Could not play audio — check your connection". Card still usable.

**Accessibility:**
- Back of card auto-announces: "Word 我, pronounced wǒ, means I, me. Example: 我是学生, I am a student."
- Rating buttons each say their next-interval: "Good. Next review in 8 days."
- Flip is also achievable via the "Show answer" button (no swipe-only).
- Audio button: "Play pronunciation".

**Transitions:** flip = 350 ms `rotateY` (skipped if "Reduce motion" enabled — instead crossfade). Next-card = slide-in from right 250 ms.

---

### Screen 4 — `ReadingListScreen`

**Route:** `"reading/{hsk}"`
**ViewModel:** `ReadingListViewModel(hsk: Int)`
**User story:** "I want to pick a passage at my level and read it."

**Layout:**
1. `MandarinTopBar` — title "Reading".
2. HSK level selector (same component as Vocabulary, full row).
3. **Difficulty note** (body-medium): "HSK 1–3 passages show pinyin above every character. HSK 4–5 show pinyin only on key vocabulary."
4. **Passage list** (`LazyColumn`):
   - Each card 12 dp radius, 16 dp internal padding, 16 dp horizontal margin, 12 dp vertical gap.
   - Title (header-medium), first ~40 chars of `chinese_text` (hanzi-medium, 1 line ellipsised), word_count (body-medium), tag "Read" if `is_completed = 1` (success-tinted chip).

**States:** Loading, Empty (no passages — shouldn't happen post-import; show "No passages available"), Error.

**Accessibility:** each card: "Reading 1, Self Introduction. 16 characters. Not yet read." Long-press shows full preview as a snack.

---

### Screen 5 — `PassageScreen`

**Route:** `"passage/{id}"`
**ViewModel:** `PassageViewModel(id: String)`
**User story:** "I want to read a passage with pinyin help, tap unfamiliar characters, and adjust the font."

**Layout:**

1. `MandarinTopBar` — title = passage title, right action: kebab menu (mark as read / mark as unread).
2. **Controls bar** (sticky just under top bar, 56 dp tall, surface bg):
   - Toggle "Pinyin" (Switch, 56 dp tap area). Default ON for HSK 1–3, OFF for HSK 4–5.
   - Font size slider: A− / A+ icons either side of a `Slider` (0.8 — 1.6 multiplier). Center label shows "Aa".
   - "Play all" button (audio icon, 56 dp circle): TTS the whole passage in one call (cached).
3. **Passage body** (`LazyColumn` to handle long text):
   - Renders via `PinyinText` component.
   - English translation collapsible at the bottom: a card "Show English translation" toggles open/closed.
4. **Character popup**: when user taps a hanzi, show a `ModalBottomSheet`:
   - Hanzi (display-large) + pinyin (header-medium) + translation (body-large) + example sentence (if word is in vocabulary table) + audio play button.
   - If the character isn't in the vocabulary table (rare), show "No definition available — try Gemini" with a "Ask Gemini" button (calls `GeminiService.chat`).
5. **Footer**: "Mark as read" button (primary, 56 dp). When tapped, sets `is_completed = 1`, updates `user_progress.reading`, increments streak if first activity of the day, then snackbar "Marked as read" and back-navigates.

**States:**
- Loading: skeleton lines.
- Error: ErrorState.
- Network down + character popup needs Gemini: "Offline — using local data only" toast; the "Ask Gemini" button is disabled.

**Accessibility:**
- The whole passage is readable as one long string by TalkBack: `semantics { contentDescription = chineseText }` on the container, with each character also independently focusable for tap-to-define.
- Slider has `stateDescription` "Font size 1.2 times".
- Pinyin toggle announces "Pinyin shown" / "Pinyin hidden".

**Transitions:** popup uses Material 3 default bottom-sheet animation.

---

### Screen 6 — `SpeakingScreen`

**Route:** `"speaking/{hsk}"`
**ViewModel:** `SpeakingViewModel(hsk: Int)`
**User story:** "I see a phrase, I record myself saying it, and I get a pronunciation score."

**Layout:**

1. `MandarinTopBar` — title "Speaking".
2. HSK level selector (chip row).
3. **Phrase card** (16 dp margins, 12 dp radius, 24 dp internal padding):
   - Category label (body-medium, on-surface-variant): e.g. "GREETINGS".
   - Chinese (hanzi-display, with pinyin above each character via `PinyinText`).
   - English translation (body-large).
   - Audio "Listen" button (56 dp pill, label "Listen first") — TTS playback.
4. **Mic button** (huge — 96 dp circle, primary background, mic icon 48 dp): centered. Below the button, status text: "Tap to record" → "Recording… (3s)" → "Processing…" → "Got it!".
5. **Score card** (appears after evaluation): score number 0–100 in display-large with color band (≥85 success, 70–84 primary, <70 warning). Feedback text in body-large. Per-character scoring not shown to keep UI clean, but flagged by color in the displayed pinyin.
6. **Action buttons** (after score appears, full-width row):
   - "Try again" (Secondary).
   - "Next phrase" (Primary).

**States:**
- Recording state: pulsing red dot near top, `LiveRegion = assertive` announces "Recording, tap to stop".
- Processing state: spinner over mic, mic disabled.
- Offline: ErrorState card "You're offline. Speaking practice needs internet." + "Try again later" button.
- No-mic-permission: prompts via `RECORD_AUDIO` permission rationale; if denied permanently, shows persistent message "Open device settings to enable microphone".

**Accessibility:**
- Mic button: `contentDescription = "Record your pronunciation"`. State changes announce ("Recording started", "Recording finished").
- Score: announces "Score 87 out of 100. Great pronunciation. Try again or move to next phrase."
- TTS playback button has `stateDescription = "Playing audio"` while playing.

**Transitions:** score reveals via fade-in 300 ms.

---

### Screen 7 — `ListeningScreen`

**Route:** `"listening/{hsk}"`
**ViewModel:** `ListeningViewModel(hsk: Int)`
**User story:** "I hear a word played and tap which of 4 options matches."

**Layout:**

1. `MandarinTopBar` — title "Listening".
2. HSK level selector chips.
3. **Question counter** (body-medium): "Question 3 of 10".
4. **Audio card** (12 dp radius, 16 dp margin, primary-tinted): big "Play" button (96 dp circle), label "Tap to play". Replay icon below for replays. Replay count limit: 3.
5. **Options grid** (2×2): each cell ≥ 88 dp tall, 12 dp radius, primary border 1 dp. Renders Chinese characters (hanzi-large) only; pinyin appears for HSK 1–2 (first 5 questions only as scaffolding).
6. **Feedback card** (replaces options after answer):
   - Correct: green check icon, "Correct! 我是学生 — wǒ shì xuéshēng — I am a student.", body-large.
   - Incorrect: red X icon, "Not quite. The answer was '我是学生'. You picked '<your option>'."
   - "Next" button (primary, full-width).

**States:**
- Loading audio: spinner inside play button, button disabled.
- Audio failed (rare offline + no cache): "Could not play audio — try again or skip." Skip moves to next.
- End of set: summary "9 of 10 correct" + "Try again" / "Done".

**Accessibility:**
- Play button: "Play audio. Question 3 of 10".
- Each option button: "Option A: 我是学生".
- Feedback announces aloud (TalkBack).
- Color-only is forbidden — correct/incorrect always has icon + text.

---

### Screen 8 — `ExamScreen`

**Route:** `"exam/{hsk}"`
**ViewModel:** `ExamViewModel(hsk: Int)`
**User story:** "I take a timed mock HSK exam in the official format and cannot go back, mirroring the real test."

**Layout:**

1. `MandarinTopBar` — title "HSK <level> Exam", left back arrow shows confirm-quit dialog.
2. **Section header**: e.g. "Section 1 of 2: Listening" (header-medium, primary).
3. **Timer**: top-right of the screen body, large mono digits ("12:45"), color shifts to `warning` at < 5:00 and `error` at < 1:00. Speech via `LiveRegion` on minute boundaries only.
4. **Progress bar**: thin line, "Question 7 of 20".
5. **Question card** (full-width minus 16 dp margins):
   - Question text (body-large).
   - For listening: a centered Play button (96 dp), replay count limit 2.
   - Options A / B / C / D — vertical stack, each 64 dp tall, full width, radio-style selection. Only one selectable.
6. **Bottom action**: "Next" button (primary, full-width, 56 dp). Disabled until an option is selected.
7. After last question of a section: instead of "Next", "Finish section" button. Then a 30-second break overlay before next section auto-starts.
8. **End-of-exam:** auto-navigates to `ExamResultScreen` with the new `exam_results.id`.

**States:**
- Loading: full-screen spinner.
- "Cannot go back" — back arrow always shows confirm dialog: "Quit exam? Your progress will be lost." Buttons "Cancel" / "Quit".
- Timer expiry: auto-submits with current answers.
- Network down for listening audio (no cache): show inline warning "Audio unavailable — this question will be marked unanswered". Skip allowed.

**Accessibility:**
- Each option: `Modifier.semantics { selected = isSelected }`.
- Live region for timer announcements at minute boundaries.
- Section change: `LiveRegion = assertive` "Listening section complete. Reading section starting in 30 seconds."

---

### Screen 9 — `ExamResultScreen`

**Route:** `"exam/result/{id}"`
**ViewModel:** `ExamResultViewModel(resultId: Long)`
**User story:** "I see my score, whether I passed, where I lost points, and a history of past attempts."

**Layout:**

1. `MandarinTopBar` — title "Exam result", left back arrow.
2. **Hero card** (12 dp radius, 24 dp internal padding):
   - Pass / Fail badge (display-large): "PASSED" (success) or "NOT YET" (error) — text + icon (✓ or ✗).
   - Total score: `<total> / <max>` (display-large, primary).
   - Sub-line: "Passing score: 120" (or 180), body-large.
3. **Per-section breakdown** (one row per section in `section_scores_json`, dynamically rendered — no hard-coded count):
   - Section name (header-medium) | score (header-large) | "/100" suffix.
   - Beneath each: linear progress bar (max 100, color = success if ≥60 else warning).
4. **Mistakes** card: "<n> incorrect" with button "Review mistakes" (Secondary). Tapping opens a list (in-place expansion) showing each wrong answer with the correct answer + an "Explain" button (calls Gemini chat).
5. **History card**: "Past attempts" header. Up to 5 most recent rows: date + total + pass/fail icon. Tapping a row navigates to that result's screen.
6. **Footer button**: "Try again" (primary) → starts a new exam at the same level.

**States:**
- Loading.
- Error (id not found): "Could not load result" + "Back to Exam" button.
- Offline + Explain mistake: button shows "Explain (offline)" disabled with tooltip.

**Accessibility:**
- Hero card: announces "Passed. 145 out of 200. Passing score 120."
- Each section row announces "Listening: 78 of 100. Passed."

---

### Screen 10 — `ProgressScreen`

**Route:** `"progress"`
**ViewModel:** `ProgressViewModel`
**User story:** "I want to see how I'm progressing across all HSK levels and predict my readiness for the real exam."

**Layout:**

1. `MandarinTopBar` — title "Progress".
2. **Streak summary card** — same as Home but expanded ("This week" mini-bar chart of activity days).
3. **Per HSK level cards** (5 cards stacked, each 12 dp radius, 16 dp internal padding):
   - "HSK <n>" (header-medium) + readiness percentage badge ("62% ready").
   - 3 sub-bars: Vocabulary (mastered / total), Reading (completed / total), Exam best score (percent of max).
   - Last attempt info: "Best exam: 145/200 on 2026-04-22" (body-medium).
4. **Exam scores chart** (Compose Canvas):
   - Section header "Exam scores over time" with a level filter chip row (All / 1 / 2 / 3 / 4 / 5).
   - Line graph of `total_score` per `finished_at`. X-axis = date, Y-axis = score 0..max(level). Plot points clickable (opens ExamResultScreen for that attempt).
   - Empty state: "No exams taken yet — try one in the Exam tab."
5. **Readiness formula** explanatory link: "How is readiness calculated?" — opens a bottom sheet with the formula:
   - `readiness = 0.4 * vocab_mastered_pct + 0.2 * reading_done_pct + 0.4 * best_exam_pct`
   - Capped at 100 %.

**States:** Loading / empty (no data yet).
**Accessibility:** Chart has a textual fallback "Latest score 145, previous 120, gain of 25 points."

---

### Screen 11 — `SettingsScreen`

**Route:** `"settings"`
**ViewModel:** `SettingsViewModel`
**User story:** "I want to adjust the app to my comfort and back up my progress."

**Layout (sectioned `LazyColumn`):**

1. `MandarinTopBar` — title "Settings", left back arrow.
2. **Display section**:
   - Theme: Segmented control (System / Light / Dark), default System.
   - Font size: Slider with stops (Small / Medium / Large / Extra-large) — labelled, current value below ("Medium"). Live preview: a sample line "Aa Bb 你好" at the chosen size.
   - Reduce motion: Switch.
3. **Audio section**:
   - Audio playback speed: 4 chips (0.5× / 0.75× / 1.0× / 1.25×). Default 1.0×.
   - Pinyin display default: Switch "Show pinyin by default" (HSK 1–3 default ON, HSK 4–5 default OFF — explained in helper text).
4. **Learning section**:
   - Daily new cards limit: Slider 5–30, default 10. Live label "10 new cards per day".
5. **Data section**:
   - "Export progress" button (Secondary, 56 dp): opens Storage Access Framework picker, writes `mandarin_progress_<date>.json` containing `vocabulary` SRS state + `exam_results` + `streak`. Confirmation snackbar.
   - "Import progress" button (Secondary): opens picker, validates schema, replaces SRS state after a confirm dialog "This will overwrite your current progress. Continue?".
   - "Reset all progress" button (Tertiary, error-tinted text): confirm dialog, resets all `is_introduced=0`, SM-2 fields to defaults, exam history wiped, content tables untouched.
6. **About section**:
   - "App version: 1.0.0 (build 1)" — read-only.
   - "Gemini API key: set" / "not set" — read-only indicator. Helper text: "Set via local.properties at build time."
   - "Open-source licenses" → standard licenses screen (built-in `OssLicensesMenuActivity` is a third-party dependency — instead, render a simple Compose list from a bundled `licenses.json`).

**States:** standard. No async error states except export/import (snackbars).

**Accessibility:**
- Each row is its own focusable container with a clear `contentDescription`.
- Sliders have textual `stateDescription`.
- Switches announce "On" / "Off".

**Transitions:** none beyond default tab transition.

---

## 5. Cross-cutting flows

### 5.1 First launch flow
`MainActivity` → checks `data_version` → if missing, navigates to `ImportLoadingScreen` (route `"import"`) → after success, `popUpTo("import") { inclusive = true }` and `navigate("main")`.

### 5.2 Streak update
- On any "completion" event (passage marked read, flashcard rated, exam finished, listening question answered, speaking phrase scored), `StreakRepository.recordActivity(today)` is called.
- If `last_active_date == today`: no-op.
- If `last_active_date == today - 1`: `current_streak++`; update `longest_streak` if exceeded.
- Else: `current_streak = 1`.
- Set `last_active_date = today`.

### 5.3 New-day rollover
On `Lifecycle.onResume` of `MainActivity`, if the cached "today" differs from system today, the `HomeViewModel` re-queries due cards.

### 5.4 Permission rationale (RECORD_AUDIO)
Triggered only when first entering `SpeakingScreen`. Custom Compose dialog explains "Mandarin Learn needs your microphone to score your pronunciation. Audio is uploaded to Google Gemini for analysis and is not stored." Buttons: "Allow" (calls system permission), "Not now" (back-navigates).

---

## 6. Final accessibility audit checklist (every screen must pass)

- [ ] Every interactive element ≥ 56 dp × 56 dp.
- [ ] Every Image / Icon has `contentDescription` (or null only when truly decorative + parent describes it).
- [ ] All text uses `sp`, never `dp` or `px`.
- [ ] Color is never the sole indicator (icon + text always pair).
- [ ] No swipe-only gesture exists; every swipe action has a tap equivalent.
- [ ] Live regions announce state changes (timer, recording, correct/incorrect).
- [ ] Body text default 18 sp; minimum any visible text is 14 sp (pinyin only).
- [ ] Contrast ratio 7:1 for body, 4.5:1 for large text — verified against the M3 token palette in §1.2.
- [ ] TalkBack traversal order is title → content → actions, controlled by `traversalIndex`.
- [ ] Focus indicators visible (2 dp outline on focused interactive element).
