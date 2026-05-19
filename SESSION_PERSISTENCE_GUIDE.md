# Session Persistence Guide

## Overview

This app implements secure session persistence using Android DataStore (Preferences) with Firebase Auth integration. Sessions are automatically saved when users authenticate and cleared when they sign out.

## Architecture

### Components

1. **SessionManager** (`data/session/SessionManager.kt`)
   - Manages encrypted session data using DataStore
   - Persists: userId, email, email verification status, last sign-in time
   - Validates session against Firebase Auth state
   - Automatically clears stale sessions

2. **AuthRepositoryImpl** (`data/repository/AuthRepositoryImpl.kt`)
   - Saves session after successful sign-in/sign-up
   - Updates email verification status after reload
   - Clears session on sign-out

3. **ProfileViewModel** (`presentation/profile/ProfileViewModel.kt`)
   - Clears persisted session when user signs out
   - Reflects Firebase Auth state changes in UI

4. **SplashScreen** (`presentation/splash/SplashScreen.kt`)
   - Checks Firebase Auth state on cold start
   - Routes based on auth status (SignIn → VerifyEmail → Profile)
   - Persisted session enables instant navigation for returning users

## Data Flow

### Sign-In/Sign-Up Flow
```
User enters credentials
       ↓
Firebase Auth API
       ↓
Account created/verified
       ↓
AuthRepository.signUpWithEmailAndPassword()
       ↓
sessionManager.saveSession()
       ↓
Session stored in DataStore
       ↓
UI State updated
       ↓
Navigate to VerifyEmail/Profile
```

### Cold Start (App Restart)
```
App launches → MainActivity
       ↓
SplashScreen checks Firebase Auth
       ↓
Firebase Auth.currentUser != null
       ↓
SessionManager.sessionFlow emits saved data
       ↓
UI reflects stored session state
       ↓
Routes to Profile (if verified) or VerifyEmail (if not)
```

### Sign-Out Flow
```
User taps Sign Out
       ↓
ProfileViewModel.signOut()
       ↓
FirebaseAuth.signOut()
       ↓
sessionManager.clearSession()
       ↓
Session cleared from DataStore
       ↓
UI state updated
       ↓
Navigate to SignIn
```

## Session Data Structure

```kotlin
data class SessionData(
    val userId: String = "",
    val userEmail: String = "",
    val emailVerified: Boolean = false,
    val lastSignInTime: String = "",
    val isSessionActive: Boolean = false
)
```

## Security Considerations

1. **DataStore Encryption**: DataStore is encrypted at rest using Android's EncryptedSharedPreferences foundation
2. **Firebase Auth Integration**: Always validates against Firebase Auth state - doesn't trust DataStore alone
3. **Automatic Expiry**: Sessions without valid Firebase Auth are automatically cleared
4. **No Sensitive Data**: Passwords and tokens are NOT persisted - only user metadata

## Testing Happy Paths

### Test 1: Sign-In and Session Persistence
```
1. App is fresh (no user logged in)
2. Sign in with valid email/password
3. Session saved to DataStore
4. Force kill app
5. Reopen app
6. App shows Splash briefly
7. Automatically navigates to Profile
8. User data loaded from persisted session
```

**Expected Result**: User sees their profile immediately without logging in again

### Test 2: Email Verification Flow
```
1. Sign up with new email
2. Session saved with emailVerified=false
3. Force kill app
4. Reopen app
5. App shows VerifyEmail screen (not Profile)
6. User clicks email verification link
7. App reloads user data
8. emailVerified updated to true in session
9. User manually triggers "Already verified?" button
10. App navigates to Profile
```

**Expected Result**: Session reflects email verification state changes

### Test 3: Sign-Out Clears Session
```
1. Sign in and verify session is saved
2. Scroll to bottom of Profile screen
3. Tap "Sign Out" button
4. Session cleared from DataStore
5. Firebase Auth.signOut() called
6. Redirect to SignIn screen
7. Force kill app
8. Reopen app
9. App shows SignIn (not Profile)
```

**Expected Result**: Session completely cleared, returning users see login screen

### Test 4: Session Survives App Restart
```
1. Sign in with verified email
2. Session stored with all user data
3. Close app (normal exit)
4. Reopen app immediately
5. Splash checks auth and loads session
6. User stays on Profile (no redirect)
7. Profile data shows correct email/name
```

**Expected Result**: Seamless return to Profile with pre-loaded data

### Test 5: Email Verification Status Sync
```
1. Sign up (emailVerified=false in session)
2. Check Profile screen - restricted
3. Manually verify email via Firebase console/emulator
4. Don't close app, but go to Verify Email screen
5. Tap "I already verified my email"
6. App reloads user from Firebase Auth
7. Session updated with emailVerified=true
8. Profile screen now accessible
```

**Expected Result**: UI reflects Firebase Auth email verification changes

## Testing Error Cases

### Error 1: Network Loss During Sign-In
```
1. Start sign-in flow
2. Turn off internet
3. Observe sign-in fails with "Network error"
4. No session saved (DataStore empty)
5. Turn on internet
6. Try sign-in again
7. Success - session now saved
```

**Expected Result**: Session only saved on successful auth, network failures don't corrupt state

### Error 2: Firebase Auth Timeout
```
1. User signs in successfully
2. Session saved to DataStore
3. Firebase Auth token expires (simulated)
4. User tries to access Profile
5. Firebase Auth.currentUser returns null
6. SessionManager.clearSession() called automatically
7. Redirect to SignIn
```

**Expected Result**: Stale sessions automatically detected and cleared

### Error 3: DataStore Corruption
```
1. User has active session saved
2. Manually delete DataStore file (adb shell)
3. Force stop app
4. Reopen app
5. Splash can't load session
6. Falls back to Firebase Auth state check
7. If user still logged in to Firebase, session recreated
```

**Expected Result**: Graceful fallback to Firebase Auth source of truth

### Error 4: Session Data Mismatch
```
1. User signs in, session saved
2. Manually update Firebase Auth (admin SDK/console)
3. Change user email to new@example.com
4. Restart app
5. Splash loads session with old email
6. Calls sessionManager.isSessionValid()
7. Detects mismatch, clears session
8. Redirect to SignIn
```

**Expected Result**: Session validation prevents stale data scenarios

### Error 5: Multiple Concurrent Sign-Ins
```
1. User A signs in on Device 1
2. Session saved: userId=A, email=user-a@example.com
3. User B signs in on same device
4. Previous session cleared
5. New session saved: userId=B, email=user-b@example.com
6. Restart app
7. Shows User B's profile
```

**Expected Result**: Latest sign-in takes precedence, previous session overwritten

## Testing Firebase Auth Events Reflection

### Test 1: Email Verification Changes UI
```
1. Sign up (emailVerified=false)
2. Observe: Profile screen locked, VerifyEmail screen available
3. Verify email via link
4. Click "Already verified?" button
5. ProfileViewModel.reloadUser() → sessionManager.updateEmailVerificationStatus(true)
6. Observe: Profile screen now accessible
```

**Assertion**: `sessionFlow.collect { emailVerified }` changes from false → true

### Test 2: Sign-Out Changes Auth State
```
1. User logged in, authState=false (signed out)
2. Observe: Profile screen available
3. Tap Sign Out
4. repo.signOut() called
5. sessionManager.clearSession() called
6. Observe: getAuthState() flow emits true (signed out)
7. ProfileViewModel redirects to SignIn
```

**Assertion**: `authState.collect()` changes from false → true after sign-out

### Test 3: Firebase Auth Token Refresh Updates Session
```
1. User signs in at 12:00 PM
2. Session.lastSignInTime = "1000000000"
3. Firebase Auth token auto-refreshes at 12:10 PM
4. App calls repo.currentUser.reload()
5. sessionManager.updateEmailVerificationStatus() syncs verification
6. Session reflects current Firebase state
```

**Assertion**: Session always reflects Firebase Auth.currentUser state

## UI State Verification Tests

### Test 1: Session State Reflects in UI
```kotlin
@Test
fun testSignIn_UIShowsUserData() = runTest {
    signInWithValidCredentials()
    
    val sessionState = sessionManager.sessionFlow.first()
    assertTrue(sessionState.isSessionActive)
    assertEquals("test@example.com", sessionState.userEmail)
    
    val profileUIState = profileViewModel.emailState.first()
    assertEquals("test@example.com", profileUIState)
}
```

### Test 2: Session Survives Recomposition
```kotlin
@Test
fun testProfileScreen_SessionPersistsAfterRecomposition() = runTest {
    val sessionBefore = sessionManager.sessionFlow.first()
    assertTrue(sessionBefore.isSessionActive)
    
    // Trigger recomposition
    composeTestRule.onRoot().performClick()
    
    val sessionAfter = sessionManager.sessionFlow.first()
    assertEquals(sessionBefore.userId, sessionAfter.userId)
}
```

### Test 3: Navigation Respects Session State
```kotlin
@Test
fun testNavigation_SignIn_UpdatesSessionThenNavigation() = runTest {
    navController.navigate(Route.SignIn)
    
    signInWithValidCredentials()
    
    val session = sessionManager.sessionFlow.first()
    assertTrue(session.isSessionActive)
    
    advanceUntilIdle()
    assertEquals(Route.Profile, navController.currentBackStackEntry?.destination?.route)
}
```

## Monitoring Session State

### In Logcat
```bash
# Session save
adb logcat | grep "SessionManager: Saving session"

# Session clear
adb logcat | grep "SessionManager: Clearing session"

# Validation
adb logcat | grep "SessionManager: Validating session"
```

### In Android Studio Profiler
1. Open Profiler → Database Inspector
2. App name → sqlite → sessionStore
3. View SessionData in real-time
4. Observe changes during sign-in/out

### Manual DataStore Inspection
```bash
# View DataStore file
adb shell ls -la /data/data/ro.alexmamo.firebasesigninwithemailandpassword/files/datastore/

# Read DataStore contents (binary, but can inspect with tools)
adb pull /data/data/ro.alexmamo.firebasesigninwithemailandpassword/files/datastore/session_store
```

## Best Practices

✅ **DO:**
- Always validate Firebase Auth state on app start
- Clear session if Firebase Auth returns null
- Sync session after critical Firebase operations (reload, updateEmail)
- Use sessionFlow for reactive UI updates
- Test both happy paths and error scenarios

❌ **DON'T:**
- Rely solely on DataStore - always verify with Firebase Auth
- Persist sensitive data (passwords, tokens)
- Skip error handling in session operations
- Assume DataStore survives app uninstall (it doesn't - reinstall clears it)
- Update UI directly from DataStore without Firebase Auth validation

## Troubleshooting

**Issue**: User sees SignIn screen after restart even though they were logged in
- **Cause**: Firebase Auth token expired, session validation failed
- **Fix**: User signs in again; then session re-established

**Issue**: Email verification status not updating in UI
- **Cause**: ProfileViewModel.reloadUser() not called after email verification
- **Fix**: Ensure VerifyEmailViewModel calls repository.reloadUser()

**Issue**: DataStore file keeps growing
- **Cause**: Multiple session saves without clearing old data
- **Fix**: SessionManager.clearSession() is idempotent, handles cleanup

## API Reference

### SessionManager

```kotlin
// Observe session changes
val sessionFlow: Flow<SessionData>

// Save session after successful auth
suspend fun saveSession(userId, userEmail, emailVerified)

// Update only email verification status
suspend fun updateEmailVerificationStatus(verified)

// Clear all session data
suspend fun clearSession()

// Validate session against Firebase Auth
suspend fun isSessionValid(): Boolean
```

### AuthRepository Extensions

```kotlin
// Called after signUp, signIn, signInWithGoogle
// Automatically saves session

// Called after reloadUser()
// Syncs email verification status

// Called on sign-out
override suspend fun clearPersistedSession()
```

