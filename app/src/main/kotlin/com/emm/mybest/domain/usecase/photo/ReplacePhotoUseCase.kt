package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.first

/**
 * Swaps the file of a photo while keeping its type, date and creation stamp.
 *
 * The new photo is saved first and the old one is deleted only after that succeeds, so a
 * failure while saving leaves the old photo untouched. If deleting the old photo fails, both
 * photos stay and the exception propagates.
 */
class ReplacePhotoUseCase(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(photoId: String, newPhotoPath: String): ReplacePhotoResult {
        val oldPhoto = photoRepository.getAllPhotos().first().firstOrNull { it.id == photoId }
            ?: return ReplacePhotoResult.PhotoNotFound
        val replacement = NewProgressPhoto(
            photoPath = newPhotoPath,
            type = oldPhoto.type,
            date = oldPhoto.date,
            createdAt = oldPhoto.createdAt,
        )
        photoRepository.savePhotos(listOf(replacement))
        photoRepository.deletePhoto(photoId)
        return ReplacePhotoResult.Replaced
    }
}
