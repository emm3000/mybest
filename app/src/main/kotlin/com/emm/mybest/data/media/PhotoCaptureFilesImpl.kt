package com.emm.mybest.data.media

import android.content.Context
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.domain.media.PhotoCaptureFiles
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "PhotoCaptureFiles"
private const val PHOTOS_DIRECTORY = "photos"

class PhotoCaptureFilesImpl(
    private val context: Context,
    private val fileNames: PhotoFileNames,
    private val dispatchers: CoroutineDispatchers,
) : PhotoCaptureFiles {

    override fun createCaptureUri(): String {
        val photoFile = File.createTempFile(
            fileNames.capturePrefix(),
            ".jpg",
            photosDirectory(),
        )
        return contentUriOf(photoFile)
    }

    override suspend fun persistCapture(captureUri: String): String? = withContext(dispatchers.io) {
        runCatching { copyCapture(captureUri) }
            .onFailure { error -> Log.e(TAG, "Error copying photo from URI", error) }
            .getOrNull()
    }

    private fun copyCapture(captureUri: String): String? {
        val targetFile = File(photosDirectory(), fileNames.persistedName())
        val isWritten = writeUprightJpeg(context.contentResolver, captureUri.toUri(), targetFile)
        if (!isWritten) return null
        return contentUriOf(targetFile)
    }

    private fun photosDirectory(): File = File(context.filesDir, PHOTOS_DIRECTORY).apply {
        if (!exists()) mkdirs()
    }

    private fun contentUriOf(file: File): String = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    ).toString()
}
