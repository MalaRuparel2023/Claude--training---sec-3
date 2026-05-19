# Firebase Google Sign-In Setup Guide

This guide walks through setting up Google Sign-In authentication for your Firebase Android app.

## Prerequisites
- Firebase project created in [Firebase Console](https://console.firebase.google.com)
- Google Cloud project linked to your Firebase project
- `google-services.json` file already in `app/` directory

## Step 1: Get Your Web Client ID

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Select your Firebase project
3. In the left sidebar, go to **APIs & Services** → **Credentials**
4. Look for **Web Client** in the "OAuth 2.0 Client IDs" section
5. Click on it to view the Client ID
6. Copy the **Client ID** (format: `xxxxx.apps.googleusercontent.com`)

## Step 2: Add Web Client ID to AppModule

Open `app/src/main/java/ro/alexmamo/firebasesigninwithemailandpassword/di/AppModule.kt`

Replace the placeholder:
```kotlin
.requestIdToken("YOUR_WEB_CLIENT_ID")
```

With your actual Web Client ID:
```kotlin
.requestIdToken("your-client-id.apps.googleusercontent.com")
```

## Step 3: Enable Google Sign-In in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Navigate to **Authentication** → **Sign-in method**
4. Click on **Google** provider
5. Enable it and ensure your **Support email** is configured
6. Click **Save**

## Step 4: Configure SignInScreen with Google Sign-In Flow

Update the `onGoogleSignIn` callback in `SignInScreen.kt`:

```kotlin
val googleSignInClient = remember {
    context.let { ctx ->
        GoogleSignIn.getClient(ctx, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build())
    }
}

onGoogleSignIn = {
    val signInIntent = googleSignInClient.signInIntent
    // Launch activity for result or use ActivityResultContracts.StartActivityForResult()
}
```

## Step 5: Handle Google Sign-In Result

In your Activity or Fragment, implement `ActivityResultContract` to handle the sign-in result:

```kotlin
val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.getResult(ApiException::class.java)
        account?.idToken?.let { idToken ->
            viewModel.signInWithGoogle(idToken)
        }
    } catch (e: ApiException) {
        showToastMessage(context, e.message ?: "Google Sign-In failed")
    }
}
```

## Step 6: Update String Resources

Add to `res/values/strings.xml`:

```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
```

## Troubleshooting

### "Sign In failed" error
- Verify your Web Client ID is correct
- Ensure Google Sign-In is enabled in Firebase Console
- Check that support email is configured

### "Invalid client" error
- Your Web Client ID doesn't match the one in Firebase Console
- Make sure you're using the **Web** client ID, not Android client ID

### Token mismatch error
- Ensure the Web Client ID in `AppModule.kt` matches the one in Firebase Console
- Verify `google-services.json` is up to date (re-download from Firebase Console if needed)

## Additional Resources

- [Firebase Google Sign-In Documentation](https://firebase.google.com/docs/auth/android/google-signin)
- [Google Sign-In Android Guide](https://developers.google.com/identity/sign-in/android)
- [Google Cloud Console](https://console.cloud.google.com)
- [Firebase Console](https://console.firebase.google.com)