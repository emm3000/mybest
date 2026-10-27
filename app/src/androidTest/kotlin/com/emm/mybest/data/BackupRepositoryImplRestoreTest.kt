package com.emm.mybest.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

private object UnconfinedDispatchers : CoroutineDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Unconfined
    override val io: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Unconfined
}

private val KNOWN_DATE = LocalDate(2026, 3, 15)
private const val SQLITE_USER_VERSION_OFFSET = 60L
private const val FAIL_AFTER_BYTES = 4096
private val LIVE_DATABASE_FILES = setOf(AppDatabase.DB_NAME, "${AppDatabase.DB_NAME}-wal", "${AppDatabase.DB_NAME}-shm")
private const val READ_SCHEMA_SQL = "SELECT COUNT(*) FROM sqlite_master"
private const val WRONG_IDENTITY_HASH_SQL = "UPDATE room_master_table SET identity_hash = 'deadbeef' WHERE id = 42"

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
    fun restore_refuses_a_backup_with_another_identity_hash_and_keeps_the_live_database_open() = runBlocking {
        val backupFile = seedBackupFile().apply { executeSql(WRONG_IDENTITY_HASH_SQL) }

        assertRestoreIsRefusedAndLiveDatabaseKept(backupFile)
    }

    @Test
    fun restore_refuses_a_foreign_sqlite_file_at_the_same_user_version() = runBlocking {
        val foreignFile = File(context.cacheDir, "test-foreign.db").apply { deleteWithSideFiles() }
        SQLiteDatabase.openOrCreateDatabase(foreignFile, null).use { foreign ->
            foreign.execSQL("CREATE TABLE t(x)")
            foreign.version = AppDatabase.SCHEMA_VERSION
        }

        assertRestoreIsRefusedAndLiveDatabaseKept(foreignFile)
    }

    @Test
    fun a_backup_stream_that_fails_mid_copy_leaves_the_live_database_untouched() = runBlocking {
        val backupFile = seedBackupFile()
        deleteDatabaseFiles()
        val liveDb = openDatabase()
        liveDb.dailyWeightDao().upsert(DailyWeightEntity(id = "live1", date = KNOWN_DATE, weight = 71.0f))
        liveDb.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

        FailingInputStream(FileInputStream(backupFile), FAIL_AFTER_BYTES).use { source ->
            assertThrows(IOException::class.java) { replaceFileAtomically(source, dbPath) { liveDb.close() } }
        }
        backupFile.delete()

        assertTrue(liveDb.isOpen)
        assertTrue(LIVE_DATABASE_FILES.containsAll(dbPath.parentFile?.list().orEmpty().toSet()))
        assertTrue(liveDb.dailyWeightDao().observeAllOrdered().first().any { it.id == "live1" })
        liveDb.close()
        assertTrue(hasWeightAfterReopen("live1"))
    }

    @Test
    fun a_restore_whose_move_fails_after_closing_the_database_requires_a_restart_and_keeps_wal_rows() = runBlocking {
        val backupFile = seedBackupFile()
        deleteDatabaseFiles()
        val liveDb = openDatabase()
        liveDb.dailyWeightDao().upsert(DailyWeightEntity(id = "live1", date = KNOWN_DATE, weight = 71.0f))
        val reader = SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READONLY)
        reader.rawQuery(READ_SCHEMA_SQL, null).use { it.moveToFirst() }

        val result = BackupRepositoryImpl(context, liveDb, UnconfinedDispatchers, moveFile = ::failMove)
            .restoreDatabase(backupFile.toURI().toString())
        val walBytesAfterRestore = File("${dbPath.path}-wal").length()
        reader.close()
        backupFile.delete()

        assertEquals(RestoreResult.NotRestoredRequiresRestart, result.getOrThrow())
        assertFalse(liveDb.isOpen)
        assertTrue(hasWeightAfterReopen("live1"))
        assertFalse(hasWeightAfterReopen("seed1"))
        assertTrue(walBytesAfterRestore > 0)
    }

    @Test
    fun after_a_restore_whose_move_fails_the_restarted_database_accepts_writes_and_leaves_no_staged_files() =
        runBlocking {
            val backupFile = seedBackupFile()
            deleteDatabaseFiles()
            val liveDb = openDatabase()
            liveDb.dailyWeightDao().upsert(DailyWeightEntity(id = "live1", date = KNOWN_DATE, weight = 71.0f))

            BackupRepositoryImpl(context, liveDb, UnconfinedDispatchers, moveFile = ::failMove)
                .restoreDatabase(backupFile.toURI().toString())
            backupFile.delete()
            val restarted = openDatabase()
            restarted.dailyWeightDao().upsert(DailyWeightEntity(id = "after1", date = KNOWN_DATE, weight = 72.0f))
            restarted.close()

            assertTrue(hasWeightAfterReopen("after1"))
            assertTrue(dbPath.parentFile?.list().orEmpty().none { it.endsWith(".restore") || it.endsWith(".aside") })
        }

    @Test
    fun export_and_restore_keep_one_row_of_every_v1_table() = runBlocking {
        deleteDatabaseFiles()
        val liveDb = openDatabase()
        ONE_ROW_PER_V1_TABLE.forEach { liveDb.openHelper.writableDatabase.execSQL(it) }
        val seeded = snapshotOf(liveDb.openHelper.readableDatabase)
        val exportFile = File(context.cacheDir, "test-round-trip.db").apply { delete() }
        BackupRepositoryImpl(context, liveDb, UnconfinedDispatchers).exportDatabase(exportFile.toURI().toString()).getOrThrow()
        liveDb.close()

        deleteDatabaseFiles()
        BackupRepositoryImpl(context, openDatabase(), UnconfinedDispatchers)
            .restoreDatabase(exportFile.toURI().toString()).getOrThrow()
        exportFile.delete()
        val reopened = openDatabase()
        val restored = snapshotOf(reopened.openHelper.readableDatabase)
        reopened.close()

        assertEquals(V1_TABLES, seeded.keys)
        assertTrue(seeded.values.all { it.size == 1 })
        assertEquals(seeded, restored)
    }

    private suspend fun assertRestoreIsRefusedAndLiveDatabaseKept(backupFile: File) {
        deleteDatabaseFiles()
        val liveDb = openDatabase()
        liveDb.dailyWeightDao().upsert(DailyWeightEntity(id = "live1", date = KNOWN_DATE, weight = 71.0f))

        val result = BackupRepositoryImpl(context, liveDb, UnconfinedDispatchers)
            .restoreDatabase(backupFile.toURI().toString())
        backupFile.deleteWithSideFiles()

        assertEquals(RestoreResult.OtherSchemaVersion, result.getOrThrow())
        assertTrue(liveDb.dailyWeightDao().observeAllOrdered().first().any { it.id == "live1" })
        liveDb.close()
        assertTrue(hasWeightAfterReopen("live1"))
        assertFalse(hasWeightAfterReopen("seed1"))
    }

    private fun File.executeSql(sql: String) {
        SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE).use { it.execSQL(sql) }
        deleteSideFiles()
    }

    private fun File.deleteWithSideFiles() {
        delete()
        deleteSideFiles()
    }

    private fun File.deleteSideFiles() {
        listOf("-wal", "-shm", "-journal").forEach { File("$path$it").delete() }
    }

    private fun File.writeUserVersion(version: Int) {
        RandomAccessFile(this, "rw").use { file ->
            file.seek(SQLITE_USER_VERSION_OFFSET)
            file.writeInt(version)
        }
    }

    private suspend fun seedBackupFile(): File {
        deleteDatabaseFiles()
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
            .withAppMigrations()
            .build()

    private fun deleteDatabaseFiles() {
        dbPath.delete()
        File("${dbPath.path}-wal").delete()
        File("${dbPath.path}-shm").delete()
    }
}

private fun failMove(staged: File, target: File) {
    throw IOException("Simulated failure moving ${staged.name} to ${target.name}")
}

private class FailingInputStream(private val source: InputStream, private val failAfter: Int) : InputStream() {
    private var bytesRead = 0

    override fun read(): Int {
        if (bytesRead >= failAfter) throw IOException("Simulated failure after $failAfter bytes")
        bytesRead++
        return source.read()
    }

    override fun close() = source.close()
}
