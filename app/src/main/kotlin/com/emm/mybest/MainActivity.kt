package com.emm.mybest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.emm.mybest.navigation.AppNavigation
import com.emm.mybest.ui.theme.DarkColorScheme
import com.emm.mybest.ui.theme.MyBestTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(DarkColorScheme.background.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(DarkColorScheme.background.toArgb())
        )
        setContent {
            MyBestTheme {
                AppNavigation()
            }
        }
    }
}