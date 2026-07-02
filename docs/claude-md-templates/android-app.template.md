<!--
  TEMPLATE: Single Android app (Kotlin + Compose + Clean Architecture).
  Derived from ClaudeTraining/CLAUDE.md. Copy to <project-root>/CLAUDE.md, then:
   1. Replace every <PLACEHOLDER>.  2. Delete sections that don't apply.
   3. Keep it factual — describe what IS, not what you wish existed.
  Guidance: docs/claude-md-templates/README.md.  Delete this comment when done.
-->

# CLAUDE.md

Guidance for Claude Code when working in **<APP_NAME>** — <one-line what the app does>.

## Project

<STACK: e.g. Android, Kotlin + Jetpack Compose + Clean Architecture, Hilt DI.>
`applicationId`/`namespace` = `<app.id>`. minSdk <N>, target/compileSdk <N>, Java <17>.

Modules:
- `:app` — Compose UI, ViewModels, framework-backed data sources, DI wiring.
- `:domain` — pure Kotlin: models + repository interfaces + use cases (no Android/SDK types).
<- `:data` — Retrofit/Room/SDK impls. (Delete if you use a 2-module app+domain split.)>

### Build & test commands

```bash
<./gradlew :app:compile<Flavor>DebugKotlin>   # fast compile
<./gradlew :app:assemble<Flavor>Debug>         # runnable APK
./gradlew :domain:test                          # pure-Kotlin domain tests (fast)
<./gradlew :app:test<Flavor>DebugUnitTest>      # app unit tests
<./gradlew :app:connected<Flavor>DebugAndroidTest>  # instrumented (needs device/emulator)
<./gradlew :app:lint<Flavor>Debug>              # lint
<./gradlew :app:koverVerify<Flavor>Debug>       # coverage gate (fails < <N>%)
```
<Note which flavor is the default for dev/test/lint and why (e.g. a credential-free `mock`).>

### CI/CD & git hooks

<What gates a PR (lint + unit/coverage + instrumented). What auto-deploys on which branch/tag.
Point to the workflow file. Note any pre-commit hook and how to install/bypass it.>

## Architecture

Dependency rule: `:app` → `:domain`, **never** the reverse; `:domain` depends on nothing
Android-specific. <Add any use-case layer / repository seam conventions.>

- **Layer boundaries**: <where do mappers live; domain models vs data entities vs UI models.>
- **DI**: <Hilt modules; @Singleton vs scoped; where repositories are bound.>
- **Rule of thumb for Claude**: put contracts in `:domain`, impls in `:app/:data`, bind in `<Module>`.

## UI framework

- **Toolkit**: <Jetpack Compose | Views/XML | hybrid>. <Compose BOM / Material3 version.>
- **Navigation**: <Navigation-Compose / single-Activity; where routes are defined (sealed Route class).>
- **Theming**: <Material3 theme location, dynamic color, dark mode.>
- **Conventions**: screens are state-in / events-out; side effects in `LaunchedEffect`/
  `DisposableEffect`, never the composition body; no business logic in Composables.

## State management

- **Pattern**: <ViewModel + `StateFlow<UiState>` (UDF) | MVI | ...>. One immutable UI-state per screen.
- **Exposure**: `val uiState: StateFlow<...>` (backing `MutableStateFlow` private); events are
  ViewModel methods. <Note `stateIn`/`SharingStarted` policy for hot flows.>
- **Async**: `viewModelScope`; inject a `TestDispatcher` for tests; results wrapped in a
  `<Response/Result>` type carrying success/loading/failure.

## Backend integration

The **domain interface is the seam** — ViewModels depend on interfaces, not SDKs, so they stay
testable. Each feature: domain model + repository interface in `:domain`, impl in `:app/:data`,
bound in `<Module>`.

| Concern | Backend | Notes |
|---------|---------|-------|
| Auth | <Firebase Auth / OAuth> | |
| Data | <Firestore / REST + Retrofit / GraphQL> | |
| Local cache | <Room / DataStore> | <offline-first? source of truth?> |
| <Realtime/chat> | <SDK> | |

<If you use build flavors to swap a fake vs real backend (like ClaudeTraining's mock/prod), document
the flavor table and the rule: interface changes must be reflected in BOTH impls or a flavor won't compile.>
**No hardcoded URLs/keys/secrets** — from `BuildConfig`/secure storage.

## Key modules

| Path | Role |
|------|------|
| `<domain/.../repository/XRepository.kt>` | <contract> |
| `<app/.../data/repository/XRepositoryImpl.kt>` | <impl> |
| `<app/.../ui/viewmodel/XViewModel.kt>` | <state> |
| `<app/.../ui/screens/XScreen.kt>` | <UI> |

## Testing approach

- Unit tests on `:domain` + `ui/viewmodel` (JVM, no device); <flavor> so no network/credentials.
- Instrumented tests for <Room/DAO, Compose UI, framework-bound code>.
- Coverage gate: ≥ <N>% (`koverVerify<Flavor>Debug`); genuinely-untestable SDK wrappers are on the
  kover **exclude** list with a justifying comment — real logic is never excluded to dodge the gate.
- Test doubles: <fakes for repos, injected dispatcher, mock() for collaborators>. When you inject a
  new collaborator into a tested class, **assert its behavior**, don't just make construction compile.

## Conventions & gotchas

<Project-specific traps: naming, module-specific quirks, "always run X before pushing",
flavor-only files, generated-code caveats. This section pays for itself — keep it current.>
