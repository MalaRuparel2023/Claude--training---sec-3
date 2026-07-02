---
name: pr-review
description: Review a PR or working-tree diff against the ClaudeTraining Android/Kotlin checklist (null safety, resources, error handling, architecture, testing, a11y, performance) and emit structured, checklist-cited findings with concrete refactors.
---

## PR Review

Structured, checklist-driven code review for this repo. Produces findings that cite checklist rule
IDs, ranked by severity, each with a before/after refactor for anything above 🟡. Use for a GitHub
PR, a branch vs `master`, or the uncommitted working tree.

The rubric is `docs/CODE_REVIEW_CHECKLIST.md` (7 categories: `NULL RES ERR ARCH TEST A11Y PERF`).
The end-to-end process — including the human-comparison loop — is `docs/CODE_REVIEW_PROCESS.md`.

### Steps

1. **Read the rubric.** Open `docs/CODE_REVIEW_CHECKLIST.md` so every comment can cite a rule ID.
2. **Get the diff.**
   - PR: `gh pr diff <n>` (or `gh pr view <n> --json files`).
   - Branch: `git diff master...HEAD`.
   - Working tree: `git diff` + read untracked files (`git status --porcelain`).
3. **Map impact with the graph** (cheaper than reading whole files — see CLAUDE.md MCP rules):
   `detect_changes` → risk scores; `get_impact_radius` → blast radius;
   `query_graph pattern="tests_for"` → coverage of changed symbols.
4. **Walk all 7 checklist sections** against the diff. Explicitly hunt for the four required
   targets: **missing error handling, potential NPEs, architectural violations, missing tests.**
5. **For each finding** record: rule ID, severity (🔴/🟠/🟡), file:line, why it matters, and a
   concrete before/after refactor for anything above 🟡.
6. **Verify the gates** would pass (or say which fails): `:app:lintMockDebug`,
   `:app:testMockDebugUnitTest`, `:app:koverVerifyMockDebug`, and the `check-architecture` skill.
   Flag new logic in `ui/viewmodel` or `:domain` that lacks tests as a likely coverage-gate failure.
7. **Write the report** to `docs/reviews/YYYY-MM-DD-<slug>.md` using the template below, then
   run the human-comparison loop and log any checklist changes back into the checklist.

### Report format

```
# Code Review — <title>
- Date / Reviewer / Scope / Files / Verdict (✅ approve · 🟠 request changes · 🔴 block)
## Summary                         (2–4 lines: what's good, what's blocking)
## Findings                        (one block per finding)
   ### <SEV> <ID> · <RULE> · <one-line title>
   <file:line> · why it matters · before/after code
## Non-issues checked              (what you verified is fine, so the human trusts the pass)
## Human review comparison         (table: Claude vs human ✅/❌; deltas → checklist changes)
```

Order findings most-severe first. Prefer a few high-confidence findings over a long noisy list.

### Rules

- **Cite a rule ID in every finding.** A comment the checklist can't back is either a new rule or noise.
- **Refactors must compile against this codebase** — real imports, real signatures, mock/prod both.
- **Any `ChatRepository`/domain-interface change** must be mirrored in `FakeChatRepository` and
  `ChatRepositoryImpl` (`ARCH-5`) — call it out.
- Keep it token-efficient: graph tools before Grep/Read; `detail_level="minimal"` first.
