package ro.alexmamo.firebasesigninwithemailandpassword.presentation.sign_in.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.alexmamo.firebasesigninwithemailandpassword.R
import ro.alexmamo.firebasesigninwithemailandpassword.components.ActionButton
import ro.alexmamo.firebasesigninwithemailandpassword.components.ActionText
import ro.alexmamo.firebasesigninwithemailandpassword.components.EmailField
import ro.alexmamo.firebasesigninwithemailandpassword.components.GoogleSignInButton
import ro.alexmamo.firebasesigninwithemailandpassword.components.PasswordField

private const val VERTICAL_DIVIDER = "|"

@Composable
fun SignInContent(
    innerPadding: PaddingValues,
    email: TextFieldValue,
    onEmailChange: (TextFieldValue) -> Unit,
    onEmailInvalid: () -> Unit,
    password: TextFieldValue,
    onPasswordChange: (TextFieldValue) -> Unit,
    onPasswordInvalid: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    isLoading: Boolean,
    onForgotPasswordTextClick: () -> Unit,
    onSignUpTextClick: () -> Unit,
    onEmailLinkSignInClick: () -> Unit = {}
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            email = email,
            onEmailChange = onEmailChange
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        PasswordField(
            password = password,
            onPasswordChange = onPasswordChange
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        ActionButton(
            onActionButtonClick = {
                val isEmailValid = email.text.isNotBlank()
                val isPasswordValid = password.text.isNotBlank()
                if (!isEmailValid) {
                    onEmailInvalid()
                } else if (!isPasswordValid) {
                    onPasswordInvalid()
                } else {
                    onSignIn(email.text, password.text)
                    keyboard?.hide()
                }
            },
            enabled = !isLoading,
            resourceId = R.string.sign_in_button
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = "or",
                fontSize = 12.sp
            )
            Divider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        GoogleSignInButton(
            onClick = onGoogleSignIn,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            ActionText(
                onActionTextClick = onForgotPasswordTextClick,
                resourceId = R.string.forgot_password
            )
            Text(
                modifier = Modifier.padding(
                    start = 4.dp,
                    end = 4.dp
                ),
                text = VERTICAL_DIVIDER,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            ActionText(
                onActionTextClick = onSignUpTextClick,
                resourceId = R.string.sign_up
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            modifier = Modifier.clickable {
                onEmailLinkSignInClick()
            },
            text = "Sign in with email link",
            fontSize = 14.sp
        )
    }
}