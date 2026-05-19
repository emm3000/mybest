package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.InsightsRecommendationAction
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
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GetInsightsUseCaseTest {

    private val weightRepository = mockk<WeightRepository>()
    private val photoRepository = mockk<PhotoRepository>()
    private val useCase = GetInsightsUseCase(weightRepository, photoRepository)

    @Test
    fun `empty weight and empty photos produces zero totals and UPLOAD_PHOTO_TODAY recommendation`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val result = useCase().first()

        assertEquals(0f, result.totalWeightLost)
        assertEquals(0, result.photoCount)
        assertEquals(InsightsRecommendationKind.UPLOAD_PHOTO_TODAY, result.recommendation.kind)
        assertEquals(InsightsRecommendationAction.ADD_PROGRESS_PHOTO, result.recommendation.action)
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

        val result = useCase().first()

        assertEquals(5f, result.totalWeightLost)
        assertEquals(10, result.photoCount)
        assertEquals(InsightsRecommendationKind.KEEP_ROUTINE, result.recommendation.kind)
        assertEquals(InsightsRecommendationAction.KEEP_ROUTINE, result.recommendation.action)
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

        val result = useCase().first()

        assertEquals(0f, result.totalWeightLost)
        assertEquals(InsightsRecommendationKind.ADJUST_WEEKLY_PLAN, result.recommendation.kind)
        assertEquals(InsightsRecommendationAction.ADJUST_WEIGHT_PLAN, result.recommendation.action)
    }
}
