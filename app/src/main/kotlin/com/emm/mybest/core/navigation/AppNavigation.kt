package com.emm.mybest.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.features.habit.presentation.AddHabitScreen
import com.emm.mybest.features.habit.presentation.AddHabitViewModel
import com.emm.mybest.features.history.presentation.HistoryScreen
import com.emm.mybest.features.history.presentation.HistoryViewModel
import com.emm.mybest.features.home.presentation.HomeScreen
import com.emm.mybest.features.home.presentation.HomeViewModel
import com.emm.mybest.features.insights.presentation.InsightsScreen
import com.emm.mybest.features.insights.presentation.InsightsViewModel
import com.emm.mybest.features.photo.presentation.AddPhotoScreen
import com.emm.mybest.features.photo.presentation.AddPhotoViewModel
import com.emm.mybest.features.photo.presentation.ComparePhotosScreen
import com.emm.mybest.features.photo.presentation.ComparePhotosViewModel
import com.emm.mybest.features.timeline.presentation.TimelineScreen
import com.emm.mybest.features.timeline.presentation.TimelineViewModel
import com.emm.mybest.features.weight.presentation.AddWeightScreen
import com.emm.mybest.features.weight.presentation.AddWeightViewModel
import com.emm.mybest.ui.components.HBottomNavigationBar
import org.koin.androidx.compose.koinViewModel

private val TOP_LEVEL_SCREENS = listOf(
    Screen.Home,
    Screen.History,
    Screen.Insights,
    Screen.Timeline,
)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    intentAction: String? = null,
    onConsumeAction: () -> Unit = {},
) {
    val navController = rememberNavController()
    val currentOnConsumeAction by rememberUpdatedState(onConsumeAction)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = TOP_LEVEL_SCREENS.any { screen ->
        currentDestination?.hasRoute(screen::class) == true
    }

    HandleIntentAction(
        intentAction = intentAction,
        navController = navController,
        onConsumeAction = currentOnConsumeAction,
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                HBottomNavigationBar(
                    currentRoute = TOP_LEVEL_SCREENS.firstOrNull { screen ->
                        currentDestination?.hasRoute(screen::class) == true
                    },
                    onNavItemClick = { screen ->
                        navController.navigate(screen) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(Screen.Home) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}

@Composable
private fun HandleIntentAction(
    intentAction: String?,
    navController: NavHostController,
    onConsumeAction: () -> Unit,
) {
    val currentOnConsumeAction by rememberUpdatedState(onConsumeAction)
    LaunchedEffect(intentAction) {
        if (intentAction == null) return@LaunchedEffect
        when (intentAction) {
            "com.emm.mybest.ACTION_ADD_WEIGHT" -> navController.navigate(Screen.AddWeight)
            "com.emm.mybest.ACTION_ADD_HABIT" -> navController.navigate(Screen.AddHabit)
            "com.emm.mybest.ACTION_ADD_PHOTO" -> navController.navigate(Screen.AddPhoto)
        }
        currentOnConsumeAction()
    }
}

@Composable
private fun AppNavGraph(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding),
    ) {
        composable<Screen.Home> {
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigate = { screen -> navController.navigate(screen) },
                modifier = Modifier,
            )
        }

        composable<Screen.AddWeight> {
            val viewModel: AddWeightViewModel = koinViewModel()
            AddWeightScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                modifier = Modifier,
            )
        }

        composable<Screen.AddHabit> {
            val viewModel: AddHabitViewModel = koinViewModel()
            AddHabitScreen(
                viewModel = viewModel,
                initialHabitId = null,
                onBackClick = { navController.popBackStack() },
                modifier = Modifier,
            )
        }

        composable<Screen.EditHabit> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.EditHabit>()
            val viewModel: AddHabitViewModel = koinViewModel()
            AddHabitScreen(
                viewModel = viewModel,
                initialHabitId = route.habitId,
                onBackClick = { navController.popBackStack() },
                modifier = Modifier,
            )
        }

        composable<Screen.AddPhoto> {
            val viewModel: AddPhotoViewModel = koinViewModel()
            AddPhotoScreen(
                viewModel = viewModel,
                mediaManager = org.koin.compose.koinInject(),
                onBackClick = { navController.popBackStack() },
                modifier = Modifier,
            )
        }

        composable<Screen.History> {
            val viewModel: HistoryViewModel = koinViewModel()
            HistoryScreen(
                viewModel = viewModel,
                modifier = Modifier,
            )
        }

        composable<Screen.Insights> {
            val viewModel: InsightsViewModel = koinViewModel()
            InsightsScreen(
                viewModel = viewModel,
                onCompareClick = { navController.navigate(Screen.ComparePhotos) },
                onRecommendationAction = { action ->
                    when (action) {
                        InsightsRecommendationAction.PRIORITIZE_HABIT -> navController.navigate(Screen.AddHabit)
                        InsightsRecommendationAction.ADJUST_WEIGHT_PLAN -> navController.navigate(Screen.AddWeight)
                        InsightsRecommendationAction.ADD_PROGRESS_PHOTO -> navController.navigate(Screen.AddPhoto)
                        InsightsRecommendationAction.KEEP_ROUTINE -> navController.navigate(Screen.Home)
                    }
                },
                modifier = Modifier,
            )
        }

        composable<Screen.ComparePhotos> {
            val viewModel: ComparePhotosViewModel = koinViewModel()
            ComparePhotosScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                modifier = Modifier,
            )
        }

        composable<Screen.Timeline> {
            val viewModel: TimelineViewModel = koinViewModel()
            TimelineScreen(
                viewModel = viewModel,
                modifier = Modifier,
            )
        }
    }
}
