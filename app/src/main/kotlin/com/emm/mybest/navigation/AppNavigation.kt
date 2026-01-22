package com.emm.mybest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emm.mybest.screens.AddHabitScreen
import com.emm.mybest.screens.AddWeightScreen
import com.emm.mybest.screens.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddWeightClick = { navController.navigate(Screen.AddWeight.route) },
                onAddHabitClick = { navController.navigate(Screen.AddHabit.route) },
                onAddPhotoClick = { /* Próximamente */ }
            )
        }
        
        composable(Screen.AddWeight.route) {
            AddWeightScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { weight, note -> 
                    // Aquí iría la lógica del ViewModel para guardar en Room
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AddHabit.route) {
            AddHabitScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { ateHealthy, didExercise, notes ->
                    // Aquí iría la lógica del ViewModel para guardar en Room
                    navController.popBackStack()
                }
            )
        }
    }
}
