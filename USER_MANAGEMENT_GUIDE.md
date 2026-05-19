# Firebase User Management Implementation Guide

This document describes all the Firebase user management methods implemented in the application.

## Overview

The application now includes comprehensive user management features based on [Firebase Authentication - Manage Users Documentation](https://firebase.google.com/docs/auth/android/manage-users).

## Implemented Methods

### 1. **Get Current User**
- **Method**: `AuthRepository.currentUser`
- **Returns**: `FirebaseUser?`
- **Description**: Retrieves the currently authenticated user
- **Usage**: Displayed in the Profile screen showing email, display name, email verification status, and account creation time

### 2. **Update User Profile**
- **Method**: `AuthRepository.updateUserProfile(displayName: String?, photoUrl: String?)`
- **Description**: Updates user's display name and/or photo URL
- **Requirements**: User must be authenticated
- **UI Location**: Profile screen → "Update Display Name" section
- **Flow**:
  1. User enters new display name
  2. Clicks "Update Profile" button
  3. Profile is updated and user is reloaded
  4. Success/error toast notification displayed

### 3. **Update Email Address**
- **Method**: `AuthRepository.updateUserEmail(newEmail: String)`
- **Prerequisite**: `AuthRepository.reauthenticateUser(email: String, password: String)`
- **Description**: Changes the user's email address (requires re-authentication)
- **Requirements**: 
  - User must be authenticated
  - User must re-authenticate with current email and password before updating
  - New email should not already be in use by another account
- **UI Location**: Profile screen → "Update Email" section
- **Flow**:
  1. User enters new email address
  2. User enters current password for re-authentication
  3. Clicks "Update Email" button
  4. Re-authentication is performed
  5. Email is updated
  6. User is reloaded
  7. Success/error notification displayed

### 4. **Update Password**
- **Method**: `AuthRepository.updateUserPassword(newPassword: String)`
- **Prerequisite**: `AuthRepository.reauthenticateUser(email: String, password: String)`
- **Description**: Changes the user's password (requires re-authentication)
- **Requirements**:
  - User must be authenticated
  - User must re-authenticate with current email and password before updating
  - New password should meet Firebase security requirements
- **UI Location**: Profile screen → "Update Password" section
- **Flow**:
  1. User enters current password for re-authentication
  2. User enters new password
  3. Clicks "Update Password" button
  4. Re-authentication is performed
  5. Password is updated
  6. Success/error notification displayed

### 5. **Re-authenticate User**
- **Method**: `AuthRepository.reauthenticateUser(email: String, password: String)`
- **Description**: Re-authenticates user for sensitive operations
- **Requirements**: User must be authenticated
- **Usage**: Called automatically before:
  - Updating email address
  - Updating password
  - Deleting account (already implemented)
- **Error Handling**: If re-authentication fails with sensitive operation error, user is prompted to sign out and sign in again

### 6. **Delete User Account**
- **Method**: `AuthRepository.deleteUser()` (already implemented)
- **Description**: Permanently deletes the user account from Firebase
- **Requirements**: User must be authenticated
- **UI Location**: Profile screen top bar (trash icon)
- **Error Handling**: If deletion requires re-authentication, user is prompted to sign out and re-authenticate

### 7. **Send Email Verification** (already implemented)
- **Method**: `AuthRepository.sendEmailVerification()`
- **Description**: Sends verification email to user's current email address

### 8. **Check Email Verification Status** (already implemented)
- **Property**: `FirebaseUser.isEmailVerified`
- **Description**: Checks if user's email is verified
- **UI Location**: Profile screen displays "Email Verified: true/false"

### 9. **User Metadata**
- **Properties**: 
  - `FirebaseUser.metadata?.creationTimestamp` - When the account was created
  - `FirebaseUser.metadata?.lastSignInTimestamp` - Last sign-in time
- **UI Location**: Profile screen displays both timestamps
- **Format**: Unix timestamp (milliseconds since epoch)

### 10. **Get Auth State**
- **Method**: `AuthRepository.getAuthState()` (already implemented)
- **Returns**: `Flow<Boolean>` (true if signed out, false if signed in)
- **Description**: Monitors authentication state changes in real-time

## ViewModel Layer

### ProfileViewModel
Manages all user management UI state with the following StateFlows:

| StateFlow | Type | Purpose |
|-----------|------|---------|
| `authState` | `StateFlow<Boolean>` | Current auth status |
| `displayName` | `StateFlow<String>` | User's display name input |
| `email` | `StateFlow<String>` | Current email (read-only display) |
| `currentPassword` | `StateFlow<String>` | Current password for re-auth |
| `newEmail` | `StateFlow<String>` | New email input |
| `newPassword` | `StateFlow<String>` | New password input |
| `updateProfileState` | `StateFlow<Response<Unit>>` | Profile update response |
| `updateEmailState` | `StateFlow<Response<Unit>>` | Email update response |
| `updatePasswordState` | `StateFlow<Response<Unit>>` | Password update response |
| `deleteUserState` | `StateFlow<Response<Unit>>` | User deletion response (existing) |

### Methods
- `updateProfile()` - Updates display name
- `updateEmail()` - Updates email with re-authentication
- `updatePassword()` - Updates password with re-authentication
- `deleteUser()` - Deletes account
- `signOut()` - Signs out user

## UI Components

### ProfileContent
Displays user information and management forms:

1. **User Information Section**
   - Current email
   - Display name
   - Email verification status
   - Account creation date
   - Last sign-in date

2. **Update Display Name Section**
   - Text field for new display name
   - "Update Profile" button

3. **Update Email Section**
   - Text field for new email
   - Text field for current password (re-authentication)
   - "Update Email" button

4. **Update Password Section**
   - Text field for current password (re-authentication)
   - Text field for new password
   - "Update Password" button

## Error Handling

The app includes comprehensive error handling via `AuthErrorHandler`:

- **Sensitive operation errors**: Detected by "sensitive" keyword in error message
  - Triggers re-authentication requirement
  - User is prompted to sign out and sign in again
- **Validation errors**: Empty field validation
- **Network errors**: Firebase handles connection errors
- **User-friendly messages**: All errors are converted to readable messages

## Security Considerations

1. **Re-authentication Required**: Email, password, and account deletion require user to re-authenticate with current password
2. **Password Validation**: Firebase enforces strong password requirements
3. **Email Uniqueness**: Firebase ensures new email is not already in use
4. **Token Refresh**: User is automatically reloaded after profile updates to get fresh data
5. **Secure Password Input**: `PasswordVisualTransformation` masks password input in UI

## Response Model

All async operations return a `Response<T>` sealed class with states:

```kotlin
sealed class Response<T> {
    object Idle : Response<Nothing>()
    object Loading : Response<Nothing>()
    data class Success<T>(val data: T) : Response<T>()
    data class Failure<T>(val e: Exception?) : Response<T>()
}
```

## Testing

To test user management features:

1. **Update Profile**: 
   - Sign in with valid credentials
   - Enter new display name
   - Click "Update Profile"
   - Verify success toast

2. **Update Email**:
   - Sign in with valid credentials
   - Enter new email address
   - Enter current password
   - Click "Update Email"
   - Verify email change and success toast

3. **Update Password**:
   - Sign in with valid credentials
   - Enter current password
   - Enter new password
   - Click "Update Password"
   - Verify success toast
   - Sign out and sign in with new password

4. **Delete Account**:
   - Sign in with valid credentials
   - Click delete icon in top bar
   - Confirm deletion
   - Verify account is deleted and redirected to sign-in

## Dependencies

- Firebase Authentication 23.2.0
- Jetpack Compose UI
- Kotlin Coroutines
- Hilt for dependency injection

## References

- [Firebase Manage Users Documentation](https://firebase.google.com/docs/auth/android/manage-users)
- [Firebase Exception Handling](https://firebase.google.com/docs/auth/handle-errors)