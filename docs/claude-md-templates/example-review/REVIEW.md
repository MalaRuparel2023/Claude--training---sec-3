# Review — Habitly `CLAUDE.md` first draft

- **Author (junior):** _(name)_ · **Reviewer:** _(senior)_ + Claude · **Date:** 2026-07-02
- **Input:** `DRAFT-CLAUDE.md` · **Result:** `FINAL-CLAUDE.md`
- **Verdict:** 🟠 Rewrite before committing — good instinct to start from the template, but it
  documents aspiration over reality and leaks a secret.

Reviewed against the "Common mistakes to avoid" checklist in `../README.md`. Each finding cites the
mistake it maps to.

## Findings

| # | Sev | Mistake | Where | Fix |
|---|-----|---------|-------|-----|
| 1 | 🔴 | **Secret in the file** | `## Backend` — hardcoded Firebase `apiKey` | Remove it. Never put keys in `CLAUDE.md`; state that config comes from `google-services.json`/`BuildConfig`. Rotate the leaked key. |
| 2 | 🟠 | **Aspirational, not actual** | `## Project` / `## Architecture` — "fully-modularized", "MVI across every feature", "every feature its own module" | Habitly is actually a 2-module `:app`/`:domain` app using MVVM (`ViewModel` + `StateFlow`). Describe what exists; Claude will follow the doc and build the wrong thing otherwise. |
| 3 | 🔴 | **Stale/unverified commands** | `## Build commands` — `./gradlew runTests` doesn't exist; `build` isn't the useful dev loop | Replace with real, verified tasks (`:app:testDebugUnitTest`, `assembleDebug`, `lint`, coverage). |
| 4 | 🟠 | **Duplicates the README / marketing prose** | Opening paragraph | `CLAUDE.md` is operational agent guidance, not a mission statement. Cut it; one factual line. |
| 5 | 🟠 | **A wall of prose** | `## Architecture` | Convert to bullets + a key-modules table; drop the SOLID/buzzword recital — it gives Claude nothing actionable. |
| 6 | 🟠 | **Missing sections the template prompts** | no UI framework, state management, or key-modules detail; `## Backend` says only "Sync happens automatically" | Fill UI framework (Compose/nav/theme), state management (UDF pattern), the domain-interface seam, and a modules table. |
| 7 | 🟠 | **No "gotchas"** | absent | Add the traps: Room schema export, the injected test dispatcher, coverage-excluded packages. |
| 8 | 🟡 | **Unfilled intent / vague testing** | "We have good test coverage" | State the real gate (e.g. Kover ≥ 80%) and which flavor tests run against, or omit the claim. |

## Discussion notes (from reviewing together)

- The junior reached for the fanciest words ("fully-modularized", "MVI") because the template *lists*
  those as options — they read the menu as a recommendation. **Takeaway:** templates should signal
  "pick the one that's true," not imply the fanciest is best.
- They didn't realize a key in `CLAUDE.md` is a real leak (it's "just docs"). **Takeaway:** make the
  no-secrets rule impossible to miss.
- They left `## Backend` thin because they weren't sure of the details — a good prompt would have
  told them exactly what to find.

## Template changes fed back (the iteration step)

Applied to the templates in `../`:
- **android-app.template.md** — added the explicit note *"describe what IS, not what you wish
  existed"* to the header, and *"delete sections that don't apply"* so unused options get removed
  rather than aspirationally filled.
- **README.md → How to customize** — added step 4 *"Verify the commands actually run"* and made
  *"Aspirational, not actual"* the first common mistake.
- **README.md → Common mistakes** — sharpened the **secrets** bullet and added the *"duplicating the
  README"* bullet, both triggered directly by this draft.

> Re-run this loop with the next new project: every recurring first-draft mistake either becomes a
> sharper template prompt or a checklist item.
