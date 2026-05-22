package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.domain.usecase.weight.GetNearestWeightOnDateUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

private fun photo(id: String, type: PhotoType, createdAt: Long): ProgressPhoto = ProgressPhoto(
    id = id,
    date = LocalDate(2026, 1, (createdAt % 28 + 1).toInt()),
    type = type,
    photoPath = "/tmp/$id.jpg",
    createdAt = createdAt,
)

class GetPhotosOverviewUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photoRepository = mockk<PhotoRepository>()
    private val weightRepository = mockk<WeightRepository>()

    private fun buildUseCase() = GetPhotosOverviewUseCase(
        photoRepository = photoRepository,
        getNearestWeightOnDateUseCase = GetNearestWeightOnDateUseCase(weightRepository),
    )

    @Test
    fun `empty photos returns empty overviews for all types`() = runTest {
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())

        val result = buildUseCase().invoke().first()

        PhotoType.entries.forEach { type ->
            val overview = result.byType[type]
            assertNull(overview?.before)
            assertNull(overview?.after)
            assertEquals(0, overview?.count ?: 0)
        }
    }

    @Test
    fun `single photo becomes before only, no after`() = runTest {
        val trunkPhoto = photo("t1", PhotoType.TRUNK, 100)
        every { photoRepository.getAllPhotos() } returns flowOf(listOf(trunkPhoto))
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())

        val result = buildUseCase().invoke().first()
        val overview = result.byType[PhotoType.TRUNK]!!

        assertEquals(trunkPhoto, overview.before)
        assertNull(overview.after)
        assertEquals(1, overview.count)
    }

    @Test
    fun `before is oldest by createdAt, after is newest`() = runTest {
        val older = photo("t1", PhotoType.TRUNK, 100)
        val newer = photo("t2", PhotoType.TRUNK, 200)
        every { photoRepository.getAllPhotos() } returns flowOf(listOf(newer, older))
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())

        val result = buildUseCase().invoke().first()
        val overview = result.byType[PhotoType.TRUNK]!!

        assertEquals(older, overview.before)
        assertEquals(newer, overview.after)
    }

    @Test
    fun `timeline is sorted ascending by createdAt`() = runTest {
        val p1 = photo("t1", PhotoType.TRUNK, 100)
        val p2 = photo("t2", PhotoType.TRUNK, 300)
        val p3 = photo("t3", PhotoType.TRUNK, 200)
        every { photoRepository.getAllPhotos() } returns flowOf(listOf(p2, p1, p3))
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())

        val result = buildUseCase().invoke().first()
        val overview = result.byType[PhotoType.TRUNK]!!

        assertEquals(listOf(p1, p3, p2), overview.timeline)
    }

    @Test
    fun `count by type reflects each type separately`() = runTest {
        val trunk = photo("t1", PhotoType.TRUNK, 100)
        val face1 = photo("f1", PhotoType.FACE, 200)
        val face2 = photo("f2", PhotoType.FACE, 300)
        every { photoRepository.getAllPhotos() } returns flowOf(listOf(trunk, face1, face2))
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())

        val result = buildUseCase().invoke().first()

        assertEquals(1, result.byType[PhotoType.TRUNK]!!.count)
        assertEquals(2, result.byType[PhotoType.FACE]!!.count)
    }

    @Test
    fun `weight pairing is nullable when no weight entries`() = runTest {
        val trunkPhoto = photo("t1", PhotoType.TRUNK, 100)
        every { photoRepository.getAllPhotos() } returns flowOf(listOf(trunkPhoto))
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())

        val result = buildUseCase().invoke().first()
        val overview = result.byType[PhotoType.TRUNK]!!

        assertNull(overview.beforeWeightKg)
        assertNull(overview.afterWeightKg)
    }

    @Test
    fun `weight is resolved for before photo when entry is nearby`() = runTest {
        val trunkPhoto = ProgressPhoto(
            id = "t1",
            date = LocalDate(2026, 1, 15),
            type = PhotoType.TRUNK,
            photoPath = "/tmp/t1.jpg",
            createdAt = 100,
        )
        val weightEntry = WeightEntry(
            id = "w1",
            date = LocalDate(2026, 1, 15),
            weight = 80f,
        )
        every { photoRepository.getAllPhotos() } returns flowOf(listOf(trunkPhoto))
        every { weightRepository.getWeightProgress() } returns flowOf(listOf(weightEntry))

        val result = buildUseCase().invoke().first()
        val overview = result.byType[PhotoType.TRUNK]!!

        assertEquals(80f, overview.beforeWeightKg)
    }
}
