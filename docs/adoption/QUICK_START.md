# Claude Code — Quick Start (2 pages)

Get from zero to your first Claude Code result in **ClaudeTraining** in ~10 minutes.

---

## 1. Install (2 min)

Claude Code is a CLI. Install it globally with npm (Node 18+):

```bash
npm install -g @anthropic-ai/claude-code
claude --version        # confirm it's on your PATH
```

IDE users: the **VS Code** and **JetBrains (Android Studio)** extensions wrap the same CLI — install
from the marketplace, but you still authenticate the same way below. (Also available on desktop and
at claude.ai/code.)

## 2. Authenticate (1 min)

```bash
claude          # first run opens the login flow
# then, inside the session:
/login          # choose Anthropic account (Pro/Team) or an API key
/status         # verify you're logged in and see the active model
```

Your work account is `mala.ruparel@smartsensesolutions.com`-style org SSO — pick the org account when
prompted. No keys go in the repo.

## 3. Open ClaudeTraining (1 min)

```bash
cd ~/ClaudeTraining
claude                  # starts Claude Code in the repo root
```

On start it reads **`CLAUDE.md`** (project guidance) and connects the **`code-review-graph`** MCP
server (structural code navigation). You'll see a session banner with the graph node/edge counts —
that means the knowledge graph is live.

Useful first commands inside the session:
- `/help` — list commands.
- `/skills` (or just ask) — see available skills: `explore-codebase`, `review-changes`, `pr-review`,
  `check-architecture`, `run`, `verify`, and more.

## 4. Run your first diagnostic (5 min)

Pick any of these — each proves your setup end-to-end:

**A. Build + test the credential-free flavor** (fastest sanity check):
```bash
./gradlew :app:compileMockDebugKotlin     # fast compile, no Stream/network creds
./gradlew :domain:test                    # pure-Kotlin domain tests
```
Or just ask Claude: *"Compile the mock flavor and run the domain tests, report failures."*

**B. Understand the codebase** (no build needed):
> "Give me an architecture overview of this repo and the main feature areas."

Claude uses the graph (`get_architecture_overview`) — cheaper and faster than reading files.

**C. Review your own changes** before a PR:
> `/pr-review`

Runs the 8-category checklist over your working-tree diff and returns severity-ranked findings.

**D. Check the coverage gate** (what CI enforces):
```bash
./gradlew :app:koverVerifyMockDebug       # fails if line coverage < 80%
```

If A–D succeed, you're ready.

---

## What to read next

| You want to… | Go to |
|--------------|-------|
| See the 5 everyday workflows at a glance | `FIVE_CORE_WORKFLOWS.md` |
| Watch a full feature get built with Claude | `FEATURE_BUILD_WALKTHROUGH.md` |
| Copy-paste good prompts | `../PROMPT_LIBRARY.md` |
| Review a PR with Claude | `../CLAUDE_ASSISTED_REVIEW.md` + `../CODE_REVIEW_CHECKLIST.md` |
| Onboard a teammate | `../../ONBOARDING.md` |
| Start a *new* project's CLAUDE.md | `../claude-md-templates/` |

## Ground rules (30-second version)

- Claude's output is **input to your judgment**, not gospel — verify before you ship or post it.
- Almost everything uses the **`mock`** flavor (no credentials). Only releases use `prod`.
- Never paste secrets/keys/customer data into a prompt.
- Stuck? **Weekly office hours** (see `README.md`) or ping the #claude-code channel.
