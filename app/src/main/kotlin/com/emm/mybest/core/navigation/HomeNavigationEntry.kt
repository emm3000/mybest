package com.emm.mybest.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.emm.mybest.domain.usecase.home.HomeDestination
import com.emm.mybest.features.checkin.presentation.CheckInScreen
import com.emm.mybest.features.home.presentation.HomeCallbacks
import com.emm.mybest.features.home.presentation.HomeScreen
import com.emm.mybest.features.home.presentation.HomeShortcut
import com.emm.mybest.features.home.presentation.HomeSlots
import com.emm.mybest.features.preouting.presentation.PreOutingScreen
import com.emm.mybest.features.summary.presentation.SummaryScreen
import org.koin.androidx.compose.koinViewModel

internal fun EntryProviderScope<NavKey>.homeEntry(navigator: Navigator) {
    entry<Screen.Home> {
        HomeScreen(
            callbacks = rememberHomeCallbacks(navigator),
            slots = HomeSlots(
                routedContent = { destination, onClose -> HomeRoutedContent(destination, onClose, navigator) },
                bottomBar = rememberBottomBar(Screen.Home, navigator),
            ),
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
            onShortcutClick = { shortcut -> navigator.navigate(shortcut.screen()) },
            onInstallmentClick = { navigator.navigate(Screen.Debts) },
        )
    }

private fun HomeShortcut.screen(): Screen = when (this) {
    HomeShortcut.PRE_OUTING -> Screen.PreOuting
    HomeShortcut.CHECK_IN -> Screen.CheckIn
    HomeShortcut.WEEKLY_SUMMARY -> Screen.WeeklySummary
}

@Composable
private fun HomeRoutedContent(destination: HomeDestination, onClose: () -> Unit, navigator: Navigator) {
    val openSettings = { navigator.navigate(Screen.Settings) }
    when (destination) {
        HomeDestination.PRE_OUTING -> PreOutingScreen(koinViewModel(), preOutingCallbacks(navigator), onClose, Modifier)
        HomeDestination.CHECK_IN -> CheckInScreen(koinViewModel(), onClose, Modifier)
        HomeDestination.WEEKLY_SUMMARY -> SummaryScreen(koinViewModel(), openSettings, onClose, Modifier)
        HomeDestination.DAILY_CHECKS -> Unit
    }
}
