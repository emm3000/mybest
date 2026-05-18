package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDetailComponentsTest {

    private val date = LocalDate(2026, 3, 10)

    private val weightEntry = WeightEntry(id = "w1", date = date, weight = 75f)
    private val facePhoto = ProgressPhoto(
        id = "p-face",
        date = date,
        type = PhotoType.FACE,
        photoPath = "/tmp/face.jpg",
        createdAt = 100L,
    )
    private val trunkPhoto = ProgressPhoto(
        id = "p-trunk",
        date = date,
        type = PhotoType.TRUNK,
        photoPath = "/tmp/trunk.jpg",
        createdAt = 200L,
    )

    @Test
    fun `buildDayTimelineEntries returns empty list when summary has no weight and no photos`() {
        val summary = DaySummary(date = date)
        val entries = buildDayTimelineEntries(summary)
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `buildDayTimelineEntries returns one WEIGHT entry when summary has only weight`() {
        val summary = DaySummary(date = date, weight = weightEntry)
        val entries = buildDayTimelineEntries(summary)
        assertEquals(1, entries.size)
        assertEquals(DayTimelineEventType.WEIGHT, entries[0].type)
        assertEquals(1L, entries[0].sequence)
    }

    @Test
    fun `buildDayTimelineEntries returns PHOTO entries for each photo in summary`() {
        val summary = DaySummary(date = date, photos = listOf(facePhoto, trunkPhoto))
        val entries = buildDayTimelineEntries(summary)
        assertEquals(2, entries.size)
        assertTrue(entries.all { it.type == DayTimelineEventType.PHOTO })
    }

    @Test
    fun `buildDayTimelineEntries photo entries carry the photo reference`() {
        val summary = DaySummary(date = date, photos = listOf(facePhoto))
        val entries = buildDayTimelineEntries(summary)
        assertEquals(1, entries.size)
        assertEquals(facePhoto, entries[0].photo)
    }

    @Test
    fun `buildDayTimelineEntries returns WEIGHT before PHOTO entries in sorted order`() {
        val summary = DaySummary(date = date, weight = weightEntry, photos = listOf(facePhoto))
        val entries = buildDayTimelineEntries(summary)
        assertEquals(2, entries.size)
        assertEquals(DayTimelineEventType.WEIGHT, entries[0].type)
        assertEquals(DayTimelineEventType.PHOTO, entries[1].type)
    }

    @Test
    fun `buildDayTimelineEntries assigns ascending sequence to multiple photos sorted by createdAt`() {
        val earlier = facePhoto.copy(id = "p1", createdAt = 50L)
        val later = trunkPhoto.copy(id = "p2", createdAt = 150L)
        val summary = DaySummary(date = date, photos = listOf(later, earlier))
        val entries = buildDayTimelineEntries(summary)

        val photoEntries = entries.filter { it.type == DayTimelineEventType.PHOTO }
        assertEquals(2, photoEntries.size)
        // sorted by createdAt: earlier photo first
        assertEquals(earlier, photoEntries[0].photo)
        assertEquals(later, photoEntries[1].photo)
        // sequences must be strictly ascending
        assertTrue(photoEntries[0].sequence < photoEntries[1].sequence)
    }

    @Test
    fun `buildDayTimelineEntries WEIGHT entry has no photo reference`() {
        val summary = DaySummary(date = date, weight = weightEntry)
        val entries = buildDayTimelineEntries(summary)
        assertEquals(null, entries.single().photo)
    }

    @Test
    fun `buildDayTimelineEntries is empty when weight is null and photos is empty list`() {
        val summary = DaySummary(date = date, weight = null, photos = emptyList())
        assertTrue(buildDayTimelineEntries(summary).isEmpty())
    }
}
