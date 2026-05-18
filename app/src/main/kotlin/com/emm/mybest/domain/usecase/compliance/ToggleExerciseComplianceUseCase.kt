package com.emm.mybest.domain.usecase.compliance

import com.emm.mybest.domain.repository.ComplianceRepository
import kotlinx.datetime.LocalDate

class ToggleExerciseComplianceUseCase(private val repo: ComplianceRepository) {
    suspend operator fun invoke(date: LocalDate, done: Boolean) =
        repo.toggleExercise(date, done)
}
