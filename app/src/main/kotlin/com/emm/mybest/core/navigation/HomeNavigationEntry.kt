package com.emm.mybest.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.emm.mybest.features.home.presentation.HomeCallbacks
import com.emm.mybest.features.home.presentation.HomeScreen

internal fun EntryProviderScope<NavKey>.homeEntry(navigator: Navigator) {
    entry<Screen.Home> {
        HomeScreen(
            callbacks = rememberHomeCallbacks(navigator),
            bottomBar = rememberBottomBar(Screen.Home, navigator),
            modifier = Modifier,
        )
    }
}

@Composable
private fun rememberHomeCallbacks(navigator: Navigator): HomeCallbacks =
    remember(navigator) {
        HomeCallbacks(
            onWeightClick = { navigator.navigate(Screen.AddWeight) },
            onPhotoClick = { navigator.navigate(Screen.Photos) },
            onSettingsClick = { navigator.navigate(Screen.Settings) },
            onHistoryClick = { navigator.navigate(Screen.History) },
        )
    }
