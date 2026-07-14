package com.mr.claudetraining.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mr.claudetraining.ui.screens.AccountSettingsScreen
import com.mr.claudetraining.ui.screens.ChatDetailScreen
import com.mr.claudetraining.ui.screens.ChatListScreen
import com.mr.claudetraining.ui.screens.DiaryScreen
import com.mr.claudetraining.ui.screens.NotificationsScreen
import com.mr.claudetraining.ui.screens.HealthDashboardScreen
import com.mr.claudetraining.ui.screens.ProfileScreen
import com.mr.claudetraining.ui.screens.WorkoutSessionCreateScreen
import com.mr.claudetraining.ui.screens.WorkoutSessionDetailScreen
import com.mr.claudetraining.ui.screens.WorkoutSessionListScreen
import com.mr.claudetraining.ui.screens.ProgressScreen
import com.mr.claudetraining.domain.model.ChatConnectionState
import com.mr.claudetraining.ui.screens.ActivityDetailScreen
import com.mr.claudetraining.ui.screens.ConfigScreen
import com.mr.claudetraining.ui.screens.NewActivityDetailScreen
import com.mr.claudetraining.ui.screens.WorkoutScreen
import com.mr.claudetraining.ui.screens.YogaScreen
import com.mr.claudetraining.ui.screens.TastyDashboardScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.ui.viewmodel.AnalyticsViewModel
import com.mr.claudetraining.ui.viewmodel.FeatureFlagsViewModel

sealed class Route(val route: String) {
    object Home : Route("home")
    object WorkoutSessions : Route("workout_sessions")
    object WorkoutSessionCreate : Route("workout_session_create")
    object WorkoutSessionDetail : Route("workout_session_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "workout_session_detail/$sessionId"
    }
    object Diary : Route("diary")
    object Workout : Route("workout")
    object Progress : Route("progress")
    object Profile : Route("profile")
    object ChatList : Route("chat_list")
    object ChatDetail : Route("chat_detail/{channelId}") {
        fun createRoute(channelId: String) = "chat_detail/$channelId"
    }
    object Notifications : Route("notifications")
    object AccountSettings : Route("account_settings")
    object WorkoutLog : Route("workout_log")
    object ActivityDetail : Route("activity_detail")
    object Config : Route("config")
    object Tasty : Route("tasty_dashboard")
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Route.Home.route, "Home", Icons.Filled.Home),
    BottomNavItem(Route.WorkoutSessions.route, "Sessions", Icons.Filled.Work),
    BottomNavItem(Route.Diary.route, "Diary", Icons.Filled.Restaurant),
    BottomNavItem(Route.Workout.route, "Workout", Icons.Filled.FitnessCenter),
    BottomNavItem(Route.Progress.route, "Progress", Icons.Filled.Insights),
    BottomNavItem(Route.Profile.route, "Profile", Icons.Filled.Person)
)

@Composable
fun SrteamChatNavigation(
    connectionState: ChatConnectionState,
    onSignOut: () -> Unit = {},
    onSignIn: () -> Unit = {},
    deepLinkRoute: String? = null,
    navController: NavHostController = rememberNavController()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (connectionState) {
                is ChatConnectionState.Connecting -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ChatConnectionState.SignedOut -> SignedOutScreen(onSignIn = onSignIn)
                else -> MainScaffold(
                    navController = navController,
                    onSignOut = onSignOut,
                    deepLinkRoute = deepLinkRoute
                )
            }
        }

        if (connectionState is ChatConnectionState.Disconnected) {
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
        } else if (connectionState is ChatConnectionState.Error) {
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

@Composable
private fun MainScaffold(
    navController: NavHostController,
    onSignOut: () -> Unit,
    deepLinkRoute: String? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    // Manual screen_view tracking — fires once per destination change (route template name).
    val analyticsViewModel: AnalyticsViewModel = hiltViewModel()
    LaunchedEffect(currentRoute) {
        currentRoute?.let { analyticsViewModel.logScreenView(it) }
    }

    // A push-notification tap delivers its target route here; navigate once it arrives.
    LaunchedEffect(deepLinkRoute) {
        deepLinkRoute?.let { navController.navigate(it) { launchSingleTop = true } }
    }

    Scaffold(
        // Let each screen's blue app bar draw under the transparent status bar;
        // only reserve space for the bottom navigation/system bars.
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Route.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Route.Home.route) {
                HealthDashboardScreen(
                    onOpenActivity = { navController.navigate(Route.ActivityDetail.route) }
                )
            }
            composable(Route.ActivityDetail.route) {
                val flagsViewModel: FeatureFlagsViewModel = hiltViewModel()
                val flags by flagsViewModel.flags.collectAsState()
                if (flags.newJobDetailUi) {
                    NewActivityDetailScreen(onBack = { navController.popBackStack() })
                } else {
                    ActivityDetailScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Route.WorkoutSessions.route) {
                WorkoutSessionListScreen(
                    onOpenSession = { sessionId -> navController.navigate(Route.WorkoutSessionDetail.createRoute(sessionId)) },
                    onCreateSession = { navController.navigate(Route.WorkoutSessionCreate.route) }
                )
            }
            composable(Route.WorkoutSessionCreate.route) {
                WorkoutSessionCreateScreen(
                    onSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Route.WorkoutSessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) {
                WorkoutSessionDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Diary.route) { DiaryScreen() }
            composable(Route.Workout.route) {
                YogaScreen(onOpenWorkout = { navController.navigate(Route.WorkoutLog.route) })
            }
            composable(Route.WorkoutLog.route) {
                WorkoutScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Progress.route) { ProgressScreen() }
            composable(Route.Profile.route) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    onOpenChat = { navController.navigate(Route.ChatList.route) },
                    onOpenNotifications = { navController.navigate(Route.Notifications.route) },
                    onOpenAccountSettings = { navController.navigate(Route.AccountSettings.route) },
                    onOpenTasty = { navController.navigate(Route.Tasty.route) }
                )
            }
            composable(Route.Tasty.route) {
                TastyDashboardScreen()
            }
            composable(Route.Notifications.route) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.AccountSettings.route) {
                AccountSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignOut = onSignOut,
                    onOpenConfig = { navController.navigate(Route.Config.route) }
                )
            }
            composable(Route.Config.route) {
                ConfigScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.ChatList.route) {
                ChatListScreen(
                    onChannelSelected = { channelId ->
                        navController.navigate(Route.ChatDetail.createRoute(channelId))
                    },
                    onBack = { navController.popBackStack() },
                    onSignOut = onSignOut
                )
            }
            composable(
                route = Route.ChatDetail.route,
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) { backStackEntry ->
                ChatDetailScreen(
                    channelId = backStackEntry.arguments?.getString("channelId").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun SignedOutScreen(onSignIn: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "You're signed out",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sign back in to continue chatting",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onSignIn) {
                Text("Sign In")
            }
        }
    }
}