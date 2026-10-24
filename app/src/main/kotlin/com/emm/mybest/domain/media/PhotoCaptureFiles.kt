package com.emm.mybest.domain.media

/**
 * Creates the files that the camera writes a new progress photo into.
 */
interface PhotoCaptureFiles {

    /**
     * Creates an empty capture file and returns the content URI the camera can write to.
     */
    fun createCaptureUri(): String
}
