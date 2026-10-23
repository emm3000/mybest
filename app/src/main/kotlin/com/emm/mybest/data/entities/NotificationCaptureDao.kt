package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationCaptureDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApp(app: CaptureAppEntity)

    @Query("DELETE FROM capture_app WHERE package_name = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("DELETE FROM capture_app")
    suspend fun deleteAllApps()

    @Query("SELECT package_name FROM capture_app")
    fun observeAppPackageNames(): Flow<List<String>>

    @Insert
    suspend fun insertCaptured(notification: CapturedNotificationEntity): Long

    @Query("SELECT * FROM captured_notification ORDER BY posted_at_epoch_ms DESC, id DESC")
    fun observeCaptured(): Flow<List<CapturedNotificationEntity>>

    @Query("DELETE FROM captured_notification")
    suspend fun deleteAllCaptured()
}
