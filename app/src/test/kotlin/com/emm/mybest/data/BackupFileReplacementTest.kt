package com.emm.mybest.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.FileSystemException

private val LIVE_BYTES = ByteArray(8192) { (it % 7).toByte() }
private val BACKUP_BYTES = ByteArray(12288) { (it % 11).toByte() }
private val WAL_BYTES = ByteArray(4096) { (it % 5).toByte() }
private val SHM_BYTES = ByteArray(2048) { (it % 3).toByte() }
private val JOURNAL_BYTES = ByteArray(512) { (it % 13).toByte() }

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

    @Test
    fun `a move that fails keeps the target and its side files with their original bytes`() {
        val target = folder.newFile("my_best_db").apply { writeBytes(LIVE_BYTES) }
        val wal = folder.newFile("my_best_db-wal").apply { writeBytes(WAL_BYTES) }
        val shm = folder.newFile("my_best_db-shm").apply { writeBytes(SHM_BYTES) }
        var isBeforeReplaceCalled = false

        assertThrows(IOException::class.java) {
            replaceFileAtomically(ByteArrayInputStream(BACKUP_BYTES), target, move = ::failMove) {
                isBeforeReplaceCalled = true
            }
        }

        assertTrue(isBeforeReplaceCalled)
        assertArrayEquals(LIVE_BYTES, target.readBytes())
        assertArrayEquals(WAL_BYTES, wal.readBytes())
        assertArrayEquals(SHM_BYTES, shm.readBytes())
        assertEquals(setOf("my_best_db", "my_best_db-wal", "my_best_db-shm"), folder.root.list()?.toSet())
    }

    @Test
    fun `a side file that cannot be set aside puts back only the moved files and keeps the original error`() {
        val target = folder.newFile("my_best_db").apply { writeBytes(LIVE_BYTES) }
        val wal = folder.newFile("my_best_db-wal").apply { writeBytes(WAL_BYTES) }
        val shm = folder.newFile("my_best_db-shm").apply { writeBytes(SHM_BYTES) }
        val staleShmAside = folder.newFolder("my_best_db-shm.aside").apply { File(this, "stale").writeBytes(SHM_BYTES) }

        val failure = assertThrows(FileSystemException::class.java) {
            replaceFileAtomically(ByteArrayInputStream(BACKUP_BYTES), target) {}
        }

        assertEquals(shm.path, failure.file)
        assertArrayEquals(LIVE_BYTES, target.readBytes())
        assertArrayEquals(WAL_BYTES, wal.readBytes())
        assertArrayEquals(SHM_BYTES, shm.readBytes())
        assertTrue(staleShmAside.isDirectory)
    }

    @Test
    fun `a put-back that fails is suppressed into the move error and the other side files still come back`() {
        val target = folder.newFile("my_best_db").apply { writeBytes(LIVE_BYTES) }
        val wal = folder.newFile("my_best_db-wal").apply { writeBytes(WAL_BYTES) }
        val shm = folder.newFile("my_best_db-shm").apply { writeBytes(SHM_BYTES) }
        val moveFailure = IOException("Simulated move failure")

        val failure = assertThrows(IOException::class.java) {
            replaceFileAtomically(ByteArrayInputStream(BACKUP_BYTES), target, move = { _, _ ->
                wal.mkdir()
                File(wal, "blocker").writeBytes(WAL_BYTES)
                throw moveFailure
            }) {}
        }

        assertSame(moveFailure, failure)
        assertEquals(1, failure.suppressed.size)
        assertArrayEquals(LIVE_BYTES, target.readBytes())
        assertArrayEquals(SHM_BYTES, shm.readBytes())
    }

    @Test
    fun `a successful move leaves only the target without side files`() {
        val target = folder.newFile("my_best_db").apply { writeBytes(LIVE_BYTES) }
        folder.newFile("my_best_db-wal").writeBytes(WAL_BYTES)
        folder.newFile("my_best_db-shm").writeBytes(SHM_BYTES)
        folder.newFile("my_best_db-journal").writeBytes(JOURNAL_BYTES)

        replaceFileAtomically(ByteArrayInputStream(BACKUP_BYTES), target) {}

        assertArrayEquals(BACKUP_BYTES, target.readBytes())
        assertEquals(listOf("my_best_db"), folder.root.list()?.toList())
    }
}

private fun failMove(staged: File, target: File) {
    throw IOException("Simulated failure moving ${staged.name} to ${target.name}")
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
