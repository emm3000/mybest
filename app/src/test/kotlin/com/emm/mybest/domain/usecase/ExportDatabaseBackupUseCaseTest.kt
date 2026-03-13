package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportDatabaseBackupUseCaseTest {

    private val backupRepository = mockk<BackupRepository>()
    private val useCase = ExportDatabaseBackupUseCase(backupRepository)

    @Test
    fun `invoke delegates export to repository with uri`() = runBlocking {
        val uri = "content://com.emm.mybest.backup/db"
        coEvery { backupRepository.exportDatabase(uri) } returns Result.success(Unit)

        val result = useCase(uri)

        coVerify(exactly = 1) { backupRepository.exportDatabase(uri) }
        assertEquals(true, result.isSuccess)
    }
}
