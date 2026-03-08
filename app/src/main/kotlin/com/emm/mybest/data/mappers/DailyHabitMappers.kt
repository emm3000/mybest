package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.domain.models.DailyHabitSummary

fun DailyHabitEntity.toDomain(): DailyHabitSummary = DailyHabitSummary(
    date = date,
    ateHealthy = ateHealthy,
    didExercise = didExercise,
    notes = notes,
)
