package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryCalendarComponentsTest {

    private val date = LocalDate(2026, 3, 10)

    private val weightEntry = WeightEntry(id = "w1", date = date, weight = 75f)
    private val photo = ProgressPhoto(
        id = "p1",
        date = date,
        type = PhotoType.FACE,
        photoPath = "/tmp/face.jpg",
        createdAt = 1L,
    )

    // region resolveDayIntensity

    @Test
    fun `resolveDayIntensity returns NONE for null summary`() {
        assertEquals(DayIntensity.NONE, resolveDayIntensity(null))
    }

    @Test
    fun `resolveDayIntensity returns NONE for summary with no weight and no photos`() {
        val summary = DaySummary(date = date)
        assertEquals(DayIntensity.NONE, resolveDayIntensity(summary))
    }

    @Test
    fun `resolveDayIntensity returns LOW for summary with only weight`() {
        val summary = DaySummary(date = date, weight = weightEntry)
        assertEquals(DayIntensity.LOW, resolveDayIntensity(summary))
    }

    @Test
    fun `resolveDayIntensity returns LOW for summary with only photos`() {
        val summary = DaySummary(date = date, photos = listOf(photo))
        assertEquals(DayIntensity.LOW, resolveDayIntensity(summary))
    }

    @Test
    fun `resolveDayIntensity returns HIGH for summary with weight and photos`() {
        val summary = DaySummary(date = date, weight = weightEntry, photos = listOf(photo))
        assertEquals(DayIntensity.HIGH, resolveDayIntensity(summary))
    }

    // endregion

    // region hasActivityInSelectedMonth

    @Test
    fun `hasActivityInSelectedMonth returns false for empty monthlyData`() {
        val month = YearMonthValue(2026, 3)
        assertFalse(hasActivityInSelectedMonth(month, emptyMap()))
    }

    @Test
    fun `hasActivityInSelectedMonth returns false when all days have no activity`() {
        val month = YearMonthValue(2026, 3)
        val inactiveDay = DaySummary(date = LocalDate(2026, 3, 5))
        val data = mapOf(LocalDate(2026, 3, 5) to inactiveDay)
        assertFalse(hasActivityInSelectedMonth(month, data))
    }

    @Test
    fun `hasActivityInSelectedMonth returns true when a day in month has weight`() {
        val month = YearMonthValue(2026, 3)
        val activeDay = DaySummary(date = date, weight = weightEntry)
        val data = mapOf(date to activeDay)
        assertTrue(hasActivityInSelectedMonth(month, data))
    }

    @Test
    fun `hasActivityInSelectedMonth returns true when a day in month has photos`() {
        val month = YearMonthValue(2026, 3)
        val activeDay = DaySummary(date = date, photos = listOf(photo))
        val data = mapOf(date to activeDay)
        assertTrue(hasActivityInSelectedMonth(month, data))
    }

    @Test
    fun `hasActivityInSelectedMonth ignores activity from different month`() {
        val march = YearMonthValue(2026, 3)
        val aprilDate = LocalDate(2026, 4, 1)
        val aprilEntry = WeightEntry(id = "w2", date = aprilDate, weight = 70f)
        val aprilDay = DaySummary(date = aprilDate, weight = aprilEntry)
        val data = mapOf(aprilDate to aprilDay)
        assertFalse(hasActivityInSelectedMonth(march, data))
    }

    // endregion

    // region buildDayCellDescription

    @Test
    fun `buildDayCellDescription includes today prefix when isToday is true`() {
        val summary = DaySummary(date = date, weight = weightEntry)
        val description = buildDayCellDescription(date, summary, isToday = true)
        assertTrue(description.startsWith("Hoy."))
    }

    @Test
    fun `buildDayCellDescription does not include today prefix when isToday is false`() {
        val summary = DaySummary(date = date, weight = weightEntry)
        val description = buildDayCellDescription(date, summary, isToday = false)
        assertFalse(description.startsWith("Hoy."))
    }

    @Test
    fun `buildDayCellDescription mentions peso when day has weight`() {
        val summary = DaySummary(date = date, weight = weightEntry)
        val description = buildDayCellDescription(date, summary, isToday = false)
        assertTrue(description.contains("peso"))
    }

    @Test
    fun `buildDayCellDescription mentions fotos when day has photos`() {
        val summary = DaySummary(date = date, photos = listOf(photo))
        val description = buildDayCellDescription(date, summary, isToday = false)
        assertTrue(description.contains("fotos"))
    }

    @Test
    fun `buildDayCellDescription reports sin actividad registrada for empty summary`() {
        val summary = DaySummary(date = date)
        val description = buildDayCellDescription(date, summary, isToday = false)
        assertTrue(description.contains("Sin actividad registrada"))
    }

    @Test
    fun `buildDayCellDescription includes intensity label`() {
        val summary = DaySummary(date = date, weight = weightEntry, photos = listOf(photo))
        val description = buildDayCellDescription(date, summary, isToday = false)
        assertTrue(description.contains("Intensidad:"))
        assertTrue(description.contains(DayIntensity.HIGH.label.lowercase()))
    }

    @Test
    fun `buildDayCellDescription always ends with detail prompt`() {
        val summary = DaySummary(date = date)
        val description = buildDayCellDescription(date, summary, isToday = false)
        assertTrue(description.endsWith("Toca para ver el detalle del día."))
    }

    // endregion
}
