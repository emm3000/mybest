package com.emm.mybest.data

import androidx.room.migration.Migration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomMigrationsChainTest {

    @Test
    fun `ALL_MIGRATIONS chains every version from the v1 baseline to the current schema`() {
        val reached = lastVersionReachedFrom(AppDatabase.V1_SCHEMA_VERSION, ALL_MIGRATIONS)

        assertEquals(AppDatabase.SCHEMA_VERSION, reached)
    }

    @Test
    fun `ALL_MIGRATIONS never starts below the v1 baseline`() {
        val preV1Starts = ALL_MIGRATIONS.filter { it.startVersion < AppDatabase.V1_SCHEMA_VERSION }

        assertTrue(preV1Starts.isEmpty())
    }

    private fun lastVersionReachedFrom(start: Int, migrations: List<Migration>): Int =
        generateSequence(start) { version ->
            migrations.singleOrNull { it.startVersion == version }?.endVersion
        }.last()
}
