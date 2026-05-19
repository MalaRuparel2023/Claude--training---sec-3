package ro.alexmamo.firebasesigninwithemailandpassword.presentation.email_link_signin.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmailLinkSignInContent(
    innerPadding: PaddingValues,
    email: String,
    onEmailChange: (String) -> Unit,
    onSendLink: () -> Unit,
    isLoading: Boolean,
    onSignInTextClick: () -> Unit,
    onPasswordSignInTextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign in with Email Link",
            fontSize = 28.sp,
            modifier = Modifier.padding(top = 48.dp, bottom = 16.dp)
        )

        Text(
            text = "Enter your email address to receive a secure sign-in link. Click the link in the email to sign in without a password.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        Button(
            onClick = onSendLink,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading && email.isNotBlank()
        ) {
            Text("Send Sign-in Link")
        }

        Text(
            text = "After you receive the email, click the link to sign in automatically.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Already signed in elsewhere?",
            fontSize = 14.sp,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )

        Button(
            onClick = onSignInTextClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Sign In with Password")
        }

        Button(
            onClick = onPasswordSignInTextClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
    }
}