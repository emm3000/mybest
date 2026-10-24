package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRecentRowStateTest {

    private val today = LocalDate(2026, 10, 24)

    @Test
    fun `weight only entry carries the weight and no photo labels`() {
        val entry = HistoryRecentEntry(date = today, weight = 80.5f, photoTypes = emptyList())

        val row = entry.toRowState(today)

        assertEquals(HistoryRecentDescription(weight = 80.5f, photoLabelResIds = emptyList()), row.description)
    }

    @Test
    fun `photos only entry carries the photo labels and no weight`() {
        val entry = HistoryRecentEntry(date = today, weight = null, photoTypes = listOf(PhotoType.FACE))

        val row = entry.toRowState(today)

        assertEquals(
            HistoryRecentDescription(weight = null, photoLabelResIds = listOf(R.string.photo_type_face)),
            row.description,
        )
    }

    @Test
    fun `weight with trunk and face keeps the photo order`() {
        val entry = HistoryRecentEntry(
            date = today,
            weight = 80.5f,
            photoTypes = listOf(PhotoType.TRUNK, PhotoType.FACE),
        )

        val row = entry.toRowState(today)

        assertEquals(
            HistoryRecentDescription(
                weight = 80.5f,
                photoLabelResIds = listOf(R.string.photo_type_trunk, R.string.photo_type_face),
            ),
            row.description,
        )
    }

    @Test
    fun `row carries the date, its label and whether it is today`() {
        val yesterday = LocalDate(2026, 10, 23)

        val todayRow = HistoryRecentEntry(today, 80.5f, emptyList()).toRowState(today)
        val yesterdayRow = HistoryRecentEntry(yesterday, 80.5f, emptyList()).toRowState(today)

        assertEquals(today, todayRow.date)
        assertEquals(formatRecentDate(today), todayRow.dateLabel)
        assertTrue(todayRow.isToday)
        assertFalse(yesterdayRow.isToday)
    }
}
