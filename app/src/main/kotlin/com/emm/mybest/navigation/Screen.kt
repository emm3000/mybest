package com.emm.mybest.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data object AddWeight : Screen

    @Serializable
    data object AddHabit : Screen

    @Serializable
    data object AddPhoto : Screen

    @Serializable
    data object History : Screen

    @Serializable
    data object Insights : Screen

    @Serializable
    data object ComparePhotos : Screen

    @Serializable
    data object Timeline : Screen

    // Example with arguments (for later use or demonstration of the "arguments" part of the requirement)
    @Serializable
    data class HabitDetail(val habitId: String) : Screen
}
