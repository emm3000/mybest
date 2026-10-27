package com.emm.mybest.data

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val FIRST_SCHEMA_VERSION = 1

internal val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE captured_notification ADD COLUMN notification_key TEXT")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_captured_notification_notification_key " +
                "ON captured_notification (notification_key)",
        )
    }
}

internal val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`app_day` TEXT NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, `text` TEXT NOT NULL)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_app_day` ON `note` (`app_day`)")
    }
}

/** Every migration from [AppDatabase.V1_SCHEMA_VERSION] up to [AppDatabase.SCHEMA_VERSION], in order. */
val ALL_MIGRATIONS: List<Migration> = listOf(MIGRATION_10_11, MIGRATION_11_12)

private val PRE_V1_SCHEMA_VERSIONS = FIRST_SCHEMA_VERSION until AppDatabase.V1_SCHEMA_VERSION

/**
 * Applies the app's migration policy: [ALL_MIGRATIONS] for every version since the v1 baseline,
 * and a recreate only for databases still on a pre-v1 schema.
 */
fun RoomDatabase.Builder<AppDatabase>.withAppMigrations(): RoomDatabase.Builder<AppDatabase> {
    val migrated = ALL_MIGRATIONS.fold(this) { builder, migration -> builder.addMigrations(migration) }
    return PRE_V1_SCHEMA_VERSIONS.fold(migrated) { builder, version ->
        builder.fallbackToDestructiveMigrationFrom(true, version)
    }
}
