package com.emm.mybest.domain.usecase.compliance

import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.repository.ComplianceRepository
import kotlinx.datetime.LocalDate

class ToggleMealComplianceUseCase(private val repo: ComplianceRepository) {
    suspend operator fun invoke(date: LocalDate, type: MealType, done: Boolean) =
        repo.toggleMeal(date, type, done)
}
