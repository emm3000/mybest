package com.emm.mybest.data

import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.mappers.toData
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepositoryImpl(
    private val dao: ProgressPhotoDao,
) : PhotoRepository {

    override fun getAllPhotos(): Flow<List<ProgressPhoto>> {
        return dao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPhotosByType(type: PhotoType): Flow<List<ProgressPhoto>> {
        return dao.observeByType(type.toData()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun savePhotos(photos: List<NewProgressPhoto>) {
        dao.insertAll(photos.map { it.toEntity() })
    }

    override suspend fun deletePhoto(photoId: String) {
        dao.deleteById(photoId)
    }
}
