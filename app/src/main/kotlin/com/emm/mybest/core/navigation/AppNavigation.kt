package com.emm.mybest.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.emm.mybest.features.history.presentation.HistoryScreen
import com.emm.mybest.features.history.presentation.HistoryViewModel
import com.emm.mybest.features.photo.presentation.ComparePhotosScreen
import com.emm.mybest.features.photo.presentation.ComparePhotosViewModel
import com.emm.mybest.features.photo.presentation.PhotosScreen
import com.emm.mybest.features.photo.presentation.PhotosViewModel
import com.emm.mybest.features.photo.presentation.viewer.PhotoViewer
import com.emm.mybest.features.preouting.presentation.PreOutingScreen
import com.emm.mybest.features.preouting.presentation.PreOutingViewModel
import com.emm.mybest.features.settings.presentation.SettingsCallbacks
import com.emm.mybest.features.settings.presentation.SettingsScreen
import com.emm.mybest.features.settings.presentation.SettingsViewModel
import com.emm.mybest.features.weight.presentation.AddWeightScreen
import com.emm.mybest.features.weight.presentation.AddWeightViewModel
import com.emm.mybest.ui.components.HBottomNavigationBar
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

private val TOP_LEVEL_SCREENS = setOf(
    Screen.Home,
    Screen.WeeklySummary,
    Screen.History,
    Screen.Settings,
)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    intentAction: String? = null,
    onConsumeAction: () -> Unit = {},
) {
    val navigationState = rememberNavigationState(
        startRoute = Screen.Home,
        topLevelRoutes = TOP_LEVEL_SCREENS,
    )
    val navigator = remember { Navigator(navigationState) }
    val currentOnConsumeAction by rememberUpdatedState(onConsumeAction)

    HandleIntentAction(
        intentAction = intentAction,
        navigator = navigator,
        onConsumeAction = currentOnConsumeAction,
    )

    val entryProvider = remember(navigator) { appEntryProvider(navigator) }
    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        transitionSpec = { navigationState.currentTransition() },
        popTransitionSpec = { navigationState.currentTransition() },
        predictivePopTransitionSpec = { popTransition() },
    )
}

private fun NavigationState.currentTransition() = when (lastTransitionKind) {
    NavTransitionKind.TabSwitch -> tabSwitchTransition()
    NavTransitionKind.Pop -> popTransition()
    NavTransitionKind.Push -> pushTransition()
}

@Composable
internal fun rememberBottomBar(current: Screen, navigator: Navigator): @Composable () -> Unit =
    remember(current, navigator) {
        {
            HBottomNavigationBar(
                currentRoute = current,
                onNavItemClick = { screen -> navigator.navigate(screen) },
            )
        }
    }

private fun appEntryProvider(navigator: Navigator): (NavKey) -> NavEntry<NavKey> = entryProvider {
    homeEntry(navigator)
    entry<Screen.AddWeight> {
        val viewModel: AddWeightViewModel = koinViewModel()
        val openSettings = { navigator.navigate(Screen.Settings) }
        AddWeightScreen(viewModel, onBackClick = { navigator.goBack() }, onOpenSettings = openSettings, Modifier)
    }
    entry<Screen.Photos> {
        val viewModel: PhotosViewModel = koinViewModel()
        PhotosScreen(
            viewModel = viewModel,
            photoCaptureFiles = koinInject(),
            onBack = { navigator.goBack() },
            onOpenViewer = { photoId -> navigator.navigate(Screen.PhotoViewer(photoId)) },
            onCompare = { beforeId, afterId -> navigator.navigate(Screen.ComparePhotos(beforeId, afterId)) },
            bottomBar = {},
            modifier = Modifier,
        )
    }
    entry<Screen.History> {
        val viewModel: HistoryViewModel = koinViewModel()
        HistoryScreen(
            viewModel = viewModel,
            onSeePhotosClick = { navigator.navigate(Screen.Photos) },
            bottomBar = rememberBottomBar(Screen.History, navigator),
            modifier = Modifier,
        )
    }
    entry<Screen.ComparePhotos> { key ->
        val viewModel: ComparePhotosViewModel = koinViewModel { parametersOf(key.beforeId, key.afterId) }
        ComparePhotosScreen(viewModel = viewModel, onBackClick = { navigator.goBack() }, modifier = Modifier)
    }
    entry<Screen.PhotoViewer> { key ->
        PhotoViewer(initialPhotoId = key.initialPhotoId, onBack = { navigator.goBack() }, modifier = Modifier)
    }
    entry<Screen.Settings> {
        val viewModel: SettingsViewModel = koinViewModel()
        SettingsScreen(
            viewModel = viewModel,
            callbacks = SettingsCallbacks(
                onDebtsClick = { navigator.navigate(Screen.Debts) },
                onPurchaseGoalClick = { navigator.navigate(Screen.PurchaseGoal) },
                onCaptureClick = { navigator.navigate(Screen.NotificationCapture) },
                bottomBar = rememberBottomBar(Screen.Settings, navigator),
            ),
            modifier = Modifier,
        )
    }
    entry<Screen.PreOuting> {
        val viewModel: PreOutingViewModel = koinViewModel()
        PreOutingScreen(
            viewModel = viewModel,
            callbacks = preOutingCallbacks(navigator),
            onBack = { navigator.goBack() },
            modifier = Modifier,
        )
    }
    debtsEntry(navigator)
    purchaseGoalEntry(navigator)
    notificationCaptureEntry(navigator)
    checkInEntry(navigator)
    weeklySummaryEntry(navigator)
}

@Composable
private fun HandleIntentAction(
    intentAction: String?,
    navigator: Navigator,
    onConsumeAction: () -> Unit,
) {
    val currentOnConsumeAction by rememberUpdatedState(onConsumeAction)
    LaunchedEffect(intentAction) {
        if (intentAction == null) return@LaunchedEffect
        when (intentAction) {
            ACTION_ADD_WEIGHT -> navigator.navigate(Screen.AddWeight)
            ACTION_ADD_PHOTO -> navigator.navigate(Screen.Photos)
            ACTION_PRE_OUTING -> navigator.navigate(Screen.PreOuting)
            ACTION_DEBTS -> navigator.navigate(Screen.Debts)
            ACTION_CHECK_IN -> navigator.navigate(Screen.CheckIn)
            ACTION_WEEKLY_SUMMARY -> navigator.navigate(Screen.WeeklySummary)
        }
        currentOnConsumeAction()
    }
}
