package com.emm.mybest.domain.usecase.exercise

import com.emm.mybest.domain.models.WeeklyExercisePlan
import com.emm.mybest.domain.repository.ExercisePlanRepository
import kotlinx.coroutines.flow.Flow

class GetWeeklyExercisePlanUseCase(private val repo: ExercisePlanRepository) {
    operator fun invoke(): Flow<WeeklyExercisePlan> = repo.observePlan()
}
