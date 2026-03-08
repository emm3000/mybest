package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getAllPhotos(): Flow<List<ProgressPhoto>>
    fun getPhotosByType(type: PhotoType): Flow<List<ProgressPhoto>>
    suspend fun savePhotos(photos: List<NewProgressPhoto>)
    suspend fun deletePhoto(photoId: String)
}
