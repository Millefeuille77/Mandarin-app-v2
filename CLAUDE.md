# Mandarin Learning App — Project Guide

A lightweight **Android-only** application that takes complete beginners (target users: 60-year-old parents) from zero Mandarin to **HSK 5**, built and maintained by a 4-agent AI workflow.

> Onboarding tip: read this file first, then `prompts/` for agent roles, then `context/README.md` for orchestrator usage.

---

## 1. Product Goals (the bar every change is measured against)

| Criterion | What it means in practice |
|---|---|
| **Minimalist design** | Flat surfaces, no decorative chrome, ≤ 4 bottom-nav items, max 3 taps to any content |
| **Easy for 60-year-olds** | 56 dp+ touch targets, 18 sp+ body text, WCAG AAA contrast, TalkBack-complete, no swipe-only gestures |
| **Vocabulary section** | Flashcards with SM-2 spaced repetition for HSK 1–5 |
| **Reading section** | Pinyin above every character for HSK 1–3; key-word pinyin only for HSK 4–5 |
| **Speaking section** | Record → Gemini STT → pronunciation score + feedback |
| **Listening section** | Gemini TTS audio → multiple-choice vocabulary quiz |
| **Exams + grading** | Real HSK format per level (0–200 scale, 120 to pass) with per-section breakdown and history |
| **Lightweight** | Minimal third-party deps, audio cached locally, offline-first for already-learned content |
| **Tidy folder system** | Every file has a defined home; see `specs/FOLDER_STRUCTURE.md` once Phase 0 is complete |

Target progression: **0 skills → HSK 5**. Anything that complicates that journey for a non-technical elderly learner is wrong.

---

## 2. Tech Stack (locked — do not negotiate without explicit approval)

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + Repository pattern (manual constructor DI — no Hilt/Dagger)
- **State:** `StateFlow<UiState>` only (never `LiveData`)
- **Persistence:** Room (SQLite)
- **AI services:** Google Gemini API (TTS, STT, conversational feedback)
- **Min SDK:** API 26 (Android 8.0+)
- **Principle:** Prefer built-in Android APIs; every dependency must justify its weight.

API key handling: Gemini key lives in `local.properties` and is exposed via `BuildConfig`. Never commit keys.

---

## 3. Workspace Layout

```
mandarin_app_v2/
├── CLAUDE.md                    # This file — project north star
├── .claude/
│   └── agents/                  # Claude Code subagents (research, architect, developer, qa)
├── context/                     # Original orchestrator + agent prompt source
│   ├── README.md                # Orchestrator usage guide
│   ├── orchestrator.py          # Python pipeline runner
│   ├── requirements.txt
│   └── agents/                  # Source-of-truth agent prompts (research/architect/developer/qa.md)
├── prompts/                     # Agent prompts consumed by orchestrator.py at runtime
│   ├── research.md
│   ├── architect.md
│   ├── developer.md
│   └── qa.md
├── data/                        # Research-agent output
│   ├── vocabulary/              # hsk{1-5}_vocab.json
│   ├── reading/                 # hsk{1-5}_readings.json
│   ├── audio/                   # tone_drills.json, conversation_phrases.json
│   ├── exams/                   # hsk{1-5}_exam_structure.json, sample_questions.json
│   └── CONTENT_REPORT.md        # Quality summary
├── specs/                       # Architect-agent output
│   ├── ARCHITECTURE.md
│   ├── UX_SPECIFICATION.md
│   ├── FOLDER_STRUCTURE.md
│   └── IMPLEMENTATION_PLAN.md
├── app/                         # Developer-agent output (Android Studio project)
├── reports/                     # Per-phase developer reports (phase_{N}_report.md)
└── qa_reports/                  # QA reports (qa_research.md, qa_architecture.md, qa_dev_phase_{N}.md)
```

> **Note for orchestrator users:** `context/orchestrator.py` currently sets `PROJECT_DIR = Path("./mandarin_app_project")`. If you want it to operate on this workspace root, change that line to `PROJECT_DIR = Path(".")` (or keep the nested layout — both are valid; just be consistent).

---

## 4. The 4-Agent Pipeline

Every artifact in `data/`, `specs/`, `app/`, `reports/` is produced by one of these agents. Each stage is gated by QA + a human review.

```
Research ─► QA ─► Human gate ─► Architect ─► QA ─► Human gate ─► Developer ×10 phases ─► QA ─► Human gate (per phase)
```

| Agent | Model | Role | Output |
|---|---|---|---|
| **Research** | Sonnet | Curates HSK 1–5 vocab, readings, exams from trusted sources | `data/**` |
| **Architect** | Opus | Locks tech decisions, designs every screen, plans the 10-phase build | `specs/**` |
| **Developer** | Sonnet | Implements one phase at a time, exactly to spec | `app/**` + `reports/phase_{N}_report.md` |
| **QA** | Sonnet | Validates every preceding output (data quality, spec consistency, code standards) | `qa_reports/**` |

Subagents live in `.claude/agents/` so they can be invoked directly inside Claude Code. The same content (without Claude Code frontmatter) lives in `prompts/` for `orchestrator.py`. **If you change one, sync the other** — they are intentionally identical bodies so behavior is consistent across both invocation paths.

### Human gates

Between stages the pipeline pauses. The human chooses:
- `approve` — proceed
- `rework` — re-run with feedback (max 3 retries)
- `abort` — stop (resumable)

### Hard rules every agent honors

1. **Pinyin uses tone marks** (ā á ǎ à …), never tone numbers.
2. **HSK 1–3 reading content has pinyin on every character**, including punctuation (empty pinyin string).
3. **HSK grading is on a 0–200 scale**, 120 to pass — not percentages.
4. **No `LiveData`**, no `!!`, no hardcoded user-facing strings (use `stringResource`).
5. **Every interactive element ≥ 56 dp**, every Icon/Image has a `contentDescription`.
6. **All third-party dependencies pinned** to exact versions — no `latest`, no `+`.
7. **Every file ≤ 300 lines.**

---

## 5. Working Conventions for Claude Code in this repo

When you (Claude) act directly in this workspace (not via the orchestrator):

- **Stay in your lane.** If you're invoked as the developer subagent on phase 4, do not also re-do phase 3. If you're QA, don't fix code — flag it.
- **Read the relevant spec before writing code.** `specs/ARCHITECTURE.md` for technical decisions, `specs/UX_SPECIFICATION.md` for layout, `specs/FOLDER_STRUCTURE.md` for file paths, `specs/IMPLEMENTATION_PLAN.md` for the phase scope.
- **Never invent file paths.** They must match `specs/FOLDER_STRUCTURE.md` exactly.
- **JSON validation is mandatory.** After any change to `data/**`, parse the file to confirm it loads.
- **Accessibility is not optional.** A change that ships without TalkBack support / 56 dp targets / non-color state indicators is a regression.

### When to invoke which subagent

| Situation | Subagent |
|---|---|
| Need to gather/refresh HSK content | `research` |
| Tech stack, screens, or build phasing needs to change | `architect` |
| Implement a specific phase from `specs/IMPLEMENTATION_PLAN.md` | `developer` |
| Validate any agent's output before the human gate | `qa` |

### Out of scope (don't add without approval)

- iOS / web ports
- Third-party DI frameworks (Hilt, Koin)
- Cloud sync / accounts (the app is single-device)
- Languages other than Mandarin
- HSK 6+ content

---

## 6. Quick Reference

- **Run the pipeline:** see `context/README.md`
- **Resume after crash:** `python orchestrator.py --resume`
- **Jump to a dev phase:** `python orchestrator.py --skip-to dev:N`
- **Inspect agent prompts:** `prompts/{research,architect,developer,qa}.md`
- **Invoke an agent in Claude Code:** spawn the matching subagent from `.claude/agents/`
