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
import com.emm.mybest.features.diet.presentation.MealPlanScreen
import com.emm.mybest.features.diet.presentation.MealPlanViewModel
import com.emm.mybest.features.exercise.presentation.ExercisePlanScreen
import com.emm.mybest.features.exercise.presentation.ExercisePlanViewModel
import com.emm.mybest.features.history.presentation.HistoryScreen
import com.emm.mybest.features.history.presentation.HistoryViewModel
import com.emm.mybest.features.home.presentation.HomeCallbacks
import com.emm.mybest.features.home.presentation.HomeScreen
import com.emm.mybest.features.insights.presentation.InsightsScreen
import com.emm.mybest.features.insights.presentation.InsightsViewModel
import com.emm.mybest.features.photo.presentation.ComparePhotosScreen
import com.emm.mybest.features.photo.presentation.ComparePhotosViewModel
import com.emm.mybest.features.photo.presentation.PhotosScreen
import com.emm.mybest.features.photo.presentation.PhotosViewModel
import com.emm.mybest.features.photo.presentation.viewer.PhotoViewer
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
    Screen.Insights,
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
private fun rememberHomeCallbacks(navigator: Navigator): HomeCallbacks =
    remember(navigator) {
        HomeCallbacks(
            onWeightClick = { navigator.navigate(Screen.AddWeight) },
            onPhotoClick = { navigator.navigate(Screen.Photos) },
            onMealPlanClick = { navigator.navigate(Screen.MealPlan) },
            onExercisePlanClick = { navigator.navigate(Screen.ExercisePlan) },
            onSettingsClick = { navigator.navigate(Screen.Settings) },
            onHistoryClick = { navigator.navigate(Screen.History) },
        )
    }

@Composable
private fun rememberBottomBar(current: Screen, navigator: Navigator): @Composable () -> Unit =
    remember(current, navigator) {
        {
            HBottomNavigationBar(
                currentRoute = current,
                onNavItemClick = { screen -> navigator.navigate(screen) },
            )
        }
    }

private fun appEntryProvider(navigator: Navigator): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<Screen.Home> {
        HomeScreen(
            callbacks = rememberHomeCallbacks(navigator),
            bottomBar = rememberBottomBar(Screen.Home, navigator),
            modifier = Modifier,
        )
    }
    entry<Screen.AddWeight> {
        val viewModel: AddWeightViewModel = koinViewModel()
        AddWeightScreen(viewModel = viewModel, onBackClick = { navigator.goBack() }, modifier = Modifier)
    }
    entry<Screen.Photos> {
        val viewModel: PhotosViewModel = koinViewModel()
        PhotosScreen(
            viewModel = viewModel,
            mediaManager = koinInject(),
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
    entry<Screen.Insights> {
        val viewModel: InsightsViewModel = koinViewModel()
        InsightsScreen(
            viewModel = viewModel,
            onHistoryClick = { navigator.navigate(Screen.History) },
            onAddWeightClick = { navigator.navigate(Screen.AddWeight) },
            bottomBar = rememberBottomBar(Screen.Insights, navigator),
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
            onMealPlanClick = { navigator.navigate(Screen.MealPlan) },
            onExercisePlanClick = { navigator.navigate(Screen.ExercisePlan) },
            bottomBar = rememberBottomBar(Screen.Settings, navigator),
            modifier = Modifier,
        )
    }
    entry<Screen.MealPlan> {
        val viewModel: MealPlanViewModel = koinViewModel()
        MealPlanScreen(viewModel = viewModel, modifier = Modifier)
    }
    entry<Screen.ExercisePlan> {
        val viewModel: ExercisePlanViewModel = koinViewModel()
        ExercisePlanScreen(viewModel = viewModel, modifier = Modifier)
    }
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
            "com.emm.mybest.ACTION_ADD_WEIGHT" -> navigator.navigate(Screen.AddWeight)
            "com.emm.mybest.ACTION_ADD_PHOTO" -> navigator.navigate(Screen.Photos)
        }
        currentOnConsumeAction()
    }
}
