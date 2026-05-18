package com.emm.mybest.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Delete meal-photo entries (BREAKFAST, LUNCH, DINNER, FOOD) — obsolete after
        //    diet moved to a checkbox-based compliance model.
        db.execSQL("DELETE FROM progress_photo WHERE type NOT IN ('FACE', 'ABDOMEN', 'BODY', 'TRUNK')")
        // 2) Unify ABDOMEN and BODY into TRUNK.
        db.execSQL("UPDATE progress_photo SET type = 'TRUNK' WHERE type IN ('ABDOMEN', 'BODY')")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Recreate progress_photo without habit_record_id FK, habit_record_id column,
        //    habit_id column, and without the indices for those columns.
        db.execSQL(
            """
            CREATE TABLE progress_photo_new (
                `id` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `photo_path` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO progress_photo_new (id, date, type, photo_path, created_at, updated_at)
            SELECT id, date, type, photo_path, created_at, updated_at FROM progress_photo
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE progress_photo")
        db.execSQL("ALTER TABLE progress_photo_new RENAME TO progress_photo")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_progress_photo_type` ON `progress_photo` (`type`)",
        )

        // 2) Recreate daily_weight without habit_id column and its index.
        db.execSQL(
            """
            CREATE TABLE daily_weight_new (
                `id` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `weight` REAL NOT NULL,
                `photo_path` TEXT,
                `note` TEXT,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO daily_weight_new (id, date, weight, photo_path, note, created_at, updated_at)
            SELECT id, date, weight, photo_path, note, created_at, updated_at FROM daily_weight
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE daily_weight")
        db.execSQL("ALTER TABLE daily_weight_new RENAME TO daily_weight")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_weight_date` ON `daily_weight` (`date`)",
        )

        // 3) Drop habit tables in safe dependency order.
        db.execSQL("DROP TABLE IF EXISTS daily_habit")
        db.execSQL("DROP TABLE IF EXISTS habit_records")
        db.execSQL("DROP TABLE IF EXISTS habits")
    }
}
