# CI/CD — TalentSure

How code gets from a commit to the Play Store, and what guards each step.

## TL;DR

| I want to… | Do this |
|---|---|
| Get fast feedback before committing | Install the pre-commit hook (below) — it runs domain tests |
| Open a PR | Push the branch; the **Android CI/CD** workflow gates the merge |
| Ship to internal testers | Push a tag `v*` (e.g. `git tag v1.2 && git push origin v1.2`) |
| Ship to production | Run **Deploy to Production**, then approve the gate |

## Workflows

Two workflows live in `.github/workflows/`:

### 1. `android-cicd.yml` — Android CI/CD

Runs on **push to `master`/`dev`**, **every pull request**, **tags `v*`**, and manual dispatch.

```
lint ─────────────┐
unit-tests ───────┤
instrumented-tests┼─► build (debug APK)         [non-dev refs]
                  ├─► dev-release (bump+APK)     [push to dev]
                  └─► deploy (Play internal)     [tags v*]
```

| Job | What it does | Gate |
|---|---|---|
| `lint` | `:app:lintMockDebug` | Fails on lint errors |
| `unit-tests` | `:domain:test` + `:app:testMockDebugUnitTest`, Kover XML/HTML report, `:app:koverVerifyMockDebug`, Codecov upload | **Fails if line coverage < 80%** |
| `instrumented-tests` | `:app:connectedMockDebugAndroidTest` on an API-30 emulator | Fails on test failure |
| `build` | Assembles `mockDebug` APK artifact (skipped on `dev`) | — |
| `dev-release` | On push to `dev`: bumps `versionCode`/`versionName`, commits back with `[skip ci]`, assembles the APK, and **distributes it to testers via Firebase App Distribution** | Needs lint+unit+instrumented green |
| `deploy` | On `v*` tags: builds signed `prodRelease` bundle, uploads to Play **internal** track | Needs all gates green; uses `play-internal` environment |

All builds, lint and coverage use the **`mock`** flavor (in-memory fakes — no Stream/network creds). Only the release bundle uses the **`prod`** flavor.

### 2. `deploy-production.yml` — Deploy to Production

Promotes a build to the Play Store **production** track. Trigger by **publishing a GitHub Release** or a **manual run** (choose ref + staged-rollout fraction).

```
verify (lint + unit + coverage gate) ─► [⏸ manual approval] ─► deploy-production
```

- `verify` re-runs the quality gates against the exact ref being shipped.
- `deploy-production` targets the **`play-production`** environment. Because that environment has *Required reviewers*, the job pauses at "Waiting for review" — **nothing reaches production without a human approval.** After approval it builds the signed bundle and uploads to the `production` track with a staged rollout (`userFraction`, default 0.1).

## Coverage gate (80%)

Enforced by Kover, configured in `app/build.gradle` (`kover { reports { verify { rule { minBound(80) } } } }`) and run in CI as `:app:koverVerifyMockDebug`. Framework-bound layers that JVM unit tests can't exercise (Compose UI, DI, Room DAOs, Firebase-backed repos, sync/storage) are excluded — see the `excludes` block — so the 80% is measured over ViewModels, use cases and domain/data logic. Instrumented tests cover the excluded layers.

Run it locally: `./gradlew :app:koverVerifyMockDebug` (HTML report at `app/build/reports/kover/htmlMockDebug/index.html`).

## Pull request checks

Every PR triggers `android-cicd.yml` (lint, unit+coverage, instrumented). To **require** them before merge, configure branch protection on `master` (and `dev`):

> Settings ▸ Branches ▸ Add rule ▸ `master`
> - ✅ Require status checks to pass: `Lint`, `Unit tests + coverage`, `Instrumented tests (API 30)`
> - ✅ Require branches to be up to date before merging
> - ✅ Require a pull request before merging

Or via the CLI:

```bash
gh api -X PUT repos/:owner/:repo/branches/master/protection \
  -F required_status_checks.strict=true \
  -f 'required_status_checks.contexts[]=Lint' \
  -f 'required_status_checks.contexts[]=Unit tests + coverage' \
  -f 'required_status_checks.contexts[]=Instrumented tests (API 30)' \
  -F enforce_admins=true \
  -F required_pull_request_reviews.required_approving_review_count=1 \
  -F restrictions=
```

The coverage gate is "maintained" because `koverVerifyMockDebug` runs inside the required `Unit tests + coverage` check — a PR that drops coverage below 80% fails that check and cannot merge.

## Pre-commit hook (local quick checks)

Fast local gate that catches the common CI failures without an emulator. Install once after cloning:

```bash
./scripts/install-git-hooks.sh
```

On each commit that touches `.kt`/`.kts`/`.gradle` files it runs `:domain:test` (pure Kotlin, a few seconds). Docs-only commits skip it. Bypass for one commit with `SKIP_HOOKS=1 git commit ...`. The heavier gates (app unit tests, coverage, instrumented tests, signed builds) stay in CI by design — the hook is meant to stay fast.

The hook source is versioned at `scripts/git-hooks/pre-commit`; `.git/hooks/` is not tracked, so each clone must run the installer.

## Required secrets & environments

Configure under **Settings ▸ Secrets and variables ▸ Actions** and **Settings ▸ Environments**:

| Secret | Used by | Purpose |
|---|---|---|
| `CODECOV_TOKEN` | unit-tests | Upload coverage to Codecov |
| `FIREBASE_APP_ID` | dev-release | Firebase Android app ID (`1:…:android:…`) |
| `FIREBASE_SERVICE_ACCOUNT` | dev-release | JSON of a service account with the *Firebase App Distribution Admin* role |
| `KEYSTORE_BASE64` | deploy jobs | base64 of the upload keystore |
| `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` | deploy jobs | Signing credentials |
| `PLAY_SERVICE_ACCOUNT_JSON` | deploy jobs | Play Developer API service account |

| Environment | Protection | Used by |
|---|---|---|
| `play-internal` | (optional reviewers) | `android-cicd.yml` → `deploy` |
| `play-production` | **Required reviewers** (the approval gate) | `deploy-production.yml` |

Create the keystore secret with:
```bash
base64 -w0 release.keystore   # paste output into KEYSTORE_BASE64
```

## Firebase App Distribution (dev builds)

Every push to `dev` that passes the gates auto-distributes the `mockDebug` APK to
testers. In `dev-release`, after the version bump and `assembleMockDebug`, the
`wzieba/Firebase-Distribution-Github-Action` step uploads the APK to Firebase App
Distribution with the bumped version and commit SHA as release notes.

Setup (one time):

1. **Service account** — in the Firebase / Google Cloud console create a service
   account with the **Firebase App Distribution Admin** role, download its JSON
   key, and store the file contents as the `FIREBASE_SERVICE_ACCOUNT` secret.
2. **App ID** — store the Android app ID as the `FIREBASE_APP_ID` secret
   (this project's is `1:375452750804:android:012069ce4b287eb8a97c41`).
3. **Tester groups** — create group(s) in Firebase ▸ App Distribution ▸ Testers &
   Groups, then set the repo **variable** `FIREBASE_TESTER_GROUPS` to the
   comma-separated group aliases (defaults to `qa` if unset):
   `gh variable set FIREBASE_TESTER_GROUPS --body "qa,internal"`

Testers in those groups get an email/notification with the new build automatically.
The active `qa` group must contain at least one tester email for the build to reach anyone.

## How to ship

- **Internal test track:** `git tag v1.2 && git push origin v1.2` → `deploy` job runs after gates pass.
- **Production:** Actions ▸ *Deploy to Production* ▸ Run (or publish a Release) → wait for the approval prompt → approve → staged rollout begins. Bump the rollout to 1.0 in the Play Console (or re-run with `rollout=1.0`) once healthy.
