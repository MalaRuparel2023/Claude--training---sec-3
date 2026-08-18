# Claude Code — Team Adoption Package

Everything a teammate needs to start using Claude Code productively on **ClaudeTraining**, plus the
rollout plan (kickoff → office hours → hands-on → feedback → iterate). Scoped to this project.

## The package

| # | Artifact | For |
|---|----------|-----|
| 1 | [`QUICK_START.md`](./QUICK_START.md) | Install, authenticate, open the repo, run your first diagnostic (~10 min). |
| 2 | [`FIVE_CORE_WORKFLOWS.md`](./FIVE_CORE_WORKFLOWS.md) | The poster: analyse · build · refactor · test · document. |
| 3 | [`FEATURE_BUILD_WALKTHROUGH.md`](./FEATURE_BUILD_WALKTHROUGH.md) | A full feature built end-to-end (written demo; record your own from it). |
| 4 | [`FAQ.md`](./FAQ.md) | "When should I use it?", "What if it doesn't work?", "Can I trust the output?" |
| 5 | [`../PROMPT_LIBRARY.md`](../PROMPT_LIBRARY.md) | Copy-paste prompts: UI, Architecture, Testing, Data. |
| 6 | [`../CODE_REVIEW_CHECKLIST.md`](../CODE_REVIEW_CHECKLIST.md) | 8-category review rubric with rule IDs. |
| 7 | [`../CLAUDE_ASSISTED_REVIEW.md`](../CLAUDE_ASSISTED_REVIEW.md) | Using Claude in review: when it helps vs human judgment; workflow. |
| 8 | [`../CODE_REVIEW_PROCESS.md`](../CODE_REVIEW_PROCESS.md) | The Claude→human→compare→iterate review loop. |
| 9 | [`../claude-md-templates/`](../claude-md-templates/) | CLAUDE.md templates for **new** projects. |
| 10 | [`../../ONBOARDING.md`](../../ONBOARDING.md) | New-teammate onboarding (also a shareable Claude Code link). |

### CLAUDE.md for our projects

This repo's **`CLAUDE.md`** is the canonical, maintained example — build commands, architecture,
feature areas, and the review process all live there and are kept current in the same PRs that change
the code. For any **new** project, start from `../claude-md-templates/` (android-app / iot-project /
ecosystem) and follow its README. (We're doing this for ClaudeTraining only right now; other repos
get the same treatment when we roll this out to them.)

## Rollout plan

### Kickoff meeting (60 min, once)
Agenda:
1. **Why** (10m) — what Claude Code is, the 5 workflows, where it helps and where humans stay in charge.
2. **Live demo** (20m) — walk `FEATURE_BUILD_WALKTHROUGH.md` on a projector: build a small feature,
   `/pr-review`, ship to `dev`.
3. **Hands-on** (20m) — everyone runs `QUICK_START.md` on their own machine to the "first diagnostic".
4. **Norms & Q&A** (10m) — agree: verify before shipping, no secrets in prompts, Claude's review is
   input not gospel. Point at the FAQ.

**Exit criteria:** everyone has Claude Code installed, authenticated, and has run one diagnostic in ClaudeTraining.

### Office hours (weekly, 30 min, first ~3 months)
- A standing 30-min slot where anyone brings a real task or a "why did it do that?" question.
- Format: screen-share, solve it together — the fastest way people learn is on their own work.
- Rotate a "host" once the early adopters are comfortable, so it's not one person's burden.
- Capture recurring questions → fold into `FAQ.md`; recurring good prompts → `PROMPT_LIBRARY.md`.
- *Optional:* set a recurring calendar reminder — Claude Code's `schedule`/`loop` can nudge the host,
  but keep the session itself human-run.

### Hands-on help (first month)
- Offer to **pair** on each person's first real Claude-assisted task (a feature or a `/pr-review`).
- Encourage authors to run `/pr-review` on their own branch before requesting human review (shift-left).
- Run the **review trial** from `../CLAUDE_ASSISTED_REVIEW.md` on one real PR and debrief.

### Collect feedback & iterate (ongoing)
Lightweight feedback form (Slack/Confluence) after week 1 and week 4:
- What did Claude speed up? Where did it waste your time or mislead you?
- Which prompt/skill do you reach for most? What's missing from the library/FAQ?
- One thing that would make it more useful?

Turn answers into edits: every recurring miss becomes a new checklist rule, prompt, or FAQ entry —
same iterate-the-artifacts loop we already use for code review. Docs are reviewed via PR, so the
package stays current instead of rotting.

## Sharing with the team

1. These docs live in the repo (reviewed via PR) — that's the authoritative copy.
2. Send the **onboarding link** (`ShareOnboardingGuide` / the link in this session) so teammates can
   open the repo pre-briefed in Claude Code.
3. Mirror this README + the Quick Start and FAQ into **Confluence/Notion** for discoverability, but
   link back here and keep the repo copy canonical so wiki and code can't silently diverge.
