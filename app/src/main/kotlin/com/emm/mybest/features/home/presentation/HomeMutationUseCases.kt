package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.usecase.compliance.ToggleExerciseComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleMealComplianceUseCase
import com.emm.mybest.domain.usecase.diet.UpsertMealUseCase

data class HomeMutationUseCases(
    val toggleMeal: ToggleMealComplianceUseCase,
    val toggleExercise: ToggleExerciseComplianceUseCase,
    val upsertMeal: UpsertMealUseCase,
)
