package ro.alexmamo.firebasesigninwithemailandpassword.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ro.alexmamo.firebasesigninwithemailandpassword.navigation.NavGraph
import ro.alexmamo.firebasesigninwithemailandpassword.theme.MyApplicationTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                NavGraph(
                    navController = rememberNavController()
                )
            }
        }
    }
}