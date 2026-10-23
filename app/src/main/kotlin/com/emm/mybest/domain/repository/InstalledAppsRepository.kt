package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.InstalledApp

/**
 * Lists the apps installed on the device that the user can pick for notification capture.
 */
interface InstalledAppsRepository {
    /**
     * Returns the launcher apps sorted by label.
     */
    suspend fun list(): List<InstalledApp>
}
