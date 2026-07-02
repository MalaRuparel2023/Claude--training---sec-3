# CLAUDE.md templates

Starter `CLAUDE.md` templates for new projects, distilled from this repo's own `CLAUDE.md`. A good
`CLAUDE.md` is the first thing Claude Code reads in a project — it's the difference between Claude
guessing your conventions and Claude following them.

| Template | Use for |
|----------|---------|
| [`android-app.template.md`](./android-app.template.md) | A single Android app (Kotlin + Compose + Clean Architecture). |
| [`iot-project.template.md`](./iot-project.template.md) | A connected-device project — app ↔ physical device ↔ cloud (BLE/MQTT/Wi-Fi), with real-time sync and offline-first. |
| [`ecosystem.template.md`](./ecosystem.template.md) | The **root** file for a multi-app system (several apps/services sharing backends, contracts, a design system). |

## When to use each

```
Is it ONE deployable app?
├─ Yes → does it talk to physical devices / need offline-first sync?
│        ├─ Yes → iot-project.template.md
│        └─ No  → android-app.template.md
└─ No (several apps/services in one workspace/system)
         → ecosystem.template.md  at the workspace root
           + one app template per app, in each app's directory
```

- **Android app** — the default for a normal mobile app. Covers UI framework, architecture, state
  management, backend integration, key modules, testing.
- **IoT project** — start from the Android template's shape but reach for this when *device
  integration, real-time sync, or offline behavior* is a first-class concern, not an afterthought.
- **Ecosystem** — when no single app is "the project." This is the map of the system; it does **not**
  replace per-app files, it sits above them. A monorepo gets one root ecosystem file plus a per-app
  `CLAUDE.md` in each app directory (Claude Code loads the nearest ones for the files you're editing).

Templates compose: an IoT product with a companion web dashboard = an **ecosystem** root + an
**iot-project** file for the mobile app + an **android-app** (or web) file per other app.

## How to customize for your project

1. **Copy, don't reference** — put the file at the right level: `<project-root>/CLAUDE.md` (and for
   ecosystems, a `CLAUDE.md` in each app dir too).
2. **Replace every `<PLACEHOLDER>`** with real values — real commands, real paths, real versions.
3. **Delete the top HTML comment** and any section that doesn't apply. A short accurate file beats a
   long half-true one.
4. **Verify the commands actually run** — paste each build/test command and confirm it works. Wrong
   commands are worse than none.
5. **Add a "Conventions & gotchas" entry for every trap you hit** — the highest-value section. If you
   just corrected Claude on something, that correction belongs here.
6. **Keep it current** — update `CLAUDE.md` in the same PR that changes the thing it describes. Treat
   staleness as a bug. Run `/init` to regenerate a first draft if the project drifted far.
7. **Size**: aim for skimmable (roughly one screen per top-level section). Link to `docs/` for depth
   rather than inlining long explanations.

## Common mistakes to avoid

- **Aspirational, not actual** — documenting the architecture you *want*. Claude will follow it and be
  wrong. Describe what the code does today.
- **Stale commands/paths** — the fastest way to lose Claude's trust. Verify on every edit.
- **Secrets in the file** — no API keys/tokens/URLs. Point to `BuildConfig`/secret storage instead.
- **Duplicating the README** — `CLAUDE.md` is *operational guidance for an agent* (how to build,
  test, where the seams are, what not to touch), not a marketing/onboarding overview.
- **A wall of prose** — use tables and short bullets; Claude and humans both skim.
- **No "gotchas"** — omitting the non-obvious traps wastes the file's biggest advantage.
- **One giant file for a multi-app repo** — split: ecosystem root + per-app files. Don't inline every
  app's detail at the root.
- **Never updating it** — a `CLAUDE.md` that lies is worse than none. If it's wrong, fix or delete it.
- **Ignoring precedence** — personal (`~/.claude/CLAUDE.md`) and nested files layer on top; don't
  restate global preferences in every project file.

## Sharing templates with your team

1. These live in the repo (`docs/claude-md-templates/`) — reviewed via PR like any code, so the
   templates themselves stay current and owned.
2. For a broader team rollout, publish an onboarding guide: put an `ONBOARDING.md` at the repo root
   pointing here and run the **ShareOnboardingGuide** flow (or ask Claude to "share the onboarding
   guide") to get a link teammates can open directly in Claude Code.
3. Announce in the team channel with a one-line "when to use which" (the decision tree above).

## Try it: junior-dev exercise + review loop

The templates get better by being *used*, the same Claude-pass → human → compare → iterate loop this
repo uses for code review (`docs/CODE_REVIEW_PROCESS.md`):

1. A junior dev copies the fitting template into their new project and fills it in.
2. Review the result **against this checklist** (see the "Common mistakes" list — each is a review item).
3. Log what was confusing or missing; fold fixes back into the template.

A full worked example — a junior's first-draft `CLAUDE.md`, the review comments, and the corrected
version — is in [`example-review/`](./example-review/). Findings from that review that improved these
templates are noted at the bottom of its `REVIEW.md`.
