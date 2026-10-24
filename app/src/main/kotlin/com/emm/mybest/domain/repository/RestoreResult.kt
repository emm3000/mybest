package com.emm.mybest.domain.repository

sealed class RestoreResult {
    object RequiresRestart : RestoreResult()
    object OtherSchemaVersion : RestoreResult()
}
