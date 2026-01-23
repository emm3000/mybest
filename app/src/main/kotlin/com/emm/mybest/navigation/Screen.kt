package com.emm.mybest.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddWeight : Screen("add_weight")
    object AddHabit : Screen("add_habit")
    object AddPhoto : Screen("add_photo")
    object History : Screen("history")
    object Insights : Screen("insights")
    object ComparePhotos : Screen("compare_photos")
    object Timeline : Screen("timeline")
}
