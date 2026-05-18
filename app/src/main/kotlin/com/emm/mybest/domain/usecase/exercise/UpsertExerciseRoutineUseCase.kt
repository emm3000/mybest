package com.emm.mybest.domain.usecase.exercise

import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.repository.ExercisePlanRepository

class UpsertExerciseRoutineUseCase(private val repo: ExercisePlanRepository) {
    suspend operator fun invoke(entry: ExercisePlanEntry) = repo.upsertEntry(entry)
}
