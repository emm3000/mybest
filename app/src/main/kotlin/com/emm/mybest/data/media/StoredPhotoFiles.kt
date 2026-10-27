package com.emm.mybest.data.media

import java.io.File

internal const val PHOTOS_DIRECTORY = "photos"

/** Root name of the photos FileProvider path. Must match `res/xml/file_paths.xml`. */
internal const val PHOTOS_URI_ROOT = "internal_photos"

/**
 * Maps the path of a photo FileProvider URI to its file under `filesDir/photos`.
 * Returns null when the path uses another root or resolves outside that directory.
 */
internal fun storedPhotoFile(filesDir: File, uriPath: String): File? {
    val relativePath = uriPath.removePrefix("/")
    if (!relativePath.startsWith("$PHOTOS_URI_ROOT/")) return null
    val photosDirectory = File(filesDir, PHOTOS_DIRECTORY)
    val file = File(photosDirectory, relativePath.removePrefix("$PHOTOS_URI_ROOT/"))
    val isInsidePhotos = file.canonicalFile.startsWith(photosDirectory.canonicalFile)
    return file.takeIf { isInsidePhotos }
}
