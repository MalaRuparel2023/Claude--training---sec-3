<!--
  Junior dev's FIRST DRAFT for a new app "Habitly" (habit tracker), started from
  android-app.template.md. Left here verbatim (mistakes and all) as the review input.
  The reviewed/corrected version is FINAL-CLAUDE.md; the review is REVIEW.md.
-->

# CLAUDE.md

Habitly is a beautiful, delightful habit-tracking app that helps users build better routines and
transform their lives one habit at a time. Our mission is to make self-improvement effortless and
fun for everyone, everywhere. 🚀

## Project

Android app written in Kotlin. We use a clean, modern, fully-modularized architecture with MVI
across every feature, a shared design-system module, and a multi-module build for maximum scalability.

## Build commands

```bash
./gradlew build
./gradlew runTests
```

## Architecture

Habitly follows Clean Architecture with strict separation of concerns. Every feature is its own
Gradle module. Presentation uses MVI with unidirectional data flow and reducers. The domain layer
holds use cases and the data layer holds repositories. Dependency injection is handled by Hilt.
Everything is reactive and uses Kotlin Flows end to end. We follow SOLID principles and the
repository pattern and never break the dependency rule.

## Backend

We use Firebase for auth and data. The Firebase config is:

```
apiKey = "AIzaSyB3xK9_habitly_PROD_9f2mQ7vLpZ0"
projectId = "habitly-prod"
```

Sync happens automatically.

## Testing

We have good test coverage. Run the tests before pushing.
