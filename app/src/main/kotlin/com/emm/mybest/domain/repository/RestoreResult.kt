package com.emm.mybest.domain.repository

sealed class RestoreResult {
    object RequiresRestart : RestoreResult()
    object OtherSchemaVersion : RestoreResult()

    /**
     * The live database was closed, or closing it failed, but the backup did not replace it. The original
     * file is kept, and the app must restart because the closed database cannot be reopened in this process.
     */
    object NotRestoredRequiresRestart : RestoreResult()
}
