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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.RandomAccessFile

private object UnconfinedDispatchers : CoroutineDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Unconfined
    override val io: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Unconfined
}

private val KNOWN_DATE = LocalDate(2026, 3, 15)
private const val SQLITE_USER_VERSION_OFFSET = 60L

private val V1_TABLES = setOf(
    "user_setup", "daily_check", "outing", "won_night", "no_outing_answer", "debt", "debt_payment",
    "purchase_goal", "goal_contribution", "capture_app", "captured_notification", "daily_weight", "progress_photo",
)

private val ONE_ROW_PER_V1_TABLE = listOf(
    "INSERT INTO user_setup VALUES (1, 72.5, 4, 8000, 3, 5, 2, 6, 120, 1380, 1, 480, 1, 540)",
    "INSERT INTO daily_check VALUES ('2026-03-15', 'GYM', 1)",
    "INSERT INTO outing VALUES (1, '2026-03-14', 'PLANNED', 10000, 4, 120, 8500, 3, 90, 4)",
    "INSERT INTO won_night VALUES ('2026-03-13', 6000)",
    "INSERT INTO no_outing_answer VALUES ('2026-03-12')",
    "INSERT INTO debt VALUES (1, 'Tarjeta', 320000, 42000, 15)",
    "INSERT INTO debt_payment VALUES (1, '2026-03', 42000)",
    "INSERT INTO purchase_goal VALUES (1, 'Bicicleta', 150000)",
    "INSERT INTO goal_contribution VALUES (1, '2026-03-15', 5000)",
    "INSERT INTO capture_app VALUES ('com.bcp.innovacxion.yapeapp')",
    "INSERT INTO captured_notification VALUES (1, 'com.bcp.innovacxion.yapeapp', 'Yape', 'Te yapearon S/ 20', 1773600000000)",
    "INSERT INTO daily_weight VALUES ('w1', '2026-03-15', 71.4, 'photos/w1.jpg', 'ayuno', 1773600000000, 1773600000001)",
    "INSERT INTO progress_photo VALUES ('p1', '2026-03-15', 'TRUNK', 'photos/p1.jpg', 1773600000000, 1773600000001)",
)

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

    @Test
    fun restore_refuses_a_backup_from_another_schema_version_and_keeps_the_current_database() = runBlocking {
        val backupFile = seedBackupFile().apply { writeUserVersion(AppDatabase.SCHEMA_VERSION - 1) }
        deleteDatabaseFiles()
        val liveDb = openDatabase()
        liveDb.dailyWeightDao().upsert(DailyWeightEntity(id = "live1", date = KNOWN_DATE, weight = 71.0f))

        val result = BackupRepositoryImpl(context, liveDb, UnconfinedDispatchers)
            .restoreDatabase(backupFile.toURI().toString())
        liveDb.close()
        backupFile.delete()

        assertEquals(RestoreResult.OtherSchemaVersion, result.getOrThrow())
        assertTrue(hasWeightAfterReopen("live1"))
        assertFalse(hasWeightAfterReopen("seed1"))
    }

    @Test
    fun export_and_restore_keep_one_row_of_every_v1_table() = runBlocking {
        deleteDatabaseFiles()
        val liveDb = openDatabase()
        ONE_ROW_PER_V1_TABLE.forEach { liveDb.openHelper.writableDatabase.execSQL(it) }
        val seeded = snapshotOf(liveDb)
        val exportFile = File(context.cacheDir, "test-round-trip.db").apply { delete() }
        BackupRepositoryImpl(context, liveDb, UnconfinedDispatchers).exportDatabase(exportFile.toURI().toString()).getOrThrow()
        liveDb.close()

        deleteDatabaseFiles()
        BackupRepositoryImpl(context, openDatabase(), UnconfinedDispatchers)
            .restoreDatabase(exportFile.toURI().toString()).getOrThrow()
        exportFile.delete()
        val reopened = openDatabase()
        val restored = snapshotOf(reopened)
        reopened.close()

        assertEquals(V1_TABLES, seeded.keys)
        assertTrue(seeded.values.all { it.size == 1 })
        assertEquals(seeded, restored)
    }

    private fun snapshotOf(database: AppDatabase): Map<String, List<List<String?>>> =
        userTablesOf(database).associateWith { table -> rowsOf(database, table) }

    private fun userTablesOf(database: AppDatabase): Set<String> =
        database.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT IN " +
                "('android_metadata', 'room_master_table', 'sqlite_sequence')",
        ).use { cursor -> buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) } }

    private fun rowsOf(database: AppDatabase, table: String): List<List<String?>> =
        database.openHelper.readableDatabase.query("SELECT * FROM $table").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add((0 until cursor.columnCount).map { cursor.getString(it) })
            }
        }

    private fun File.writeUserVersion(version: Int) {
        RandomAccessFile(this, "rw").use { file ->
            file.seek(SQLITE_USER_VERSION_OFFSET)
            file.writeInt(version)
        }
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
