package com.emm.mybest.data.media

import android.content.Context
import androidx.core.content.FileProvider
import com.emm.mybest.domain.media.PhotoCaptureFiles
import java.io.File

class PhotoCaptureFilesImpl(
    private val context: Context,
    private val fileNames: PhotoFileNames,
) : PhotoCaptureFiles {

    override fun createCaptureUri(): String {
        val photoFile = createPhotoFile()
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile,
        ).toString()
    }

    private fun createPhotoFile(): File {
        val storageDir = File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        return File.createTempFile(
            fileNames.capturePrefix(),
            ".jpg",
            storageDir,
        )
    }
}
