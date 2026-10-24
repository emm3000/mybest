package com.emm.mybest.data

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal fun replaceFileAtomically(source: InputStream, target: File, beforeReplace: () -> Unit) {
    val staged = File(target.parentFile, "${target.name}$STAGED_FILE_SUFFIX")
    try {
        writeSynced(source, staged)
        beforeReplace()
        Files.move(
            staged.toPath(),
            target.toPath(),
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING,
        )
    } finally {
        staged.delete()
    }
}

private fun writeSynced(source: InputStream, staged: File) {
    FileOutputStream(staged).use { output ->
        source.copyTo(output)
        output.fd.sync()
    }
}

private const val STAGED_FILE_SUFFIX = ".restore"
