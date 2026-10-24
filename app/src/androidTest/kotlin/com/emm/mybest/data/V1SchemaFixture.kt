package com.emm.mybest.data

import androidx.sqlite.db.SupportSQLiteDatabase

internal val V1_TABLES = setOf(
    "user_setup", "daily_check", "outing", "won_night", "no_outing_answer", "debt", "debt_payment",
    "purchase_goal", "goal_contribution", "capture_app", "captured_notification", "daily_weight", "progress_photo",
)

internal val ONE_ROW_PER_V1_TABLE = listOf(
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

internal fun snapshotOf(database: SupportSQLiteDatabase): Map<String, List<List<String?>>> =
    userTablesOf(database).associateWith { table -> rowsOf(database, table) }

private fun userTablesOf(database: SupportSQLiteDatabase): Set<String> =
    database.query(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT IN " +
            "('android_metadata', 'room_master_table', 'sqlite_sequence')",
    ).use { cursor -> buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) } }

private fun rowsOf(database: SupportSQLiteDatabase, table: String): List<List<String?>> =
    database.query("SELECT * FROM $table").use { cursor ->
        buildList {
            while (cursor.moveToNext()) add((0 until cursor.columnCount).map { cursor.getString(it) })
        }
    }
