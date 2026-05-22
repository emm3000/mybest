package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.InsightsRecommendationKind
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetInsightsUseCaseTest {

    private val weightRepository = mockk<WeightRepository>()
    private val photoRepository = mockk<PhotoRepository>()
    private val useCase = GetInsightsUseCase(weightRepository, photoRepository)
    private val today = LocalDate(2026, 2, 15)

    @Test
    fun `empty weight and empty photos produces zero totals and UPLOAD_PHOTO_TODAY recommendation`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(0f, result.totalWeightLost)
        assertEquals(0, result.photoCount)
        assertEquals(InsightsRecommendationKind.UPLOAD_PHOTO_TODAY, result.recommendation.kind)
    }

    @Test
    fun `weight dropped 5kg with 10 photos produces KEEP_ROUTINE recommendation`() = runTest {
        val startDate = LocalDate(2026, 1, 1)
        val endDate = LocalDate(2026, 2, 1)
        val weights = listOf(
            WeightEntry(id = "1", date = startDate, weight = 80f),
            WeightEntry(id = "2", date = endDate, weight = 75f),
        )
        val photos = (1..10).map { i ->
            ProgressPhoto(
                id = "p$i",
                date = startDate,
                type = PhotoType.TRUNK,
                photoPath = "/photo/$i.jpg",
                createdAt = i.toLong(),
            )
        }
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(photos)

        val result = useCase(today).first()

        assertEquals(5f, result.totalWeightLost)
        assertEquals(10, result.photoCount)
        assertEquals(InsightsRecommendationKind.KEEP_ROUTINE, result.recommendation.kind)
    }

    @Test
    fun `flat weight delta with photos produces ADJUST_WEEKLY_PLAN recommendation`() = runTest {
        val startDate = LocalDate(2026, 1, 1)
        val endDate = LocalDate(2026, 2, 1)
        val weights = listOf(
            WeightEntry(id = "1", date = startDate, weight = 78f),
            WeightEntry(id = "2", date = endDate, weight = 78f),
        )
        val photos = listOf(
            ProgressPhoto(
                id = "p1",
                date = startDate,
                type = PhotoType.TRUNK,
                photoPath = "/photo/1.jpg",
                createdAt = 1L,
            ),
            ProgressPhoto(
                id = "p2",
                date = endDate,
                type = PhotoType.FACE,
                photoPath = "/photo/2.jpg",
                createdAt = 2L,
            ),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(photos)

        val result = useCase(today).first()

        assertEquals(0f, result.totalWeightLost)
        assertEquals(InsightsRecommendationKind.ADJUST_WEEKLY_PLAN, result.recommendation.kind)
    }

    @Test
    fun `empty weights produce null new metrics`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertNull(result.deltaWeightKg)
        assertNull(result.deltaWeightPercent)
        assertNull(result.kgPerDayRate14d)
    }

    @Test
    fun `single weight produces null new metrics`() = runTest {
        val weights = listOf(WeightEntry(id = "1", date = today, weight = 80f))
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertNull(result.deltaWeightKg)
        assertNull(result.deltaWeightPercent)
        assertNull(result.kgPerDayRate14d)
    }

    @Test
    fun `weight dropped from 80 to 75 computes correct delta and percent`() = runTest {
        val initial = today.minus(DatePeriod(days = 30))
        val weights = listOf(
            WeightEntry(id = "1", date = initial, weight = 80f),
            WeightEntry(id = "2", date = today, weight = 75f),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(-5f, result.deltaWeightKg!!, 0.001f)
        assertEquals(-6.25f, result.deltaWeightPercent!!, 0.001f)
    }

    @Test
    fun `weight gained from 70 to 75 computes positive delta and percent`() = runTest {
        val initial = today.minus(DatePeriod(days = 30))
        val weights = listOf(
            WeightEntry(id = "1", date = initial, weight = 70f),
            WeightEntry(id = "2", date = today, weight = 75f),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(5f, result.deltaWeightKg!!, 0.001f)
        assertEquals(7.142857f, result.deltaWeightPercent!!, 0.001f)
    }

    @Test
    fun `zero initial weight returns null percent but non-null delta`() = runTest {
        val initial = today.minus(DatePeriod(days = 30))
        val weights = listOf(
            WeightEntry(id = "1", date = initial, weight = 0f),
            WeightEntry(id = "2", date = today, weight = 5f),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(5f, result.deltaWeightKg!!, 0.001f)
        assertNull(result.deltaWeightPercent)
    }

    @Test
    fun `no entries in lookback window produces null rate`() = runTest {
        val weights = listOf(
            WeightEntry(id = "1", date = today.minus(DatePeriod(days = 3)), weight = 80f),
            WeightEntry(id = "2", date = today, weight = 78f),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertNull(result.kgPerDayRate14d)
    }

    @Test
    fun `entry at today minus 14 produces expected rate`() = runTest {
        val baseline = today.minus(DatePeriod(days = 14))
        val weights = listOf(
            WeightEntry(id = "1", date = baseline, weight = 80f),
            WeightEntry(id = "2", date = today, weight = 78f),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(-0.1428f, result.kgPerDayRate14d!!, 0.001f)
    }

    @Test
    fun `multiple entries in window picks closest to today minus 14`() = runTest {
        val entry1 = today.minus(DatePeriod(days = 10))
        val entry2 = today.minus(DatePeriod(days = 9))
        val weights = listOf(
            WeightEntry(id = "1", date = entry1, weight = 80f),
            WeightEntry(id = "2", date = entry2, weight = 79.8f),
            WeightEntry(id = "3", date = today, weight = 78f),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(-0.2f, result.kgPerDayRate14d!!, 0.001f)
    }

    @Test
    fun `daysSinceFirstWeight is null when weights are empty`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertNull(result.daysSinceFirstWeight)
    }

    @Test
    fun `daysSinceFirstWeight computes correct days from first weight to today`() = runTest {
        val firstDate = today.minus(DatePeriod(days = 30))
        val weights = listOf(
            WeightEntry(id = "1", date = firstDate, weight = 80f),
            WeightEntry(id = "2", date = today, weight = 75f),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(30, result.daysSinceFirstWeight)
    }

    @Test
    fun `troncoPhotoCount counts only TRUNK photos`() = runTest {
        val date = today.minus(DatePeriod(days = 5))
        val photos = listOf(
            ProgressPhoto(id = "p1", date = date, type = PhotoType.TRUNK, photoPath = "/p1.jpg", createdAt = 1L),
            ProgressPhoto(id = "p2", date = date, type = PhotoType.TRUNK, photoPath = "/p2.jpg", createdAt = 2L),
            ProgressPhoto(id = "p3", date = date, type = PhotoType.FACE, photoPath = "/p3.jpg", createdAt = 3L),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(photos)

        val result = useCase(today).first()

        assertEquals(2, result.troncoPhotoCount)
        assertEquals(1, result.caraPhotoCount)
    }

    @Test
    fun `caraPhotoCount counts only FACE photos`() = runTest {
        val date = today.minus(DatePeriod(days = 5))
        val photos = listOf(
            ProgressPhoto(id = "p1", date = date, type = PhotoType.FACE, photoPath = "/p1.jpg", createdAt = 1L),
            ProgressPhoto(id = "p2", date = date, type = PhotoType.FACE, photoPath = "/p2.jpg", createdAt = 2L),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(photos)

        val result = useCase(today).first()

        assertEquals(0, result.troncoPhotoCount)
        assertEquals(2, result.caraPhotoCount)
    }
}
