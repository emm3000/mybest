package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository

class SavePhotosUseCase(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(photos: List<NewProgressPhoto>) {
        photoRepository.savePhotos(photos)
    }
}
