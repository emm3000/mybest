package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.CapturedNotificationEntity
import com.emm.mybest.domain.models.CapturedNotification
import kotlin.time.Instant

fun CapturedNotification.toEntity(): CapturedNotificationEntity = CapturedNotificationEntity(
    id = id,
    packageName = packageName,
    title = title,
    text = text,
    postedAtEpochMs = postedAt.toEpochMilliseconds(),
)

fun CapturedNotificationEntity.toDomain(): CapturedNotification = CapturedNotification(
    id = id,
    packageName = packageName,
    title = title,
    text = text,
    postedAt = Instant.fromEpochMilliseconds(postedAtEpochMs),
)
