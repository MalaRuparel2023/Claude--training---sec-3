# Code Review Process

How we review PRs in ClaudeTraining using Claude Code as a first-pass reviewer, then a human, then
feeding the deltas back into the checklist. The rubric is `docs/CODE_REVIEW_CHECKLIST.md`; the
automated pass is the `/pr-review` skill.

## The loop

```
diff ─▶ Claude pass (/pr-review) ─▶ human review ─▶ compare ─▶ update checklist ─▶ (next PR)
```

1. **Claude first pass.** Run `/pr-review` (or ask: *"review this PR against
   `docs/CODE_REVIEW_CHECKLIST.md`"*). Claude walks all 7 categories, explicitly hunting the four
   required targets — **missing error handling, potential NPEs, architectural violations, missing
   tests** — and writes a structured report to `docs/reviews/YYYY-MM-DD-<slug>.md`. Every finding
   cites a checklist rule ID and, above 🟡, includes a before/after refactor.
2. **Post to the PR.** Paste findings as review comments, or use `gh pr review`. Optionally use the
   built-in `/code-review --comment` skill to post inline.
3. **Human review.** A teammate reviews independently (not anchored on Claude's output) and fills
   the **Human** column of the comparison table in the report.
4. **Compare & iterate.** For each divergence:
   - Human found something Claude missed → **add or sharpen a checklist rule** so the pattern is
     prompted next time.
   - Claude flagged something the human rejects as noise → **scope or soften** that rule.
   - Both agree → the rule is working; leave it.
   Log the changes in the report's "Checklist changes" section and edit the checklist.
5. **Gate.** A PR merges only after `:app:lintMockDebug`, `:app:testMockDebugUnitTest`,
   `:app:koverVerifyMockDebug` (≥80 % line), and the `check-architecture` skill pass — the same
   checks CI enforces (`.github/workflows/android-cicd.yml`).

## Severity

- 🔴 **blocker** — correctness/crash/security/data-loss. Must fix before merge.
- 🟠 **major** — should fix before merge (leak, untested behavior, arch violation, coverage-gate risk).
- 🟡 **minor** — nit / follow-up; may merge with a tracked follow-up.

## Worked example

`docs/reviews/2026-07-02-analytics-performance.md` reviews the Firebase Analytics + Performance
feature. It surfaced a Flow trace-leak (`RES-1`), analytics behavior injected-but-untested with a
`koverVerify` risk (`TEST-2/3`), a half-wired `setUserId` (`ARCH-9`), and PII/param-length nits —
and four checklist rules were added/sharpened as a result. Use it as the template for new reviews.

## Tips

- Prefer the `code-review-graph` MCP tools (`detect_changes`, `get_impact_radius`,
  `query_graph tests_for`) over reading whole files — faster, cheaper, gives caller/test context.
- Keep Claude's pass high-signal: a few confident, checklist-backed findings beat a long noisy list.
- The checklist is living: it should get better every PR, not stay frozen.
