package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.data.entities.PhotoType as DataPhotoType

fun ProgressPhotoEntity.toDomain(): ProgressPhoto = ProgressPhoto(
    id = id,
    date = date,
    type = type.toDomain(),
    photoPath = photoPath,
    createdAt = createdAt,
)

fun NewProgressPhoto.toEntity(): ProgressPhotoEntity = ProgressPhotoEntity(
    date = date,
    type = type.toData(),
    photoPath = photoPath,
)

fun DataPhotoType.toDomain(): PhotoType = when (this) {
    DataPhotoType.FACE -> PhotoType.FACE
    DataPhotoType.TRUNK -> PhotoType.TRUNK
}

fun PhotoType.toData(): DataPhotoType = when (this) {
    PhotoType.FACE -> DataPhotoType.FACE
    PhotoType.TRUNK -> DataPhotoType.TRUNK
}
