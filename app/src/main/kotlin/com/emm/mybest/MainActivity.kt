package com.emm.mybest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.emm.mybest.navigation.AppNavigation
import com.emm.mybest.ui.theme.MyBestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBestTheme {
                AppNavigation()
            }
        }
    }
}