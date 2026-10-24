package com.emm.mybest.features.home.presentation

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

private val FIXED_DATE = LocalDate(2026, 5, 17)
private val FIXED_CLOCK = object : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(1_779_019_200L)
}

class HomeLastPhotoTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(photos: List<ProgressPhoto>): HomeViewModel {
        val appDayClock = AppDayClock(FIXED_CLOCK, TimeZone.UTC)
        return HomeViewModel(HomeUseCasesFixture(photos = photos).build(appDayClock), appDayClock)
    }

    @Test
    fun `photos with type TRUNK and last 4 days ago produces correct state`() = runTest {
        val photoDate = FIXED_DATE.minus(DatePeriod(days = 4))
        val photos = listOf(
            ProgressPhoto(
                id = "p1",
                date = photoDate,
                type = PhotoType.TRUNK,
                photoPath = "/path/photo.jpg",
                createdAt = 0L,
            ),
        )
        val viewModel = buildViewModel(photos)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PhotoType.TRUNK, state.lastPhotoType)
        assertEquals(4, state.lastPhotoDaysAgo)
    }

    @Test
    fun `same-day photos pick the latest created whatever the list order`() = runTest {
        val trunk = ProgressPhoto("p1", FIXED_DATE, PhotoType.TRUNK, "/path/trunk.jpg", createdAt = 1L)
        val face = ProgressPhoto("p2", FIXED_DATE, PhotoType.FACE, "/path/face.jpg", createdAt = 2L)

        listOf(listOf(trunk, face), listOf(face, trunk)).forEach { photos ->
            val viewModel = buildViewModel(photos)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(PhotoType.FACE, state.lastPhotoType)
            assertEquals(0, state.lastPhotoDaysAgo)
        }
    }

    @Test
    fun `a later day wins over a same-day tie on an earlier day`() = runTest {
        val earlierDay = FIXED_DATE.minus(DatePeriod(days = 1))
        val photos = listOf(
            ProgressPhoto("p1", earlierDay, PhotoType.TRUNK, "/path/a.jpg", createdAt = 9L),
            ProgressPhoto("p2", FIXED_DATE, PhotoType.TRUNK, "/path/b.jpg", createdAt = 1L),
            ProgressPhoto("p3", earlierDay, PhotoType.FACE, "/path/c.jpg", createdAt = 10L),
        )
        val viewModel = buildViewModel(photos)
        advanceUntilIdle()

        assertEquals(PhotoType.TRUNK, viewModel.state.value.lastPhotoType)
        assertEquals(0, viewModel.state.value.lastPhotoDaysAgo)
    }
}
