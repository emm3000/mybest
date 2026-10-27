package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

private val REPLACED_DATE = LocalDate(2026, 11, 9)

private val OLD_PHOTO = ProgressPhoto(
    id = "p1",
    date = REPLACED_DATE,
    type = PhotoType.FACE,
    photoPath = "content://old",
    createdAt = 1_000L,
)

class ReplacePhotoUseCaseTest {

    private val repository = mockk<PhotoRepository>(relaxed = true).also {
        every { it.getAllPhotos() } returns flowOf(listOf(OLD_PHOTO))
    }
    private val useCase = ReplacePhotoUseCase(repository)

    @Test
    fun `saves the new photo with the type, date and stamp of the old one, then deletes the old one`() = runTest {
        val result = useCase("p1", "content://new")

        assertEquals(ReplacePhotoResult.Replaced, result)
        val replacement = NewProgressPhoto("content://new", PhotoType.FACE, REPLACED_DATE, createdAt = 1_000L)
        coVerifyOrder {
            repository.savePhotos(listOf(replacement))
            repository.deletePhoto("p1")
        }
    }

    @Test
    fun `keeps the old photo when saving the new one fails`() = runTest {
        val failure = IllegalStateException("disk full")
        coEvery { repository.savePhotos(any()) } throws failure

        val thrown = runCatching { useCase("p1", "content://new") }.exceptionOrNull()

        assertSame(failure, thrown)
        coVerify(exactly = 0) { repository.deletePhoto(any()) }
    }

    @Test
    fun `propagates the failure when deleting the old photo fails after saving`() = runTest {
        val failure = IllegalStateException("locked")
        coEvery { repository.deletePhoto("p1") } throws failure

        val thrown = runCatching { useCase("p1", "content://new") }.exceptionOrNull()

        assertSame(failure, thrown)
        coVerify(exactly = 1) { repository.savePhotos(any()) }
    }

    @Test
    fun `reports a missing photo without saving or deleting anything`() = runTest {
        val result = useCase("missing", "content://new")

        assertEquals(ReplacePhotoResult.PhotoNotFound, result)
        coVerify(exactly = 0) { repository.savePhotos(any()) }
        coVerify(exactly = 0) { repository.deletePhoto(any()) }
    }
}
