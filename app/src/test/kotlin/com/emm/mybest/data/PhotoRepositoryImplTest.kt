package com.emm.mybest.data

import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import com.emm.mybest.data.entities.PhotoType as DataPhotoType

class PhotoRepositoryImplTest {

    private val dao = mockk<ProgressPhotoDao>()
    private val repository = PhotoRepositoryImpl(dao)

    @Test
    fun `getAllPhotos maps dao entities to domain models`() = runTest {
        val entity = ProgressPhotoEntity(
            id = "p1",
            date = LocalDate(2026, 3, 8),
            type = DataPhotoType.FACE,
            photoPath = "/tmp/face.jpg",
            createdAt = 123L,
        )
        every { dao.observeAll() } returns flowOf(listOf(entity))

        val result = repository.getAllPhotos().first()

        assertEquals(1, result.size)
        assertEquals("p1", result.first().id)
        assertEquals(PhotoType.FACE, result.first().type)
        assertEquals("/tmp/face.jpg", result.first().photoPath)
    }

    @Test
    fun `getPhotosByType delegates type and maps output`() = runTest {
        val entity = ProgressPhotoEntity(
            id = "p2",
            date = LocalDate(2026, 3, 8),
            type = DataPhotoType.BODY,
            photoPath = "/tmp/body.jpg",
            createdAt = 999L,
        )
        every { dao.observeByType(DataPhotoType.BODY) } returns flowOf(listOf(entity))

        val result = repository.getPhotosByType(PhotoType.BODY).first()

        assertEquals(1, result.size)
        assertEquals("p2", result.first().id)
        assertEquals(PhotoType.BODY, result.first().type)
        assertEquals("/tmp/body.jpg", result.first().photoPath)
    }

    @Test
    fun `savePhotos maps domain models to entities before insertAll`() = runTest {
        val captured = slot<List<ProgressPhotoEntity>>()
        coEvery { dao.insertAll(capture(captured)) } returns Unit
        val photos = listOf(
            NewProgressPhoto(
                photoPath = "/tmp/a.jpg",
                type = PhotoType.ABDOMEN,
                date = LocalDate(2026, 3, 7),
            ),
            NewProgressPhoto(
                photoPath = "/tmp/b.jpg",
                type = PhotoType.FACE,
                date = LocalDate(2026, 3, 8),
            ),
        )

        repository.savePhotos(photos)

        coVerify(exactly = 1) { dao.insertAll(any()) }
        assertEquals(2, captured.captured.size)
        assertEquals(DataPhotoType.ABDOMEN, captured.captured[0].type)
        assertEquals("/tmp/a.jpg", captured.captured[0].photoPath)
        assertEquals(LocalDate(2026, 3, 8), captured.captured[1].date)
    }

    @Test
    fun `deletePhoto delegates to dao`() = runTest {
        coEvery { dao.deleteById("p3") } returns Unit

        repository.deletePhoto("p3")

        coVerify(exactly = 1) { dao.deleteById("p3") }
    }
}
