package com.emm.mybest.data

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.domain.repository.RestoreResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

private object UnconfinedDispatchers : CoroutineDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Unconfined
    override val io: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Unconfined
}

private val KNOWN_DATE = LocalDate(2026, 3, 15)

class BackupRepositoryImplRestoreTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dbPath: File = context.getDatabasePath(AppDatabase.DB_NAME)

    @Test
    fun export_includes_rows_still_in_wal_and_restores_them() = runBlocking {
        deleteDatabaseFiles()
        val liveDb = openDatabase()
        liveDb.dailyWeightDao().upsert(DailyWeightEntity(id = "wal1", date = KNOWN_DATE, weight = 70.2f))
        assertTrue(File("${dbPath.path}-wal").length() > 0)

        val exportFile = File(context.cacheDir, "test-export.db").apply { delete() }
        val repo = BackupRepositoryImpl(context, liveDb, UnconfinedDispatchers)
        repo.exportDatabase(exportFile.toURI().toString()).getOrThrow()
        liveDb.close()

        deleteDatabaseFiles()
        val result = BackupRepositoryImpl(context, openDatabase(), UnconfinedDispatchers)
            .restoreDatabase(exportFile.toURI().toString())
        exportFile.delete()

        assertEquals(RestoreResult.RequiresRestart, result.getOrThrow())
        assertTrue(hasWeightAfterReopen("wal1"))
    }

    @Test
    fun restore_overwrites_database_and_signals_restart() = runBlocking {
        val backupFile = seedBackupFile()

        deleteDatabaseFiles()
        val repo = BackupRepositoryImpl(context, openDatabase(), UnconfinedDispatchers)
        val result = repo.restoreDatabase(backupFile.toURI().toString())
        backupFile.delete()

        assertEquals(RestoreResult.RequiresRestart, result.getOrThrow())
        assertTrue(hasWeightAfterReopen("seed1"))
    }

    @Test
    fun restore_rejects_non_sqlite_file_with_failure_result() = runBlocking {
        val fakeFile = File.createTempFile("not-sqlite-", ".db", context.cacheDir)
        fakeFile.writeBytes("not a database".toByteArray())

        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val repo = BackupRepositoryImpl(context, db, UnconfinedDispatchers)

        val result = repo.restoreDatabase(fakeFile.toURI().toString())

        db.close()
        fakeFile.delete()

        assertTrue(result.isFailure)
    }

    private suspend fun seedBackupFile(): File {
        val seedDb = openDatabase()
        seedDb.dailyWeightDao().upsert(DailyWeightEntity(id = "seed1", date = KNOWN_DATE, weight = 68.5f))
        seedDb.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        seedDb.close()

        val backupFile = File(context.cacheDir, "test-backup.db")
        dbPath.copyTo(backupFile, overwrite = true)
        return backupFile
    }

    private suspend fun hasWeightAfterReopen(id: String): Boolean {
        val reopened = openDatabase()
        val weights = reopened.dailyWeightDao().observeAllOrdered().first()
        reopened.close()
        return weights.any { it.id == id && it.date == KNOWN_DATE }
    }

    private fun openDatabase(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()

    private fun deleteDatabaseFiles() {
        dbPath.delete()
        File("${dbPath.path}-wal").delete()
        File("${dbPath.path}-shm").delete()
    }
}
