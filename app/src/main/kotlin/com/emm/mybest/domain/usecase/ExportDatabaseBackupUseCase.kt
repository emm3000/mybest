package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.repository.BackupRepository

class ExportDatabaseBackupUseCase(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(targetUri: String): Result<Unit> {
        return backupRepository.exportDatabase(targetUri)
    }
}
