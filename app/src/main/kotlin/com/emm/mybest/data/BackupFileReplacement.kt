package com.emm.mybest.data

import com.emm.mybest.domain.repository.RestoreResult
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal fun replaceFileAtomically(
    source: InputStream,
    target: File,
    move: (File, File) -> Unit = ::moveAtomically,
    beforeReplace: () -> Unit,
) {
    val staged = File(target.parentFile, "${target.name}$STAGED_FILE_SUFFIX")
    try {
        writeSynced(source, staged)
        beforeReplace()
        replaceKeepingSideFilesOnFailure(staged, target, move)
    } finally {
        staged.delete()
    }
}

internal fun replaceDatabaseFile(
    source: InputStream,
    target: File,
    move: (File, File) -> Unit = ::moveAtomically,
    closeDatabase: () -> Unit,
): RestoreResult {
    var isCloseStarted = false
    val replacement = runCatching {
        source.use { input ->
            replaceFileAtomically(input, target, move) {
                isCloseStarted = true
                closeDatabase()
            }
        }
    }
    if (replacement.isFailure && isCloseStarted) return RestoreResult.NotRestoredRequiresRestart
    replacement.getOrThrow()
    return RestoreResult.RequiresRestart
}

internal fun moveAtomically(source: File, target: File) {
    Files.move(
        source.toPath(),
        target.toPath(),
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING,
    )
}

internal fun sqliteSideFilesOf(file: File): List<File> =
    SQLITE_SIDE_FILE_SUFFIXES.map { suffix -> File("${file.path}$suffix") }

private fun replaceKeepingSideFilesOnFailure(staged: File, target: File, move: (File, File) -> Unit) {
    val setAside = mutableListOf<File>()
    runCatching {
        sqliteSideFilesOf(target).filter(File::exists).forEach { sideFile ->
            moveAtomically(sideFile, asideFileOf(sideFile))
            setAside += sideFile
        }
        move(staged, target)
    }.onFailure { failure -> putBack(setAside, failure) }.getOrThrow()
    setAside.forEach { sideFile -> asideFileOf(sideFile).delete() }
}

private fun putBack(setAside: List<File>, failure: Throwable) {
    setAside.forEach { sideFile ->
        runCatching { moveAtomically(asideFileOf(sideFile), sideFile) }
            .exceptionOrNull()
            ?.let(failure::addSuppressed)
    }
}

private fun asideFileOf(sideFile: File): File = File("${sideFile.path}$SET_ASIDE_FILE_SUFFIX")

private fun writeSynced(source: InputStream, staged: File) {
    FileOutputStream(staged).use { output ->
        source.copyTo(output)
        output.fd.sync()
    }
}

private val SQLITE_SIDE_FILE_SUFFIXES = listOf("-wal", "-shm", "-journal")
private const val STAGED_FILE_SUFFIX = ".restore"
private const val SET_ASIDE_FILE_SUFFIX = ".aside"
