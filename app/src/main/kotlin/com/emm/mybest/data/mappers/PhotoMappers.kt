package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.ProgressPhoto

fun ProgressPhotoEntity.toDomain(): ProgressPhoto = ProgressPhoto(
    id = id,
    date = date,
    type = type,
    photoPath = photoPath,
    createdAt = createdAt,
)

fun NewProgressPhoto.toEntity(): ProgressPhotoEntity = ProgressPhotoEntity(
    date = date,
    type = type,
    photoPath = photoPath,
)
