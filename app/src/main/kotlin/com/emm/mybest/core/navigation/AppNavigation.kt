package com.emm.mybest.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.emm.mybest.domain.models.InsightsRecommendationAction
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
import com.emm.mybest.features.photo.presentation.AddPhotoScreen
import com.emm.mybest.features.photo.presentation.AddPhotoViewModel
import com.emm.mybest.features.photo.presentation.ComparePhotosScreen
import com.emm.mybest.features.photo.presentation.ComparePhotosViewModel
import com.emm.mybest.features.settings.presentation.ReminderSettingsScreen
import com.emm.mybest.features.settings.presentation.ReminderSettingsViewModel
import com.emm.mybest.features.timeline.presentation.PhotoViewer
import com.emm.mybest.features.timeline.presentation.TimelineScreen
import com.emm.mybest.features.timeline.presentation.TimelineViewModel
import com.emm.mybest.features.weight.presentation.AddWeightScreen
import com.emm.mybest.features.weight.presentation.AddWeightViewModel
import com.emm.mybest.ui.components.HBottomNavigationBar
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

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

    val showBottomBar = navigationState.topLevelRoute in TOP_LEVEL_SCREENS &&
        navigationState.backStacks[navigationState.topLevelRoute]?.size == 1

    HandleIntentAction(
        intentAction = intentAction,
        navigator = navigator,
        onConsumeAction = currentOnConsumeAction,
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                HBottomNavigationBar(
                    currentRoute = navigationState.topLevelRoute as? Screen,
                    onNavItemClick = { screen -> navigator.navigate(screen) },
                )
            }
        },
    ) { innerPadding ->
        val entryProvider = remember(navigator) { appEntryProvider(navigator) }
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            transitionSpec = {
                when (navigationState.lastTransitionKind) {
                    NavTransitionKind.TabSwitch -> tabSwitchTransition()
                    NavTransitionKind.Pop -> popTransition()
                    NavTransitionKind.Push -> pushTransition()
                }
            },
            popTransitionSpec = { popTransition() },
            predictivePopTransitionSpec = { popTransition() },
        )
    }
}

private fun appEntryProvider(navigator: Navigator): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<Screen.Home> {
        HomeScreen(
            callbacks = HomeCallbacks(
                onWeightClick = { navigator.navigate(Screen.AddWeight) },
                onPhotoClick = { navigator.navigate(Screen.AddPhoto) },
                onMealPlanClick = { navigator.navigate(Screen.MealPlan) },
                onExercisePlanClick = { navigator.navigate(Screen.ExercisePlan) },
                onSettingsClick = { navigator.navigate(Screen.Settings) },
                onHistoryClick = { navigator.navigate(Screen.History) },
            ),
            modifier = Modifier,
        )
    }

    entry<Screen.AddWeight> {
        val viewModel: AddWeightViewModel = koinViewModel()
        AddWeightScreen(
            viewModel = viewModel,
            onBackClick = { navigator.goBack() },
            modifier = Modifier,
        )
    }

    entry<Screen.AddPhoto> {
        val viewModel: AddPhotoViewModel = koinViewModel()
        AddPhotoScreen(
            viewModel = viewModel,
            mediaManager = koinInject(),
            onBackClick = { navigator.goBack() },
            modifier = Modifier,
        )
    }

    entry<Screen.History> {
        val viewModel: HistoryViewModel = koinViewModel()
        HistoryScreen(
            viewModel = viewModel,
            onSeePhotosClick = { navigator.navigate(Screen.Timeline) },
            modifier = Modifier,
        )
    }

    entry<Screen.Insights> {
        val viewModel: InsightsViewModel = koinViewModel()
        InsightsScreen(
            viewModel = viewModel,
            onCompareClick = { navigator.navigate(Screen.ComparePhotos) },
            onRecommendationAction = { action ->
                when (action) {
                    InsightsRecommendationAction.ADJUST_WEIGHT_PLAN ->
                        navigator.navigate(Screen.MealPlan)
                    InsightsRecommendationAction.ADD_PROGRESS_PHOTO ->
                        navigator.navigate(Screen.AddPhoto)
                    InsightsRecommendationAction.KEEP_ROUTINE -> Unit
                }
            },
            onHistoryClick = { navigator.navigate(Screen.History) },
            onAddWeightClick = { navigator.navigate(Screen.AddWeight) },
            modifier = Modifier,
        )
    }

    entry<Screen.ComparePhotos> {
        val viewModel: ComparePhotosViewModel = koinViewModel()
        ComparePhotosScreen(
            viewModel = viewModel,
            onBackClick = { navigator.goBack() },
            modifier = Modifier,
        )
    }

    entry<Screen.Timeline> {
        val viewModel: TimelineViewModel = koinViewModel()
        TimelineScreen(
            viewModel = viewModel,
            onAddPhotoClick = { navigator.navigate(Screen.AddPhoto) },
            onCompareClick = { navigator.navigate(Screen.ComparePhotos) },
            onOpenViewer = { photoId -> navigator.navigate(Screen.PhotoViewer(photoId)) },
            modifier = Modifier,
        )
    }

    entry<Screen.PhotoViewer> { key ->
        PhotoViewer(
            initialPhotoId = key.initialPhotoId,
            onBack = { navigator.goBack() },
            modifier = Modifier,
        )
    }

    entry<Screen.Settings> {
        val viewModel: ReminderSettingsViewModel = koinViewModel()
        ReminderSettingsScreen(
            viewModel = viewModel,
            onBackClick = { navigator.goBack() },
            onMealPlanClick = { navigator.navigate(Screen.MealPlan) },
            onExercisePlanClick = { navigator.navigate(Screen.ExercisePlan) },
            modifier = Modifier,
        )
    }

    entry<Screen.MealPlan> {
        val viewModel: MealPlanViewModel = koinViewModel()
        MealPlanScreen(
            viewModel = viewModel,
            modifier = Modifier,
        )
    }

    entry<Screen.ExercisePlan> {
        val viewModel: ExercisePlanViewModel = koinViewModel()
        ExercisePlanScreen(
            viewModel = viewModel,
            modifier = Modifier,
        )
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
            "com.emm.mybest.ACTION_ADD_PHOTO" -> navigator.navigate(Screen.AddPhoto)
        }
        currentOnConsumeAction()
    }
}
