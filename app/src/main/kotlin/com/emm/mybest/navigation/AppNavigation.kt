package com.emm.mybest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.mybest.screens.AddHabitScreen
import com.emm.mybest.screens.AddPhotoScreen
import com.emm.mybest.screens.AddWeightScreen
import com.emm.mybest.screens.HistoryScreen
import com.emm.mybest.screens.HomeScreen
import com.emm.mybest.screens.InsightsScreen
import com.emm.mybest.viewmodel.AddHabitViewModel
import com.emm.mybest.viewmodel.AddPhotoViewModel
import com.emm.mybest.viewmodel.AddWeightViewModel
import com.emm.mybest.viewmodel.HistoryViewModel
import com.emm.mybest.viewmodel.HomeViewModel
import com.emm.mybest.viewmodel.InsightsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = viewModel,
                onAddWeightClick = { navController.navigate(Screen.AddWeight.route) },
                onAddHabitClick = { navController.navigate(Screen.AddHabit.route) },
                onAddPhotoClick = { navController.navigate(Screen.AddPhoto.route) },
                onViewHistoryClick = { navController.navigate(Screen.History.route) },
                onViewInsightsClick = { navController.navigate(Screen.Insights.route) }
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
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
