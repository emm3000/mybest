package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.validation.SetupValidationError
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.SlowSetupRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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

    @Test
    fun `saving a second goal during a slow save keeps both goals`() = runTest {
        val repository = SlowSetupRepository(SLOW_SAVE_MS)
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(repository), UpdateSetupUseCase(repository))

        editor.handle(SettingsIntent.OnFieldOpen(SettingsField.MONTHLY_OUTING_LIMIT))
        editor.handle(SettingsIntent.OnFieldInputChange("3"))
        launch { editor.handle(SettingsIntent.OnFieldSave) }
        runCurrent()
        editor.handle(SettingsIntent.OnFieldOpen(SettingsField.WEEKLY_GYM))
        editor.handle(SettingsIntent.OnFieldInputChange("4"))
        launch { editor.handle(SettingsIntent.OnFieldSave) }
        advanceUntilIdle()

        assertEquals(
            Setup(monthlyOutingLimit = 3, weeklyTargets = mapOf(DailyCheckType.GYM to 4)),
            repository.observe().first(),
        )
    }

    @Test
    fun `a slow successful save does not close the goal opened meanwhile`() = runTest {
        val repository = SlowSetupRepository(SLOW_SAVE_MS)
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(repository), UpdateSetupUseCase(repository))

        openSecondGoalDuringSlow(editor, SettingsIntent.OnFieldSave)

        assertEquals(SettingsEditorState(SettingsField.WEEKLY_GYM, "4"), editor.goals.first().editor)
    }

    @Test
    fun `a slow rejected save does not show its error on the goal opened meanwhile`() = runTest {
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), rejectingSlowUpdate())

        openSecondGoalDuringSlow(editor, SettingsIntent.OnFieldSave)

        assertEquals(SettingsEditorState(SettingsField.WEEKLY_GYM, "4"), editor.goals.first().editor)
    }

    @Test
    fun `a slow successful clear does not close the goal opened meanwhile`() = runTest {
        val repository = SlowSetupRepository(SLOW_SAVE_MS)
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(repository), UpdateSetupUseCase(repository))

        openSecondGoalDuringSlow(editor, SettingsIntent.OnFieldClear)

        assertEquals(SettingsEditorState(SettingsField.WEEKLY_GYM, "4"), editor.goals.first().editor)
    }

    @Test
    fun `a slow rejected clear does not show its error on the goal opened meanwhile`() = runTest {
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), rejectingSlowUpdate())

        openSecondGoalDuringSlow(editor, SettingsIntent.OnFieldClear)

        assertEquals(SettingsEditorState(SettingsField.WEEKLY_GYM, "4"), editor.goals.first().editor)
    }

    private fun rejectingSlowUpdate(): UpdateSetupUseCase {
        val slowUpdate = mockk<UpdateSetupUseCase>()
        coEvery { slowUpdate(any()) } coAnswers {
            delay(SLOW_SAVE_MS)
            listOf(SetupValidationError.NonPositiveTargetWeight)
        }
        return slowUpdate
    }

    private suspend fun TestScope.openSecondGoalDuringSlow(
        editor: SettingsGoalsEditor,
        slowIntent: SettingsIntent.FieldIntent,
    ) {
        editor.handle(SettingsIntent.OnFieldOpen(SettingsField.MONTHLY_OUTING_LIMIT))
        editor.handle(SettingsIntent.OnFieldInputChange("3"))
        launch { editor.handle(slowIntent) }
        runCurrent()
        editor.handle(SettingsIntent.OnFieldOpen(SettingsField.WEEKLY_GYM))
        editor.handle(SettingsIntent.OnFieldInputChange("4"))
        advanceUntilIdle()
    }
}
