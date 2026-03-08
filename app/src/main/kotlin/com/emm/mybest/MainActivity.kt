package com.emm.mybest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.emm.mybest.navigation.AppNavigation
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var intentAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intentAction = intent.action
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsState()
            val darkTheme = state.isDarkMode ?: isSystemInDarkTheme()

            MyBestTheme(
                darkTheme = darkTheme,
                dynamicColor = state.useDynamicColor
            ) {
                AppNavigation(
                    intentAction = intentAction,
                    onConsumeAction = { intentAction = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentAction = intent.action
    }
}
