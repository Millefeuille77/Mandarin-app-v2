#!/usr/bin/env python3
"""
Mandarin Learning App — Agent Orchestrator
==========================================
Automates the full pipeline:
  Agent 1 (Research) → QA → Human Gate
  Agent 2 (Architect) → QA → Human Gate
  Agent 3 (Developer, ×10 phases) → QA → Human Gate (per phase)

Requirements:
  pip install anthropic rich

Usage:
  export ANTHROPIC_API_KEY=sk-ant-...
  python orchestrator.py                   # Start from beginning
  python orchestrator.py --resume          # Resume from last checkpoint
  python orchestrator.py --skip-to dev:3   # Jump to dev phase 3
  python orchestrator.py --status          # Show current pipeline status
"""

import anthropic
import json
import os
import sys
import time
import subprocess
from pathlib import Path
from datetime import datetime
from typing import Optional

try:
    from rich.console import Console
    from rich.panel import Panel
    from rich.prompt import Prompt
    from rich.table import Table
    from rich.progress import Progress, SpinnerColumn, TextColumn
except ImportError:
    print("Missing dependency: pip install rich")
    sys.exit(1)

# ─────────────────────────────────────────────────────────────
# Configuration
# ─────────────────────────────────────────────────────────────

PROJECT_DIR = Path("./mandarin_app_project")
STATE_FILE = PROJECT_DIR / ".orchestrator_state.json"
PROMPTS_DIR = PROJECT_DIR / "prompts"

AGENT_MODELS = {
    "research":  "claude-sonnet-4-20250514",
    "architect": "claude-opus-4-20250514",
    "developer": "claude-sonnet-4-20250514",
    "qa":        "claude-sonnet-4-20250514",
}

DIRS = {
    "data":    PROJECT_DIR / "data",
    "specs":   PROJECT_DIR / "specs",
    "app":     PROJECT_DIR / "app",
    "reports": PROJECT_DIR / "reports",
    "qa":      PROJECT_DIR / "qa_reports",
}

MAX_AGENT_TURNS = 80       # Max tool-use loops per agent run
MAX_TOKENS = 16384         # Max tokens per response
TOTAL_DEV_PHASES = 10
MAX_REWORK_ATTEMPTS = 3    # Max rework retries per stage

console = Console()

# ─────────────────────────────────────────────────────────────
# State Management
# ─────────────────────────────────────────────────────────────

def load_state() -> dict:
    if STATE_FILE.exists():
        return json.loads(STATE_FILE.read_text())
    return {
        "current_stage": "research",
        "dev_phase": 0,
        "completed_stages": [],
        "rework_counts": {},
        "started_at": datetime.now().isoformat(),
        "last_updated": datetime.now().isoformat(),
    }


def save_state(state: dict):
    state["last_updated"] = datetime.now().isoformat()
    STATE_FILE.write_text(json.dumps(state, indent=2))


def show_status(state: dict):
    table = Table(title="Pipeline Status")
    table.add_column("Property", style="cyan")
    table.add_column("Value", style="white")
    table.add_row("Current Stage", state["current_stage"])
    table.add_row("Dev Phase", str(state.get("dev_phase", 0)))
    table.add_row("Completed", ", ".join(state["completed_stages"]) or "None")
    table.add_row("Started", state.get("started_at", "Unknown"))
    table.add_row("Last Updated", state.get("last_updated", "Unknown"))
    console.print(table)


# ─────────────────────────────────────────────────────────────
# Tool Definitions
# ─────────────────────────────────────────────────────────────

TOOLS = [
    {
        "name": "file_create",
        "description": "Create a file with the given path (relative to project root) and content.",
        "input_schema": {
            "type": "object",
            "properties": {
                "path":    {"type": "string", "description": "File path relative to project root (e.g., data/vocabulary/hsk1_vocab.json)"},
                "content": {"type": "string", "description": "File content to write"},
            },
            "required": ["path", "content"],
        },
    },
    {
        "name": "bash",
        "description": "Run a bash command in the project directory. Use for validation, data processing, or checks.",
        "input_schema": {
            "type": "object",
            "properties": {
                "command": {"type": "string", "description": "Bash command to execute"},
            },
            "required": ["command"],
        },
    },
    {
        "name": "web_search",
        "description": "Search the web for information. Returns search results with titles, URLs, and snippets.",
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {"type": "string", "description": "Search query (keep short and specific, 1-6 words)"},
            },
            "required": ["query"],
        },
    },
    {
        "name": "web_fetch",
        "description": "Fetch the full text content of a web page at a given URL.",
        "input_schema": {
            "type": "object",
            "properties": {
                "url": {"type": "string", "description": "Full URL to fetch (must include https://)"},
            },
            "required": ["url"],
        },
    },
]


# ─────────────────────────────────────────────────────────────
# Tool Handlers
# ─────────────────────────────────────────────────────────────

def handle_file_create(tool_input: dict) -> str:
    filepath = PROJECT_DIR / tool_input["path"]
    filepath.parent.mkdir(parents=True, exist_ok=True)
    filepath.write_text(tool_input["content"], encoding="utf-8")
    size = len(tool_input["content"])
    console.print(f"    [green]📄 {filepath.relative_to(PROJECT_DIR)}[/green] ({size:,} bytes)")
    return f"File created successfully: {filepath.relative_to(PROJECT_DIR)}"


def handle_bash(tool_input: dict) -> str:
    cmd = tool_input["command"]
    console.print(f"    [dim]$ {cmd[:100]}{'...' if len(cmd) > 100 else ''}[/dim]")
    try:
        result = subprocess.run(
            cmd, shell=True, capture_output=True, text=True,
            cwd=str(PROJECT_DIR), timeout=120,
        )
        output = ""
        if result.stdout:
            output += result.stdout[:10000]
        if result.stderr:
            output += f"\nSTDERR: {result.stderr[:5000]}"
        output += f"\n(exit code: {result.returncode})"
        return output
    except subprocess.TimeoutExpired:
        return "ERROR: Command timed out after 120 seconds"
    except Exception as e:
        return f"ERROR: {e}"


def handle_web_search(tool_input: dict, client: anthropic.Anthropic) -> str:
    """
    Use Claude with the server-side web_search tool to perform the search.
    This avoids needing a separate search API key.
    """
    query = tool_input["query"]
    console.print(f"    [blue]🔍 Searching: {query}[/blue]")
    try:
        response = client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=4096,
            tools=[{"type": "web_search_20250305", "name": "web_search"}],
            messages=[{"role": "user", "content": f"Search the web for: {query}\n\nReturn the relevant findings as structured text."}],
        )
        text_parts = [b.text for b in response.content if hasattr(b, "text")]
        return "\n".join(text_parts) if text_parts else "No results found."
    except Exception as e:
        return f"Search error: {e}"


def handle_web_fetch(tool_input: dict) -> str:
    url = tool_input["url"]
    console.print(f"    [blue]🌐 Fetching: {url[:80]}[/blue]")
    try:
        import urllib.request
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=30) as resp:
            content = resp.read().decode("utf-8", errors="replace")
            return content[:50000]  # Truncate to manage token usage
    except Exception as e:
        return f"Fetch error: {e}"


def process_tool_calls(tool_blocks: list, client: anthropic.Anthropic) -> list:
    results = []
    for block in tool_blocks:
        name = block.name
        inp = block.input

        if name == "file_create":
            result = handle_file_create(inp)
        elif name == "bash":
            result = handle_bash(inp)
        elif name == "web_search":
            result = handle_web_search(inp, client)
        elif name == "web_fetch":
            result = handle_web_fetch(inp)
        else:
            result = f"Unknown tool: {name}"

        results.append({
            "type": "tool_result",
            "tool_use_id": block.id,
            "content": str(result),
        })

    return results


# ─────────────────────────────────────────────────────────────
# Agent Runner
# ─────────────────────────────────────────────────────────────

def run_agent(
    client: anthropic.Anthropic,
    agent_name: str,
    system_prompt: str,
    user_message: str,
    tools: Optional[list] = None,
) -> str:
    console.print(f"\n[bold cyan]{'='*60}[/bold cyan]")
    console.print(f"[bold cyan]▶ AGENT: {agent_name.upper()} ({AGENT_MODELS[agent_name]})[/bold cyan]")
    console.print(f"[bold cyan]{'='*60}[/bold cyan]")

    messages = [{"role": "user", "content": user_message}]
    model = AGENT_MODELS[agent_name]
    all_text = []

    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        console=console,
    ) as progress:
        task = progress.add_task(f"Running {agent_name}...", total=None)

        for turn in range(MAX_AGENT_TURNS):
            progress.update(task, description=f"Running {agent_name}... (turn {turn + 1})")

            kwargs = {
                "model": model,
                "max_tokens": MAX_TOKENS,
                "system": system_prompt,
                "messages": messages,
            }
            if tools:
                kwargs["tools"] = tools

            try:
                response = client.messages.create(**kwargs)
            except anthropic.APIError as e:
                console.print(f"[red]API Error: {e}[/red]")
                if "rate_limit" in str(e).lower():
                    console.print("[yellow]Rate limited. Waiting 60 seconds...[/yellow]")
                    time.sleep(60)
                    continue
                raise

            # Collect text
            text_parts = [b.text for b in response.content if hasattr(b, "text") and b.text]
            if text_parts:
                all_text.extend(text_parts)

            # Check for tool use
            tool_blocks = [b for b in response.content if b.type == "tool_use"]

            if response.stop_reason == "end_turn" or not tool_blocks:
                break

            # Process tools and continue
            tool_results = process_tool_calls(tool_blocks, client)
            messages.append({"role": "assistant", "content": response.content})
            messages.append({"role": "user", "content": tool_results})

    console.print(f"[bold green]✓ {agent_name.upper()} completed in {turn + 1} turns[/bold green]")
    return "\n".join(all_text)


# ─────────────────────────────────────────────────────────────
# Artifact Collector
# ─────────────────────────────────────────────────────────────

def collect_artifacts(directory: Path, max_chars: int = 200000) -> str:
    """Read all files in a directory tree, formatted for agent context."""
    if not directory.exists():
        return "(empty — directory does not exist yet)"

    artifacts = []
    total_chars = 0

    for filepath in sorted(directory.rglob("*")):
        if filepath.is_file() and not filepath.name.startswith("."):
            relative = filepath.relative_to(PROJECT_DIR)
            try:
                content = filepath.read_text(encoding="utf-8")
                if total_chars + len(content) > max_chars:
                    artifacts.append(f"--- FILE: {relative} (TRUNCATED — context limit) ---")
                    break
                artifacts.append(
                    f"--- FILE: {relative} ---\n{content}\n--- END FILE ---"
                )
                total_chars += len(content)
            except (UnicodeDecodeError, PermissionError):
                artifacts.append(f"--- FILE: {relative} (binary/unreadable, skipped) ---")

    if not artifacts:
        return "(empty — no files found)"

    return "\n\n".join(artifacts)


# ─────────────────────────────────────────────────────────────
# Human Approval Gate
# ─────────────────────────────────────────────────────────────

def human_gate(stage_name: str, qa_report_path: Path) -> tuple[bool, str]:
    """
    Returns (approved: bool, feedback: str).
    feedback is empty if approved, contains rework instructions if not.
    """
    console.print()
    console.print(Panel(
        f"[bold yellow]⏸  HUMAN REVIEW GATE: {stage_name}[/bold yellow]\n\n"
        f"📋 QA Report: {qa_report_path.relative_to(PROJECT_DIR) if qa_report_path.exists() else 'NOT FOUND'}\n"
        f"📁 Project: {PROJECT_DIR.resolve()}",
        title="🔍 Review Required",
        border_style="yellow",
    ))

    # Show QA report summary
    if qa_report_path.exists():
        report = qa_report_path.read_text()
        # Show first 4000 chars
        display = report[:4000]
        if len(report) > 4000:
            display += "\n\n... (report truncated — open file for full report)"
        console.print(Panel(display, title="QA Report", border_style="dim"))
    else:
        console.print("[red]⚠ QA report file not found — QA agent may have failed[/red]")

    console.print()
    console.print("[bold]Choose an action:[/bold]")
    console.print("  [green]approve[/green]  — Continue to next stage")
    console.print("  [yellow]rework[/yellow]   — Re-run this stage with your feedback")
    console.print("  [red]abort[/red]    — Stop the pipeline")
    console.print()

    while True:
        choice = Prompt.ask(
            "Action",
            choices=["approve", "rework", "abort"],
            default="approve",
        )

        if choice == "approve":
            console.print("[bold green]✓ Approved — proceeding to next stage[/bold green]")
            return True, ""

        elif choice == "rework":
            console.print("[yellow]Enter your rework instructions (what should be fixed):[/yellow]")
            feedback = Prompt.ask("Rework instructions")
            console.print(f"[yellow]↩ Rework requested. Re-running stage...[/yellow]")
            return False, feedback

        elif choice == "abort":
            console.print("[bold red]✖ Pipeline aborted by user.[/bold red]")
            sys.exit(0)


# ─────────────────────────────────────────────────────────────
# Prompt Loader
# ─────────────────────────────────────────────────────────────

def load_prompt(agent_name: str) -> str:
    prompt_file = PROMPTS_DIR / f"{agent_name}.md"
    if not prompt_file.exists():
        console.print(f"[red]ERROR: Missing prompt file: {prompt_file}[/red]")
        console.print(f"[red]Create it and re-run with --resume[/red]")
        sys.exit(1)
    return prompt_file.read_text()


# ─────────────────────────────────────────────────────────────
# Pipeline Stages
# ─────────────────────────────────────────────────────────────

def run_research_stage(client: anthropic.Anthropic, state: dict, rework_feedback: str = ""):
    user_msg = (
        "Begin your research task. Gather all HSK 1–5 vocabulary, reading passages, "
        "audio reference data, and exam structures from trusted online sources. "
        "Save all output files under the data/ directory using the file_create tool.\n\n"
        "Start with HSK 1 vocabulary and work your way up. Verify JSON validity after each file."
    )
    if rework_feedback:
        user_msg += f"\n\n⚠️ REWORK REQUESTED. Fix these issues:\n{rework_feedback}"

    run_agent(client, "research", load_prompt("research"), user_msg, TOOLS)


def run_qa_research(client: anthropic.Anthropic):
    data_artifacts = collect_artifacts(DIRS["data"])
    user_msg = (
        "You are reviewing the output of the RESEARCH agent (Context A: Data Validation).\n\n"
        "Validate all data files for completeness, schema correctness, pinyin format, "
        "duplicates, and data quality. Run all automated checks via bash, then perform manual review.\n\n"
        "Write your QA report to: qa_reports/qa_research.md\n\n"
        f"FILES TO REVIEW:\n{data_artifacts}"
    )
    run_agent(client, "qa", load_prompt("qa"), user_msg, TOOLS)


def run_architect_stage(client: anthropic.Anthropic, state: dict, rework_feedback: str = ""):
    data_artifacts = collect_artifacts(DIRS["data"])
    user_msg = (
        "Here is the content data gathered by the Research agent. Use it to produce "
        "the 4 specification documents. Save all specs under specs/\n\n"
        f"CONTENT DATA:\n{data_artifacts}"
    )
    if rework_feedback:
        user_msg += f"\n\n⚠️ REWORK REQUESTED. Fix these issues:\n{rework_feedback}"

    run_agent(client, "architect", load_prompt("architect"), user_msg, TOOLS)


def run_qa_architect(client: anthropic.Anthropic):
    data_artifacts = collect_artifacts(DIRS["data"], max_chars=50000)
    spec_artifacts = collect_artifacts(DIRS["specs"])
    user_msg = (
        "You are reviewing the output of the ARCHITECT agent (Context B: Spec Validation).\n\n"
        "Validate all spec files for internal consistency, completeness, and alignment with the data.\n\n"
        "Write your QA report to: qa_reports/qa_architecture.md\n\n"
        f"DATA FILES (summary):\n{data_artifacts}\n\n"
        f"SPEC FILES:\n{spec_artifacts}"
    )
    run_agent(client, "qa", load_prompt("qa"), user_msg, TOOLS)


def run_dev_phase(client: anthropic.Anthropic, phase: int, rework_feedback: str = ""):
    spec_artifacts = collect_artifacts(DIRS["specs"])
    data_artifacts = collect_artifacts(DIRS["data"], max_chars=50000)
    existing_code = collect_artifacts(DIRS["app"], max_chars=100000)

    # Include previous QA feedback
    prev_qa = ""
    prev_qa_file = DIRS["qa"] / f"qa_dev_phase_{phase - 1}.md"
    if prev_qa_file.exists():
        prev_qa = f"\nPREVIOUS PHASE QA FEEDBACK:\n{prev_qa_file.read_text()[:5000]}"

    user_msg = (
        f"Implement PHASE {phase} of the implementation plan.\n\n"
        f"SPECS:\n{spec_artifacts}\n\n"
        f"HSK DATA (for reference):\n{data_artifacts}\n\n"
        f"EXISTING CODE FROM PREVIOUS PHASES:\n{existing_code}\n\n"
        f"{prev_qa}\n\n"
        f"Create all files for phase {phase} using file_create. "
        f"Save code under app/ and your phase report to reports/phase_{phase}_report.md"
    )
    if rework_feedback:
        user_msg += f"\n\n⚠️ REWORK REQUESTED FOR THIS PHASE. Fix these issues:\n{rework_feedback}"

    run_agent(client, "developer", load_prompt("developer"), user_msg, TOOLS)


def run_qa_dev_phase(client: anthropic.Anthropic, phase: int):
    spec_artifacts = collect_artifacts(DIRS["specs"], max_chars=50000)
    code_artifacts = collect_artifacts(DIRS["app"], max_chars=100000)

    phase_report = ""
    report_file = DIRS["reports"] / f"phase_{phase}_report.md"
    if report_file.exists():
        phase_report = report_file.read_text()

    user_msg = (
        f"You are reviewing DEVELOPER PHASE {phase} (Context C: Code Validation).\n\n"
        f"SPECS:\n{spec_artifacts}\n\n"
        f"DEV PHASE {phase} REPORT:\n{phase_report}\n\n"
        f"ALL CODE:\n{code_artifacts}\n\n"
        f"Validate code quality, spec compliance, accessibility, and identify bugs.\n"
        f"Write your report to: qa_reports/qa_dev_phase_{phase}.md"
    )
    run_agent(client, "qa", load_prompt("qa"), user_msg, TOOLS)


# ─────────────────────────────────────────────────────────────
# Main Pipeline
# ─────────────────────────────────────────────────────────────

def main():
    # Initialize client
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        console.print("[red]ERROR: ANTHROPIC_API_KEY environment variable not set[/red]")
        console.print("Run: export ANTHROPIC_API_KEY=sk-ant-...")
        sys.exit(1)

    client = anthropic.Anthropic(api_key=api_key)

    # Create directories
    for d in DIRS.values():
        d.mkdir(parents=True, exist_ok=True)
    PROMPTS_DIR.mkdir(parents=True, exist_ok=True)

    # Load state
    state = load_state()

    console.print(Panel(
        "[bold]🀄 Mandarin Learning App — Automated Pipeline[/bold]",
        border_style="blue",
    ))
    show_status(state)
    console.print()

    # ── STAGE: Research ────────────────────────────────────
    if state["current_stage"] == "research":
        rework_count = 0
        while rework_count <= MAX_REWORK_ATTEMPTS:
            run_research_stage(client, state)
            run_qa_research(client)

            qa_path = DIRS["qa"] / "qa_research.md"
            approved, feedback = human_gate("Research & Content", qa_path)

            if approved:
                state["current_stage"] = "architect"
                state["completed_stages"].append("research")
                save_state(state)
                break
            else:
                rework_count += 1
                state["rework_counts"]["research"] = rework_count
                save_state(state)
                if rework_count > MAX_REWORK_ATTEMPTS:
                    console.print(f"[red]Max rework attempts ({MAX_REWORK_ATTEMPTS}) reached for research stage.[/red]")
                    console.print("[red]Fix issues manually and run: python orchestrator.py --skip-to architect[/red]")
                    sys.exit(1)
                run_research_stage(client, state, rework_feedback=feedback)
                run_qa_research(client)

    # ── STAGE: Architecture ────────────────────────────────
    if state["current_stage"] == "architect":
        rework_count = 0
        while rework_count <= MAX_REWORK_ATTEMPTS:
            run_architect_stage(client, state)
            run_qa_architect(client)

            qa_path = DIRS["qa"] / "qa_architecture.md"
            approved, feedback = human_gate("Architecture & Design", qa_path)

            if approved:
                state["current_stage"] = "developer"
                state["dev_phase"] = 1
                state["completed_stages"].append("architect")
                save_state(state)
                break
            else:
                rework_count += 1
                state["rework_counts"]["architect"] = rework_count
                save_state(state)
                if rework_count > MAX_REWORK_ATTEMPTS:
                    console.print(f"[red]Max rework attempts reached. Fix manually and --skip-to developer[/red]")
                    sys.exit(1)
                run_architect_stage(client, state, rework_feedback=feedback)
                run_qa_architect(client)

    # ── STAGE: Development (10 phases) ─────────────────────
    while state["current_stage"] == "developer" and state["dev_phase"] <= TOTAL_DEV_PHASES:
        phase = state["dev_phase"]

        console.print(Panel(
            f"[bold magenta]🔨 Development Phase {phase} of {TOTAL_DEV_PHASES}[/bold magenta]",
            border_style="magenta",
        ))

        rework_count = 0
        while rework_count <= MAX_REWORK_ATTEMPTS:
            run_dev_phase(client, phase)
            run_qa_dev_phase(client, phase)

            qa_path = DIRS["qa"] / f"qa_dev_phase_{phase}.md"
            approved, feedback = human_gate(f"Dev Phase {phase}/{TOTAL_DEV_PHASES}", qa_path)

            if approved:
                state["dev_phase"] = phase + 1
                state["completed_stages"].append(f"dev_phase_{phase}")
                save_state(state)
                break
            else:
                rework_count += 1
                state["rework_counts"][f"dev_phase_{phase}"] = rework_count
                save_state(state)
                if rework_count > MAX_REWORK_ATTEMPTS:
                    console.print(f"[red]Max rework attempts for phase {phase}. Fix manually and --skip-to dev:{phase+1}[/red]")
                    sys.exit(1)
                run_dev_phase(client, phase, rework_feedback=feedback)
                run_qa_dev_phase(client, phase)

    # ── COMPLETE ───────────────────────────────────────────
    state["current_stage"] = "complete"
    state["completed_at"] = datetime.now().isoformat()
    save_state(state)

    console.print()
    console.print(Panel(
        "[bold green]🎉 Pipeline Complete![/bold green]\n\n"
        f"📁 Project directory: {PROJECT_DIR.resolve()}\n"
        f"📱 Android app code:  {DIRS['app'].resolve()}\n"
        f"📋 QA reports:        {DIRS['qa'].resolve()}\n\n"
        "Next steps:\n"
        "1. Open the app/ folder in Android Studio\n"
        "2. Add your Gemini API key to local.properties\n"
        "3. Build and run on your device",
        title="✅ All Stages Approved",
        border_style="green",
    ))


# ─────────────────────────────────────────────────────────────
# CLI Entry Point
# ─────────────────────────────────────────────────────────────

if __name__ == "__main__":
    if "--status" in sys.argv:
        state = load_state()
        show_status(state)
        sys.exit(0)

    if "--skip-to" in sys.argv:
        idx = sys.argv.index("--skip-to")
        if idx + 1 >= len(sys.argv):
            console.print("[red]Usage: --skip-to <stage> (research|architect|dev:N)[/red]")
            sys.exit(1)
        target = sys.argv[idx + 1]
        state = load_state()
        if target.startswith("dev:"):
            state["current_stage"] = "developer"
            state["dev_phase"] = int(target.split(":")[1])
        elif target in ("research", "architect", "developer", "complete"):
            state["current_stage"] = target
            if target == "developer":
                state["dev_phase"] = 1
        else:
            console.print(f"[red]Unknown stage: {target}[/red]")
            sys.exit(1)
        save_state(state)
        console.print(f"[yellow]State updated. Resuming from: {target}[/yellow]")

    if "--resume" in sys.argv:
        console.print("[yellow]Resuming from saved state...[/yellow]")

    main()
