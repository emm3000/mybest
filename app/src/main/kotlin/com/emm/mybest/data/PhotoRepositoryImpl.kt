package com.emm.mybest.data

import android.content.Context
import android.net.Uri
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

class PhotoRepositoryImpl(
    private val context: Context,
    private val dao: ProgressPhotoDao,
    private val dispatchers: CoroutineDispatchers,
) : PhotoRepository {

    override fun getAllPhotos(): Flow<List<ProgressPhoto>> {
        return dao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPhotosByType(type: PhotoType): Flow<List<ProgressPhoto>> {
        return dao.observeByType(type).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun savePhotos(photos: List<NewProgressPhoto>) {
        dao.insertAll(photos.map { it.toEntity() })
    }

    override suspend fun deletePhoto(photoId: String) {
        withContext(dispatchers.io) {
            val entity = dao.getById(photoId)
            dao.deleteById(photoId)
            if (entity != null) {
                tryDeleteLocalFile(entity.photoPath)
            }
        }
    }

    private fun tryDeleteLocalFile(photoPath: String) {
        val uri = runCatching { Uri.parse(photoPath) }.getOrNull() ?: return
        resolveLocalFile(uri)?.delete()
    }

    private fun resolveLocalFile(uri: Uri): File? {
        val scheme = uri.scheme
        val path = uri.path ?: return null
        return when {
            scheme == "file" -> File(path)
            scheme == "content" && isInternalFileProvider(uri) -> resolveFileProviderPath(path)
            else -> null
        }
    }

    private fun isInternalFileProvider(uri: Uri): Boolean =
        uri.authority?.endsWith(FILE_PROVIDER_AUTHORITY_SUFFIX) == true

    private fun resolveFileProviderPath(uriPath: String): File? {
        val parent = context.filesDir.parent ?: return null
        return File(parent, uriPath.removePrefix("/"))
    }
}
