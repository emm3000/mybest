package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RestoreDatabaseBackupUseCaseTest {

    private val backupRepository = mockk<BackupRepository>()
    private val useCase = RestoreDatabaseBackupUseCase(backupRepository)

    @Test
    fun `invoke delegates restore to repository with uri`() = runBlocking {
        val uri = "content://com.emm.mybest.backup/db"
        coEvery { backupRepository.restoreDatabase(uri) } returns Result.success(Unit)

        val result = useCase(uri)

        coVerify(exactly = 1) { backupRepository.restoreDatabase(uri) }
        assertEquals(true, result.isSuccess)
    }
}
