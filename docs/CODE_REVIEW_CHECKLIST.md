# Android / Kotlin Code Review Checklist

Project-specific review checklist for **ClaudeTraining** (Kotlin, Jetpack Compose, Clean
Architecture, Hilt, Firebase, Stream Chat). Use it as the rubric for every PR — see
`docs/CODE_REVIEW_PROCESS.md` (or the `/pr-review` skill) for how it's applied.

Each item has an ID (e.g. `NULL-3`) so review comments can cite the rule they enforce.
Severity guidance: **🔴 blocker** (correctness/crash/security), **🟠 major** (should fix before
merge), **🟡 minor** (nit / follow-up).

---

## 1. Null safety `NULL`

- **NULL-1** No non-null assertions (`!!`) on values that can realistically be null; prefer
  `?.`, `?:`, `requireNotNull(x) { "why" }`, or an early return.
- **NULL-2** Platform types from Java/Firebase/Stream SDKs (`DocumentSnapshot.get`, `User`,
  `Message`, intent extras) are treated as nullable at the boundary and validated before use.
- **NULL-3** `lateinit` / `by lazy` are only used where initialization is guaranteed before first
  read; no `lateinit` on nullable-by-nature values.
- **NULL-4** `SavedStateHandle` / nav-arg reads use a default or null-check (`get<String>(key)`
  can be null even for "required" args).
- **NULL-5** Nullable `Flow`/`StateFlow` values have a sensible non-null initial/`?:` fallback in
  the UI so the screen never renders a null-driven blank state.
- **NULL-6** Mapper functions (`toDomain`, `toEntity`) don't silently `!!` remote fields — a
  missing field maps to a typed default or is filtered out.

## 2. Resource management `RES`

- **RES-1** Every acquired resource is released on **all** paths — including cancellation and
  exceptions. For timing/tracing, spans **stop** on error and empty/no-emit completion, not just
  the happy path (use `try/finally`, `Flow.onCompletion`, `use {}`).
- **RES-2** Coroutines are launched in a scoped context (`viewModelScope`, `repositoryScope`),
  never `GlobalScope`; long-running collectors are cancellable.
- **RES-3** `Flow` collectors, listeners, and `SnapshotListener`/Stream subscriptions are removed
  when their owner is destroyed (channelFlow `awaitClose`, `DisposableEffect` in Compose).
- **RES-4** No leaked `Context`/`Activity`/`View` references held past their lifecycle (esp. in
  singletons, `object`s, or captured lambdas).
- **RES-5** Room/DataStore/network handles come from DI singletons, not re-created per call.
- **RES-6** Bitmaps, `Cursor`, `InputStream`, `okio` sources are closed.

## 3. Error handling `ERR`

- **ERR-1** Suspend calls that can fail (network, Firestore, Stream, auth) are wrapped
  (`runCatching` / try-catch) and surface a **user-facing** state, never crash silently.
- **ERR-2** Caught exceptions are not swallowed: either mapped to UI error state, logged via
  `CrashReporter`, or rethrown — never an empty `catch {}`.
- **ERR-3** `catch (e: Exception)` is scoped as tightly as possible; `CancellationException` is
  **not** swallowed inside coroutines (rethrow it, or use `runCatching` which is fine only if the
  result isn't used to hide cancellation).
- **ERR-4** Error messages shown to users are meaningful and localizable; raw SDK/exception
  strings are not the primary UX.
- **ERR-5** Cross-cutting side effects (analytics, tracing) never break the primary flow — a
  failure to log must not fail the user action.
- **ERR-6** `Response.Failure` / result types carry the cause; no `Exception()` with a lost stack.

## 4. Architecture `ARCH`

- **ARCH-1** `:domain` stays pure Kotlin — no Android, Firebase, Stream, Compose, or `android.*`
  imports. Models/interfaces/use cases only. (Enforce with the `check-architecture` skill.)
- **ARCH-2** Dependency rule: `:app` → `:domain`, never the reverse; `:domain` depends on nothing
  app-specific.
- **ARCH-3** ViewModels depend on **domain interfaces**, not concrete data impls or SDK types, so
  they're unit-testable with fakes/mocks.
- **ARCH-4** New repository contracts live in `:domain`; Firebase/Room/Stream impls in `:app/data`
  and are bound in `di/RepositoryModule.kt` (or the flavor `ChatModule.kt`).
- **ARCH-5** `ChatRepository` (and any flavor-split interface) changes are mirrored in **both**
  `FakeChatRepository` (mock) and `ChatRepositoryImpl` (prod) — otherwise one flavor won't compile.
- **ARCH-6** No business logic in Composables; screens are state-in / events-out. Side effects run
  in `LaunchedEffect`/`DisposableEffect`, not the composition body.
- **ARCH-7** No hardcoded env URLs/keys/secrets — come from `BuildConfig` / secure storage.
- **ARCH-8** DI scopes are correct (`@Singleton` for stateless singletons; VM-scoped state stays
  in the ViewModel). Constructor injection, no service-locator/manual `getInstance()` in app code.
- **ARCH-9** Single source of truth: domain events/constants defined once (e.g. `AnalyticsEvent`),
  UI never hardcodes the raw Firebase strings.

## 5. Testing `TEST`

- **TEST-1** New/changed logic in `:domain` and `ui/viewmodel` has unit tests; the mock flavor
  (`testMockDebugUnitTest`) is used — no network/credentials required.
- **TEST-2** Injecting a new collaborator into a tested class means its **behavior** is asserted,
  not just that construction still compiles (`verify(analytics).log(...)`), including the
  not-called cases.
- **TEST-3** Coverage stays ≥ 80 % line (`koverVerifyMockDebug`). Anything genuinely untestable
  (Firebase SDK wrappers) is added to the kover **exclude** list with a justifying comment; real
  logic (ViewModels, mappers, repo operators) is **not** excluded to dodge the gate.
- **TEST-4** Tests cover success **and** failure/edge branches (blank input, empty list, error
  emission, cancellation), not only the happy path.
- **TEST-5** Coroutine tests use an injected `TestDispatcher` + `runTest`; no real delays, no
  flakiness from `Dispatchers.Main`.
- **TEST-6** Test doubles are shared and framework-free where possible (e.g.
  `NoOpPerformanceTracer`) and live in a sensible test-support location.

## 6. Accessibility `A11Y`

- **A11Y-1** Every meaningful icon/image has a `contentDescription`; purely decorative ones are
  explicitly `null`.
- **A11Y-2** Touch targets ≥ 48dp; interactive elements use `Modifier.clickable`/role semantics,
  not raw gesture detectors that TalkBack can't see.
- **A11Y-3** Text uses `sp` and respects font scaling; no fixed-height containers that clip scaled
  text.
- **A11Y-4** Color contrast meets WCAG AA; state (error/selected) isn't conveyed by color alone.
- **A11Y-5** Custom components expose semantics (`Modifier.semantics`, `stateDescription`,
  merged/clearAndSet where needed); live regions announce async changes.
- **A11Y-6** User-facing strings are in `strings.xml`, not hardcoded literals.

## 7. Performance `PERF`

- **PERF-1** No blocking work on the main thread; IO/CPU work is dispatched off it
  (`Dispatchers.IO`/`Default`), suspend all the way down.
- **PERF-2** Compose: state reads are scoped to minimize recomposition; `remember`/`derivedStateOf`
  used appropriately; stable params / keys on `LazyColumn` items; no allocation-heavy work in the
  composition body.
- **PERF-3** Flows use `distinctUntilChanged`, debounce for high-frequency inputs (search), and
  `flowOn` for upstream work; `stateIn`/`shareIn` with a sensible `SharingStarted` for hot state.
- **PERF-4** No N+1 network/DB queries; batch reads; Room queries are indexed and observed, not
  polled.
- **PERF-5** Object allocation in hot paths (list binding, per-frame) is minimized; no per-emit
  allocation that could be hoisted.
- **PERF-6** Measurable, user-facing flows have a custom `PerformanceTracer` span where the SDK's
  auto-capture (app-start, screen render, network) doesn't cover them.

---

## How to use

1. Diff scope: `git diff` (or the PR's changed files).
2. Walk each of the 7 sections against the diff; cite the rule ID in every comment.
3. Classify severity, propose a concrete refactor (before/after code) for anything above 🟡.
4. Confirm the CI gates the PR must pass: `:app:lintMockDebug`, `:app:testMockDebugUnitTest`,
   `:app:koverVerifyMockDebug`, and the `check-architecture` skill.
