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
import com.emm.mybest.core.navigation.AppRoot
import com.emm.mybest.core.navigation.LaunchActionRule
import com.emm.mybest.core.permission.NotificationPermissionRule
import com.emm.mybest.features.onboarding.presentation.OnboardingGate
import com.emm.mybest.features.onboarding.presentation.OnboardingViewModel
import com.emm.mybest.ui.theme.AtelierTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private var intentAction by mutableStateOf<String?>(null)
    private var wasNotificationPermissionRequested = false
    private val onboardingViewModel: OnboardingViewModel by viewModel()
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            onboardingViewModel.state.value.gate == OnboardingGate.LOADING
        }
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        wasNotificationPermissionRequested =
            savedInstanceState?.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED) ?: false
        requestNotificationPermissionIfNeeded()

        intentAction = resolveLaunchAction(savedInstanceState)

        setContent {
            DisposableEffect(Unit) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
                    navigationBarStyle = SystemBarStyle.dark(DefaultDarkScrim),
                )
                onDispose {}
            }

            AtelierTheme {
                AppRoot(
                    intentAction = intentAction,
                    onConsumeAction = { intentAction = null },
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PENDING_ACTION, intentAction)
        outState.putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, wasNotificationPermissionRequested)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentAction = intent.action
    }

    private fun resolveLaunchAction(savedInstanceState: Bundle?): String? {
        val saved = savedInstanceState?.let { LaunchActionRule.Saved(it.getString(KEY_PENDING_ACTION)) }
        val isFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
        return LaunchActionRule.resolve(saved, intent.action, isFromHistory)
    }

    private fun requestNotificationPermissionIfNeeded() {
        val isGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        val shouldRequest = NotificationPermissionRule.shouldRequest(
            isSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
            isGranted = isGranted,
            wasRequested = wasNotificationPermissionRequested,
        )
        if (!shouldRequest) return

        wasNotificationPermissionRequested = true
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private const val KEY_PENDING_ACTION = "pending_launch_action"
        private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
        private val DefaultDarkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
