package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase

data class HomePlanUseCases(
    val getMealPlan: GetWeeklyMealPlanUseCase,
    val getExercisePlan: GetWeeklyExercisePlanUseCase,
)
