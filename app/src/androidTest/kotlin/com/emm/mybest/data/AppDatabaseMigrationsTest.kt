package com.emm.mybest.data

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val TEST_DB = "migration-test-db"
private const val PRE_V1_SCHEMA_VERSION = 5
private const val V10_SCHEMA_VERSION = 10
private const val V11_SCHEMA_VERSION = 11
private const val V12_SCHEMA_VERSION = 12
private const val CAPTURED_NOTIFICATION = "captured_notification"
private const val NOTE = "note"
private const val PRE_V1_WEIGHT_ROW =
    "INSERT INTO daily_weight VALUES ('w1', '2026-01-01', 70.0, NULL, NULL, 1000, 1000)"
private const val V10_CAPTURED_ROW =
    "INSERT INTO captured_notification (id, package_name, title, text, posted_at_epoch_ms) " +
        "VALUES (1, 'com.bcp.innovacxion.yapeapp', ' Yape ', 'Te yapearon S/ 20\n', 1773600000000)"

class AppDatabaseMigrationsTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @After
    fun deleteTestDatabase() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun v1_database_keeps_one_row_of_every_table_when_opened_by_the_app() {
        val seeded = helper.createDatabase(TEST_DB, AppDatabase.V1_SCHEMA_VERSION).use { db ->
            ONE_ROW_PER_V1_TABLE.forEach { db.execSQL(it) }
            snapshotOf(db)
        }

        val opened = openWithAppMigrations()
        val reopened = snapshotOf(opened.openHelper.readableDatabase)
        opened.close()

        assertEquals(V1_TABLES, seeded.keys)
        assertTrue(seeded.values.all { it.size == 1 })
        assertEquals(seeded, reopened.minus(NOTE).withoutKeyColumn())
        assertTrue(reopened.getValue(CAPTURED_NOTIFICATION).all { it.last() == null })
        assertTrue(reopened.getValue(NOTE).isEmpty())
    }

    @Test
    fun v10_captured_notification_keeps_every_column_and_gets_a_null_key() {
        val seeded = helper.createDatabase(TEST_DB, V10_SCHEMA_VERSION).use { db ->
            db.execSQL(V10_CAPTURED_ROW)
            rowsOf(db, CAPTURED_NOTIFICATION)
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, V11_SCHEMA_VERSION, true, MIGRATION_10_11).use { db ->
            rowsOf(db, CAPTURED_NOTIFICATION)
        }

        assertEquals(seeded, migrated.map { it.dropLast(1) })
        assertTrue(migrated.all { it.last() == null })
    }

    @Test
    fun v11_database_keeps_one_row_of_every_table_and_gets_an_empty_note_table() {
        val seeded = helper.createDatabase(TEST_DB, V11_SCHEMA_VERSION).use { db ->
            ONE_ROW_PER_V1_TABLE.forEach { db.execSQL(it) }
            snapshotOf(db)
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, V12_SCHEMA_VERSION, true, MIGRATION_11_12).use { db ->
            snapshotOf(db)
        }

        assertEquals(V1_TABLES, seeded.keys)
        assertTrue(seeded.values.all { it.size == 1 })
        assertEquals(seeded, migrated.minus(NOTE))
        assertEquals(emptyList<List<String?>>(), migrated.getValue(NOTE))
    }

    @Test
    fun pre_v1_database_is_recreated_empty_when_opened_by_the_app() {
        helper.createDatabase(TEST_DB, PRE_V1_SCHEMA_VERSION).use { db -> db.execSQL(PRE_V1_WEIGHT_ROW) }

        val opened = openWithAppMigrations()
        val version = opened.openHelper.readableDatabase.version
        val recreated = snapshotOf(opened.openHelper.readableDatabase)
        opened.close()

        assertEquals(AppDatabase.SCHEMA_VERSION, version)
        assertEquals(CURRENT_TABLES, recreated.keys)
        assertTrue(recreated.values.all { it.isEmpty() })
    }

    private fun openWithAppMigrations(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .withAppMigrations()
            .build()
}

private fun Map<String, List<List<String?>>>.withoutKeyColumn(): Map<String, List<List<String?>>> =
    mapValues { (table, rows) -> if (table == CAPTURED_NOTIFICATION) rows.map { it.dropLast(1) } else rows }
