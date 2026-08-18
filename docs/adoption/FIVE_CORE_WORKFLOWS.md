# The 5 Core Workflows — Claude Code in ClaudeTraining

A one-glance poster. Print it, pin it. Each workflow: **what it's for → the go-to prompt/skill → a
real command → the guardrail.** All examples use this repo's `mock` flavor (no credentials).

---

## 🔍 1. ANALYSE — understand before you touch

> *"How does X work? What calls it? What breaks if I change it?"*

- **Go-to:** `explore-codebase` skill, or ask directly. Uses the `code-review-graph` MCP tools.
- **Try:** *"Give me an architecture overview and the feature areas."* ·
  *"Trace what calls `ChatDetailViewModel.sendMessage` and its blast radius."*
- **Guardrail:** graph tools first (cheap, structural); fall back to file reads only when needed.

## 🏗️ 2. BUILD — add a feature across the layers

> *"Add feature X following the project's Clean Architecture seam."*

- **Go-to:** describe the feature; let Claude scaffold `:domain` contract → `:app/data` impl → DI →
  ViewModel → UI. Run it with the `run` skill.
- **Try:** *"Add a Saved Jobs feature: domain repo interface + use case, Room-backed impl, Hilt
  binding, ViewModel, and a responsive screen."*
- **Guardrail:** interface changes must be mirrored in **both** flavors (`mock` + `prod`) or one won't
  compile. Compile as you go: `./gradlew :app:compileMockDebugKotlin`.

## ♻️ 3. REFACTOR — change shape, keep behaviour

> *"Refactor X for Y without changing behaviour."*

- **Go-to:** `refactor-safely` skill (uses dependency analysis to find all call sites).
- **Try:** *"Refactor ProfileScreen to be responsive and dark-mode correct, keep behaviour."* ·
  *"Extract this into a reusable component."*
- **Guardrail:** run tests before **and** after; `refactor-safely` maps impact first so nothing is missed.

## ✅ 4. TEST — cover the logic, pass the gate

> *"Generate tests for X; what branches are uncovered?"*

- **Go-to:** `kotlin-unit-test-writer` agent; `query_graph pattern="tests_for"` to find gaps.
- **Try:** *"Write unit tests for `AuthViewModel` sign-in success and failure paths."* ·
  *"What's untested here, and will it drop us under the 80% gate?"*
- **Command:** `./gradlew :app:testMockDebugUnitTest` · `./gradlew :app:koverVerifyMockDebug`
- **Guardrail:** when you inject a mock, **assert its behaviour** (`verify(...)`), don't just make it compile.

## 📝 5. DOCUMENT — capture the why

> *"Document X / summarise these changes / update CLAUDE.md."*

- **Go-to:** `summarize-changes` skill (PR write-ups); `/init` to (re)generate a CLAUDE.md draft.
- **Try:** *"Write a PR description for these commits."* · *"Add KDoc explaining why this trace stops
  in onCompletion."* · *"Update CLAUDE.md's feature table for the new area."*
- **Guardrail:** document the **why**, not the code; update docs in the **same PR** as the change.

---

### The thread that ties them together

```
ANALYSE ─▶ BUILD ─▶ REFACTOR ─▶ TEST ─▶ DOCUMENT ─▶ /pr-review ─▶ PR ─▶ merge to dev ─▶ Firebase distribute
```

Then review with **`/pr-review`** (8-category checklist) and ship: a push to `dev` runs CI and
auto-distributes via Firebase App Distribution. **Claude assists every box; a human owns the merge.**
