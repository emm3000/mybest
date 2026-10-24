package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.CapturedNotification
import com.emm.mybest.domain.models.PostedNotification
import kotlinx.coroutines.flow.Flow

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
     * Stores [notification] with its title and text exactly as given.
     */
    suspend fun saveCaptured(notification: PostedNotification)

    /**
     * Removes every captured notification.
     */
    suspend fun deleteAllCaptured()
}
