package com.emm.mybest.features.photo.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.repository.HabitRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddPhotoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<PhotoRepository>()
    private val habitRepository = mockk<HabitRepository>()

    private fun buildViewModel(): AddPhotoViewModel {
        every { habitRepository.getAllHabits() } returns flowOf(emptyList())
        return AddPhotoViewModel(repository, habitRepository)
    }

    @Test
    fun `OnPhotosSelected and OnTypeSelected update selection state`() {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg", "b.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(1, PhotoType.BODY))

        val state = viewModel.state.value
        assertEquals(2, state.selectedPhotos.size)
        assertEquals(PhotoType.FACE, state.selectedPhotos[0].type)
        assertEquals(PhotoType.BODY, state.selectedPhotos[1].type)
    }

    @Test
    fun `OnRemovePhoto removes selected photo by index`() {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg", "b.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnRemovePhoto(0))

        assertEquals(listOf("b.jpg"), viewModel.state.value.selectedPhotos.map { it.uri })
    }

    @Test
    fun `OnTypeSelected and OnRemovePhoto ignore invalid indexes`() {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(8, PhotoType.BODY))
        viewModel.onIntent(AddPhotoIntent.OnRemovePhoto(7))

        assertEquals(1, viewModel.state.value.selectedPhotos.size)
        assertEquals("a.jpg", viewModel.state.value.selectedPhotos.first().uri)
        assertEquals(PhotoType.FACE, viewModel.state.value.selectedPhotos.first().type)
    }

    @Test
    fun `OnSaveClick emits error when there are no selected photos`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            assertEquals(
                AddPhotoEffect.ShowError("Debes seleccionar al menos una foto"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSaveClick success maps and saves photos then navigates back`() = runTest {
        val captured = slot<List<NewProgressPhoto>>()
        coEvery { repository.savePhotos(capture(captured)) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(0, PhotoType.BODY))

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            advanceUntilIdle()

            assertEquals(AddPhotoEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.savePhotos(any()) }
        assertEquals(1, captured.captured.size)
        assertEquals("a.jpg", captured.captured.first().photoPath)
        assertEquals(PhotoType.BODY, captured.captured.first().type)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `OnSaveClick error from repository emits ShowError`() = runTest {
        coEvery { repository.savePhotos(any()) } throws IllegalStateException("save fail")
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg")))

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            advanceUntilIdle()

            assertEquals(AddPhotoEffect.ShowError("Error al guardar: save fail"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `selected habit id is assigned to saved photos`() = runTest {
        val habits = listOf(
            Habit(
                id = "habit-1",
                name = "Entrenar",
                icon = "FitnessCenter",
                color = 1,
                category = "Salud",
                type = HabitType.BOOLEAN,
                scheduledDays = setOf(DayOfWeek.MONDAY),
            ),
        )
        every { habitRepository.getAllHabits() } returns flowOf(habits)
        val captured = slot<List<NewProgressPhoto>>()
        coEvery { repository.savePhotos(capture(captured)) } returns Unit
        val viewModel = AddPhotoViewModel(repository, habitRepository)

        viewModel.onIntent(AddPhotoIntent.OnHabitSelected("habit-1"))
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnSaveClick)
        advanceUntilIdle()

        assertEquals("habit-1", captured.captured.first().habitId)
    }
}
