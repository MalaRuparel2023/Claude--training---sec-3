# Walkthrough — Building a Complete Feature with Claude Code

A written demo (stands in for a screen recording) of building one real feature in **ClaudeTraining**
end to end: **Saved Jobs** — let a user bookmark a job, stored offline-first. Chosen because the
`save_job` analytics event already exists in `AnalyticsEvent` but there's no feature behind it.

It walks the 5 core workflows (Analyse → Build → Test → Refactor → Document) and ends the way our real
changes ship: `/pr-review` → PR → merge to `dev` → Firebase App Distribution. Each step shows the
**prompt**, what Claude does, and the **verification** you should insist on.

> Record your own version by screen-capturing a session that follows these steps — ~12 minutes.

---

## 0. Setup (10s)
```bash
cd ~/ClaudeTraining && claude
```
`CLAUDE.md` + the `code-review-graph` server load automatically.

## 1. ANALYSE — learn the seam (1 min)

**Prompt:** *"How do existing features wire domain → data → UI here? Show me the Job repository seam
and where repositories are bound."*

Claude uses the graph (`semantic_search_nodes`, `query_graph`) and reports: contracts in `:domain`,
Firebase/Room impls in `:app/data`, bound in `di/RepositoryModule.kt`; jobs are cached in Room
(`data/local`) for offline-first reads. **Now you know the pattern to follow — don't invent a new one.**

## 2. BUILD — scaffold across the layers (4 min)

Do it layer by layer so each compiles before the next.

**2a. Domain (prompt):** *"Add a `SavedJobsRepository` interface in `:domain` (Flow-based:
`observeSavedIds(): Flow<Set<String>>`, `setSaved(jobId, saved)`), plus a `ToggleSavedJobUseCase`.
Pure Kotlin only."*
→ Claude adds the interface + use case. Verify: `./gradlew :domain:test` still green (nothing to break yet).

**2b. Data (prompt):** *"Implement it offline-first: a Room `SavedJobEntity` + DAO as the source of
truth, and a `SavedJobsRepositoryImpl` that reads the DAO as a Flow and writes through. Follow the
existing Room setup — bump the DB version and export the schema."*
→ Claude adds the entity/DAO, wires the `@Database`, writes the impl. Verify:
`./gradlew :app:compileMockDebugKotlin`.

**2c. DI (prompt):** *"Bind `SavedJobsRepositoryImpl` to `SavedJobsRepository` in `RepositoryModule`
and provide the new DAO where the other DAOs are provided."*
→ Claude edits the Hilt modules. Verify it compiles (Hilt graph resolves at compile time).

**2d. ViewModel + UI (prompt):** *"Add a `SavedJobsViewModel` exposing `StateFlow<UiState>`, and a
responsive `SavedJobsScreen` with a reusable `JobCard`. Log `AnalyticsEvent.SaveJob` on toggle."*
→ Verify: `./gradlew :app:compileMockDebugKotlin`.

**Guardrail hit here:** if `SavedJobsRepository` had a `prod`-only counterpart, you'd mirror it in both
flavors. It doesn't, so a single `:app/data` impl is fine.

## 3. TEST — cover it and hold the gate (2 min)

**Prompt:** *"Write unit tests for `ToggleSavedJobUseCase` and `SavedJobsViewModel` — cover
save, un-save, and that `SaveJob` is logged with the right args. Use a fake repository and an injected
`TestDispatcher`."* (or launch the `kotlin-unit-test-writer` agent)

```bash
./gradlew :app:testMockDebugUnitTest
./gradlew :app:koverVerifyMockDebug        # confirm we didn't drop under 80%
```
**Insist on:** the tests actually `verify(analytics).log(SaveJob(...))`, and a not-saved-path test —
injecting a mock without asserting behaviour is a false pass (checklist `TEST-2`).

## 4. REFACTOR — polish safely (2 min)

**Prompt:** *"Make `JobCard` dark-mode correct (theme tokens, no hardcoded colors) and add a
save/un-save toggle animation. Use `refactor-safely` to confirm nothing else uses it in a way this breaks."*

Verify with the `verify` or `run` skill (launch the app, toggle a bookmark, check dark mode). Tests
stay green.

## 5. DOCUMENT — capture the why (1 min)

**Prompt:** *"Update `CLAUDE.md`'s feature table with Saved Jobs, add KDoc to the repository
interface, and write a PR description for the change."* (`summarize-changes` skill)

## 6. REVIEW + SHIP (2 min)

**Prompt:** `/pr-review`
→ 8-category, severity-ranked findings with refactors. **Triage them (human step)** — fix the real
ones, discard noise. Then:

```bash
git checkout -b feat/saved-jobs && git add -A && git commit -m "feat: saved jobs (offline-first)"
git push -u origin feat/saved-jobs
gh pr create --base dev --title "feat: Saved Jobs" --body "…"
gh pr merge --squash --delete-branch
```
The push to `dev` runs CI (lint + unit/coverage + instrumented), auto-bumps the version, and
distributes via **Firebase App Distribution** — exactly how the analytics and docs changes shipped
(`1.14`, `1.15`).

---

## What this demonstrates

- Claude is fastest when you **feed it the existing pattern** (step 1) instead of asking it to invent one.
- **Compile/test between layers** — small verified steps beat one big generation you can't trust.
- The human stays in the loop at **triage** (step 6) and the **merge** — Claude drafts, you decide.
- Every artifact it produced (interface, impl, tests, docs, PR) is real, reviewed, and shipped through
  the same gate as hand-written code.
