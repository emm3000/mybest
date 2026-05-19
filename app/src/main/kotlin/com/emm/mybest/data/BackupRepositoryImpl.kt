package com.emm.mybest.data

import android.content.Context
import android.net.Uri
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.domain.repository.BackupRepository
import com.emm.mybest.domain.repository.RestoreResult
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
    private val dispatchers: CoroutineDispatchers,
) : BackupRepository {

    override suspend fun exportDatabase(targetUri: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            val uri = Uri.parse(targetUri)
            forceWalCheckpoint()

            val databaseFile = context.getDatabasePath(AppDatabase.DB_NAME)
            check(databaseFile.exists()) { "Database file not found" }

            context.contentResolver.openOutputStream(uri).use { output ->
                checkNotNull(output) { "Unable to open output stream for backup" }
                FileInputStream(databaseFile).use { input ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun forceWalCheckpoint() {
        database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
    }

    override suspend fun restoreDatabase(sourceUri: String): Result<RestoreResult> = runCatching {
        withContext(dispatchers.io) {
            val uri = Uri.parse(sourceUri)
            val tempFile = File.createTempFile("mybest-restore-", ".db", context.cacheDir)

            context.contentResolver.openInputStream(uri).use { input ->
                checkNotNull(input) { "Unable to open input stream for restore" }
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            validateBackupFile(tempFile)

            val databaseFile = context.getDatabasePath(AppDatabase.DB_NAME)
            database.close()
            databaseFile.parentFile?.mkdirs()
            tempFile.copyTo(databaseFile, overwrite = true)

            File("${databaseFile.path}-wal").delete()
            File("${databaseFile.path}-shm").delete()

            tempFile.delete()

            RestoreResult.RequiresRestart
        }
    }

    private fun validateBackupFile(file: File) {
        check(file.length() > 0) { "Backup file is empty" }
        FileInputStream(file).use { input ->
            val header = ByteArray(SQLITE_HEADER_LENGTH)
            val read = input.read(header)
            check(isValidSqliteHeader(header, read)) { "Invalid SQLite backup file" }
        }
    }
}

internal fun isValidSqliteHeader(headerBytes: ByteArray, bytesRead: Int): Boolean {
    if (bytesRead != SQLITE_HEADER_LENGTH) return false
    val headerText = headerBytes.toString(Charsets.UTF_8)
    return headerText.startsWith(SQLITE_HEADER_PREFIX)
}

private const val SQLITE_HEADER_LENGTH = 16
private const val SQLITE_HEADER_PREFIX = "SQLite format 3"
