package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.DailyCheckEntity
import com.emm.mybest.domain.models.DailyCheck

fun DailyCheck.toEntity(): DailyCheckEntity = DailyCheckEntity(
    appDay = appDay,
    type = type,
    isDone = isDone,
)

fun DailyCheckEntity.toDomain(): DailyCheck = DailyCheck(
    appDay = appDay,
    type = type,
    isDone = isDone,
)
