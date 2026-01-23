package com.emm.mybest.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    intentAction: String? = null,
    onActionConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(intentAction) {
        if (intentAction != null) {
            when (intentAction) {
                "com.emm.mybest.ACTION_ADD_WEIGHT" -> navController.navigate(Screen.AddWeight.route)
                "com.emm.mybest.ACTION_ADD_HABIT" -> navController.navigate(Screen.AddHabit.route)
                "com.emm.mybest.ACTION_ADD_PHOTO" -> navController.navigate(Screen.AddPhoto.route)
            }
            onActionConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = viewModel,
                onAddWeightClick = { navController.navigate(Screen.AddWeight.route) },
                onAddHabitClick = { navController.navigate(Screen.AddHabit.route) },
                onAddPhotoClick = { navController.navigate(Screen.AddPhoto.route) },
                onViewHistoryClick = { navController.navigate(Screen.History.route) },
                onViewInsightsClick = { navController.navigate(Screen.Insights.route) },
                onViewTimelineClick = { navController.navigate(Screen.Timeline.route) }
            )
        }
        
        composable(Screen.AddWeight.route) {
            val viewModel: AddWeightViewModel = koinViewModel()
            AddWeightScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AddHabit.route) {
            val viewModel: AddHabitViewModel = koinViewModel()
            AddHabitScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddPhoto.route) {
            val viewModel: AddPhotoViewModel = koinViewModel()
            AddPhotoScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = koinViewModel()
            HistoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Insights.route) {
            val viewModel: InsightsViewModel = koinViewModel()
            InsightsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onCompareClick = { navController.navigate(Screen.ComparePhotos.route) }
            )
        }

        composable(Screen.ComparePhotos.route) {
            val viewModel: ComparePhotosViewModel = koinViewModel()
            ComparePhotosScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Timeline.route) {
            val viewModel: TimelineViewModel = koinViewModel()
            TimelineScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
