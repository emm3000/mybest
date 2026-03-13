package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.repository.BackupRepository

class RestoreDatabaseBackupUseCase(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(sourceUri: String): Result<Unit> {
        return backupRepository.restoreDatabase(sourceUri)
    }
}
