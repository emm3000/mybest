package com.emm.mybest.data

import androidx.room.RoomDatabase
import androidx.room.migration.Migration

private const val FIRST_SCHEMA_VERSION = 1

/** Every migration from [AppDatabase.V1_SCHEMA_VERSION] up to [AppDatabase.SCHEMA_VERSION], in order. */
val ALL_MIGRATIONS: List<Migration> = emptyList()

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
