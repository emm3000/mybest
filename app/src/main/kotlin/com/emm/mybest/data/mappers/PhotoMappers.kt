package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.data.entities.PhotoType as DataPhotoType

fun ProgressPhotoEntity.toDomain(): ProgressPhoto = ProgressPhoto(
    id = id,
    habitRecordId = habitRecordId,
    habitId = habitId,
    date = date,
    type = type.toDomain(),
    photoPath = photoPath,
    createdAt = createdAt,
)

fun NewProgressPhoto.toEntity(): ProgressPhotoEntity = ProgressPhotoEntity(
    habitId = habitId,
    date = date,
    type = type.toData(),
    photoPath = photoPath,
)

fun DataPhotoType.toDomain(): PhotoType = PhotoType.valueOf(name)
fun PhotoType.toData(): DataPhotoType = DataPhotoType.valueOf(name)
