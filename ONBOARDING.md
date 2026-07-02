# Onboarding — ClaudeTraining

Welcome. This repo is our sandbox for **Android engineering with Claude Code** — a real Kotlin/Compose
app plus the workflows we use it with (code review, CLAUDE.md authoring). Open it in Claude Code and
this guide (and the repo's `CLAUDE.md`) tells the agent — and you — how we work.

## What's here

- A working **Android app** (Kotlin, Jetpack Compose, Clean Architecture, Hilt, Firebase, Stream Chat).
  Two modules: `:app` (UI/VMs/data) and `:domain` (pure Kotlin contracts + models).
- **`CLAUDE.md`** — the operational guide Claude reads first: build commands, architecture, feature
  areas, and our code-review process. Read it before anything else.
- **`docs/`** — the workflows below.

## First 15 minutes

```bash
./gradlew :app:compileMockDebugKotlin     # fast compile, no credentials needed
./gradlew :app:assembleMockDebug          # runnable APK (mock flavor — no Stream/network)
./gradlew :domain:test                    # fast pure-Kotlin tests
./gradlew :app:testMockDebugUnitTest      # app unit tests
./gradlew :app:koverVerifyMockDebug       # coverage gate (fails < 80%)
```

Almost everything uses the **`mock`** flavor (no network/credentials); only release builds use `prod`.
Install the pre-commit hook once: `./scripts/install-git-hooks.sh` (bypass with `SKIP_HOOKS=1`).

## How we review code

We run a **checklist-driven, Claude-first review** and feed the results back into the checklist.

- Rubric: **`docs/CODE_REVIEW_CHECKLIST.md`** — 7 categories (null safety, resources, error handling,
  architecture, testing, a11y, performance) with cite-able rule IDs.
- Process: **`docs/CODE_REVIEW_PROCESS.md`** — Claude pass → human review → compare → iterate.
- Run it: the **`/pr-review`** skill over a PR/branch/working-tree diff.
- Worked example: **`docs/reviews/2026-07-02-analytics-performance.md`**.

## Writing a CLAUDE.md for a new project

Start from our templates in **`docs/claude-md-templates/`**:

- `android-app.template.md` — single Android app.
- `iot-project.template.md` — connected-device app (device integration, real-time sync, offline-first).
- `ecosystem.template.md` — root file for a multi-app system.
- `README.md` — when to use each, how to customize, common mistakes.
- `example-review/` — a junior dev's first draft, the review, and the corrected version.

## Ship / distribute

Pushing to **`dev`** runs CI (lint + unit/coverage + instrumented tests), then auto-bumps the version
and distributes the build via **Firebase App Distribution**. `v*` tags ship to Play internal. See
`docs/CICD.md` and `.github/workflows/`.

## Where to get help

- Repo conventions & gotchas → `CLAUDE.md`.
- Ask Claude Code directly in this repo — it has the graph-based code-review MCP tools wired up
  (`code-review-graph`), so "explore", "review", and "impact of" questions work out of the box.
