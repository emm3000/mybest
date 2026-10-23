package com.emm.mybest.domain.models

/**
 * An app the user can open from the launcher, shown by its [label].
 */
data class InstalledApp(
    val packageName: String,
    val label: String,
)
