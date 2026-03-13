package com.emm.mybest.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupRepositoryImplTest {

    @Test
    fun `isValidSqliteHeader returns true for valid sqlite header`() {
        val header = "SQLite format 3\u0000".toByteArray(Charsets.UTF_8)

        val result = isValidSqliteHeader(header, header.size)

        assertEquals(true, result)
    }

    @Test
    fun `isValidSqliteHeader returns false for invalid header`() {
        val header = "not-a-sqlite-file".toByteArray(Charsets.UTF_8)

        val result = isValidSqliteHeader(header, header.size)

        assertEquals(false, result)
    }
}
