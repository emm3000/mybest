package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.repository.PhotoRepository

class DeletePhotoUseCase(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(id: String) {
        photoRepository.deletePhoto(id)
    }
}
