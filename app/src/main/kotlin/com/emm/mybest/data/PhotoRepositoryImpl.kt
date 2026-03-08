package com.emm.mybest.data

import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.emm.mybest.data.entities.PhotoType as DataPhotoType

class PhotoRepositoryImpl(
    private val dao: ProgressPhotoDao
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
}

private fun ProgressPhotoEntity.toDomain() = ProgressPhoto(
    id = id,
    habitRecordId = habitRecordId,
    date = date,
    type = type.toDomain(),
    photoPath = photoPath,
    createdAt = createdAt
)

private fun NewProgressPhoto.toEntity() = ProgressPhotoEntity(
    date = date,
    type = type.toData(),
    photoPath = photoPath,
)

private fun DataPhotoType.toDomain() = PhotoType.valueOf(name)
private fun PhotoType.toData() = DataPhotoType.valueOf(name)
