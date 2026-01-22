package com.emm.mybest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.mybest.screens.AddHabitScreen
import com.emm.mybest.screens.AddWeightScreen
import com.emm.mybest.screens.HomeScreen
import com.emm.mybest.viewmodel.AddHabitViewModel
import com.emm.mybest.viewmodel.AddWeightViewModel
import com.emm.mybest.viewmodel.HomeViewModel
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
                onAddPhotoClick = { /* Próximamente */ }
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
    }
}
