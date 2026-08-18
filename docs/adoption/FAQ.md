# Claude Code — FAQ

Short, honest answers. When in doubt, the meta-answer is always: **Claude's output is input to your
judgment, not gospel — verify before you ship it.**

---

### When should I use Claude Code?

Reach for it when the task is **structural, repetitive, or exploratory**:
- **Understanding code** — "how does X work / what calls it / what's the blast radius" (the graph
  beats grepping).
- **Building to an existing pattern** — new feature across the Clean Architecture layers, a Room
  entity, a Flow-based repository, a ViewModel.
- **Tests** — generating unit tests, finding uncovered branches, holding the 80% gate.
- **Refactors** — mechanical changes with a known shape, especially with `refactor-safely` finding
  call sites.
- **Review** — a fast first pass (`/pr-review`) before a human reviews.
- **Docs** — PR descriptions, KDoc, keeping `CLAUDE.md` current.

**Reach for a human instead** when the question is *should we build this*, *is this the right UX*,
*what's the product/security trade-off*, or *is this worth the deadline risk*. See the split table in
`../CLAUDE_ASSISTED_REVIEW.md`.

### What if Claude's suggestion doesn't work?

Normal — treat it like a PR from a fast junior who never gets tired:
1. **Show it the failure.** Paste the compiler error / stack trace / failing test output. It fixes
   most things with the actual error in hand.
2. **Give it the missing context.** Point it at the real file/pattern: *"follow how `JobRepositoryImpl`
   does this."* Wrong output is usually missing context, not a dead end.
3. **Constrain the scope.** Break "build the feature" into "add the interface", then "add the impl" —
   verify each compiles.
4. **Redirect, don't wrestle.** If two tries don't land, tell it the approach you want, or take the
   wheel. You're the senior engineer.
5. **If a tool call was denied or a command failed,** it adjusts — tell it what you actually want.

### How do I know if I can trust the output?

Trust is **earned per claim, by verification** — not granted wholesale:
- **Run it.** Compile (`:app:compileMockDebugKotlin`), test (`:app:testMockDebugUnitTest`), check the
  gate (`:app:koverVerifyMockDebug`). Green is trust; a prediction is not.
  *Real example:* a review claimed a change would fail the coverage gate — running it showed it
  **passed**. We corrected the claim before it misled anyone. Verify, don't paste predictions.
- **Read the diff.** You own every line you commit. If you can't explain it, don't ship it.
- **Prefer cited/structural claims.** "This has no test" (checkable) is more trustworthy than "this is
  fine" (vague). Ask it to cite the file/line or checklist rule.
- **Higher stakes → more verification.** A throwaway script needs less scrutiny than auth, payments,
  data migrations, or anything user-facing.

### Does it change my files without asking?

It proposes edits; the harness gates them by your permission mode. Review edits like any change. It
won't push/merge/deploy unless you ask.

### Is my code sent anywhere? Can I paste secrets?

It works against your local repo. **Never paste secrets, keys, tokens, or customer data** into a
prompt — treat a prompt like a message that leaves your machine.

### Which flavor/commands should I use?

Almost everything dev/test/lint uses **`mock`** (no Stream/network credentials). Only releases use
`prod`. See `QUICK_START.md` for the command cheat-sheet.

### It's slow / burning context on a huge task — what do I do?

Scope down. One layer at a time, verify between steps. For big multi-file sweeps, ask it to explore
first and propose a plan before editing.

### Where do I get help?

Weekly **office hours** (30 min — see `README.md`), the **#claude-code** channel, and this `docs/`
folder. Bring a real task; that's the fastest way to learn.
