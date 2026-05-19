# Email Link Authentication Setup Guide

This guide explains how to configure email link authentication in your Firebase project and Android app.

## Prerequisites

- Firebase project with Authentication enabled
- Android app registered in Firebase Console
- Android Manifest file

## Step 1: Enable Email Link Sign-in in Firebase Console

1. Go to Firebase Console → Your Project
2. Navigate to **Authentication** → **Sign-in method**
3. Enable **Email/Password** provider (if not already enabled)
4. Scroll to **Email Link (Passwordless)** section
5. Enable **Email Link (Passwordless Sign-in)**
6. Configure settings:
   - **Email**: Sender email (shows in emails sent to users)
   - **Email Template**: Customize email message if desired

## Step 2: Set up Firebase Dynamic Links

Email link authentication uses Firebase Dynamic Links. Set these up:

1. Go to Firebase Console → **Dynamic Links**
2. Click **New Dynamic Link**
3. Create a domain:
   - Firebase provides a default domain (e.g., `appname.page.link`)
   - Or use custom domain (requires domain verification)
4. Note the domain URL for later use

### Example Domain
```
https://firebasesigninwithemailandpassword.page.link
```

## Step 3: Configure Android Intent Filters

Add intent filter to `AndroidManifest.xml` to handle email links:

### Location: `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <activity
            android:name=".MainActivity"
            android:exported="true">
            
            <!-- Existing launcher intent filter -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <!-- NEW: Intent filter for Firebase Dynamic Links -->
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
    </application>
</manifest>
```

**Important**: Replace `firebasesigninwithemailandpassword.page.link` with your actual Dynamic Link domain.

## Step 4: Update Action Code Settings in Code

The app already has this configured in `AuthRepositoryImpl.kt`:

```kotlin
override suspend fun sendSignInLinkToEmail(email: String) {
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
    auth.sendSignInLinkToEmail(email, actionCodeSettings).await()
}
```

**Update the URLs to match your Dynamic Link domain:**

```kotlin
.setUrl("https://YOUR_DYNAMIC_LINK_DOMAIN.page.link")
```

## Step 5: Handle Intent in MainActivity

The app needs to intercept the deep link and handle sign-in. In your `MainActivity.kt`:

```kotlin
import android.os.Bundle
import ro.alexmamo.firebasesigninwithemailandpassword.core.logErrorMessage
import ro.alexmamo.firebasesigninwithemailandpassword.core.showToastMessage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep links from email sign-in
        handleEmailLinkSignIn(intent)
        
        setContent {
            FirebaseSignInWithEmailAndPasswordTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        // Handle deep links if app is already open
        handleEmailLinkSignIn(intent)
    }

    private fun handleEmailLinkSignIn(intent: android.content.Intent) {
        intent.data?.let { uri ->
            val emailLink = uri.toString()
            logErrorMessage("Received deep link: $emailLink")
            
            // Check if it's an email link sign-in
            if (Firebase.auth.isSignInWithEmailLink(emailLink)) {
                // Navigate to email link sign-in screen with the link
                // This will be handled by EmailLinkSignInScreen
                showToastMessage(this, "Email link received, redirecting to sign-in...")
                
                // If not already on the EmailLinkSignIn screen,
                // the app will navigate there automatically
            }
        }
    }
}
```

## Step 6: App Configuration in Firebase

1. Go to Firebase Console → Project Settings
2. Select your Android app
3. Ensure these are set correctly:
   - **Package name**: `ro.alexmamo.firebasesigninwithemailandpassword`
   - **SHA-1 fingerprint**: Get from `./gradlew signingReport`
4. Download updated `google-services.json`
5. Place in `app/` directory

### Getting SHA-1 Fingerprint

Run this command to get your app's SHA-1 fingerprint:

```bash
./gradlew signingReport
```

Look for the debug variant:
```
Variant: debugAndroidTest
Config: debug
Store: ~/.android/keystore
Alias: androiddebugkey
MD5: ...
SHA1: XX:XX:XX:...  <- Use this value
SHA256: ...
```

## Step 7: Enable OAuth Redirect URI (Optional)

If using email link auth with redirect:

1. Firebase Console → Authentication → Settings
2. Add redirect URI: `https://your-app.firebaseapp.com/__/auth/handler`
3. This allows users to sign in on web browsers

## Testing Email Link Authentication

### Test Scenario 1: Send Email Link

```
1. Launch app
2. Navigate to Sign In → "Sign in with email link"
3. Enter valid email address
4. Click "Send Sign-in Link"
5. Check email inbox for sign-in email
6. Email should contain a link with oobCode parameter
```

### Test Scenario 2: Click Email Link

```
1. Copy the email link from received email
2. Open link in mobile browser
3. Should open app automatically (if intent filter configured)
4. App should sign in user
5. Should navigate to Profile screen
```

### Test Scenario 3: Different Device/Browser

```
1. Send sign-in link to email
2. Click link on different device/browser
3. Deep link may open browser instead
4. User can manually enter email on EmailLinkSignInScreen
5. Then paste the link and sign in
```

## Troubleshooting

### Issue: Deep Link Not Opening App

**Solution**:
- Check intent filter matches exact domain
- Verify URL scheme is `https` (not `http`)
- Test with `adb shell am start -a android.intent.action.VIEW -d "https://...link..."`
- Ensure app is installed (not just run from IDE)
- Check AndroidManifest.xml syntax

### Issue: "Invalid Action Code" Error

**Solution**:
- Verify link hasn't expired (24-hour limit)
- Confirm email in link matches entered email
- Check Firebase Dynamic Links is enabled
- Verify domain in ActionCodeSettings matches real domain

### Issue: Email Not Received

**Solution**:
- Check spam/junk folder
- Verify email address is correct and active
- Wait a few seconds for delivery (can be delayed)
- Check Firebase Console quotas/limits
- Try resending the link

### Issue: Wrong Domain in Email

**Solution**:
- Update ActionCodeSettings in AuthRepositoryImpl.kt
- Ensure domain matches Firebase Dynamic Link domain
- Rebuild and deploy app

### Issue: App Not Opening from Link

**Solution**:
- Verify intent filter in AndroidManifest.xml
- Check app is production signed (debug signature won't work by default)
- Add SHA-1 fingerprint to Firebase console
- Wait a few minutes for Firebase to propagate changes
- Test on physical device (not emulator)

## Security Considerations

1. **Link Expiration**: Links expire after 24 hours
2. **One-time Use**: Links can only be used once
3. **HTTPS Only**: All links use secure HTTPS
4. **No Password Storage**: User email only, no password
5. **Rate Limiting**: Firebase limits email sending per IP
6. **Session Timeout**: Session expires after 30 days of inactivity

## Implementation Checklist

- [ ] Firebase Authentication enabled
- [ ] Email Link Sign-in enabled in Firebase Console
- [ ] Firebase Dynamic Links set up with domain
- [ ] ActionCodeSettings updated with correct domain
- [ ] Intent filter added to AndroidManifest.xml
- [ ] SHA-1 fingerprint added to Firebase console
- [ ] App can send email links successfully
- [ ] App can receive and process deep links
- [ ] Email link sign-in completes successfully
- [ ] User redirected to Profile after sign-in
- [ ] Tested on physical device

## Next Steps

1. Implement MainActivity deep link handling
2. Test sending email link
3. Test clicking link from email
4. Test on physical device with different network
5. Test deep link with app closed/background
6. Monitor Firebase Auth logs for errors
7. Consider adding custom email templates
8. Implement email link re-sending
9. Add analytics to track email link usage
10. Consider 2FA for additional security

## References

- [Firebase Email Link Auth Documentation](https://firebase.google.com/docs/auth/android/email-link-auth)
- [Firebase Dynamic Links Guide](https://firebase.google.com/docs/dynamic-links/android/start)
- [Android Intent Filters Documentation](https://developer.android.com/guide/components/intents-filters)
- [Firebase ActionCodeSettings API](https://firebase.google.com/docs/reference/kotlin/com/google/firebase/auth/ActionCodeSettings)