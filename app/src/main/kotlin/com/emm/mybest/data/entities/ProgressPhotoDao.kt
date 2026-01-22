package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {

    @Insert
    suspend fun insert(photo: ProgressPhotoEntity)

    @Insert
    suspend fun insertAll(photos: List<ProgressPhotoEntity>)

    @Query("""
        SELECT * FROM progress_photo
        WHERE type = :type
        ORDER BY date ASC
    """)
    fun observeByType(type: PhotoType): Flow<List<ProgressPhotoEntity>>

    @Query("""
        SELECT * FROM progress_photo
        ORDER BY date DESC
    """)
    fun observeAll(): Flow<List<ProgressPhotoEntity>>

    @Query("""
        SELECT * FROM progress_photo
        WHERE type = :type
        ORDER BY date ASC
        LIMIT 1
    """)
    suspend fun getFirstByType(type: PhotoType): ProgressPhotoEntity?

    @Query("""
        SELECT * FROM progress_photo
        WHERE type = :type
        ORDER BY date DESC
        LIMIT 1
    """)
    suspend fun getLastByType(type: PhotoType): ProgressPhotoEntity?

    @Query("DELETE FROM progress_photo WHERE id = :photoId")
    suspend fun deleteById(photoId: String)
}
