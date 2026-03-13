package com.emm.mybest.core.navigation

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
    data class EditHabit(val habitId: String) : Screen

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

    @Serializable
    data object ReminderSettings : Screen

    // Example with arguments (for later use or demonstration of the "arguments" part of the requirement)
    @Serializable
    data class HabitDetail(val habitId: String) : Screen
}
