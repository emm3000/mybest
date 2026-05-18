package com.emm.mybest.domain.usecase.diet

import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.repository.MealPlanRepository

class UpsertMealUseCase(private val repo: MealPlanRepository) {
    suspend operator fun invoke(entry: MealPlanEntry) = repo.upsertEntry(entry)
}
