package ro.alexmamo.firebasesigninwithemailandpassword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ro.alexmamo.firebasesigninwithemailandpassword.ui.navigation.SrteamChatNavigation
import ro.alexmamo.firebasesigninwithemailandpassword.ui.theme.SrteamChatTheme
import ro.alexmamo.firebasesigninwithemailandpassword.ui.viewmodel.UserConnectionViewModel

class MainActivity : ComponentActivity() {
    private val userConnectionViewModel: UserConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SrteamChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val connectionState = userConnectionViewModel.connectionState.collectAsState().value
                    SrteamChatNavigation(connectionState = connectionState)
                }
            }
        }
    }
}