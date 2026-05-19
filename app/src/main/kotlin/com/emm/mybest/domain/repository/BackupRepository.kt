package com.emm.mybest.domain.repository

sealed class RestoreResult {
    object RequiresRestart : RestoreResult()
}

interface BackupRepository {
    suspend fun exportDatabase(targetUri: String): Result<Unit>
    suspend fun restoreDatabase(sourceUri: String): Result<RestoreResult>
}
