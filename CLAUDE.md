# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Firebase Authentication app demonstrating email/password authentication and user management using **Kotlin**, **Jetpack Compose**, and **Clean Architecture** with MVVM pattern. The app includes sign-up, sign-in, email verification, password reset, and user profile management flows.

## Build & Development Commands

```bash
# Build the app
./gradlew build

# Run unit tests
./gradlew test

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Build a debug APK
./gradlew assembleDebug

# Build a release APK (requires keystore config)
./gradlew assembleRelease

# Clean build files
./gradlew clean

# Lint checks
./gradlew lint
```

## Architecture Overview

The project follows **Clean Architecture** with three layers:

### 1. **Data Layer** (`data/repository/`)
- **AuthRepositoryImpl**: Implements Firebase Authentication API
  - Email/password sign-up, sign-in
  - Email verification
  - Password reset
  - Auth state management via Flow
  - User reload for email verification status

### 2. **Domain Layer** (`domain/`)
- **AuthRepository** (interface): Defines auth operations contract
- **Response** (sealed class): State holder for async operations (Idle, Loading, Success, Failure)

### 3. **Presentation Layer** (`presentation/`)
- **ViewModels**: Manage UI state using StateFlow
- **Screens** + **Components**: Jetpack Compose UI built with Material Design
- **Navigation**: Compose Navigation handles route management

#### Key Screens:
- `sign_in/`: Email/password login
- `sign_up/`: User registration
- `splash/`: Initial auth check
- `verify_email/`: Email verification flow
- `forgot_password/`: Password reset
- `profile/`: User dashboard

## Dependency Injection (Hilt)

All instances provided in `di/AppModule.kt`. Key bindings:
- `FirebaseAuth` as singleton
- `AuthRepository` interface bound to `AuthRepositoryImpl`

To inject: `@Inject constructor(private val repository: AuthRepository)`

## Key Technologies

| Technology | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 2.1.20 | Language |
| Jetpack Compose | 2025.03.01 | UI |
| Compose Material | Latest | Design system |
| Firebase Auth | 23.2.0 | Authentication |
| Navigation Compose | 2.8.9 | Screen navigation |
| Hilt | 2.56.1 | Dependency injection |
| Kotlin Coroutines | Bundled | Async operations |
| Kotlin Serialization | 1.7.3 | JSON serialization |

## Firebase Setup

1. **google-services.json** must be placed in `app/` (download from Firebase Console)
2. **Google Services plugin** configured in `app/build.gradle` (version 4.4.2)
3. **FirebaseAuth** initialized in Hilt module

### Google Sign-In Integration

Google Sign-In is fully integrated in the data and domain layers:
- `AuthRepositoryImpl.signInWithGoogle(idToken)` handles Google credential authentication
- `GoogleSignInButton` component provides the UI
- See **FIREBASE_GOOGLE_SIGNIN_SETUP.md** for complete configuration steps

**Important**: Replace `YOUR_WEB_CLIENT_ID` in `di/AppModule.kt` with your Firebase project's Web Client ID (from Google Cloud Console).

## Code Patterns & Conventions

### State Management
- Use `StateFlow` in ViewModels for UI state
- Collect with `collectAsStateWithLifecycle()` in Composables
- Separate fields for each input (email, password) and state (signInState)

### Error Handling
- Responses use sealed class `Response<T>` with four states
- Failures carry exception info: `is Response.Failure -> e?.message`
- Toast notifications for user-facing errors via `showToastMessage(context, msg)`

### Navigation
- Route objects defined in `navigation/Route.kt`
- Pass `navigate` and `navigateAndClear` lambdas to screens
- `navigateAndClear` clears back stack for auth flows (prevent return after login)

### Composition
- Keep Composables focused; extract complex sections into `components/` subdirectories
- Use `@Composable` and follow single-responsibility pattern
- Preview functions in separate `*PreviewScreen.kt` files

### Google Sign-In Flow
- `GoogleSignInButton`: Styled button component for initiating Google sign-in
- `SignInViewModel.signInWithGoogle(idToken)`: Handles Firebase authentication with Google credential
- `SignInScreen`: TODO comment marks where Google Sign-In activity launch occurs
- Pass ID token from Google Sign-In result to ViewModel for Firebase authentication

### Async Operations
- All Firebase calls are suspended (`.await()` on Tasks)
- ViewModel wraps repository calls in `viewModelScope.launch`
- Loading state managed via `Response.Loading` during execution

## Testing

Tests should hit real Firebase or use emulator:

```bash
# Run Firebase emulator suite (requires separate setup)
./gradlew connectedAndroidTest
```

Test instrumentation runner: `androidx.test.runner.AndroidJUnitRunner`

## Important Notes

- **No hardcoded secrets**: API keys and Firebase config come from `google-services.json` only
- **Email verification**: After sign-up, users must verify email before profile access
- **Auth persistence**: Firebase Auth handles session persistence automatically
- **Gradle version**: 8.8.2 (Kotlin 2.1.20 compatible)
- **Minimum SDK**: 26 (Android 8.0+)
- **Target SDK**: 35 (Android 15)
- **JVM target**: Java 21