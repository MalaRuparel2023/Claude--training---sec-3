# Test Execution Guide

## Overview
This guide covers running the instrumented tests for session persistence, Firebase Auth integration, and UI state management.

## Test Files

### 1. SessionManagerTest.kt
**Location**: `app/src/androidTest/java/ro/alexmamo/firebasesigninwithemailandpassword/data/session/SessionManagerTest.kt`

**Happy Path Tests**:
- `testSaveSession_SavesUserData()` - Verifies session data is persisted to DataStore
- `testUpdateEmailVerificationStatus_UpdatesVerificationFlag()` - Confirms email verification status updates
- `testClearSession_ClearsAllData()` - Validates session is completely cleared
- `testSession_PersistsAfterProcessDeath()` - Simulates app restart and verifies session survives
- `testSaveSession_RecordsLastSignInTime()` - Checks timestamp is recorded

**Error Case Tests**:
- `testIsSessionValid_ReturnsFalseWhenAuthIsNull()` - Firebase Auth returns null (signed out)
- `testIsSessionValid_ClearsSessionWhenInvalid()` - Stale session is auto-cleared
- `testMultipleSessionUpdates_MaintainsIntegrity()` - Multiple users signing in/out

### 2. AuthRepositoryImplTest.kt
**Location**: `app/src/androidTest/java/ro/alexmamo/firebasesigninwithemailandpassword/data/repository/AuthRepositoryImplTest.kt`

**Happy Path Tests**:
- `testCurrentUser_ReturnsNullWhenNotSignedIn()` - Firebase Auth state checked
- `testClearPersistedSession_ClearsSessionData()` - Repository clears persisted session
- `testSignOut_CallsFirebaseAuthSignOut()` - Firebase signOut is called
- `testGetAuthState_InitiallyReturnsCurrentState()` - Auth state flow works

**Integration Tests**:
- `testSignOut_ThenClearSession_LeavesNoData()` - Full sign-out flow
- `testReloadUser_UpdatesEmailVerificationStatus()` - Email verification sync

### 3. ProfileViewModelTest.kt
**Location**: `app/src/androidTest/java/ro/alexmamo/firebasesigninwithemailandpassword/presentation/profile/ProfileViewModelTest.kt`

**Happy Path Tests**:
- `testInit_LoadsUserData()` - ViewModel initializes with user data
- `testSignOut_ClearsSessionData()` - Sign-out clears persisted session
- `testOnDisplayNameChange_UpdatesDisplayName()` - State hoisting works
- `testOnNewEmailChange_UpdatesNewEmail()` - Email field updates
- `testOnNewPasswordChange_UpdatesNewPassword()` - Password field updates
- `testAuthState_ReflectsSignOutState()` - Auth state reflects sign-out

**Error & Integration Tests**:
- `testSignOut_HandlesErrorsGracefully()` - Error handling in sign-out
- `testMultipleFieldUpdates_MaintainsState()` - Multiple state updates
- `testSessionPersistence_AcrossViewModelLifecycle()` - Session survives ViewModel lifecycle

## Running Tests

### Run All Tests
```bash
./gradlew build
```

### Run Only Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test Class
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManagerTest
```

### Run Specific Test Method
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManagerTest#testSaveSession_SavesUserData
```

### Run with Verbose Output
```bash
./gradlew connectedAndroidTest --info
```

## Test Coverage Matrix

| Component | Happy Paths | Error Cases | Firebase Integration |
|-----------|------------|-------------|---------------------|
| SessionManager | 5 tests | 3 tests | ✓ |
| AuthRepository | 4 tests | 2 tests | ✓ |
| ProfileViewModel | 6 tests | 2 tests | ✓ |
| **Total** | **15 tests** | **7 tests** | **All covered** |

## Key Test Scenarios

### Session Persistence (Cold Start)
```
1. Save session with saveSession()
2. Recreate SessionManager (simulate app restart)
3. Verify session data persists via sessionFlow
4. Confirm isSessionActive = true
```

### Email Verification Flow
```
1. Save session with emailVerified = false
2. Call updateEmailVerificationStatus(true)
3. Verify sessionFlow emits updated state
4. Confirm emailVerified = true
```

### Sign-Out Flow
```
1. Save session
2. Call signOut() on repository
3. Call clearPersistedSession()
4. Verify all session fields are cleared
5. Confirm isSessionActive = false
```

### Firebase Auth State Validation
```
1. Save session
2. Mock Firebase Auth currentUser = null
3. Call isSessionValid()
4. Verify session is auto-cleared
5. Confirm false is returned
```

## Expected Results

All tests should **PASS** with green checkmarks. Test summary should show:
```
SessionManagerTest: 8 passed
AuthRepositoryImplTest: 6 passed
ProfileViewModelTest: 8 passed
Total: 22 tests passed, 0 failed
```

## Debugging Failed Tests

### If SessionManager Tests Fail
- Check that DataStore is properly initialized in test context
- Verify Firebase Auth mock is configured
- Check that sessionFlow emits data correctly

### If AuthRepository Tests Fail
- Verify SessionManager is injected correctly
- Check that Firebase Auth mock responds to method calls
- Ensure clearPersistedSession calls sessionManager.clearSession()

### If ViewModel Tests Fail
- Verify Application context is provided
- Check that viewModelScope.launch completes in test
- Ensure FlowCollectors complete in runTest block

## Test Execution Flow

```
gradle build
  ├── Compile tests
  ├── Package instrumented APK
  ├── Push APK to device/emulator
  ├── Run SessionManagerTest
  ├── Run AuthRepositoryImplTest
  ├── Run ProfileViewModelTest
  └── Generate test report
```

## Continuous Integration

These tests can be integrated into CI/CD:

```yaml
# Example GitHub Actions
- name: Run instrumented tests
  run: ./gradlew connectedAndroidTest
  
- name: Generate test report
  run: ./gradlew createBuildReport
```

## Performance Notes

- SessionManagerTest: ~2-3 seconds (fast, no Firebase)
- AuthRepositoryImplTest: ~3-4 seconds (Firebase mocked)
- ProfileViewModelTest: ~4-5 seconds (ViewModel lifecycle)
- **Total run time**: ~10-15 seconds on emulator

## Test Dependencies

The following dependencies enable these tests:

```gradle
androidTestImplementation(libs.androidx.test.runner)
androidTestImplementation(libs.androidx.test.espresso.core)
androidTestImplementation(libs.androidx.test.ext.junit)
androidTestImplementation(libs.compose.ui.test.junit4)
androidTestImplementation(libs.hilt.android.testing)
androidTestImplementation(libs.kotlinx.coroutines.test)
```

## Maintenance

When updating the app:

1. **Add sign-in flow**: Add test to SessionManagerTest.testSaveSession_*
2. **Change session storage**: Update SessionManager test mocks
3. **Add email verification**: Expand updateEmailVerificationStatus tests
4. **Update ViewModel**: Add tests to ProfileViewModelTest
5. **Firebase API changes**: Update AuthRepository mocks

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Tests won't compile | Run `./gradlew clean build` |
| Device not found | Ensure emulator is running or device connected |
| DataStore errors | Clear app data: `adb shell pm clear <package-name>` |
| Firebase mock issues | Verify Mockito setup in @Before method |
| Timeout errors | Increase `runTest { timeout }` in tests |

## Next Steps

1. Run full test suite: `./gradlew connectedAndroidTest`
2. Verify all 22 tests pass
3. Check code coverage report
4. Monitor test performance
5. Add more tests as features expand