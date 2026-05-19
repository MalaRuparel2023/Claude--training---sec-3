package ro.alexmamo.firebasesigninwithemailandpassword.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.alexmamo.firebasesigninwithemailandpassword.R
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileContent(
    innerPadding: PaddingValues,
    currentUser: FirebaseUser?,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onUpdateProfile: () -> Unit,
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newEmail: String,
    onNewEmailChange: (String) -> Unit,
    onUpdateEmail: () -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onUpdatePassword: () -> Unit,
    isUpdatingProfile: Boolean,
    isUpdatingEmail: Boolean,
    isUpdatingPassword: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome_message),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "User Information",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Email: ${currentUser?.email ?: "N/A"}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Display Name: ${currentUser?.displayName ?: "Not set"}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Email Verified: ${currentUser?.isEmailVerified ?: false}",
                    fontSize = 14.sp
                )
                if (currentUser?.metadata != null) {
                    Text(
                        text = "Account Created: ${currentUser.metadata?.creationTimestamp}",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Last Sign In: ${currentUser.metadata?.lastSignInTimestamp}",
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Update Display Name",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text("Display Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
                Button(
                    onClick = onUpdateProfile,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 8.dp),
                    enabled = !isUpdatingProfile
                ) {
                    Text("Update Profile")
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Update Email",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "(Requires current password for re-authentication)",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = onNewEmailChange,
                    label = { Text("New Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = { Text("Current Password (for verification)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Button(
                    onClick = onUpdateEmail,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 8.dp),
                    enabled = !isUpdatingEmail
                ) {
                    Text("Update Email")
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Update Password",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "(Requires current password for re-authentication)",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = { Text("Current Password (for verification)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text("New Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Button(
                    onClick = onUpdatePassword,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 16.dp),
                    enabled = !isUpdatingPassword
                ) {
                    Text("Update Password")
                }
            }
        }
    }
}