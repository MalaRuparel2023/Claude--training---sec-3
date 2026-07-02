<!--
  Corrected "Habitly" CLAUDE.md after the review in REVIEW.md. This is what actually gets committed
  to the project root (with this comment removed). Shown here as the "after" of the worked example.
-->

# CLAUDE.md

Guidance for Claude Code in **Habitly** — an offline-first Android habit tracker (streaks, reminders).

## Project

Android, Kotlin + Jetpack Compose + Clean Architecture, Hilt DI. minSdk 26, target/compileSdk 35, Java 17.

Two modules:
- `:app` — Compose UI, ViewModels, Room/Firebase-backed data sources, Hilt wiring.
- `:domain` — pure Kotlin: models + repository interfaces + use cases (no Android/SDK types).

### Build & test commands

```bash
./gradlew :app:compileDebugKotlin       # fast compile
./gradlew :app:assembleDebug            # runnable APK
./gradlew :domain:test                  # pure-Kotlin domain tests (fast)
./gradlew :app:testDebugUnitTest        # app unit tests
./gradlew :app:lintDebug                # lint
./gradlew :app:koverVerifyDebug         # coverage gate (fails < 80%)
```

## Architecture

Dependency rule: `:app` → `:domain`, never the reverse; `:domain` has no Android types.

- Contracts (repository interfaces, models, use cases) live in `:domain`; Room/Firebase impls in
  `:app/data`; bound in `di/RepositoryModule.kt`.
- Mappers convert Room entities / Firestore docs → domain models at the data boundary.

## UI framework

- Jetpack Compose (Material3). Single-Activity; routes in a sealed `Route` class, Navigation-Compose.
- Screens are state-in / events-out; side effects in `LaunchedEffect`/`DisposableEffect`, not the
  composition body.

## State management

- MVVM + UDF: one immutable `data class …UiState` per screen, exposed as `StateFlow` from the
  ViewModel (private `MutableStateFlow` backing). Events are ViewModel methods.
- Coroutines on `viewModelScope`; an injected `TestDispatcher` in tests. Results wrapped in `Result`.

## Backend integration

Domain interfaces are the seam — ViewModels depend on `HabitRepository`, not Room/Firebase.

| Concern | Backend | Notes |
|---------|---------|-------|
| Auth | Firebase Auth (email + Google) | |
| Sync | Firestore | offline-first: Room is the source of truth; a `SyncWorker` reconciles remote↔local |
| Local | Room (`data/local`, schemas in `app/schemas/`) | UI reads local, never blocks on network |

Config comes from `google-services.json` + `BuildConfig` — **no keys/secrets in source or this file.**

## Key modules

| Path | Role |
|------|------|
| `domain/.../repository/HabitRepository.kt` | contract |
| `app/.../data/repository/HabitRepositoryImpl.kt` | Room + Firestore impl |
| `app/.../data/sync/SyncWorker.kt` | offline reconciliation |
| `app/.../ui/viewmodel/HabitListViewModel.kt` | list state |

## Testing approach

- Unit tests on `:domain` + `ui/viewmodel` (JVM); coverage gate ≥ 80% (`koverVerifyDebug`).
- Instrumented tests for Room DAOs + Compose UI.
- Inject a `TestDispatcher`; use fakes for repositories. When adding a collaborator to a tested
  class, assert its behavior — don't just fix construction.

## Conventions & gotchas

- Bump the Room schema version and commit the exported schema (`app/schemas/`) on any entity change.
- `data/sync` + Firebase wrappers are kover-excluded (need SDK/instrumented) — real logic isn't.
- Reads come from Room only; a network read that blocks the UI is a bug.
