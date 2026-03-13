package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryDetailComponentsTest {

    @Test
    fun `buildDayTimelineEntries returns habit weight then photos by createdAt`() {
        val date = LocalDate(2026, 3, 13)
        val summary = DaySummary(
            date = date,
            habit = DailyHabitSummary(
                date = date,
                ateHealthy = true,
                didExercise = true,
                notes = "ok",
            ),
            weight = WeightEntry(
                id = "w1",
                date = date,
                weight = 72f,
            ),
            photos = listOf(
                ProgressPhoto(
                    id = "p-late",
                    date = date,
                    type = PhotoType.BODY,
                    photoPath = "/tmp/late.jpg",
                    createdAt = 200L,
                ),
                ProgressPhoto(
                    id = "p-early",
                    date = date,
                    type = PhotoType.FACE,
                    photoPath = "/tmp/early.jpg",
                    createdAt = 100L,
                ),
            ),
        )

        val timeline = buildDayTimelineEntries(summary)

        assertEquals(4, timeline.size)
        assertEquals(DayTimelineEventType.HABIT, timeline[0].type)
        assertEquals(DayTimelineEventType.WEIGHT, timeline[1].type)
        assertEquals("p-early", timeline[2].photo?.id)
        assertEquals("p-late", timeline[3].photo?.id)
    }
}
