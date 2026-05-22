package com.emm.mybest.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Home : Screen

    @Serializable
    data object AddWeight : Screen

    @Serializable
    data object Photos : Screen

    @Serializable
    data object History : Screen

    @Serializable
    data object Insights : Screen

    @Serializable
    data class ComparePhotos(val beforeId: String, val afterId: String) : Screen

    @Serializable
    data class PhotoViewer(val initialPhotoId: String) : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object MealPlan : Screen

    @Serializable
    data object ExercisePlan : Screen
}
