package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.domain.models.WeightEntry

fun DailyWeightEntity.toDomain(): WeightEntry = WeightEntry(
    id = id,
    date = date,
    weight = weight,
    habitId = habitId,
    photoPath = photoPath,
    note = note,
)
