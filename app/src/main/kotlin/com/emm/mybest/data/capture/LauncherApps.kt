package com.emm.mybest.data.capture

import com.emm.mybest.domain.models.InstalledApp

internal fun List<InstalledApp>.sortedByLabel(): List<InstalledApp> =
    distinctBy { it.packageName }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
