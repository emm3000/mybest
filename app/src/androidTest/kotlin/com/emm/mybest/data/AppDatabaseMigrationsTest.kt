package com.emm.mybest.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

private const val TEST_DB = "migration-test-db"

class AppDatabaseMigrationsTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate_2_to_3_drops_habit_columns_and_tables() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                "INSERT INTO daily_weight (id, date, weight, habit_id, created_at, updated_at) " +
                    "VALUES ('w1', '2026-01-01', 70.0, NULL, 1000, 1000)",
            )
            execSQL(
                "INSERT INTO progress_photo " +
                    "(id, habit_record_id, habit_id, date, type, photo_path, created_at, updated_at) " +
                    "VALUES ('p1', NULL, NULL, '2026-01-01', 'FACE', '/tmp/p.jpg', 1000, 1000)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        val weightCursor = db.query("SELECT * FROM daily_weight")
        assertEquals(1, weightCursor.count)
        assertFalse(columnExists(weightCursor.columnNames, "habit_id"))
        weightCursor.close()

        val photoCursor = db.query("SELECT * FROM progress_photo")
        assertEquals(1, photoCursor.count)
        assertFalse(columnExists(photoCursor.columnNames, "habit_record_id"))
        assertFalse(columnExists(photoCursor.columnNames, "habit_id"))
        photoCursor.close()

        val tables = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name IN " +
                "('daily_habit','habit_records','habits')",
        )
        assertEquals(0, tables.count)
        tables.close()
    }

    @Test
    fun migrate_3_to_4_removes_meal_photo_types_and_unifies_trunk() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO progress_photo (id, date, type, photo_path, created_at, updated_at) " +
                    "VALUES ('meal1', '2026-01-02', 'BREAKFAST', '/tmp/meal.jpg', 2000, 2000)",
            )
            execSQL(
                "INSERT INTO progress_photo (id, date, type, photo_path, created_at, updated_at) " +
                    "VALUES ('body1', '2026-01-03', 'ABDOMEN', '/tmp/body.jpg', 2000, 2000)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

        val allCursor = db.query("SELECT id, type FROM progress_photo ORDER BY id")
        assertEquals(1, allCursor.count)
        allCursor.moveToFirst()
        val idIdx = allCursor.getColumnIndex("id")
        val typeIdx = allCursor.getColumnIndex("type")
        assertEquals("body1", allCursor.getString(idIdx))
        assertEquals("TRUNK", allCursor.getString(typeIdx))
        allCursor.close()
    }

    private fun columnExists(columnNames: Array<String>, name: String): Boolean =
        columnNames.any { it == name }
}
