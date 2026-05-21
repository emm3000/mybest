package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.usecase.compliance.ToggleExerciseComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleMealComplianceUseCase

data class HomeToggleUseCases(
    val toggleMeal: ToggleMealComplianceUseCase,
    val toggleExercise: ToggleExerciseComplianceUseCase,
)
