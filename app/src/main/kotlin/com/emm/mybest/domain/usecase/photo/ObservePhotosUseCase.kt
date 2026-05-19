package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow

class ObservePhotosUseCase(
    private val photoRepository: PhotoRepository,
) {
    operator fun invoke(): Flow<List<ProgressPhoto>> = photoRepository.getAllPhotos()
}
