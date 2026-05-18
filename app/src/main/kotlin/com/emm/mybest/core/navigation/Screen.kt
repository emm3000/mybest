package com.emm.mybest.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data object AddWeight : Screen

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

    @Serializable
    data object MealPlan : Screen

    @Serializable
    data object ExercisePlan : Screen
}
