package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.validation.SetupValidationError
import com.emm.mybest.testing.FakeSetupRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val SLOW_SAVE_MS = 1_000L

class SettingsGoalsEditorTest {

    private val setupRepository = FakeSetupRepository()

    @Test
    fun `typing during a slow rejected save keeps the typed value`() = runTest {
        val slowUpdate = mockk<UpdateSetupUseCase>()
        coEvery { slowUpdate(any()) } coAnswers {
            delay(SLOW_SAVE_MS)
            listOf(SetupValidationError.NonPositiveTargetWeight)
        }
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), slowUpdate)

        editor.handle(SettingsIntent.OnFieldOpen(SettingsField.TARGET_WEIGHT))
        editor.handle(SettingsIntent.OnFieldInputChange("0"))
        launch { editor.handle(SettingsIntent.OnFieldSave) }
        runCurrent()
        editor.handle(SettingsIntent.OnFieldInputChange("72"))
        advanceUntilIdle()

        assertEquals("72", editor.goals.first().editor?.input)
    }
}
