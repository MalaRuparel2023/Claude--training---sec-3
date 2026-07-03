# Prompt Library — Android/Kotlin with Claude Code

Reusable prompts for everyday work in **ClaudeTraining** (Kotlin, Compose, Clean Architecture, Hilt,
Room, Firebase). Grouped by category. For each: **what it does · when to use · example (this repo) ·
expected output.** Copy, fill the `<…>`, run.

> These are starting points, not magic strings. Feed Claude the existing pattern to follow, verify the
> output (compile/test), and iterate. Review-specific prompts live in `CLAUDE_ASSISTED_REVIEW.md`.

---

## 🎨 UI & Compose

### 1. Generate a responsive Compose component
- **What:** produces a Composable that adapts to width (phone/tablet/landscape).
- **When:** new screen/component, or one that stretches badly on tablets.
- **Example:** *"Generate a responsive `JobCard` Composable: single column on compact width, and cap
  content width / two-pane above 600dp. Use `MaterialTheme.colorScheme` tokens."*
- **Expected:** a Composable using `BoxWithConstraints`/`WindowSizeClass`, a stateless preview, no
  hardcoded colors. (See the real `ProfileScreen` refactor: `widthIn(max=840.dp)` + 600dp two-pane.)

### 2. Refactor a screen for dark mode
- **What:** replaces hardcoded colors with theme tokens and fixes missing dark tokens.
- **When:** a screen looks wrong in dark theme, or before shipping a new screen.
- **Example:** *"Refactor `<Screen>` for dark mode — use `colorScheme` tokens, and check
  `Theme.kt`'s `DarkColorScheme` for missing `onSurface`/`onBackground` tokens. Add light+dark previews."*
- **Expected:** token-based colors, theme fixes if the dark scheme is incomplete, `@Preview` with
  `uiMode = UI_MODE_NIGHT_YES`. (Real precedent: we added `onSurface`/`onSurfaceVariant`/`onBackground`
  to the dark scheme during the ProfileScreen pass.)

### 3. Add animations to a transition
- **What:** adds entry/state-change/press animations.
- **When:** UI feels static; you want tactile feedback or a polished entry.
- **Example:** *"Add a press-scale animation to `SettingsItem` and stagger the settings rows in on
  first composition."*
- **Expected:** `animateFloatAsState`/`AnimatedVisibility`/`interactionSource`-driven motion, spring
  specs, no jank in the composition body. (Real precedent: the `AnimatedEntry` cascade + press-scale
  on ProfileScreen.)

## 🏛️ Architecture

### 4. Design a Clean Architecture use case
- **What:** a single-responsibility use case in `:domain` (pure Kotlin) over a repository interface.
- **When:** business logic that shouldn't live in a ViewModel or repository.
- **Example:** *"Design a `ToggleSavedJobUseCase` in `:domain` over `SavedJobsRepository` — pure
  Kotlin, no Android types."*
- **Expected:** a small `class …UseCase @Inject constructor(private val repo: …)` with an `operator
  fun invoke(...)`, no framework imports, easily unit-testable.

### 5. Create a Hilt module
- **What:** a DI module that provides/binds a dependency.
- **When:** you add a new repository/impl/SDK object that needs injecting.
- **Example:** *"Create the Hilt binding for `SavedJobsRepositoryImpl` → `SavedJobsRepository` in
  `RepositoryModule`, and provide the new DAO alongside the other DAOs."*
- **Expected:** `@Module @InstallIn(SingletonComponent::class)` with `@Binds`/`@Provides`, correct
  scope (`@Singleton`), following the existing `RepositoryModule`/`AppModule` pattern.

### 6. Review a ViewModel for architecture violations
- **What:** flags dependency-rule breaks, SDK types in the VM, untestable construction, logic in the
  wrong layer.
- **When:** before merging a new/changed ViewModel.
- **Example:** *"Review `ChatDetailViewModel` for architecture violations — does it depend only on
  domain interfaces? Is it unit-testable without the Firebase/Stream SDK?"* (or the `check-architecture` skill)
- **Expected:** findings citing `ARCH-*` rules, e.g. "depends on concrete impl instead of interface",
  "does business logic that belongs in a use case", with fixes.

## 🧪 Testing

### 7. Generate unit tests
- **What:** JUnit tests for a use case / ViewModel / mapper, success + failure branches.
- **When:** new logic, or coverage under the 80% gate.
- **Example:** *"Generate unit tests for `AuthViewModel` — sign-in success logs `Login("email")`,
  failure logs nothing. Use a `TestDispatcher` and mock the `AnalyticsLogger`."*
- **Expected:** `runTest`-based tests, injected dispatcher, `verify(...)` on behaviour (not just
  construction), edge cases. Run `:app:testMockDebugUnitTest`.

### 8. Create integration tests for a flow
- **What:** tests that exercise several units together (repo + DAO, or VM + fake repo).
- **When:** the risk is in the wiring, not a single function.
- **Example:** *"Create an integration test for the Saved Jobs flow: toggle via the repository, assert
  the DAO Flow emits the updated set and the ViewModel state reflects it."*
- **Expected:** a test using an in-memory/fake DAO + real repo (or Room in-memory for a DAO
  integration test), asserting end-to-end behaviour and offline reconciliation.

### 9. Add UI tests for a screen
- **What:** Compose UI tests (androidTest) for a screen's states and interactions.
- **When:** critical user-facing screens; interaction logic worth pinning.
- **Example:** *"Add Compose UI tests for `SavedJobsScreen`: empty state, a saved job renders, and
  tapping the bookmark calls the toggle."*
- **Expected:** `createComposeRule()` tests with `onNodeWithText`/`onNodeWithContentDescription`,
  `performClick`, state assertions. Note: these run on device/emulator (`connectedMockDebugAndroidTest`).

## 🗄️ Data

### 10. Design a Room entity
- **What:** a `@Entity` + DAO for local persistence, following the existing schema-export setup.
- **When:** you need offline storage/caching for a new data type.
- **Example:** *"Design a `SavedJobEntity` (jobId PK, savedAt) + `SavedJobDao` with a `Flow`-returning
  query; wire it into the `@Database`, bump the version, and export the schema to `app/schemas/`."*
- **Expected:** entity + DAO with `Flow` reads and suspend writes, `@Database` version bump, migration,
  exported schema JSON — matching the existing `data/local` pattern.

### 11. Create a Flow-based repository
- **What:** a repository whose reads are cold `Flow`s (reactive, lifecycle-friendly).
- **When:** UI should react to data changes instead of one-shot fetches.
- **Example:** *"Create `SavedJobsRepositoryImpl`: expose `observeSavedIds(): Flow<Set<String>>` from
  the DAO, `setSaved` writes through. Interface in `:domain`, impl in `:app/data`, bound in Hilt."*
- **Expected:** interface in `:domain`, impl mapping entities→domain at the boundary, `Flow` reads,
  `distinctUntilChanged` where sensible, no Android types leaking into `:domain`.

### 12. Implement offline-first sync
- **What:** local store as source of truth, reconciled with the remote, with a durable outbound queue.
- **When:** the feature must work offline and converge when back online.
- **Example:** *"Make Saved Jobs offline-first: Room is the source of truth (UI reads it), writes
  enqueue a remote sync to Firestore, and reconnect flushes the queue idempotently."*
- **Expected:** UI reads never block on network; remote→local on connect; local→remote flush of a
  persisted queue (dedup by id); a documented conflict rule. Test the airplane-mode→act→reconnect path.

---

## Contributing to this library

Found a prompt that works well (or one here that doesn't)? Open a PR editing this file. New entries
follow the same four fields. The library is expected to grow — it's reviewed like code and shared via
the adoption package (`adoption/README.md`).
