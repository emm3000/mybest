package com.emm.mybest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.emm.mybest.core.navigation.AppNavigation
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var intentAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        intentAction = intent.action

        setContent {
            val state by viewModel.state.collectAsState()
            val darkTheme = state.isDarkMode ?: isSystemInDarkTheme()

            // Modern Edge-to-Edge configuration that syncs with the app's theme state.
            // This ensures status bar and navigation bar icons correctly adapt even if
            // the user overrides the system theme within the app.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb(),
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        DefaultLightScrim,
                        DefaultDarkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }

            MyBestTheme(
                darkTheme = darkTheme,
            ) {
                AppNavigation(
                    intentAction = intentAction,
                    onConsumeAction = { intentAction = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentAction = intent.action
    }

    companion object {
        /**
         * The default light scrim, as defined by [androidx.activity.enableEdgeToEdge].
         */
        private val DefaultLightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

        /**
         * The default dark scrim, as defined by [androidx.activity.enableEdgeToEdge].
         */
        private val DefaultDarkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
