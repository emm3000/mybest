package com.emm.mybest.domain.usecase.capture

import com.emm.mybest.domain.models.CaptureAppOption
import com.emm.mybest.domain.models.CaptureOverview
import com.emm.mybest.domain.models.CaptureSummary
import com.emm.mybest.domain.models.CapturedNotification
import com.emm.mybest.domain.models.CapturedNotificationEntry
import com.emm.mybest.domain.models.InstalledApp
import com.emm.mybest.domain.repository.InstalledAppsRepository
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class ObserveCaptureOverviewUseCase(
    private val installedApps: InstalledAppsRepository,
    private val repository: NotificationCaptureRepository,
) {

    /**
     * Emits the installed apps with their selection and the captured notifications with their app label, newest first.
     * When the installed apps cannot be listed, the apps list is empty and each notification shows its package name.
     */
    operator fun invoke(): Flow<CaptureOverview> = flow {
        val apps = listInstalledApps()
        emitAll(
            combine(repository.observeSelectedApps(), repository.observeCaptured()) { selected, captured ->
                CaptureOverview(
                    apps = apps.map { CaptureAppOption(it, it.packageName in selected) },
                    entries = captured.map { it.withAppLabel(apps) },
                    summary = CaptureSummary(selected.size),
                )
            },
        )
    }

    private suspend fun listInstalledApps(): List<InstalledApp> =
        try {
            installedApps.list()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            emptyList()
        }
}

private fun CapturedNotification.withAppLabel(apps: List<InstalledApp>) = CapturedNotificationEntry(
    notification = this,
    appLabel = apps.firstOrNull { it.packageName == packageName }?.label ?: packageName,
)
