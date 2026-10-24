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
import java.nio.ByteBuffer

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
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
    }

    override suspend fun restoreDatabase(sourceUri: String): Result<RestoreResult> = runCatching {
        withContext(dispatchers.io) {
            val backupCopy = File.createTempFile("mybest-restore-", ".db", context.cacheDir)
            try {
                copySource(sourceUri, backupCopy)
                restoreFrom(backupCopy)
            } finally {
                backupCopy.delete()
            }
        }
    }

    private fun copySource(sourceUri: String, target: File) {
        context.contentResolver.openInputStream(Uri.parse(sourceUri)).use { input ->
            checkNotNull(input) { "Unable to open input stream for restore" }
            FileOutputStream(target).use { output -> input.copyTo(output) }
        }
    }

    private fun restoreFrom(backupCopy: File): RestoreResult = when (readHeaderCheck(backupCopy)) {
        BackupHeaderCheck.NOT_SQLITE -> error("Invalid SQLite backup file")
        BackupHeaderCheck.OTHER_VERSION -> RestoreResult.OtherSchemaVersion
        BackupHeaderCheck.SAME_VERSION -> {
            replaceDatabaseWith(backupCopy)
            RestoreResult.RequiresRestart
        }
    }

    private fun readHeaderCheck(file: File): BackupHeaderCheck = FileInputStream(file).use { input ->
        val header = ByteArray(SQLITE_HEADER_LENGTH)
        checkBackupHeader(header, input.read(header), AppDatabase.SCHEMA_VERSION)
    }

    private fun replaceDatabaseWith(backupCopy: File) {
        val databaseFile = context.getDatabasePath(AppDatabase.DB_NAME)
        database.close()
        databaseFile.parentFile?.mkdirs()
        backupCopy.copyTo(databaseFile, overwrite = true)
        File("${databaseFile.path}-wal").delete()
        File("${databaseFile.path}-shm").delete()
    }
}

internal enum class BackupHeaderCheck { SAME_VERSION, OTHER_VERSION, NOT_SQLITE }

internal fun checkBackupHeader(headerBytes: ByteArray, bytesRead: Int, expectedVersion: Int): BackupHeaderCheck =
    when {
        bytesRead < SQLITE_USER_VERSION_END || !hasSqlitePrefix(headerBytes) -> BackupHeaderCheck.NOT_SQLITE
        readUserVersion(headerBytes) != expectedVersion -> BackupHeaderCheck.OTHER_VERSION
        else -> BackupHeaderCheck.SAME_VERSION
    }

private fun hasSqlitePrefix(headerBytes: ByteArray): Boolean =
    headerBytes.decodeToString(0, SQLITE_HEADER_PREFIX.length) == SQLITE_HEADER_PREFIX

private fun readUserVersion(headerBytes: ByteArray): Int =
    ByteBuffer.wrap(headerBytes, SQLITE_USER_VERSION_OFFSET, Int.SIZE_BYTES).int

private const val SQLITE_HEADER_LENGTH = 100
private const val SQLITE_HEADER_PREFIX = "SQLite format 3"
private const val SQLITE_USER_VERSION_OFFSET = 60
private const val SQLITE_USER_VERSION_END = SQLITE_USER_VERSION_OFFSET + Int.SIZE_BYTES
