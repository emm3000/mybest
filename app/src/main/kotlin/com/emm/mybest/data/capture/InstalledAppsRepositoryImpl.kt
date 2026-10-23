package com.emm.mybest.data.capture

import android.content.Intent
import android.content.pm.PackageManager
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.domain.models.InstalledApp
import com.emm.mybest.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.withContext

class InstalledAppsRepositoryImpl(
    private val packageManager: PackageManager,
    private val dispatchers: CoroutineDispatchers,
) : InstalledAppsRepository {

    override suspend fun list(): List<InstalledApp> = withContext(dispatchers.io) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(launcherIntent, 0)
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(packageManager).toString()) }
            .sortedByLabel()
    }
}
