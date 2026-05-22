package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.PhotoType

fun photoTypeLabel(type: PhotoType): String = when (type) {
    PhotoType.TRUNK -> "Tronco"
    PhotoType.FACE -> "Cara"
}
