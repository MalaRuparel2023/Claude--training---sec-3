# Password & Email Link Authentication Implementation

This document describes the implementation of password-based authentication and passwordless email link authentication.

## Overview

The app supports two authentication methods:
1. **Password Authentication** - Traditional email/password sign-up and sign-in
2. **Email Link Authentication** - Passwordless sign-in via email link

## Part 1: Password Authentication

### Email & Password Validation

All password operations include validation helpers in `core/Utils.kt`:

```kotlin
fun isValidEmail(email: String): Boolean
fun isValidPassword(password: String): Boolean
```

- **Email validation**: Uses Android's `Patterns.EMAIL_ADDRESS` regex
- **Password validation**: Minimum 6 characters
- **Validation triggers**: Before sign-in/sign-up operations

### Sign-up Flow

**Repository Method**: `AuthRepository.signUpWithEmailAndPassword(email: String, password: String)`

**Steps**:
1. User enters email and password on SignUpScreen
2. Input validation performed
3. `Firebase.createUserWithEmailAndPassword()` called
4. New user account created in Firebase
5. User is automatically signed in
6. Email verification link is sent
7. User navigates to VerifyEmail screen
8. Can proceed to profile only after email verification

**Error Handling**:
- Weak password errors (< 6 characters)
- Email already in use
- Invalid email format
- Network errors
- All handled via `AuthErrorHandler.handleAuthException()`

### Sign-in Flow

**Repository Method**: `AuthRepository.signInWithEmailAndPassword(email: String, password: String)`

**Steps**:
1. User enters email and password on SignInScreen
2. Input validation performed
3. `Firebase.signInWithEmailAndPassword()` called
4. Check if email is verified via `currentUser.isEmailVerified`
5. If verified → navigate to Profile
6. If not verified → navigate to VerifyEmail screen
7. User must click link in verification email

**Error Handling**:
- Invalid credentials (email not found, wrong password)
- Too many failed attempts (account temporarily locked)
- Network errors
- User disabled account

### Key Security Features

- Passwords transmitted over HTTPS only
- Firebase stores passwords with bcrypt hashing
- Session tokens managed automatically by Firebase
- Token refresh happens transparently
- Sign-out clears all local session data

### Sign-up Screen Components

- **SignUpViewModel**: Manages email, password state and validation
- **SignUpScreen**: Orchestrates UI and navigation
- **SignUpContent**: Form fields with validation feedback

### Sign-in Screen Components

- **SignInViewModel**: Manages email, password, sign-in state
- **SignInScreen**: Handles auth response and navigation
- **SignInContent**: Login form + social sign-in options
- Added: Email link sign-in alternative

### Email Verification

**Repository Method**: `AuthRepository.sendEmailVerification()`

After sign-up or email change, user must verify their email:

1. Verification link sent to email address
2. Link expires after 24 hours (Firebase default)
3. User clicks link in email to verify
4. Redirects to deep link handled by the app
5. User reloaded to refresh email verification status
6. Can now access protected screens (Profile, etc.)

**VerifyEmailScreen Features**:
- Displays current email address
- "Resend Email" button for new link
- "Already verified?" check button
- Shows verification status
- Auto-navigates to Profile when verified

### Password Reset Flow

**Repository Method**: `AuthRepository.sendPasswordResetEmail(email: String)`

When user forgets password:

1. User navigates to ForgotPasswordScreen
2. Enters email address
3. Password reset link sent to email
4. Link expires after 1 hour
5. User clicks link and creates new password
6. Next sign-in uses new password
7. All previous sessions invalidated
8. User must sign in again with new password

## Part 2: Email Link Authentication (Passwordless)

### Overview

Email link authentication allows users to sign in without a password:
- User provides only email address
- Firebase sends a secure sign-in link
- User clicks link to authenticate automatically
- No password required
- Safer than password-based auth for some use cases

### Implementation Files

- **EmailLinkSignInViewModel.kt**: State and logic
- **EmailLinkSignInScreen.kt**: Screen orchestration
- **EmailLinkSignInContent.kt**: UI layout

### Repository Methods

**1. Send Sign-in Link**
```kotlin
suspend fun sendSignInLinkToEmail(email: String)
```
- Configures action code settings with app-specific details
- Sets deep link URL for email link handling
- Sends email with secure sign-in link
- Link valid for 24 hours

**2. Sign-in with Email Link**
```kotlin
suspend fun signInWithEmailLink(email: String, emailLink: String)
```
- Verifies the email link is valid
- Signs in user with provided email and link
- Creates authenticated session
- Sets user as current authenticated user

**3. Check if Link is Valid**
```kotlin
fun isSignInWithEmailLink(link: String): Boolean
```
- Checks if the provided link is a valid Firebase email sign-in link
- Used to verify intents from deep links
- Returns true only for valid sign-in links

### Action Code Settings

Email link authentication requires configuration:

```kotlin
val actionCodeSettings = ActionCodeSettings.newBuilder()
    .setUrl("https://firebasesigninwithemailandpassword.page.link")
    .setHandleCodeInApp(true)
    .setIOSBundleId("ro.alexmamo.firebasesigninwithemailandpassword")
    .setAndroidPackageName(
        "ro.alexmamo.firebasesigninwithemailandpassword",
        true,
        null
    )
    .build()
```

**Key Settings**:
- `setUrl()`: Deep link URL Firebase will redirect to
- `setHandleCodeInApp(true)`: Handle link in app instead of browser
- `setAndroidPackageName()`: Package name to receive deep link
- Firebase Dynamic Links used automatically

### Email Link Sign-in Flow

**User-facing Steps**:

1. User selects "Sign in with email link" on SignInScreen
2. Navigates to EmailLinkSignInScreen
3. Enters email address
4. Clicks "Send Sign-in Link"
5. Receives email with sign-in link
6. Clicks link in email
7. App opens automatically
8. Link intercepted via deep link
9. User signed in automatically
10. Navigated to Profile screen

**Behind the Scenes**:

1. `sendSignInLinkToEmail(email)` called
2. Firebase Auth generates secure link
3. Email sent with link and deep link URL
4. User clicks link
5. System detects deep link with `oobCode` parameter
6. Intent received by app (requires AndroidManifest configuration)
7. Extract email link from intent
8. Verify link is valid: `isSignInWithEmailLink(link)`
9. Call `signInWithEmailLink(email, link)`
10. User authenticated and current user set
11. Navigation to Profile

### AndroidManifest Configuration (Required)

To handle email links, add intent filter to your main activity:

```xml
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="firebasesigninwithemailandpassword.page.link"
            android:pathPrefix="/" />
    </intent-filter>
</activity>
```

Note: Update URL to match your Firebase Dynamic Link domain.

### Firebase Dynamic Links Setup

Email link authentication uses Firebase Dynamic Links:

1. Enable Firebase Dynamic Links in Firebase Console
2. Create domain (e.g., `firebasesigninwithemailandpassword.page.link`)
3. Add Android app in Dynamic Links settings
4. Configure Android package name and fingerprint
5. Set handled code in app to true
6. Update AndroidManifest.xml with intent filters

### ViewModel States

**EmailLinkSignInViewModel** manages:

```kotlin
val email: StateFlow<String>                          // Email input
val sendLinkState: StateFlow<Response<Unit>>          // Send link response
val signInWithLinkState: StateFlow<Response<Unit>>    // Sign-in response
```

**Methods**:
- `onEmailChange(email: String)` - Update email input
- `sendSignInLink()` - Send sign-in link to email
- `signInWithEmailLink(emailLink: String)` - Sign in with link
- `isSignInWithEmailLink(link: String)` - Validate link

### UI Flow

**EmailLinkSignInScreen**:
1. Top bar with back button
2. Title and description
3. Email input field
4. "Send Sign-in Link" button
5. Success message when link sent
6. Links to password sign-in and sign-up

**Responses Handled**:
- `Idle`: Initial state
- `Loading`: Link being sent or authentication in progress
- `Success`: Link sent or user signed in
- `Failure`: Error message displayed

### Advantages of Email Link Authentication

1. **No password to remember** - Users only need email
2. **Better security** - No phishing-prone passwords
3. **Seamless UX** - Single click from email link
4. **Works offline** - Link can be processed when app opens
5. **Fallback option** - Alternative if password auth fails
6. **Cross-device** - Can sign in on different devices

### Email Link Sign-in Integration Points

1. **SignInScreen**: Added "Sign in with email link" text link
2. **Route.kt**: Added `EmailLinkSignIn` route
3. **NavGraph.kt**: Added composable for EmailLinkSignInScreen
4. **MainActivity**: Needs intent filter for deep links

### Email Link Validation

Link validation checks:
- Link contains `oobCode` parameter (Firebase sign-in code)
- Link may contain `continueUrl` (redirect after sign-in)
- Uses `FirebaseAuth.isSignInWithEmailLink()` for verification

### Deep Link Handling in MainActivity

```kotlin
// In onCreate() or relevant activity lifecycle
intent?.data?.let { uri ->
    val emailLink = uri.toString()
    if (repository.isSignInWithEmailLink(emailLink)) {
        // Show email entry screen if not available
        // Call signInWithEmailLink when ready
    }
}
```

### Expiration & Limits

- **Link expiration**: 24 hours (Firebase default)
- **Resend limit**: No limit, but rate limited per IP
- **Session duration**: 30 days (Firebase default)
- **Token expiration**: 1 hour with auto-refresh

## Security Comparison

| Feature | Password Auth | Email Link Auth |
|---------|---------------|-----------------|
| Password required | Yes | No |
| Phishing vulnerable | Yes | No (code in link) |
| Session management | Auto | Auto |
| 2FA capable | Yes | Yes |
| Device specific | No | No |
| Cross-device sign-in | Yes | Yes |
| Complexity | Medium | Low |

## Error Handling

Both authentication methods use `AuthErrorHandler` for consistent error messages:

- **Firebase exceptions** → User-friendly messages
- **Network errors** → Suggest retry
- **Invalid input** → Field validation errors
- **User disabled** → Account deactivated message
- **Too many attempts** → Account temporarily locked

## Testing

### Password Authentication Testing

1. **Sign-up**:
   - Valid email + valid password → Success
   - Invalid email format → Error
   - Short password (< 6 chars) → Error
   - Email already in use → Error

2. **Sign-in**:
   - Valid credentials → Signed in
   - Wrong password → Error
   - Non-existent email → Error
   - Email not verified → Verify Email screen

3. **Email Verification**:
   - Receive email with link → Works
   - Click link → Verifies automatically
   - Resend link → New link sent

### Email Link Authentication Testing

1. **Send Link**:
   - Valid email → Link sent successfully
   - Invalid email → Error message
   - Already registered → Can still send link

2. **Sign-in with Link**:
   - Click link from email → Auto sign-in
   - Copy link to browser → Opens app (with intent filter)
   - Expired link → Error message
   - Invalid link → Error message

3. **Deep Link Handling**:
   - Receive email on different device → Still works
   - Click link while app is closed → Opens app and signs in
   - Valid intent filter configured → Link handled properly

## Best Practices

1. **Always validate input** before Firebase calls
2. **Use both methods** for maximum user choice
3. **Show loading indicators** during auth operations
4. **Handle network errors** gracefully
5. **Implement account recovery** (forgot password)
6. **Clear sensitive data** on sign-out
7. **Use HTTPS only** for deep links
8. **Test deep links** on physical devices
9. **Implement rate limiting** on client side
10. **Monitor auth errors** for security issues

## References

- [Firebase Email/Password Authentication](https://firebase.google.com/docs/auth/android/password-auth)
- [Firebase Email Link Authentication](https://firebase.google.com/docs/auth/android/email-link-auth)
- [Firebase Dynamic Links Documentation](https://firebase.google.com/docs/dynamic-links)
- [Handling Deep Links in Android](https://developer.android.com/training/app-links)
- [FirebaseUser API Reference](https://firebase.google.com/docs/reference/kotlin/com/google/firebase/auth/FirebaseUser)