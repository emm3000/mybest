package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.emm.mybest.domain.models.PhotoType
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {

    @Insert
    suspend fun insertAll(photos: List<ProgressPhotoEntity>)

    @Query(
        """
        SELECT * FROM progress_photo
        WHERE type = :type
        ORDER BY date ASC
    """,
    )
    fun observeByType(type: PhotoType): Flow<List<ProgressPhotoEntity>>

    @Query(
        """
        SELECT * FROM progress_photo
        ORDER BY date DESC
    """,
    )
    fun observeAll(): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photo WHERE id = :photoId LIMIT 1")
    suspend fun getById(photoId: String): ProgressPhotoEntity?

    @Query("DELETE FROM progress_photo WHERE id = :photoId")
    suspend fun deleteById(photoId: String)
}
