package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.usecase.compliance.GetCompletionStreakUseCase
import com.emm.mybest.domain.usecase.compliance.ObserveDailyComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleExerciseComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleMealComplianceUseCase
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.diet.UpsertMealUseCase
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.preferences.ObserveDailySlotTimesUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase

data class HomeUseCases(
    val observeDailyCompliance: ObserveDailyComplianceUseCase,
    val getCompletionStreak: GetCompletionStreakUseCase,
    val observeDailySlotTimes: ObserveDailySlotTimesUseCase,
    val getMealPlan: GetWeeklyMealPlanUseCase,
    val getExercisePlan: GetWeeklyExercisePlanUseCase,
    val toggleMeal: ToggleMealComplianceUseCase,
    val toggleExercise: ToggleExerciseComplianceUseCase,
    val upsertMeal: UpsertMealUseCase,
    val observeWeightProgress: ObserveWeightProgressUseCase,
    val observePhotos: ObservePhotosUseCase,
)
