package com.emm.mybest.features.diet.presentation

sealed interface MealPlanEffect {
    data object DismissSheet : MealPlanEffect
}
