package com.emm.mybest.data.media

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File

private const val JPEG_COMPRESSION_QUALITY = 85
private const val ROTATE_90 = 90f
private const val ROTATE_180 = 180f
private const val ROTATE_270 = 270f

internal fun writeUprightJpeg(
    contentResolver: ContentResolver,
    sourceUri: Uri,
    targetFile: File,
): Boolean {
    val bitmap = decodeBitmapFromUri(contentResolver = contentResolver, sourceUri = sourceUri) ?: return false
    val orientation = readExifOrientation(contentResolver = contentResolver, sourceUri = sourceUri)
    val rotatedBitmap = rotateBitmapIfNeeded(bitmap = bitmap, orientation = orientation)

    targetFile.outputStream().use { output ->
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, output)
    }
    if (rotatedBitmap != bitmap) rotatedBitmap.recycle()
    bitmap.recycle()
    return true
}

private fun decodeBitmapFromUri(
    contentResolver: ContentResolver,
    sourceUri: Uri,
): Bitmap? = contentResolver.openInputStream(sourceUri)?.use { input ->
    BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inJustDecodeBounds = false })
}

private fun readExifOrientation(
    contentResolver: ContentResolver,
    sourceUri: Uri,
): Int = contentResolver.openInputStream(sourceUri)?.use { orientationInput ->
    ExifInterface(orientationInput).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED,
    )
} ?: ExifInterface.ORIENTATION_UNDEFINED

private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap = when (orientation) {
    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, ROTATE_90)
    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, ROTATE_180)
    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, ROTATE_270)
    else -> bitmap
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
