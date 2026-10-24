package com.emm.mybest.data.media

import android.content.Context
import androidx.core.content.FileProvider
import com.emm.mybest.domain.media.PhotoCaptureFiles
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoCaptureFilesImpl(private val context: Context) : PhotoCaptureFiles {

    override fun createCaptureUri(): String {
        val photoFile = createPhotoFile()
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile,
        ).toString()
    }

    private fun createPhotoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir,
        )
    }
}
