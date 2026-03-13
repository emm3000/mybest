package com.emm.mybest.domain.repository

interface BackupRepository {
    suspend fun exportDatabase(targetUri: String): Result<Unit>
}
