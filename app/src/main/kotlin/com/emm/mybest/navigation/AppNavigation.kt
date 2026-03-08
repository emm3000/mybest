package com.emm.mybest.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.mybest.screens.AddHabitScreen
import com.emm.mybest.screens.AddPhotoScreen
import com.emm.mybest.screens.AddWeightScreen
import com.emm.mybest.screens.ComparePhotosScreen
import com.emm.mybest.screens.HistoryScreen
import com.emm.mybest.screens.HomeScreen
import com.emm.mybest.screens.InsightsScreen
import com.emm.mybest.screens.TimelineScreen
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

    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = modifier.background(MaterialTheme.colorScheme.background)
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
                onViewTimelineClick = { navController.navigate(Screen.Timeline) }
            )
        }

        composable<Screen.AddWeight> {
            val viewModel: AddWeightViewModel = koinViewModel()
            AddWeightScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.AddHabit> {
            val viewModel: AddHabitViewModel = koinViewModel()
            AddHabitScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.AddPhoto> {
            val viewModel: AddPhotoViewModel = koinViewModel()
            AddPhotoScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.History> {
            val viewModel: HistoryViewModel = koinViewModel()
            HistoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.Insights> {
            val viewModel: InsightsViewModel = koinViewModel()
            InsightsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onCompareClick = { navController.navigate(Screen.ComparePhotos) }
            )
        }

        composable<Screen.ComparePhotos> {
            val viewModel: ComparePhotosViewModel = koinViewModel()
            ComparePhotosScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.Timeline> {
            val viewModel: TimelineViewModel = koinViewModel()
            TimelineScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
