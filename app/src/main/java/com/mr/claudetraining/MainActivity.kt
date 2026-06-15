package com.mr.claudetraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.mr.claudetraining.ui.navigation.SrteamChatNavigation
import com.mr.claudetraining.ui.screens.SignInScreen
import com.mr.claudetraining.ui.screens.SplashScreen
import com.mr.claudetraining.ui.theme.SrteamChatTheme
import com.mr.claudetraining.ui.viewmodel.AuthGateState
import com.mr.claudetraining.ui.viewmodel.AuthViewModel
import com.mr.claudetraining.ui.viewmodel.UserConnectionViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val userConnectionViewModel: UserConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SrteamChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (authViewModel.gateState.collectAsState().value) {
                        AuthGateState.Loading -> SplashScreen()
                        AuthGateState.SignedOut -> SignInScreen(viewModel = authViewModel)
                        AuthGateState.SignedIn -> {
                            val connectionState by userConnectionViewModel.connectionState.collectAsState()
                            SrteamChatNavigation(
                                connectionState = connectionState,
                                onSignOut = {
                                    userConnectionViewModel.signOut()
                                    authViewModel.signOut()
                                },
                                onSignIn = { userConnectionViewModel.signIn() }
                            )
                        }
                    }
                }
            }
        }
    }
}