package ro.alexmamo.firebasesigninwithemailandpassword.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import ro.alexmamo.firebasesigninwithemailandpassword.navigation.Route

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    navigateAndClear: (Route) -> Unit
) {
    val isUserSignedOut = viewModel.isUserSignedOut
    val isEmailVerified = viewModel.isEmailVerified

    LaunchedEffect(isUserSignedOut, isEmailVerified) {
        if (isUserSignedOut) {
            navigateAndClear(Route.SignIn)
        } else {
            if (isEmailVerified) {
                navigateAndClear(Route.Profile)
            } else {
                navigateAndClear(Route.VerifyEmail)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}