package com.mr.claudetraining

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import com.mr.claudetraining.data.messaging.AppFirebaseMessagingService
import com.mr.claudetraining.ui.navigation.SrteamChatNavigation
import com.mr.claudetraining.ui.screens.SignInScreen
import com.mr.claudetraining.ui.screens.SplashScreen
import com.mr.claudetraining.ui.theme.SrteamChatTheme
import com.mr.claudetraining.ui.viewmodel.AuthGateState
import com.mr.claudetraining.ui.viewmodel.AuthViewModel
import com.mr.claudetraining.ui.viewmodel.PushTokenViewModel
import com.mr.claudetraining.ui.viewmodel.UserConnectionViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val userConnectionViewModel: UserConnectionViewModel by viewModels()
    private val pushTokenViewModel: PushTokenViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* token sync runs regardless */ }

    // Route delivered by a tapped push notification; consumed once by navigation.
    private var pendingNavRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        pushTokenViewModel.syncToken() // logs the FCM token to Logcat (tag FCM_TOKEN)
        pendingNavRoute = intent?.getStringExtra(AppFirebaseMessagingService.EXTRA_NAV_ROUTE)
        setContent {
            SrteamChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (authViewModel.gateState.collectAsState().value) {
                        AuthGateState.Loading -> SplashScreen()
                        AuthGateState.SignedOut -> SignInScreen(viewModel = authViewModel)
                        AuthGateState.SignedIn -> {
                            // Register the FCM token once the user is signed in so it
                            // can be stored under their account.
                            LaunchedEffect(Unit) { pushTokenViewModel.syncToken() }
                            val connectionState by userConnectionViewModel.connectionState.collectAsState()
                            SrteamChatNavigation(
                                connectionState = connectionState,
                                onSignOut = {
                                    userConnectionViewModel.signOut()
                                    authViewModel.signOut()
                                },
                                onSignIn = { userConnectionViewModel.signIn() },
                                deepLinkRoute = pendingNavRoute
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(AppFirebaseMessagingService.EXTRA_NAV_ROUTE)?.let {
            pendingNavRoute = it
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}