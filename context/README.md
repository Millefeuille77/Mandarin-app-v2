# 🀄 Mandarin Learning App — Agent Pipeline

An automated AI agent pipeline that builds a Mandarin Chinese learning Android app (HSK 1–5) using Claude as the AI backbone and Gemini for in-app speech features.

## Quick Start

### Prerequisites

- Python 3.10+
- An [Anthropic API key](https://console.anthropic.com/)

### Setup (3 steps)

```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Set your API key
export ANTHROPIC_API_KEY=sk-ant-your-key-here

# 3. Run the pipeline
python orchestrator.py
```

That's it. The orchestrator handles everything else automatically.

## What Happens When You Run It

The pipeline runs 4 AI agents in sequence:

| Step | Agent | What it does | Model | ~Time |
|------|-------|-------------|-------|-------|
| 1 | Research | Gathers HSK 1–5 vocab, readings, exam data from the web | Sonnet | 15–30 min |
| 1b | QA | Validates data quality, JSON schemas, pinyin format | Sonnet | 5–10 min |
| | **You** | **Review QA report → approve or request rework** | | |
| 2 | Architect | Creates 4 spec documents (architecture, UX, folders, plan) | Opus | 10–20 min |
| 2b | QA | Validates spec consistency, accessibility, completeness | Sonnet | 5–10 min |
| | **You** | **Review QA report → approve or request rework** | | |
| 3 | Developer | Writes Kotlin code, 10 phases | Sonnet | 10–15 min each |
| 3b | QA | Validates code quality per phase | Sonnet | 5–10 min each |
| | **You** | **Review QA report per phase → approve or request rework** | | |

**Total estimated time:** 3–6 hours (depends on rework cycles)

### At Each Human Gate

The pipeline pauses and shows you the QA report. You choose:

- **`approve`** — Move to the next stage
- **`rework`** — Re-run this stage with your feedback (up to 3 retries)
- **`abort`** — Stop the pipeline (you can resume later)

## CLI Commands

```bash
# Start from the beginning
python orchestrator.py

# Resume after a crash or abort
python orchestrator.py --resume

# Skip to a specific stage
python orchestrator.py --skip-to architect
python orchestrator.py --skip-to dev:3      # Jump to dev phase 3

# Check current status
python orchestrator.py --status
```

## Project Structure

After the pipeline completes:

```
mandarin_app_project/
├── orchestrator.py              # This automation script
├── requirements.txt
├── .orchestrator_state.json     # Pipeline state (auto-managed)
├── prompts/                     # Agent system prompts
│   ├── research.md
│   ├── architect.md
│   ├── developer.md
│   └── qa.md
├── data/                        # HSK content (from Research agent)
│   ├── vocabulary/hsk{1-5}_vocab.json
│   ├── reading/hsk{1-5}_readings.json
│   ├── audio/
│   ├── exams/
│   └── CONTENT_REPORT.md
├── specs/                       # Technical specs (from Architect agent)
│   ├── ARCHITECTURE.md
│   ├── UX_SPECIFICATION.md
│   ├── FOLDER_STRUCTURE.md
│   └── IMPLEMENTATION_PLAN.md
├── app/                         # Android app code (from Developer agent)
│   ├── build.gradle.kts
│   └── src/main/...
├── reports/                     # Dev phase reports
│   └── phase_{N}_report.md
└── qa_reports/                  # QA reports
    ├── qa_research.md
    ├── qa_architecture.md
    └── qa_dev_phase_{N}.md
```

## After Pipeline Completes

1. Open the `app/` folder in **Android Studio**
2. Add your Gemini API key to `local.properties`:
   ```
   GEMINI_API_KEY="AIzaSyCy2FsvuamiVhwsX82MwN0VVcVRjo3uuQw"
   ```
3. Sync Gradle, build, and run on your Android device
4. Test with your parents and iterate

## Troubleshooting

**Pipeline crashed mid-run:**
```bash
python orchestrator.py --resume
```

**Agent is stuck in a loop:**
Press `Ctrl+C`, then `--resume`. The pipeline picks up at the last saved gate.

**QA keeps failing the same issue:**
After 3 rework attempts, the pipeline stops. Fix the files manually, then skip ahead:
```bash
python orchestrator.py --skip-to dev:4   # or whatever the next stage is
```

**API rate limits:**
The orchestrator automatically waits 60 seconds on rate limit errors and retries.

## Cost Estimate

For a full pipeline run (no rework):

| Agent | Model | Runs | Est. tokens | Est. cost |
|-------|-------|------|-------------|-----------|
| Research | Sonnet | 1 | ~200K | ~$1.20 |
| Architect | Opus | 1 | ~100K | ~$3.00 |
| Developer | Sonnet | 10 | ~500K | ~$3.00 |
| QA | Sonnet | 12 | ~300K | ~$1.80 |
| **Total** | | | **~1.1M** | **~$9.00** |

Each rework cycle adds ~$1–2. Actual costs vary with data volume and complexity.
