package com.emm.mybest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.emm.mybest.navigation.AppNavigation
import com.emm.mybest.ui.theme.MyBestTheme

class MainActivity : ComponentActivity() {

    private var intentAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        intentAction = intent.action
        enableEdgeToEdge()

        setContent {
            MyBestTheme {
                AppNavigation(
                    intentAction = intentAction,
                    onActionConsumed = { intentAction = null }
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