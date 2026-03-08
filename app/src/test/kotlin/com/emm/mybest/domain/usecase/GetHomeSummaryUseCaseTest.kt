package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetHomeSummaryUseCaseTest {

    private val weightRepository = mockk<WeightRepository>()
    private val photoRepository = mockk<PhotoRepository>()
    private val dailyHabitsUseCase = mockk<GetDailyHabitsUseCase>()
    private val useCase = GetHomeSummaryUseCase(weightRepository, photoRepository, dailyHabitsUseCase)

    @Test
    fun `invoke builds summary with latest weight lost and photo count`() = runTest {
        val habits = listOf(
            HabitWithRecord(
                habit = Habit(
                    id = "h-1",
                    name = "Leer",
                    icon = "Book",
                    color = 2,
                    category = "Mente",
                    type = HabitType.BOOLEAN,
                    scheduledDays = setOf(DayOfWeek.TUESDAY),
                ),
                record = null,
            ),
        )
        val weights = listOf(
            WeightEntry(id = "w-1", date = LocalDate(2026, 3, 1), weight = 80f),
            WeightEntry(id = "w-2", date = LocalDate(2026, 3, 8), weight = 77.5f),
        )
        val photos = listOf(
            ProgressPhoto(
                id = "p-1",
                date = LocalDate(2026, 3, 8),
                type = PhotoType.BODY,
                photoPath = "/tmp/p-1.jpg",
                createdAt = 1L,
            ),
            ProgressPhoto(
                id = "p-2",
                date = LocalDate(2026, 3, 8),
                type = PhotoType.FACE,
                photoPath = "/tmp/p-2.jpg",
                createdAt = 2L,
            ),
        )

        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(photos)
        every { dailyHabitsUseCase.invoke(any()) } returns flowOf(habits)

        val summary = useCase().single()

        assertEquals(habits, summary.dailyHabits)
        assertEquals(77.5f, summary.latestWeight)
        assertEquals(2.5f, summary.totalWeightLost)
        assertEquals(2, summary.totalPhotos)
    }

    @Test
    fun `invoke returns safe defaults when there is no weight data`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())
        every { dailyHabitsUseCase.invoke(any()) } returns flowOf(emptyList())

        val summary = useCase().single()

        assertNull(summary.latestWeight)
        assertEquals(0f, summary.totalWeightLost)
        assertEquals(0, summary.totalPhotos)
        assertEquals(emptyList<HabitWithRecord>(), summary.dailyHabits)
    }
}
