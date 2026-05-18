package com.emm.mybest.domain.usecase.compliance

import com.emm.mybest.domain.models.DailyCompliance
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.repository.ComplianceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveDailyComplianceUseCase(
    private val mealRepo: ComplianceRepository,
) {
    operator fun invoke(date: LocalDate): Flow<DailyCompliance> =
        combine(
            mealRepo.observeMealsByDate(date),
            mealRepo.observeExerciseByDate(date),
        ) { meals, exercise ->
            val mealsMap = MealType.entries.associateWith { type ->
                meals.firstOrNull { it.mealType == type }?.done ?: false
            }
            DailyCompliance(date, mealsMap, exercise?.done ?: false)
        }
}
