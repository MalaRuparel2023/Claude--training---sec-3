package ro.alexmamo.firebasesigninwithemailandpassword.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ro.alexmamo.firebasesigninwithemailandpassword.ui.screens.ChatDetailScreen
import ro.alexmamo.firebasesigninwithemailandpassword.ui.screens.ChatListScreen
import ro.alexmamo.firebasesigninwithemailandpassword.ui.viewmodel.ConnectionUiState

sealed class Route(val route: String) {
    object ChatList : Route("chatList")
    object ChatDetail : Route("chatDetail/{channelId}") {
        fun createRoute(channelId: String) = "chatDetail/$channelId"
    }
}

@Composable
fun SrteamChatNavigation(
    connectionState: ConnectionUiState,
    navController: NavHostController = rememberNavController()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (connectionState) {
                is ConnectionUiState.Connecting -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    NavHost(
                        navController = navController,
                        startDestination = Route.ChatList.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Route.ChatList.route) {
                            ChatListScreen(
                                onChannelSelected = { channelId ->
                                    navController.navigate(Route.ChatDetail.createRoute(channelId))
                                }
                            )
                        }

                        composable(Route.ChatDetail.route) { backStackEntry ->
                            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                            ChatDetailScreen(
                                channelId = channelId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }

        if (connectionState is ConnectionUiState.Disconnected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp)
            ) {
                Text(
                    "Disconnected from chat",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        } else if (connectionState is ConnectionUiState.Error) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp)
            ) {
                Text(
                    "Connection error: ${connectionState.message}",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}