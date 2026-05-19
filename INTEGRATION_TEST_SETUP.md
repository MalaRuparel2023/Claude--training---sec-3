# Integration Test Setup & Execution

## Prerequisites

### 1. Android Emulator or Physical Device
- Android API 26+ (minimum for app)
- Recommended: API 33 or higher
- At least 2GB RAM available

### 2. Firebase Emulator Suite (Optional, for local testing)
```bash
# Install Firebase CLI globally
npm install -g firebase-tools

# Start Firebase emulator
firebase emulators:start --only auth
```

### 3. ADB Access
```bash
# Verify device/emulator is connected
adb devices
# Output should show your device in "device" state
```

## Emulator Setup

### Start Emulator
```bash
# List available emulators
emulator -list-avds

# Start specific emulator
emulator -avd Pixel_4_API_33

# Or from Android Studio: Tools → AVD Manager → Play button
```

### Pre-test Cleanup
```bash
# Clear app data before running tests
adb shell pm clear ro.alexmamo.firebasesigninwithemailandpassword

# Clear DataStore specifically
adb shell rm -rf /data/data/ro.alexmamo.firebasesigninwithemailandpassword/files/datastore/
```

## Running Tests

### Build & Install Tests
```bash
# Build instrumented test APK
./gradlew assembleAndroidTest

# Install app and test APK
./gradlew installDebug installDebugAndroidTest
```

### Run All Tests
```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# With full output
./gradlew connectedAndroidTest --info
```

### Run Specific Test Class
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManagerTest
```

### Run Specific Test Method
```bash
./gradlew connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManagerTest#testSaveSession_SavesUserData
```

## Test Execution Flow

### SessionManagerTest Execution
```
1. Initialize mocked Firebase Auth
2. Create SessionManager with test context
3. Test 1: Save session data to DataStore
4. Verify: Session flow emits correct data
5. Test 2: Update email verification
6. Verify: SessionFlow updates immediately
7. Test 3-8: Additional scenarios
```

### AuthRepositoryImplTest Execution
```
1. Create mocked FirebaseAuth
2. Create AuthRepositoryImpl with SessionManager
3. Test auth state flows
4. Test session clearing on sign-out
5. Verify repository integrates with SessionManager
```

### ProfileViewModelTest Execution
```
1. Create application context
2. Inject dependencies (Auth, Repository)
3. Create ProfileViewModel
4. Test UI state changes
5. Test sign-out flow
6. Verify session is cleared
```

## Monitoring Test Execution

### View Live Test Output
```bash
# Stream logcat during tests
adb logcat | grep SessionManager

# Or use Android Studio: View → Tool Windows → Logcat
```

### Test Report Location
```
app/build/reports/androidTests/connected/
```

### View HTML Report
```bash
# After tests complete, open HTML report
open app/build/reports/androidTests/connected/index.html
```

## Handling Test Failures

### SessionManager Test Fails
```bash
# Check DataStore initialization
adb shell ls -la /data/data/ro.alexmamo.firebasesigninwithemailandpassword/files/datastore/

# View DataStore contents (binary, for debugging)
adb pull /data/data/ro.alexmamo.firebasesigninwithemailandpassword/files/datastore/session_store
```

### Firebase Mock Issues
```
# Ensure Firebase is properly mocked in @Before setup
# Check that Mockito annotations are initialized
# Verify mock FirebaseAuth returns expected values
```

### Process Death Simulation
```
# Manually kill app process to simulate cold start
adb shell am force-stop ro.alexmamo.firebasesigninwithemailandpassword

# Rerun tests - SessionManager should restore session
```

## Manual Test Verification

### Cold Start Test
```bash
1. Run app normally
2. Sign in with test@example.com / password123
3. Verify: Session saved (ViewModel shows email)
4. Force kill app: adb shell am force-stop <package>
5. Reopen app
6. Expected: App navigates to Profile directly
7. Verify: Email and other data displayed
```

### Email Verification Test
```bash
1. Sign up with new account
2. Verify: VerifyEmail screen shown
3. Manually verify email (Firebase console)
4. Click "I already verified my email"
5. Expected: Profile screen becomes accessible
6. Close and reopen app
7. Expected: Session remembers verified state
```

### Sign-Out Test
```bash
1. Sign in and verify profile loads
2. Scroll to bottom, tap "Sign Out"
3. Verify: Session cleared
4. Check DataStore: adb shell rm ... (should work after restart)
5. Force kill app
6. Reopen app
7. Expected: SignIn screen shown (not Profile)
```

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Instrumented Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Run emulator and tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 33
          script: ./gradlew connectedAndroidTest
      
      - name: Upload test reports
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: android-test-reports
          path: app/build/reports/androidTests/
```

## Troubleshooting

### "No connected devices" Error
```bash
# Restart adb
adb kill-server
adb start-server

# Restart emulator or reconnect device
adb devices  # Should list device
```

### "Test APK failed to install" Error
```bash
# Clear existing test APK
adb uninstall ro.alexmamo.firebasesigninwithemailandpassword.test

# Rebuild and reinstall
./gradlew cleanBuildCache connectedAndroidTest
```

### Timeout Errors
```bash
# Increase timeout in test
@Test(timeout = 30000)  // 30 seconds

# Or in gradle
testOptions {
    animationsDisabled = true  // Speed up emulator
}
```

### Firebase Auth Mocking Issues
```kotlin
// Ensure mock is reset between tests
@Before
fun setup() {
    MockitoAnnotations.openMocks(this)
    reset(mockAuth)  // Reset mock state
}
```

## Performance Optimization

### Disable Animations
```bash
# On emulator, disable animations for faster testing
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
```

### Run Tests in Parallel
```bash
# gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=4
```

### Disable Debugger
```bash
./gradlew connectedAndroidTest -Dandroid.testInstrumentationRunnerArguments.debug=false
```

## Test Coverage Report

### Generate Coverage Report
```bash
# Add to build.gradle
android {
    buildTypes {
        debug {
            testCoverageEnabled true
        }
    }
}

# Run tests with coverage
./gradlew connectedAndroidTest --coverage

# View coverage report
open app/build/reports/coverage/
```

## Success Criteria

✅ **All tests pass** (22/22 green)
✅ **No timeout errors**
✅ **Session persists across app restart**
✅ **Sign-out clears all data**
✅ **Email verification status syncs**
✅ **Firebase Auth state reflected in UI**

## Next Steps After Successful Tests

1. **Fix any failures** - Review test output and fix root cause
2. **Monitor coverage** - Aim for >80% coverage on data/presentation layers
3. **Add to CI/CD** - Integrate into continuous integration pipeline
4. **Expand tests** - Add more edge cases and error scenarios
5. **Performance profile** - Monitor test execution time, optimize if needed