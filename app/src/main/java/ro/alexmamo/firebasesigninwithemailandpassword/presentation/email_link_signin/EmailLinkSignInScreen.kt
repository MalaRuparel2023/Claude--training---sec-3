package ro.alexmamo.firebasesigninwithemailandpassword.presentation.email_link_signin

import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.Text
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ro.alexmamo.firebasesigninwithemailandpassword.components.LoadingIndicator
import ro.alexmamo.firebasesigninwithemailandpassword.core.logErrorMessage
import ro.alexmamo.firebasesigninwithemailandpassword.core.showToastMessage
import ro.alexmamo.firebasesigninwithemailandpassword.domain.model.Response
import ro.alexmamo.firebasesigninwithemailandpassword.navigation.Route
import ro.alexmamo.firebasesigninwithemailandpassword.presentation.email_link_signin.components.EmailLinkSignInContent

@Composable
fun EmailLinkSignInScreen(
    viewModel: EmailLinkSignInViewModel = hiltViewModel(),
    navigate: (Route) -> Unit,
    navigateAndClear: (Route) -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val email by viewModel.email.collectAsStateWithLifecycle()
    val sendLinkResponse by viewModel.sendLinkState.collectAsStateWithLifecycle()
    val signInWithLinkResponse by viewModel.signInWithLinkState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Link Sign-in") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        EmailLinkSignInContent(
            innerPadding = innerPadding,
            email = email,
            onEmailChange = viewModel::onEmailChange,
            onSendLink = viewModel::sendSignInLink,
            isLoading = sendLinkResponse is Response.Loading,
            onSignInTextClick = {
                navigate(Route.SignIn)
            },
            onPasswordSignInTextClick = {
                navigate(Route.SignUp)
            }
        )
    }

    when (sendLinkResponse) {
        is Response.Idle -> {}
        is Response.Loading -> LoadingIndicator()
        is Response.Success -> LaunchedEffect(Unit) {
            showToastMessage(context, "Sign-in link sent to your email. Check your inbox.")
        }
        is Response.Failure -> (sendLinkResponse as Response.Failure).e?.message?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                logErrorMessage(errorMessage)
                showToastMessage(context, errorMessage)
            }
        }
    }

    when (signInWithLinkResponse) {
        is Response.Idle -> {}
        is Response.Loading -> LoadingIndicator()
        is Response.Success -> LaunchedEffect(Unit) {
            showToastMessage(context, "Signed in successfully!")
            navigateAndClear(Route.Profile)
        }
        is Response.Failure -> (signInWithLinkResponse as Response.Failure).e?.message?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                logErrorMessage(errorMessage)
                showToastMessage(context, errorMessage)
            }
        }
    }
}