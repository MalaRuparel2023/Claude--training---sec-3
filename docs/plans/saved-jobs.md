# Feature Plan — Saved Jobs (Browse & Bookmark)

- **Status:** Draft for review · **Date:** 2026-07-02 · **Author:** _(you)_ + Claude Code
- **Goal:** Let a signed-in user browse the job catalog, open a job, and bookmark/un-bookmark it, with
  a dedicated "Saved" view. Ships the dormant `save_job` / `view_job` / `view_job_list` analytics.

## Context — what already exists (don't rebuild it)

Grounding this plan in the current code so we scope only the gap:

| Layer | Already there | Gap |
|-------|---------------|-----|
| **Domain** | `Job`, `JobFilter`, `EnhancedJob(isFavorite)`, `JobRepository` (`observeJobs`, `observeEnhancedJobs`, `observeJob`, `observeSavedJobIds`, `setSaved`, `fetchJobs`), `FilteredJobsRepository`, `SearchRepository` | Browse/save use cases are thin — logic can live in VMs; no `ApplyToJob` (out of scope) |
| **Data** | `JobRepositoryImpl` — jobs cached in Room (`JobEntity`/`JobDao`), saved ids in Firestore `users/{uid}/savedJobs`, resilient (`retryTransient`/`fallbackTo`), `job_list_load` perf trace | **Saved ids are Firestore-only — not Room-cached** (no local source of truth for favorites) |
| **Analytics** | `AnalyticsEvent.ViewJobList/ViewJob/SaveJob/SearchJobs` all defined | **None emitted** (no UI to hook them) |
| **UI / VM / Nav** | — | **Nothing**: no jobs screens, no job ViewModels, no routes, not in bottom nav |

> **Key takeaway:** this is ~80% a **presentation-layer** feature plus two small data tasks
> (offline saved-state, analytics wiring). The catalog/favorites plumbing is done.

---

## 1. Component breakdown

### UI screens / components
- **`JobListScreen`** — scrollable list of `EnhancedJob`, each a `JobCard` with a bookmark toggle;
  loading / empty / error states. Entry point (new bottom-nav tab or from Home).
- **`JobDetailScreen`** — full posting (title, company, location, type, remote, salary range, tags,
  posted date) + bookmark toggle + (future) apply CTA.
- **`SavedJobsScreen`** — same list, filtered to `isFavorite`; empty state ("No saved jobs yet").
- **`JobCard`** (reusable component) — responsive, dark-mode-correct, animated bookmark toggle.
- **(Optional) filter bar** — reuse `JobFilter`; defer to v2 to keep scope tight.

### "API endpoints" (this app = Firestore, no REST)
- **`jobs` collection** (read) — the catalog; backs `observeJobs()`/`fetchJobs()`.
- **`users/{uid}/savedJobs/{jobId}` sub-collection** (read/write) — bookmark set; `{savedAt}` doc.
- No new endpoints to *write* — but the `jobs` collection **must be populated** (see Dependencies).

### Database changes (Room)
- **New:** `SavedJobEntity` (`jobId` PK, `savedAt`) + `SavedJobDao` (Flow read, upsert, delete).
- Register in `TalentSureDatabase`; **bump version 1 → 2**, add migration, export schema to `app/schemas/`.
- Purpose: make favorites **offline-first** (local SoT) like jobs already are, instead of Firestore-only.

### State management
- **`JobListViewModel`** → `StateFlow<JobListUiState>` (`loading`, `jobs: List<EnhancedJob>`, `error`);
  `stateIn(viewModelScope, WhileSubscribed(5s), Loading)`. Actions: `toggleSaved(jobId, saved)`.
- **`JobDetailViewModel`** — `observeJob(id)` + saved state via `SavedStateHandle` job id.
- **`SavedJobsViewModel`** — reuse `observeEnhancedJobs().map { it.filter { e -> e.isFavorite } }`.
- Emit analytics from the VMs: `ViewJobList(count)`, `ViewJob(id,title)`, `SaveJob(id,saved)`.

---

## 2. Architecture

```
┌ :app (UI) ─────────────────────────────────────────────┐
│ JobListScreen / JobDetailScreen / SavedJobsScreen       │
│        │ state-in / events-out                          │
│ JobListVM ─ JobDetailVM ─ SavedJobsVM  (+ AnalyticsLogger)
│        │ depend on domain interfaces only               │
└────────┼────────────────────────────────────────────────┘
         ▼
┌ :domain ── JobRepository (exists) ─ EnhancedJob (exists) ┐
└──────────────────────────────┬───────────────────────────┘
                               ▼
┌ :app/data ── JobRepositoryImpl (exists) ─────────────────┐
│  Room: JobDao (exists) + SavedJobDao (NEW, offline SoT)  │
│  Firestore: jobs + users/{uid}/savedJobs (exists)        │
└───────────────────────────────────────────────────────────┘
```

- **Layers touched:** `:app` (new screens/VMs/nav) heavily; `:app/data` lightly (SavedJobDao + wire
  saved persistence into `JobRepositoryImpl`); `:domain` **unchanged** (interfaces already cover it).
- **Interaction:** VMs collect `observeEnhancedJobs()` (which `combine`s catalog + saved ids). Toggling
  writes via `setSaved()` → Room (immediate, offline) → Firestore sync. UI binds one enhanced stream.
- **New entities/models:** `SavedJobEntity` (data), `JobListUiState`/`JobDetailUiState` (UI). No new
  domain models — `EnhancedJob` already exists for exactly this.

---

## 3. Dependencies

- **Backend / data:**
  - The **`jobs` Firestore collection must be seeded** — without it every screen is empty. Blocker for demo.
  - **Firestore security rules** for `users/{uid}/savedJobs` (owner-only read/write). Needs a rules change + deploy.
  - **Signed-in user id (uid)** for the savedJobs path. `AuthRepository` exposes `currentUserEmail`
    but **no uid** — confirm how `JobRepositoryImpl` obtains uid (likely `FirebaseAuth` directly) and
    the unauthenticated behavior.
- **Feature dependencies:** none hard, but **navigation** (add a "Jobs" tab → touches `NavigationHost`
  + bottom-nav items) and **auth** (favorites require sign-in).
- **Testing required:** unit (3 VMs, incl. analytics emission + not-emitted cases), integration
  (`SavedJobDao` + offline saved-state, the catalog∧saved `combine`), Compose UI (list/detail/empty +
  toggle). Must keep `koverVerifyMockDebug` ≥ 80% (VMs are in a non-excluded package).

---

## 4. Implementation plan (estimated hours)

| # | Component | Layer | Est. (h) |
|---|-----------|-------|:--------:|
| 1 | `SavedJobEntity` + `SavedJobDao`, DB v2 + migration + schema export | data | 3 |
| 2 | Wire saved-state into `JobRepositoryImpl` (Room SoT + Firestore sync) | data | 4 |
| 3 | `JobCard` reusable component (responsive, dark mode, toggle animation) | ui | 4 |
| 4 | `JobListScreen` + `JobListViewModel` (+ states, analytics) | ui | 6 |
| 5 | `JobDetailScreen` + `JobDetailViewModel` | ui | 5 |
| 6 | `SavedJobsScreen` + `SavedJobsViewModel` | ui | 3 |
| 7 | Navigation routes + bottom-nav "Jobs" tab | ui | 2 |
| 8 | Analytics wiring (`ViewJobList`/`ViewJob`/`SaveJob`) + verify in DebugView | ui | 2 |
| 9 | Unit tests (3 VMs) | test | 5 |
| 10 | Integration tests (SavedJobDao + combine) | test | 3 |
| 11 | Compose UI tests (list/detail/empty/toggle) | test | 4 |
| 12 | Firestore rules + seed catalog + docs/CLAUDE.md update | infra/docs | 3 |
| | **Subtotal** | | **44** |
| | **+ ~25% buffer** (review, integration, unknowns) | | **~55** |

Roughly **1.5 engineer-weeks**. Shippable in **two PRs**: (A) data + list + card + nav (items
1–4,7,8), (B) detail + saved view + full tests (5,6,9–12).

---

## 5. Risks & edge cases

| Risk | Impact | Mitigation |
|------|--------|-----------|
| **Empty `jobs` catalog** (not seeded) | Blocker — feature looks broken | Seed collection early (item 12); add a friendly empty state; test with `mock` data |
| **Saved ids Firestore-only today** | Offline toggle lost if app killed before sync | Item 2 — Room SoT + write-through; document conflict rule (last-write-wins by `savedAt`) |
| **No uid on `AuthRepository`** | savedJobs path/unauth handling unclear | Confirm uid source; define signed-out UX (hide bookmark or prompt sign-in) |
| **DB migration (v1→v2)** | Crash on upgrade if migration wrong | Provide + test the migration; schema export diff in review |
| **`combine` re-emits on every toggle** | List flicker / redundant recomposition | `distinctUntilChanged`; stable `EnhancedJob` + `LazyColumn` keys (job id) |
| **Optimistic toggle vs Firestore echo** | Double state update, brief flip-back | Optimistic local write (Room) is the SoT; Firestore is sync only |
| **Coverage gate** | CI red on merge | Test the 3 new VMs (item 9) — they're not kover-excluded |
| **Analytics PII / 100-char cap** | Bad data / policy | Don't log free-text beyond need; `search` deferred to v2 anyway |
| **New bottom-nav tab** | Touches shared `NavigationHost`, affects every screen's bar | Small, isolated change; screenshot-verify all tabs |

---

## 6. UI descriptions / mockups

```
JOB LIST (JobListScreen)                 JOB DETAIL (JobDetailScreen)
┌───────────────────────────┐           ┌───────────────────────────┐
│  Jobs                🔖    │           │ ‹  Senior Android Eng   🔖 │
├───────────────────────────┤           ├───────────────────────────┤
│ ┌───────────────────────┐ │           │ Acme Corp · Berlin        │
│ │ Senior Android Eng  🔖│ │           │ Full-time · Remote        │
│ │ Acme · Berlin · Remote│ │           │ €70k–€90k                 │
│ │ Full-time  €70–90k    │ │           │                           │
│ └───────────────────────┘ │           │ [ kotlin ] [ compose ]    │
│ ┌───────────────────────┐ │           │                           │
│ │ Android Dev        🔖 │ │           │ Posted 3 days ago         │
│ │ Globex · Remote       │ │           │                           │
│ └───────────────────────┘ │           │  ( Apply — v2 )           │
│           …               │           │                           │
└───────────────────────────┘           └───────────────────────────┘
 🔖 filled = saved (animated toggle)      Wide/tablet: JobCard grid caps
 Empty: "No jobs right now"               width & centers (per ProfileScreen pattern)

SAVED (SavedJobsScreen): same JobCard list, isFavorite only.
Empty: bookmark icon + "No saved jobs yet — tap 🔖 on any job."
```

- **Responsive:** single column compact; capped-width / 2-column grid ≥ 600dp (reuse ProfileScreen approach).
- **Dark mode:** `MaterialTheme.colorScheme` tokens only (theme already dark-correct after the ProfileScreen fix).
- **Animation:** bookmark toggle = scale/crossfade of the icon; list items stagger in via `AnimatedEntry`.

---

## 7. Claude Code review of this plan

> Prompt used: *"Is this realistic? Did I miss anything? What could go wrong?"* — captured here so the
> plan carries its own critique. Treat as input, not gospel.

**Is it realistic?** Mostly yes, *because* it's scoped to the real gap (UI + 2 data tasks) rather than
re-inventing the catalog/favorites plumbing that already exists. The 44h core (~55h buffered) is
reasonable for 3 screens + 3 VMs + Room change + full tests. The **two-PR split is the right call** —
don't let this land as one 2,000-line PR.

**What's likely mis-estimated:**
- **Item 12 (seed catalog + Firestore rules)** is underestimated at 3h if no seeding tooling exists —
  could be a half-day of its own, and it's a **hard blocker**, so pull it to the front, not the end.
- **Compose UI tests (item 11, 4h)** tend to overrun — they need an emulator (`connectedMockDebugAndroidTest`)
  and are flakier than unit tests. Budget more or make them thinner.

**What's missing / did you miss anything?**
- **Signed-out UX is unspecified.** Favorites need a uid; decide now: hide the bookmark, or prompt
  sign-in. This is a product/UX call — **not Claude's to make** (see the assisted-review split).
- **No "where does JobList enter from"** decision — new bottom-nav tab vs a Home entry. That changes
  item 7 and the nav blast radius.
- **Pagination.** Plan assumes the whole catalog streams. Fine for a demo; if `jobs` is large, you
  need paging (Paging 3) — a real scope jump. State the assumption ("catalog is small").
- **Accessibility** isn't a line item — the bookmark toggle needs a `contentDescription` that reflects
  state ("Save"/"Saved"), and cards need merged semantics. Add it to items 3–5, don't bolt it on.

**What could go wrong (top 3):**
1. **Empty catalog** — you build everything and every screen is blank because `jobs` was never seeded.
   Seed + test with data on day one.
2. **The migration** — v1→v2 without a tested migration crashes existing installs on update. Write and
   test it; review the exported schema diff.
3. **Offline saved-state (item 2)** is the subtle one — if you skip Room write-through and rely on
   Firestore's cache, a toggle made offline then app-kill can silently drop. Either do item 2 properly
   or explicitly document favorites as online-only for v1.

**Verdict:** Realistic and well-scoped. Fix the estimate on seeding, front-load the two blockers
(catalog seed, migration), and make the signed-out/entry-point **product decisions** before starting —
those are human calls this plan shouldn't paper over.
