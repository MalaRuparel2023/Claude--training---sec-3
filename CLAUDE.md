# CLAUDE.md

Guidance for Claude Code when working in this repository (the `ClaudeTraining` Android app).

## Project

Android app, Kotlin + Jetpack Compose + Clean Architecture, Hilt DI. Forked from the
`firebasesigninwithemailandpassword` template — hence the `ro.alexmamo.firebasesigninwithemailandpassword`
`applicationId`/`namespace` (kept stable for Firebase/Play; package dirs are `com.mr.claudetraining`).
minSdk 26, target/compileSdk 36, Java 17.

Two modules:
- `:app` — Compose UI, ViewModels, Firebase/Room/Stream-backed data sources, Hilt wiring.
- `:domain` — pure Kotlin: models + repository interfaces + use cases (no Android/SDK types).

Despite the chat-heavy docs below, the app spans several feature areas — see **Feature areas**.

### Build commands

```bash
./gradlew :app:compileMockDebugKotlin    # fast compile, no Stream SDK
./gradlew :app:assembleMockDebug         # runnable APK without any chat/Stream credentials
./gradlew :app:assembleProdDebug         # real Stream-backed build

# Tests
./gradlew :domain:test                   # pure-Kotlin domain tests (fast)
./gradlew :app:testMockDebugUnitTest     # app unit tests (mock flavor)
./gradlew :app:testMockDebugUnitTest --tests "*ChatDetailViewModelTest"   # single test class
./gradlew :app:connectedMockDebugAndroidTest   # instrumented tests (needs emulator/device)

# Lint & coverage (same checks CI gates on)
./gradlew :app:lintMockDebug
./gradlew :app:koverVerifyMockDebug      # fails if line coverage < 80% (report: app/build/reports/kover/htmlMockDebug/)
```

Almost all dev/test/lint uses the **`mock`** flavor — it needs no network or credentials. Only release
bundles use **`prod`**.

### CI/CD & git hooks

`.github/workflows/android-cicd.yml` gates every PR (lint + unit/coverage + instrumented); pushes to
`dev` auto-distribute via Firebase App Distribution; `v*` tags ship to Play internal.
`deploy-production.yml` promotes to the Play production track behind a manual approval gate. Full details
in `docs/CICD.md`. Install the pre-commit hook (runs domain tests) once after cloning:
`./scripts/install-git-hooks.sh` (bypass with `SKIP_HOOKS=1`).

### Product flavors

| Flavor | Chat backend | When |
|--------|--------------|------|
| `mock` | `FakeChatRepository` (in-memory, seeded) | run/preview/test without network or Stream credentials |
| `prod` | `ChatRepositoryImpl` (real Stream Chat SDK) | real backend |

The `ChatRepository` interface in `:domain` is the seam — both flavors implement it, so
**any change to the interface must be reflected in both `FakeChatRepository` (mock) and
`ChatRepositoryImpl` (prod)**, or one flavor won't compile.

## Code review

Every PR is reviewed against a project-specific checklist before merge. Rubric, process, and the
automated first pass:

| Artifact | Purpose |
|----------|---------|
| `docs/CODE_REVIEW_CHECKLIST.md` | 7-category Android/Kotlin rubric (`NULL RES ERR ARCH TEST A11Y PERF`); every review comment cites a rule ID |
| `docs/CODE_REVIEW_PROCESS.md` | The loop: Claude first pass → human review → compare → iterate on the checklist |
| `.claude/skills/pr-review/` | `/pr-review` skill — runs the checklist over a PR/branch/working-tree diff and writes a structured report |
| `docs/reviews/` | One report per PR (`YYYY-MM-DD-<slug>.md`); `2026-07-02-analytics-performance.md` is the worked example |

**Workflow**: run `/pr-review` (Claude walks all 7 categories, hunting missing error handling,
NPEs, architectural violations, and missing tests; emits severity-ranked findings with before/after
refactors) → post to the PR → human reviews independently and fills the comparison table → deltas
feed back into the checklist. A PR merges only after `:app:lintMockDebug`,
`:app:testMockDebugUnitTest`, `:app:koverVerifyMockDebug` (≥80 %), and the `check-architecture`
skill pass. Prefer the `code-review-graph` MCP tools over whole-file reads when reviewing.

## Feature areas

Each feature follows the same seam: domain model + repository interface in `:domain`, Firebase/Room/Stream
impl in `:app/data`, ViewModel in `:app/ui/viewmodel`. Repos are bound in `di/RepositoryModule.kt`
(plus flavor-specific `di/ChatModule.kt`).

| Area | Domain | Backend |
|------|--------|---------|
| Auth | `AuthRepository` | Firebase Auth (email/password + Google Sign-In) |
| Chat | `ChatRepository` | Stream Chat (`prod`) / in-memory fake (`mock`) — see below |
| Jobs | `JobRepository`, `FilteredJobsRepository`, `SearchRepository` | Firestore + Room cache (`data/local`) |
| Health / Fitness | `HealthRepository` | Firestore |
| Yoga | `YogaRepository` | Firestore |
| Dashboard | `Dashboard` model | aggregates the above |
| Feature flags | `FeatureFlagProvider` | Firebase Remote Config (`data/config`) |
| Push | `PushTokenRepository` | FCM token registration (`data/messaging`); Cloud Functions in `functions/` send |
| Crash / Analytics / Perf | `CrashReporter`, `AnalyticsLogger`, `PerformanceTracer` | Firebase (see **Analytics & Performance**) |

**Local persistence**: Room (`data/local`, schemas exported to `app/schemas/`) caches jobs/messages/users
for offline-first reads; DataStore + `security-crypto` hold preferences/secrets. `data/sync` reconciles
remote→local.

## Stream Chat integration

SDK: `io.getstream:stream-chat-android-client` + `-compose` `6.5.0` (`prod` flavor only).

### Key files

| File | Role |
|------|------|
| `domain/.../repository/ChatRepository.kt` | framework-free chat contract |
| `domain/.../model/Chat.kt` | `ChatChannel`, `ChatMessage`, `ChatUser`, `MessageReaction`, `ChannelSnapshot`, `ChatConnectionState` |
| `app/src/prod/.../data/repository/ChatRepositoryImpl.kt` | Stream-backed impl |
| `app/src/prod/.../data/model/ChatMappers.kt` | Stream `Channel`/`Message`/`User` → domain |
| `app/src/prod/.../di/ChatModule.kt` | provides `ChatClient`, binds repo (prod) |
| `app/src/mock/.../data/repository/FakeChatRepository.kt` | in-memory impl |
| `app/src/mock/.../di/ChatModule.kt` | binds repo (mock) |
| `app/.../ui/viewmodel/ChatDetailViewModel.kt` | per-channel state: messages, typing, reactions, read receipts, presence, search |
| `app/.../ui/viewmodel/ChatListViewModel.kt` | channel list |
| `app/.../ui/viewmodel/UserConnectionViewModel.kt` | sign-in/out → connect/disconnect |
| `app/.../ui/screens/ChatDetailScreen.kt` | conversation UI |
| `app/.../SrteamChatApp.kt` | app-lifecycle ↔ Stream connection wiring |

### Feature map

- **Connection lifecycle** — `ProcessLifecycleOwner` observer in `SrteamChatApp` connects on
  foreground, `disconnect(clearData = false)` on background. `connect()` is state-guarded
  (no-op when already Connected/Connecting; re-opens the socket from Disconnected).
- **Network loss** — handled by Stream's own auto-reconnect; `ChatRepositoryImpl` mirrors
  `ConnectedEvent`/`DisconnectedEvent` into `connectionState`, which drives the "Disconnected"
  banner in `NavigationHost`. An explicit sign-out (`SignedOut`) is never auto-reconnected.
- **Typing indicators** — `startTyping`/`stopTyping` (Stream keystroke/stopTyping);
  `TypingStart/StopEvent` populate `ChannelSnapshot.typingUsers`; "… is typing" row in the list.
- **Reactions** — long-press a message bubble → `ReactionPicker` bottom sheet → `addReaction`
  (`ChatClient.sendReaction`). Counts shown under the bubble from `Message.reactionCounts`.
- **Read receipts** — channel marked read on open and on each new message
  (`ChannelClient.markRead`). `ChannelSnapshot.lastReadByOthers` = max `lastRead` across other
  members (seeded from `channel.read`, updated by `MessageReadEvent`); UI shows "Seen" under my
  latest message at/under that watermark (`ChatDetailUiState.lastSeenMessageId`).
- **Presence** — `ChatUser.isOnline` from `User.online`; `UserPresenceChangedEvent` updates
  cached authors/members; green/grey `PresenceDot` next to incoming author names.
- **Message search** — search icon → `MessageSearchBar` (300 ms debounce) →
  `ChatClient.searchMessages(channelFilter = cid+members, messageFilter = autocomplete text)`.

### Credentials / Stream Dashboard

Currently uses Stream's **public quickstart demo** key/user/token (hardcoded in
`ChatRepositoryImpl` + `ChatModule`):
- API key `yj2prjbtfw2k`, user `tutorial-demi`, demo JWT.

For real use:
1. Create an app in the [Stream Dashboard](https://dashboard.getstream.io) → get the **API key**.
2. **Server-side** (or via the dashboard for testing): generate **user tokens** — never ship the
   API secret in the app. For local testing you can mint dev tokens in the dashboard or with the
   Stream CLI/`StreamChat` server SDK.
3. Replace `STREAM_API_KEY` in `prod` `ChatModule`, and swap the demo `User`/token in
   `ChatRepositoryImpl.connect()` for per-user credentials issued by your backend
   (move them out of source into `BuildConfig`/secure storage).
4. Channels must have the signed-in user as a member to appear in `observeChannels()`
   (filter: `type == "messaging"` AND `members ∋ currentUserId`).

No special device/runtime permissions are required by the Stream SDK itself beyond `INTERNET`
(already granted). Push notifications use the existing FCM setup; notification display needs the
`POST_NOTIFICATIONS` runtime permission (handled in `MainActivity`).

## Analytics & Performance

Firebase Analytics + Performance Monitoring, wired through the same framework-free seam as
`CrashReporter`: typed contracts in `:domain`, Firebase-backed impls in `:app`, bound in
`RepositoryModule`. ViewModels log domain events, never raw Firebase strings, so they stay
unit-testable (pass `mock()` / `NoOpPerformanceTracer`).

### Key files

| File | Role |
|------|------|
| `domain/.../model/AnalyticsEvent.kt` | typed event catalog (name + params); event/param names defined once |
| `domain/.../repository/AnalyticsLogger.kt` | `log(event)`, `setUserId`, `setUserProperty` |
| `domain/.../repository/PerformanceTracer.kt` | `newTrace`/`trace {}`; `PerfTrace`; trace-name constants |
| `app/.../data/analytics/FirebaseAnalyticsLogger.kt` | maps `AnalyticsEvent` → one `logEvent` (Bundle) |
| `app/.../data/analytics/FirebasePerformanceTracer.kt` | wraps Firebase `Trace` |
| `app/.../ui/viewmodel/AnalyticsViewModel.kt` | app-wide `screen_view` tracking from `NavigationHost` |

### Events wired today

- `sign_up` / `login` (param `method`) + `sign_out` — `AuthViewModel`.
- `screen_view` (param `screen_name`) — `NavigationHost` `LaunchedEffect(currentRoute)`.
- `send_message` (param `channel_id`) + `add_reaction` (param `reaction`) — `ChatDetailViewModel`.

The job-funnel events (`view_job_list`, `view_job`, `apply_to_job`, `save_job`, `search`) are
defined in `AnalyticsEvent` but **not yet emitted** — there's no job list/detail/apply UI to hook
them to. Wire them when those screens land.

### Performance traces

- App-start time, screen rendering (slow/frozen frames), and network requests are **auto-captured**
  by the Performance SDK — no code.
- `job_list_load` — `JobRepositoryImpl.observeJobs` (subscription → first emitted list).
- `chat_message_send` — `ChatDetailViewModel.sendMessage` (send → SDK ack).

### Firebase Console setup (one-time, project owner)

The project/app already exist (`google-services.json` checked in). Still required:
1. **Link Google Analytics**: Project settings → Integrations → Google Analytics → Enable, then
   re-download `google-services.json` (current file has no measurement block).
2. **Performance**: open the Performance dashboard once — it self-initializes from the SDK.
3. **Verify events live**: `adb shell setprop debug.firebase.analytics.app
   ro.alexmamo.firebasesigninwithemailandpassword`, run the **prod** build, watch Analytics →
   DebugView. (Standard reports lag ~24h; Performance data up to ~12–24h.)
4. **Optional**: register custom params (`job_id`, `recruiter_id`, …) as custom dimensions to use
   them in standard reports.

Analytics/Performance report from **both** flavors (single impl in `:app` main, like
`CrashlyticsReporter`) — `mock` builds also emit. Filter them out in the console by app version
(mock carries the `-mock` versionName suffix) if that noise is unwanted.
