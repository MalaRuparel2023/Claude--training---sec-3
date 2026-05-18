# Navigation Flow Guide

## Overview

The app implements a clean, auth-aware navigation flow that handles cold starts intelligently. Users see the appropriate screen based on their authentication and email verification status.

## Navigation Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    Cold Start (MainActivity)                 │
│                    Starts at: Splash Screen                  │
└────────────────────────────┬────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
        User Signed Out?             User Signed In?
                │                         │
                ↓                         ├─────────────┬─────────────┐
            ┌────────┐                   │             │             │
            │ SignIn │          Email Verified?   Email Not Verified?
            └────────┘                   │             │
                                         ↓             ↓
                                    ┌───────────┐  ┌──────────────┐
                                    │  Profile  │  │ VerifyEmail  │
                                    │  (Home)   │  │ (Email Check)│
                                    └───────────┘  └──────────────┘
                                                         │
                                                    User verifies
                                                    email link
                                                         │
                                                         ↓
                                                    ┌───────────┐
                                                    │  Profile  │
                                                    │  (Home)   │
                                                    └───────────┘
```

## Routes

| Route | Purpose | Triggered From |
|-------|---------|-----------------|
| `Splash` | Auth state check on app launch | MainActivity (startDestination) |
| `SignIn` | Email/password login | Splash (unsigned out users) / SignUp (after signup) |
| `SignUp` | New user registration | SignIn (sign up link) |
| `ForgotPassword` | Password reset | SignIn (forgot password link) |
| `VerifyEmail` | Email verification screen | Splash (signed in but unverified) / SignUp (after signup) |
| `Profile` | Home/Dashboard after auth | Splash (verified users) / VerifyEmail (after verification) |

## Key Components

### 1. SplashScreen
**File:** `presentation/splash/SplashScreen.kt`

- Shows a loading spinner during auth state check
- Runs once on cold start via `LaunchedEffect`
- Logic:
  - If `currentUser == null` → Navigate to SignIn
  - If `currentUser != null && isEmailVerified == false` → Navigate to VerifyEmail
  - If `currentUser != null && isEmailVerified == true` → Navigate to Profile (Home)

**Key Implementation:**
```kotlin
LaunchedEffect(isUserSignedOut, isEmailVerified) {
    if (isUserSignedOut) {
        navigateAndClear(Route.SignIn)
    } else {
        if (isEmailVerified) {
            navigateAndClear(Route.Profile)
        } else {
            navigateAndClear(Route.VerifyEmail)
        }
    }
}
```

### 2. NavGraph
**File:** `navigation/NavGraph.kt`

- Defines all routes and their composables
- Provides `navigateAndClear` extension for proper back stack management
- Prevents users from navigating back to auth screens after login

### 3. MainActivity
**File:** `presentation/MainActivity.kt`

- Entry point for the app
- Applies Material Design 3 theme
- Creates NavController and passes it to NavGraph
- Hilt-enabled for dependency injection

## Navigation Functions

### `navigateAndClear(route: Route)`
Navigates to a destination and clears the back stack:
```kotlin
fun NavHostController.navigateAndClear(route: Route) = navigate(route) {
    popUpTo(graph.startDestinationId) { inclusive = true }
    graph.setStartDestination(route)
}
```

**Used in:**
- Splash → SignIn/VerifyEmail/Profile
- SignUp → VerifyEmail (after account creation)
- VerifyEmail → Profile (after email verification)
- Profile → SignIn (after sign-out)

This prevents users from:
- Going back to SignIn after logging in
- Going back through the auth flow
- Seeing signed-out screens after signing in

## Error Handling

All authentication errors are caught and converted to user-friendly messages via `AuthErrorHandler`:
- Weak password → "Password must be at least 6 characters long"
- Email already in use → "This email is already registered"
- Network errors → "Network error. Check your connection"
- Invalid email → "Please enter a valid email address"

See `FIREBASE_AUTH_ERROR_HANDLING.md` for complete error mapping.

## Auth State Management

**SplashViewModel** checks current auth state:
```kotlin
val isUserSignedOut get() = repo.currentUser == null
val isEmailVerified get() = repo.currentUser?.isEmailVerified == true
```

**ProfileViewModel** listens to auth state changes via Flow:
```kotlin
private fun getAuthState() = viewModelScope.launch {
    repo.getAuthState().collect { isUserSignedOut ->
        _authState.value = isUserSignedOut
    }
}
```

When user signs out from Profile screen, `authState` changes and triggers re-navigation.

## User Flows

### New User (Signup Flow)
1. App launches → Splash checks auth (none) → SignIn screen
2. Taps "No account? Sign up" → SignUp screen
3. Enters email/password → SignUpViewModel creates account
4. On success → Navigates to VerifyEmail (clears stack)
5. User clicks verification link in email
6. Taps "Already verified?" → VerifyEmailViewModel reloads user
7. On verification confirmed → Navigates to Profile (Home)

### Returning User (Login Flow)
1. App launches → Splash checks auth → SignIn screen
2. Enters credentials → SignInViewModel signs in
3. On success:
   - If email verified → Navigate to Profile (Home)
   - If email not verified → Navigate to VerifyEmail

### Existing Session (Auto-Login)
1. App launches → Splash checks auth (session exists)
2. Checks email verification:
   - If verified → Navigate to Profile (Home)
   - If not verified → Navigate to VerifyEmail

### Sign Out
1. User taps sign out in Profile screen
2. ProfileViewModel calls `repo.signOut()`
3. Auth state changes trigger ProfileViewModel's Flow
4. Navigation back to SignIn screen

## Best Practices Used

✅ **State Hoisting:** Navigation state lives in ViewModels, passed to Screens  
✅ **LaunchedEffect:** Navigation in Splash wrapped to prevent infinite loops  
✅ **Back Stack Management:** `navigateAndClear` prevents returning to auth screens  
✅ **Material Design 3:** Theme applied in MainActivity  
✅ **Error Handling:** Firebase exceptions mapped to user-friendly messages  
✅ **Dependency Injection:** Hilt provides ViewModels and repositories  
✅ **Type-Safe Routes:** Using serializable Route sealed interface  

## Testing the Navigation

### Test Cold Start
```
1. Uninstall the app
2. Install fresh build
3. App should show Splash briefly with loading spinner
4. Immediately navigate to SignIn (no user logged in)
```

### Test Returning User
```
1. Sign up and verify email through the app
2. Force stop the app
3. Reopen the app
4. App should show Splash briefly
5. Navigate directly to Profile screen (auto-login)
```

### Test Email Verification Flow
```
1. Sign up but don't verify email
2. Close app completely
3. Reopen app
4. App should navigate to VerifyEmail screen
5. Verify email (use Firebase emulator or real email)
6. Tap "Already verified?" button
7. Should navigate to Profile after email reload
```
