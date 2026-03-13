package com.emm.mybest.data

import android.content.Context
import android.net.Uri
import com.emm.mybest.domain.repository.BackupRepository
import java.io.FileInputStream

class BackupRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
) : BackupRepository {

    override suspend fun exportDatabase(targetUri: String): Result<Unit> = runCatching {
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

    private fun forceWalCheckpoint() {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
    }
}
