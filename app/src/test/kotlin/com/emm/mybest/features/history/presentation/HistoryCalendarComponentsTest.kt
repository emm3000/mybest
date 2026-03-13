package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryCalendarComponentsTest {

    @Test
    fun `resolveDayIntensity maps activity score to expected level`() {
        val date = LocalDate(2026, 3, 13)
        val low = DaySummary(date = date, weight = WeightEntry("w1", date, 70f))
        val medium = DaySummary(
            date = date,
            weight = WeightEntry("w1", date, 70f),
            habit = DailyHabitSummary(date, ateHealthy = true, didExercise = false, notes = null),
        )
        val high = DaySummary(
            date = date,
            weight = WeightEntry("w1", date, 70f),
            habit = DailyHabitSummary(date, ateHealthy = true, didExercise = false, notes = null),
            photos = listOf(
                ProgressPhoto("p1", date = date, type = PhotoType.BODY, photoPath = "/tmp/a.jpg", createdAt = 1L),
            ),
        )

        assertEquals(DayIntensity.NONE, resolveDayIntensity(null))
        assertEquals(DayIntensity.LOW, resolveDayIntensity(low))
        assertEquals(DayIntensity.MEDIUM, resolveDayIntensity(medium))
        assertEquals(DayIntensity.HIGH, resolveDayIntensity(high))
    }

    @Test
    fun `buildDayCellDescription includes intensity and activity detail`() {
        val date = LocalDate(2026, 3, 13)
        val summary = DaySummary(
            date = date,
            habit = DailyHabitSummary(date, ateHealthy = true, didExercise = false, notes = null),
            photos = listOf(
                ProgressPhoto("p1", date = date, type = PhotoType.BODY, photoPath = "/tmp/a.jpg", createdAt = 1L),
            ),
        )

        val description = buildDayCellDescription(
            date = date,
            summary = summary,
            isToday = true,
        )

        assertTrue(description.contains("Intensidad: media"))
        assertTrue(description.contains("Actividad registrada: hábitos, fotos"))
    }
}
