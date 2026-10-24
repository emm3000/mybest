package com.emm.mybest.domain.media

/**
 * Creates the files that the camera writes a new progress photo into, and keeps the captured photos.
 */
interface PhotoCaptureFiles {

    /**
     * Creates an empty capture file and returns the content URI the camera can write to.
     */
    fun createCaptureUri(): String

    /**
     * Copies the photo the camera wrote to [captureUri] into app storage, upright and re-encoded as JPEG.
     * Returns the content URI of the stored copy, or null when the copy fails.
     */
    suspend fun persistCapture(captureUri: String): String?
}
