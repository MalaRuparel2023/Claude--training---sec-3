# Code Review — Analytics & Performance instrumentation

- **Date:** 2026-07-02
- **Reviewer:** Claude (Opus 4.8) against `docs/CODE_REVIEW_CHECKLIST.md`
- **Scope:** working-tree changes adding Firebase Analytics + Performance Monitoring
- **Files:** `AnalyticsEvent.kt`, `AnalyticsLogger.kt`, `PerformanceTracer.kt` (domain);
  `FirebaseAnalyticsLogger.kt`, `FirebasePerformanceTracer.kt` (data/analytics);
  `AnalyticsViewModel.kt`, `AuthViewModel.kt`, `ChatDetailViewModel.kt`,
  `JobRepositoryImpl.kt`, `NavigationHost.kt`; DI + gradle wiring; 2 test files touched.
- **Verdict:** 🟠 **Request changes** — 2 major, 2 minor. Architecture is clean; gaps are in
  resource cleanup, analytics correctness, and test coverage of the new behavior.

## Summary

The feature follows the established framework-free seam well: typed `AnalyticsEvent` catalog and
`AnalyticsLogger`/`PerformanceTracer` interfaces in `:domain`, Firebase-backed impls in `:app`,
bound in `RepositoryModule`. `ARCH-1..9` all pass — domain stays pure, ViewModels log typed events,
and the Firebase wrappers are correctly kover-excluded. The problems are: a trace that can leak,
user-id association that's half-wired, and new behavior that's injected but never asserted.

---

## Findings

### 🟠 MAJOR-1 · `RES-1` · Performance trace leaks when the flow doesn't emit

`JobRepositoryImpl.traceLoad` starts a trace in `onStart` and stops it in `onEach` on the first
emission. If the flow **completes empty, errors, or is cancelled before the first item**, `onEach`
never runs and the trace is never stopped — a dangling span that Firebase silently drops, so
`job_list_load` under-reports exactly the slow/failing loads you most want to see.

`app/src/main/java/com/mr/claudetraining/data/repository/JobRepositoryImpl.kt`

```kotlin
// before
private fun <T> Flow<T>.traceLoad(name: String): Flow<T> {
    var trace: PerfTrace? = null
    return onStart { trace = performance.newTrace(name).also { it.start() } }
        .onEach { trace?.let { it.stop(); trace = null } }
}
```

```kotlin
// after — stop on first emit AND guarantee stop on any terminal event
import kotlinx.coroutines.flow.onCompletion

private fun <T> Flow<T>.traceLoad(name: String): Flow<T> {
    var trace: PerfTrace? = null
    return onStart { trace = performance.newTrace(name).also { it.start() } }
        .onEach { trace?.let { it.stop(); trace = null } }          // first emit = "loaded"
        .onCompletion { trace?.stop() }                              // empty/error/cancel safety net
}
```

`onCompletion` runs on normal completion, cancellation, and upstream error; the `trace` is nulled
after the first emit so it isn't stopped twice.

---

### 🟠 MAJOR-2 · `TEST-2` `TEST-3` · New analytics/tracing behavior is injected but never verified

`AuthViewModelTest` and `ChatDetailViewModelTest` were updated only to pass the new collaborators
(`analytics = mock()`, `NoOpPerformanceTracer`) so construction compiles — no test asserts that
the events actually fire. The whole reason the taxonomy was made framework-free was testability,
and none of it is exercised:

- no `verify(analytics).log(AnalyticsEvent.Login("email"))` on sign-in success;
- no assertion that a **failed** sign-in does **not** log a success event;
- `AnalyticsEvent.SignOut` + `setUserId(null)` on sign-out unverified;
- `SendMessage` / `AddReaction` on chat success unverified;
- `AnalyticsViewModel.logScreenView` has **no test at all** and lives in `ui.viewmodel`, which is
  **not** kover-excluded.
  > **Verified 2026-07-02:** `:app:koverVerifyMockDebug` still **passes** — the one-method class
  > doesn't sink the project under 80 %. So this is a genuine testing gap (`TEST-2`) but **not** a
  > CI-gate failure; the initial "fails CI" prediction was over-stated. Add the test anyway so the
  > behavior is pinned, but it doesn't block merge.

Add behavior assertions, e.g. in `AuthViewModelTest`:

```kotlin
@Test
fun `sign-in success logs login event`() = runTest(dispatcher) {
    whenever(authRepository.signInWithEmailAndPassword("a@b.com", "pw")).thenReturn(Unit)
    val vm = viewModel().apply { onEmailChange("a@b.com"); onPasswordChange("pw") }

    vm.signIn(); advanceUntilIdle()

    verify(analytics).log(AnalyticsEvent.Login("email"))
}

@Test
fun `sign-in failure does not log login event`() = runTest(dispatcher) {
    whenever(authRepository.signInWithEmailAndPassword(any(), any()))
        .thenThrow(RuntimeException("bad creds"))
    val vm = viewModel().apply { onEmailChange("a@b.com"); onPasswordChange("pw") }

    vm.signIn(); advanceUntilIdle()

    verify(analytics, never()).log(any())
}
```

and a trivial `AnalyticsViewModel` test:

```kotlin
@Test
fun `logScreenView emits ScreenView`() {
    val analytics: AnalyticsLogger = mock()
    AnalyticsViewModel(analytics).logScreenView("job_list")
    verify(analytics).log(AnalyticsEvent.ScreenView("job_list"))
}
```

Run `./gradlew :app:koverVerifyMockDebug` before merge to confirm the gate is green.

---

### 🟡 MINOR-1 · `ARCH-9` (analytics correctness) · `setUserId` is never called on sign-in

`signOut()` calls `analytics.setUserId(null)`, but no path ever sets a real id, so events are
**never** associated with a user and the null-out is a no-op. Set it where the app learns it's
signed in — the `authState()` collector in `init` — so it also covers Firebase restoring a session
on cold start:

```kotlin
// AuthViewModel.init — authState() currently emits only Boolean
authRepository.authState().collect { signedIn ->
    analytics.setUserId(if (signedIn) authRepository.currentUserId else null)
    _gateState.value = if (signedIn) AuthGateState.SignedIn else AuthGateState.SignedOut
}
```

`AuthRepository` exposes `currentUserEmail` but **not** a uid. Prefer adding a non-PII
`val currentUserId: String?` (the Firebase uid) over using email — see MINOR-2. This is a domain
interface change, so mirror it in the real impl (and any fake) per `ARCH-5`.

---

### 🟡 MINOR-2 · `PERF`/privacy · PII and length in analytics params

Two related nits on what gets sent to Firebase:

- `AnalyticsEvent.SearchJobs.query` and `ViewJob.jobTitle` are free-form strings sent as params.
  Firebase truncates string values at 100 chars (they'll silently clip) and, more importantly,
  raw search text can contain PII — avoid logging the verbatim query, or hash/trim it. Using email
  as the user id (MINOR-1) would be the same PII problem; use the uid.
- Not blocking for this PR since `search`/`view_job` aren't emitted yet (no UI), but fix the shape
  now so the wiring is safe when those screens land.

---

## Non-issues checked (and why they're fine)

- **`PerformanceTracer.trace {}`** uses `try/finally` — stops the span even when the block throws
  (`RES-1` ✅). The `chat_message_send` trace does record failed sends in its duration, but that's
  an acceptable tradeoff here; a `putAttribute("status", …)` would be a nice-to-have, not required.
- **`FirebaseAnalyticsLogger.toBundle`** correctly restricts to Firebase-accepted types and maps
  `Boolean`→1/0; no `!!`, null values dropped (`NULL-6` ✅).
- **Domain purity** — `AnalyticsEvent`/`AnalyticsLogger`/`PerformanceTracer` import nothing from
  Android/Firebase (`ARCH-1` ✅). Firebase wrappers are kover-excluded with a comment (`TEST-3` ✅).
- **`ERR-5`** — analytics/trace calls sit on the success path of `runCatching`; a logging failure
  can't fail the user's sign-in/send. ✅
- **A11Y** — no UI surface added (only a `LaunchedEffect`), nothing to score.

---

## Human review comparison & checklist iteration

The point of this pass is to compare Claude's output against a human reviewer and feed the deltas
back into the checklist. Fill the **Human** column from the actual PR review, then log changes.

| # | Finding | Claude | Human reviewer | Notes |
|---|---------|:------:|:--------------:|-------|
| MAJOR-1 | Trace leak on no-emit/error | ✅ | _(pending)_ | Subtle Flow-lifecycle bug; often missed by humans skimming a diff |
| MAJOR-2 | New behavior untested + coverage gate | ✅ | _(pending)_ | Humans usually catch "no tests" but not the specific `koverVerify` failure |
| MINOR-1 | `setUserId` never set on sign-in | ✅ | _(pending)_ | Requires cross-file reasoning (VM ↔ AuthRepository) |
| MINOR-2 | PII / 100-char param limit | ✅ | _(pending)_ | Domain-knowledge (Firebase policy) finding |
| H-1 | _(human-only finding, if any)_ | — | _(pending)_ | e.g. naming, product nuance, prior-art in repo |

### What a human typically adds that Claude missed

Record real divergences here after the human review. Common categories to watch for:

- **Product/UX intent** — is `screen_view` using the right screen granularity? Should
  `send_message` include `recruiter_id` now vs later?
- **Team conventions** — event-name style, where test doubles live, PR size/splitting.
- **Prior art** — does an equivalent trace/event already exist elsewhere in the app?

### Checklist changes derived from this review

- **Added `RES-1` explicit "spans stop on error and empty/no-emit completion"** — the trace-leak
  class of bug wasn't obvious from a generic "release resources" rule.
- **Added `TEST-2` "assert behavior, not just construction"** — injecting a mock and never
  verifying it is a recurring false-confidence pattern.
- **Added `TEST-3` "don't exclude real logic from kover to dodge the gate"** and the reminder to
  run `koverVerifyMockDebug` — coverage-gate failures are cheap to catch pre-push.
- **Added `ARCH-9` single-source-of-truth for domain constants** — reinforced by the
  `setUserId`/user-association gap.

> Re-run this loop each PR: any finding a human makes that the checklist didn't prompt becomes a
> new rule; any Claude finding the human rejects as noise gets its rule softened or scoped.
