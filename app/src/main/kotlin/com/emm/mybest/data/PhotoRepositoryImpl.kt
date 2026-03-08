package com.emm.mybest.data

import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepositoryImpl(
    private val dao: ProgressPhotoDao
) : PhotoRepository {

    override fun getAllPhotos(): Flow<List<ProgressPhoto>> {
        return dao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }
}

private fun ProgressPhotoEntity.toDomain() = ProgressPhoto(
    id = id,
    habitRecordId = habitRecordId,
    date = date,
    type = type.name, // Mapping PhotoType enum to String
    photoPath = photoPath,
    createdAt = createdAt
)
