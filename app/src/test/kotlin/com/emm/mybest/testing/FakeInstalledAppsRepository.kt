package com.emm.mybest.testing

import com.emm.mybest.domain.models.InstalledApp
import com.emm.mybest.domain.repository.InstalledAppsRepository

class FakeInstalledAppsRepository(
    private val apps: List<InstalledApp> = emptyList(),
) : InstalledAppsRepository {

    override suspend fun list(): List<InstalledApp> = apps
}
