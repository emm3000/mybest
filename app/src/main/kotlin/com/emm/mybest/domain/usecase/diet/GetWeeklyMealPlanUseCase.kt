package com.emm.mybest.domain.usecase.diet

import com.emm.mybest.domain.models.WeeklyMealPlan
import com.emm.mybest.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow

class GetWeeklyMealPlanUseCase(private val repo: MealPlanRepository) {
    operator fun invoke(): Flow<WeeklyMealPlan> = repo.observePlan()
}
