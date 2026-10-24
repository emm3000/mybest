package com.emm.mybest.data.media

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val CAPTURE_TIMESTAMP_FORMAT = LocalDateTime.Format {
    year()
    monthNumber()
    day()
    char('_')
    hour()
    minute()
    second()
}

/**
 * Names the photo files written into app storage from the time of [clock] in [timeZone].
 */
class PhotoFileNames(
    private val clock: Clock,
    private val timeZone: TimeZone,
) {

    /**
     * Returns the prefix of a new camera capture file, such as `JPEG_20261023_220405_`.
     */
    fun capturePrefix(): String {
        val timestamp = CAPTURE_TIMESTAMP_FORMAT.format(clock.now().toLocalDateTime(timeZone))
        return "JPEG_${timestamp}_"
    }

    /**
     * Returns the name of a new stored photo copy, such as `IMG_1792811045678.jpg`.
     */
    fun persistedName(): String = "IMG_${clock.now().toEpochMilliseconds()}.jpg"
}
