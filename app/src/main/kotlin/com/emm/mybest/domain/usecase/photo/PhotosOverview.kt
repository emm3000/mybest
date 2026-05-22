package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.PhotoType

data class PhotosOverview(
    val byType: Map<PhotoType, PhotoTypeOverview>,
)
