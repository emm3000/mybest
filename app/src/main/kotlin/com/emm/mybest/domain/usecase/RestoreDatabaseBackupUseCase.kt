package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.repository.BackupRepository
import com.emm.mybest.domain.repository.RestoreResult

class RestoreDatabaseBackupUseCase(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(sourceUri: String): Result<RestoreResult> {
        return backupRepository.restoreDatabase(sourceUri)
    }
}
