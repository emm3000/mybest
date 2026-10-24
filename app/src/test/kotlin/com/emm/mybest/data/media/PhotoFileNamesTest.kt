package com.emm.mybest.data.media

import com.emm.mybest.testing.FixedClock
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class PhotoFileNamesTest {

    private val fileNames = PhotoFileNames(
        clock = FixedClock(Instant.parse("2026-10-24T03:04:05.678Z")),
        timeZone = TimeZone.of("America/Lima"),
    )

    @Test
    fun `capture prefix uses the local date and time of the clock`() {
        assertEquals("JPEG_20261023_220405_", fileNames.capturePrefix())
    }

    @Test
    fun `persisted name uses the epoch milliseconds of the clock`() {
        assertEquals("IMG_1792811045678.jpg", fileNames.persistedName())
    }
}
