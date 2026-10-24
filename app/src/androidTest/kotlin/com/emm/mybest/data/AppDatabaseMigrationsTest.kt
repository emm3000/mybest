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
private const val PRE_V1_WEIGHT_ROW =
    "INSERT INTO daily_weight VALUES ('w1', '2026-01-01', 70.0, NULL, NULL, 1000, 1000)"

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
        assertEquals(seeded, reopened)
    }

    @Test
    fun pre_v1_database_is_recreated_empty_when_opened_by_the_app() {
        helper.createDatabase(TEST_DB, PRE_V1_SCHEMA_VERSION).use { db -> db.execSQL(PRE_V1_WEIGHT_ROW) }

        val opened = openWithAppMigrations()
        val version = opened.openHelper.readableDatabase.version
        val recreated = snapshotOf(opened.openHelper.readableDatabase)
        opened.close()

        assertEquals(AppDatabase.SCHEMA_VERSION, version)
        assertEquals(V1_TABLES, recreated.keys)
        assertTrue(recreated.values.all { it.isEmpty() })
    }

    private fun openWithAppMigrations(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .withAppMigrations()
            .build()
}
