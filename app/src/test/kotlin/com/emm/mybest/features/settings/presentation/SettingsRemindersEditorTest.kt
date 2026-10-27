package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.SlowSetupRepository
import com.emm.mybest.testing.failingUpdateSetupUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

private const val SLOW_SAVE_MS = 1_000L

class SettingsRemindersEditorTest {

    @Test
    fun `two quick changes both survive a slow save`() = runTest {
        val repository = SlowSetupRepository(SLOW_SAVE_MS)
        val editor = SettingsRemindersEditor(ObserveSetupUseCase(repository), UpdateSetupUseCase(repository))

        launch { editor.handle(SettingsIntent.OnPhotoReminderToggle(isEnabled = true)) }
        launch { editor.handle(SettingsIntent.OnWeighInDayChange(DayOfWeek.MONDAY)) }
        advanceUntilIdle()

        assertEquals(Setup(isPhotoReminderEnabled = true, weighInDay = DayOfWeek.MONDAY), repository.observe().first())
    }

    @Test
    fun `a cancelled save rethrows the cancellation instead of reporting a failure`() = runTest {
        val cancellation = CancellationException("stop")
        val editor = SettingsRemindersEditor(
            ObserveSetupUseCase(FakeSetupRepository()),
            failingUpdateSetupUseCase(cancellation),
        )

        try {
            editor.handle(SettingsIntent.OnPhotoReminderToggle(isEnabled = true))
            fail("expected CancellationException")
        } catch (thrown: CancellationException) {
            assertSame(cancellation, thrown)
        }
    }

    @Test
    fun `a save after a cancelled save still completes`() = runTest {
        val update = mockk<UpdateSetupUseCase>()
        coEvery { update(any()) } throws CancellationException("stop") andThen emptyList()
        val editor = SettingsRemindersEditor(ObserveSetupUseCase(FakeSetupRepository()), update)

        runCatching { editor.handle(SettingsIntent.OnPhotoReminderToggle(isEnabled = true)) }
        val effect = editor.handle(SettingsIntent.OnPhotoReminderToggle(isEnabled = true))

        assertEquals(null, effect)
    }

    @Test
    fun `a failed save reports a reminder save failure`() = runTest {
        val editor =
            SettingsRemindersEditor(
                ObserveSetupUseCase(FakeSetupRepository()),
                failingUpdateSetupUseCase(IllegalStateException()),
            )

        val effect = editor.handle(SettingsIntent.OnPhotoReminderToggle(isEnabled = true))

        assertEquals(SettingsEffect.ReminderSaveFailed, effect)
    }
}
