package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.CapturedNotification
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Stores the apps the user picked for capture and the raw notifications captured from them.
 */
interface NotificationCaptureRepository {
    /**
     * Emits the package names of the apps selected for capture; empty means capture is off.
     */
    fun observeSelectedApps(): Flow<Set<String>>

    /**
     * Adds or removes the app with [packageName] from the selected apps.
     */
    suspend fun setAppSelected(packageName: String, isSelected: Boolean)

    /**
     * Deselects every app, which turns capture off.
     */
    suspend fun clearSelectedApps()

    /**
     * Emits every captured notification, newest first.
     */
    fun observeCaptured(): Flow<List<CapturedNotification>>

    /**
     * Stores a notification from [packageName] with [title] and [text] exactly as given.
     */
    suspend fun saveCaptured(packageName: String, title: String?, text: String?, postedAt: Instant)

    /**
     * Removes every captured notification.
     */
    suspend fun deleteAllCaptured()
}
