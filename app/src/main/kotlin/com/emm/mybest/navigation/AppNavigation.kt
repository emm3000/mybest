package com.emm.mybest.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.emm.mybest.screens.AddHabitScreen
import com.emm.mybest.screens.AddPhotoScreen
import com.emm.mybest.screens.AddWeightScreen
import com.emm.mybest.screens.ComparePhotosScreen
import com.emm.mybest.screens.HistoryScreen
import com.emm.mybest.screens.HomeScreen
import com.emm.mybest.screens.InsightsScreen
import com.emm.mybest.screens.TimelineScreen
import com.emm.mybest.ui.components.HBottomNavigationBar
import com.emm.mybest.viewmodel.AddHabitViewModel
import com.emm.mybest.viewmodel.AddPhotoViewModel
import com.emm.mybest.viewmodel.AddWeightViewModel
import com.emm.mybest.viewmodel.ComparePhotosViewModel
import com.emm.mybest.viewmodel.HistoryViewModel
import com.emm.mybest.viewmodel.HomeViewModel
import com.emm.mybest.viewmodel.InsightsViewModel
import com.emm.mybest.viewmodel.TimelineViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    intentAction: String? = null,
    onConsumeAction: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentOnConsumeAction by rememberUpdatedState(onConsumeAction)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Define top-level screens for bottom navigation
    val topLevelScreens = listOf(
        Screen.Home,
        Screen.History,
        Screen.Insights,
        Screen.Timeline
    )

    val showBottomBar = topLevelScreens.any { screen ->
        currentDestination?.hasRoute(screen::class) == true
    }

    LaunchedEffect(intentAction) {
        if (intentAction != null) {
            when (intentAction) {
                "com.emm.mybest.ACTION_ADD_WEIGHT" -> navController.navigate(Screen.AddWeight)
                "com.emm.mybest.ACTION_ADD_HABIT" -> navController.navigate(Screen.AddHabit)
                "com.emm.mybest.ACTION_ADD_PHOTO" -> navController.navigate(Screen.AddPhoto)
            }
            currentOnConsumeAction()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                HBottomNavigationBar(
                    currentRoute = topLevelScreens.firstOrNull { screen ->
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
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable<Screen.Home> {
                val viewModel: HomeViewModel = koinViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onAddWeightClick = { navController.navigate(Screen.AddWeight) },
                    onAddHabitClick = { navController.navigate(Screen.AddHabit) },
                    onAddPhotoClick = { navController.navigate(Screen.AddPhoto) },
                    onViewHistoryClick = { navController.navigate(Screen.History) },
                    onViewInsightsClick = { navController.navigate(Screen.Insights) },
                    onViewTimelineClick = { navController.navigate(Screen.Timeline) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<Screen.AddWeight> {
                val viewModel: AddWeightViewModel = koinViewModel()
                AddWeightScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<Screen.AddHabit> {
                val viewModel: AddHabitViewModel = koinViewModel()
                AddHabitScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<Screen.AddPhoto> {
                val viewModel: AddPhotoViewModel = koinViewModel()
                AddPhotoScreen(
                    viewModel = viewModel,
                    mediaManager = org.koin.compose.koinInject(),
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<Screen.History> {
                val viewModel: HistoryViewModel = koinViewModel()
                HistoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<Screen.Insights> {
                val viewModel: InsightsViewModel = koinViewModel()
                InsightsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onCompareClick = { navController.navigate(Screen.ComparePhotos) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<Screen.ComparePhotos> {
                val viewModel: ComparePhotosViewModel = koinViewModel()
                ComparePhotosScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<Screen.Timeline> {
                val viewModel: TimelineViewModel = koinViewModel()
                TimelineScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
