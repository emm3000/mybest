package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.testing.SlowSetupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
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
}
