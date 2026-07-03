# Claude-Assisted Code Review — Team Guide

How our team uses Claude Code as a **reviewer's assistant** — what it's good at, what it isn't, the
prompts to use, the workflow, and the expectation that **its output is input to human judgment, not a
verdict.** Pairs with the rubric (`CODE_REVIEW_CHECKLIST.md`), the loop (`CODE_REVIEW_PROCESS.md`),
and the `/pr-review` skill.

## The one principle

> Claude is strong at *"is this correct, consistent, and covered?"* and weak at
> *"is this the right thing to build and ship?"* Use it for the former; **humans own the latter.**

## When Claude Code is useful vs when human judgment is essential

| Claude Code is genuinely useful (structural / verifiable) | Human judgment is essential (intent / taste / trade-offs) |
|---|---|
| **Clean Architecture violations** — dependency-rule breaks, framework leaks into `:domain`, ViewModels touching SDK types | **UX decisions** — is this flow/copy/interaction good for the user? |
| **Error handling gaps** — unhandled throwing calls, swallowed exceptions, missing failure states | **Product trade-offs** — should this feature exist, ship now, or be cut/scoped? |
| **Null-safety** — unchecked `!!`, platform-type boundaries, nullable nav args | **Prioritisation** — is this bug/nit worth blocking the release? |
| **Resource cleanup** — unclosed flows/listeners/traces, leaked scopes | **API/naming taste** that shapes how consumers think, not just compiles |
| **Test-coverage gaps** — untested branches, injected-but-unverified collaborators, coverage-gate risk | **Security threat modelling** — is this the right trust boundary / data to expose? |
| **Performance anti-patterns** — main-thread IO, recomposition, N+1, per-emit allocation | **Accessibility *experience*** — Claude checks the mechanics; a human judges the actual TalkBack journey |
| **Accessibility mechanics** — missing `contentDescription`, touch-target size, hardcoded strings | **Business-logic correctness vs intent** — does it do what the ticket *meant*? |
| **Consistency & boilerplate** — does this match the pattern used elsewhere? | **Unwritten team conventions** and organisational context |
| **Documentation completeness** — missing KDoc, stale docs, undocumented config | **Cost/benefit of a large refactor** under a real deadline |

**Collaborative middle ground** (Claude proposes, human decides): severity ranking, refactor
suggestions, "should this be split into two PRs?", alternative approaches. Take the option, not the order.

## The checklist

Review against **`CODE_REVIEW_CHECKLIST.md`** — 8 categories, each with cite-able rule IDs:

1. Null safety `NULL` · 2. Resource management `RES` · 3. Error handling `ERR` ·
4. Architecture `ARCH` · 5. Testing `TEST` · 6. Accessibility `A11Y` · 7. Performance `PERF` ·
8. Documentation `DOC`.

Every review comment should cite a rule ID — a comment the checklist can't back is either a new rule
(add it) or noise (drop it).

## How to use Claude Code in a review (prompt library)

Open the PR locally (`gh pr checkout <n>`), then run these. Start with `/pr-review` for the full
sweep, then drill in with targeted prompts. Each is **a question, not an oracle** — verify before you
post it.

| Prompt | Surfaces | Backed by |
|--------|----------|-----------|
| `/pr-review` | Full 8-category pass, severity-ranked, with refactors | the skill + checklist |
| "Review this diff for **Clean Architecture violations** — dependency rule, `:domain` purity, ViewModels using SDK types." | `ARCH-*` | `check-architecture` skill / graph |
| "**Review this ViewModel** for architecture violations and untestable dependencies." | `ARCH-3`, `TEST-1` | graph `query_graph` |
| "What **error handling is missing**? What calls here can throw and aren't handled?" | `ERR-*` | — |
| "Flag potential **null-pointer / null-safety** issues, especially at SDK boundaries." | `NULL-*` | — |
| "Any **resource leaks** — unclosed flows, listeners, traces, scopes?" | `RES-*` | — |
| "What **test coverage is missing**? Suggest test cases for the untested branches." | `TEST-*` | `query_graph tests_for` |
| "Any **performance issues** — main-thread work, recomposition, N+1 queries, hot-path allocation?" | `PERF-*` | — |
| "**Accessibility gaps** in this screen — content descriptions, touch targets, contrast, semantics?" | `A11Y-*` | — |
| "Is the public API **documented**? Are the non-obvious decisions explained? Any stale docs?" | `DOC-*` | — |
| "What's the **blast radius** of this change — who calls it, what flows are affected?" | impact | `get_impact_radius` |

Prefer the `code-review-graph` MCP tools over whole-file reads — cheaper and they give caller/test context.

## The workflow

```
author self-reviews (Claude) ─▶ requests review
        │
reviewer: gh pr checkout ─▶ /pr-review + targeted prompts ─▶ TRIAGE findings
        │                                                       (keep / discard — human call)
        └─▶ post kept findings as PR comments ─▶ discuss with author ─▶ author addresses
                                                        │
                                                 iterate checklist + prompt library
```

1. **Author, before requesting review**: run `/pr-review` on your own branch and fix the easy stuff.
   Shift-left — don't spend a human reviewer on what Claude catches for free.
2. **Reviewer opens the PR in Claude Code**: `gh pr checkout <n>` (or review the working tree).
3. **Run the prompts** above — `/pr-review` first, then targeted questions for risk areas.
4. **Triage** — this is the human step. Keep the findings that are real and matter; discard noise;
   re-rank severity with product/deadline context. You own the review, not Claude.
5. **Post** kept findings as PR review comments (cite rule IDs). Optionally `/code-review --comment`.
6. **Discuss with the author** — normal review conversation; Claude's notes are conversation starters.
7. **Iterate** — anything a human caught that the prompts didn't → new checklist rule or prompt.

## Expectations & guardrails

- **Input, not gospel.** Claude's feedback is a draft for the reviewer. The human reviewer signs off
  and is accountable for the review.
- **Verify before you post.** Claude can be confidently wrong. *Real example from this repo:* a review
  predicted the analytics change would fail the 80 % coverage gate; running `koverVerifyMockDebug`
  showed it **passed** — the claim was corrected before it misled anyone. Run the check, don't paste
  the prediction.
- **Don't outsource judgment calls.** UX, product scope, security trust boundaries, and prioritisation
  stay with people. Claude informs them; it doesn't make them.
- **No secrets in prompts.** Don't paste keys/tokens/customer data; review runs against the repo locally.
- **Keep it high-signal.** A few confident, checklist-backed findings beat a long noisy list — for
  Claude and for human reviewers alike.

## Trial runbook (run this once with the team)

1. **Pick one real, medium PR.** Two reviewers: one reviews as usual, one reviews Claude-assisted with
   this guide.
2. **Compare** using the table in a review doc (`docs/reviews/<date>-<slug>.md`): which findings
   overlapped, which were Claude-only, which were human-only, which Claude findings were noise.
3. **Debrief (30 min):** Where did Claude save time? Where did it mislead? What did it miss that a
   human caught? Was the signal/noise acceptable?
4. **Iterate the artifacts:** fold the deltas into `CODE_REVIEW_CHECKLIST.md` and the prompt table
   above (the same feedback loop we already use — see `docs/reviews/2026-07-02-analytics-performance.md`
   for a worked example, including a checklist rule that was added from a review).
5. **Write it up** in the team wiki / Confluence: link this guide, the checklist, and the trial's
   review doc; capture the team's agreed norms (when to require a Claude self-review, how to mark
   Claude-sourced comments, etc.).

## Where this lives

Source of truth is this repo (`docs/`), reviewed via PR like code. Mirror the guide into
Confluence/Notion for discoverability, but **link back here** and keep the repo copy authoritative so
it can't silently drift from the checklist and skill it references.
