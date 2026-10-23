package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val WEIGH_IN_DAY = LocalDate(2026, 10, 23)
private val MORNING = LocalTime(8, 0)

private class FakePhotoRepository(private val photos: List<ProgressPhoto>) : PhotoRepository {
    override fun getAllPhotos(): Flow<List<ProgressPhoto>> = flowOf(photos)

    override fun getPhotosByType(type: PhotoType): Flow<List<ProgressPhoto>> = flowOf(photos.filter { it.type == type })

    override suspend fun savePhotos(photos: List<NewProgressPhoto>) = Unit

    override suspend fun deletePhoto(photoId: String) = Unit
}

class IsPhotoReminderDueUseCaseTest {

    private fun useCase(photos: List<ProgressPhoto>, isEnabled: Boolean = true) = IsPhotoReminderDueUseCase(
        FakeSetupRepository(Setup(isPhotoReminderEnabled = isEnabled)),
        FakePhotoRepository(photos),
        AppDayClock(FixedClock(LocalDateTime(WEIGH_IN_DAY, MORNING).toInstant(LIMA)), LIMA),
    )

    private fun photoTakenDaysAgo(days: Int) =
        ProgressPhoto("$days", WEIGH_IN_DAY.minus(days, DateTimeUnit.DAY), PhotoType.TRUNK, "photo-$days.jpg", 0L)

    @Test
    fun `is not due when the latest progress photo is 27 days old`() = runTest {
        assertFalse(useCase(photos = listOf(photoTakenDaysAgo(40), photoTakenDaysAgo(27))).invoke())
    }

    @Test
    fun `is due when the latest progress photo is 28 days old`() = runTest {
        assertTrue(useCase(photos = listOf(photoTakenDaysAgo(28), photoTakenDaysAgo(56))).invoke())
    }

    @Test
    fun `is due when no progress photo exists`() = runTest {
        assertTrue(useCase(photos = emptyList()).invoke())
    }

    @Test
    fun `is not due when the photo reminder is off`() = runTest {
        assertFalse(useCase(photos = emptyList(), isEnabled = false).invoke())
    }
}
