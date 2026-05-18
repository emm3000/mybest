package com.emm.mybest.features.exercise.presentation

sealed interface ExercisePlanEffect {
    data object DismissSheet : ExercisePlanEffect
}
