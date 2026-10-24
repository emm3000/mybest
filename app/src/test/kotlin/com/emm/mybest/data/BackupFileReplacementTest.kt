package com.emm.mybest.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

private val LIVE_BYTES = ByteArray(8192) { (it % 7).toByte() }
private val BACKUP_BYTES = ByteArray(12288) { (it % 11).toByte() }

class BackupFileReplacementTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `a source that fails mid-copy leaves the target bytes and no staged file`() {
        val target = folder.newFile("my_best_db").apply { writeBytes(LIVE_BYTES) }
        var isBeforeReplaceCalled = false

        assertThrows(IOException::class.java) {
            replaceFileAtomically(FailingInputStream(BACKUP_BYTES, failAfter = 4096), target) {
                isBeforeReplaceCalled = true
            }
        }

        assertArrayEquals(LIVE_BYTES, target.readBytes())
        assertEquals(listOf("my_best_db"), folder.root.list()?.toList())
        assertFalse(isBeforeReplaceCalled)
    }

    @Test
    fun `a complete source replaces the target bytes and leaves no staged file`() {
        val target = folder.newFile("my_best_db").apply { writeBytes(LIVE_BYTES) }
        var isBeforeReplaceCalled = false

        replaceFileAtomically(ByteArrayInputStream(BACKUP_BYTES), target) { isBeforeReplaceCalled = true }

        assertArrayEquals(BACKUP_BYTES, target.readBytes())
        assertEquals(listOf("my_best_db"), folder.root.list()?.toList())
        assertTrue(isBeforeReplaceCalled)
    }

    @Test
    fun `the target is untouched when the step before the replacement runs`() {
        val target = folder.newFile("my_best_db").apply { writeBytes(LIVE_BYTES) }
        var bytesBeforeReplace = ByteArray(0)

        replaceFileAtomically(ByteArrayInputStream(BACKUP_BYTES), target) { bytesBeforeReplace = target.readBytes() }

        assertArrayEquals(LIVE_BYTES, bytesBeforeReplace)
    }
}

private class FailingInputStream(bytes: ByteArray, private val failAfter: Int) : InputStream() {
    private val source = ByteArrayInputStream(bytes)
    private var bytesRead = 0

    override fun read(): Int {
        if (bytesRead >= failAfter) throw IOException("Simulated failure after $failAfter bytes")
        bytesRead++
        return source.read()
    }
}
