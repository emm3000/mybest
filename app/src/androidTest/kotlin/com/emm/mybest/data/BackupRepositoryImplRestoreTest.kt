package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.domain.repository.RestoreResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
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

class BackupRepositoryImplRestoreTest {

    @Test
    fun restore_overwrites_database_and_signals_restart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbPath = context.getDatabasePath(AppDatabase.DB_NAME)

        val seedDb = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()

        val knownDate = LocalDate(2026, 3, 15)
        seedDb.dailyWeightDao().upsert(DailyWeightEntity(id = "seed1", date = knownDate, weight = 68.5f))
        seedDb.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
        seedDb.close()

        val backupFile = File(context.cacheDir, "test-backup.db")
        dbPath.copyTo(backupFile, overwrite = true)

        dbPath.delete()
        File("${dbPath.path}-wal").delete()
        File("${dbPath.path}-shm").delete()

        val freshDb = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()

        val repo = BackupRepositoryImpl(context, freshDb, UnconfinedDispatchers)
        val result = repo.restoreDatabase(backupFile.toURI().toString())

        assertEquals(RestoreResult.RequiresRestart, result.getOrThrow())

        val reopened = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()

        val weights = reopened.dailyWeightDao().observeAllOrdered().first()
        reopened.close()
        backupFile.delete()

        assertTrue(weights.any { it.id == "seed1" && it.date == knownDate })
    }

    @Test
    fun restore_rejects_non_sqlite_file_with_failure_result() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fakeFile = File.createTempFile("not-sqlite-", ".db", context.cacheDir)
        fakeFile.writeBytes("not a database".toByteArray())

        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val repo = BackupRepositoryImpl(context, db, UnconfinedDispatchers)

        val result = repo.restoreDatabase(fakeFile.toURI().toString())

        db.close()
        fakeFile.delete()

        assertTrue(result.isFailure)
    }
}
