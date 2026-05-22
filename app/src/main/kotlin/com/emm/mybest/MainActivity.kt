package com.emm.mybest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.emm.mybest.core.navigation.AppNavigation
import com.emm.mybest.ui.theme.AtelierTheme

class MainActivity : ComponentActivity() {

    private var intentAction by mutableStateOf<String?>(null)
    private var hasAttemptedNotificationPermissionRequest = false
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* no-op: app follows OS-level permission state */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        requestNotificationPermissionIfNeeded()

        intentAction = intent.action

        setContent {
            DisposableEffect(Unit) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
                    navigationBarStyle = SystemBarStyle.dark(DefaultDarkScrim),
                )
                onDispose {}
            }

            AtelierTheme {
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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val isGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (isGranted || hasAttemptedNotificationPermissionRequest) return

        hasAttemptedNotificationPermissionRequest = true
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private val DefaultDarkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
