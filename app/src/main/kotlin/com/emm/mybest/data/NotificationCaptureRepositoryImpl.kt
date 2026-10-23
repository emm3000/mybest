package com.emm.mybest.data

import com.emm.mybest.data.entities.CaptureAppEntity
import com.emm.mybest.data.entities.CapturedNotificationEntity
import com.emm.mybest.data.entities.NotificationCaptureDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.domain.models.CapturedNotification
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class NotificationCaptureRepositoryImpl(
    private val notificationCaptureDao: NotificationCaptureDao,
) : NotificationCaptureRepository {

    override fun observeSelectedApps(): Flow<Set<String>> =
        notificationCaptureDao.observeAppPackageNames().map { it.toSet() }

    override suspend fun setAppSelected(packageName: String, isSelected: Boolean) {
        if (isSelected) {
            notificationCaptureDao.insertApp(CaptureAppEntity(packageName))
        } else {
            notificationCaptureDao.deleteApp(packageName)
        }
    }

    override suspend fun clearSelectedApps() {
        notificationCaptureDao.deleteAllApps()
    }

    override fun observeCaptured(): Flow<List<CapturedNotification>> =
        notificationCaptureDao.observeCaptured().map { list -> list.map { it.toDomain() } }

    override suspend fun saveCaptured(packageName: String, title: String?, text: String?, postedAt: Instant) {
        notificationCaptureDao.insertCaptured(
            CapturedNotificationEntity(
                packageName = packageName,
                title = title,
                text = text,
                postedAtEpochMs = postedAt.toEpochMilliseconds(),
            ),
        )
    }

    override suspend fun deleteAllCaptured() {
        notificationCaptureDao.deleteAllCaptured()
    }
}
