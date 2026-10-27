package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.validation.SetupValidationError
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.SlowSetupRepository
import com.emm.mybest.testing.failingUpdateSetupUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

private const val SLOW_SAVE_MS = 1_000L

class SettingsGoalsEditorTest {

    private val setupRepository = FakeSetupRepository()

    @Test
    fun `typing during a slow rejected save keeps the typed value`() = runTest {
        val slowUpdate = mockk<UpdateSetupUseCase>()
        coEvery { slowUpdate(any()) } coAnswers {
            delay(SLOW_SAVE_MS)
            listOf(SetupValidationError.TargetWeightOutOfRange)
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
    fun `an out-of-range target weight is rejected and nothing is stored`() = runTest {
        val repository = FakeSetupRepository(Setup(targetWeightKg = 70f))
        val editor = editorOn(repository)

        val effect = saveTargetWeight(editor, "9999")

        assertEquals(null, effect)
        assertEquals(
            SettingsEditorState(SettingsField.TARGET_WEIGHT, "9999", SettingsFieldError.WEIGHT_OUT_OF_RANGE),
            editor.goals.first().editor,
        )
        assertEquals(emptyList<Setup>(), repository.saved)
    }

    @Test
    fun `scientific notation is an invalid number and nothing is stored`() = runTest {
        val repository = FakeSetupRepository(Setup(targetWeightKg = 70f))
        val editor = editorOn(repository)

        val effect = saveTargetWeight(editor, "1e3")

        assertEquals(null, effect)
        assertEquals(SettingsFieldError.INVALID_NUMBER, editor.goals.first().editor?.error)
        assertEquals(emptyList<Setup>(), repository.saved)
    }

    @Test
    fun `a target weight with a decimal comma is stored`() = runTest {
        val repository = FakeSetupRepository()
        val editor = editorOn(repository)

        val effect = saveTargetWeight(editor, "75,5")

        assertEquals(SettingsEffect.GoalSaved, effect)
        assertEquals(75.5f, repository.observe().first().targetWeightKg)
    }

    @Test
    fun `clearing the target weight stores null`() = runTest {
        val repository = FakeSetupRepository(Setup(targetWeightKg = 70f))
        val editor = editorOn(repository)

        editor.handle(SettingsIntent.OnFieldOpen(SettingsField.TARGET_WEIGHT))
        val effect = editor.handle(SettingsIntent.OnFieldClear)

        assertEquals(SettingsEffect.GoalSaved, effect)
        assertEquals(null, repository.observe().first().targetWeightKg)
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

    @Test
    fun `a cancelled save rethrows the cancellation instead of reporting a failure`() = runTest {
        val cancellation = CancellationException("stop")
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), failingUpdateSetupUseCase(cancellation))

        try {
            saveTargetWeight(editor, "72")
            fail("expected CancellationException")
        } catch (thrown: CancellationException) {
            assertSame(cancellation, thrown)
        }
    }

    @Test
    fun `a save after a cancelled save still completes`() = runTest {
        val update = mockk<UpdateSetupUseCase>()
        coEvery { update(any()) } throws CancellationException("stop") andThen emptyList()
        val editor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), update)

        runCatching { saveTargetWeight(editor, "72") }
        val effect = editor.handle(SettingsIntent.OnFieldSave)

        assertEquals(SettingsEffect.GoalSaved, effect)
    }

    @Test
    fun `a failed save reports a goal save failure`() = runTest {
        val editor =
            SettingsGoalsEditor(
                ObserveSetupUseCase(setupRepository),
                failingUpdateSetupUseCase(IllegalStateException()),
            )

        val effect = saveTargetWeight(editor, "72")

        assertEquals(SettingsEffect.GoalSaveFailed, effect)
    }

    private fun editorOn(repository: FakeSetupRepository) =
        SettingsGoalsEditor(ObserveSetupUseCase(repository), UpdateSetupUseCase(repository))

    private suspend fun saveTargetWeight(editor: SettingsGoalsEditor, input: String): SettingsEffect? {
        editor.handle(SettingsIntent.OnFieldOpen(SettingsField.TARGET_WEIGHT))
        editor.handle(SettingsIntent.OnFieldInputChange(input))
        return editor.handle(SettingsIntent.OnFieldSave)
    }

    private fun rejectingSlowUpdate(): UpdateSetupUseCase {
        val slowUpdate = mockk<UpdateSetupUseCase>()
        coEvery { slowUpdate(any()) } coAnswers {
            delay(SLOW_SAVE_MS)
            listOf(SetupValidationError.TargetWeightOutOfRange)
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
