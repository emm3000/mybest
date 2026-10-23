package com.emm.mybest.features.settings.presentation

import app.cash.turbine.test
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.RestoreResult
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.usecase.UpdateWeighInReminderTimeUseCase
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<UserPreferencesRepository>()
    private val exportUseCase = mockk<ExportDatabaseBackupUseCase>()
    private val restoreUseCase = mockk<RestoreDatabaseBackupUseCase>()
    private val updateWeighInReminderTimeUseCase = mockk<UpdateWeighInReminderTimeUseCase>()

    private fun buildViewModel(
        versionName: String = "1.0",
        versionCode: Int = 1,
        setupRepository: FakeSetupRepository = FakeSetupRepository(),
    ): SettingsViewModel = SettingsViewModel(
        userPreferencesRepository = preferencesRepository,
        exportDatabaseBackupUseCase = exportUseCase,
        restoreDatabaseBackupUseCase = restoreUseCase,
        updateWeighInReminderTimeUseCase = updateWeighInReminderTimeUseCase,
        goalsEditor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), UpdateSetupUseCase(setupRepository)),
        appVersionName = versionName,
        appVersionCode = versionCode,
    )

    private fun stubDefaults() {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(RestoreResult.RequiresRestart)
        coEvery { updateWeighInReminderTimeUseCase.invoke(any()) } returns Unit
    }

    @Test
    fun `initial state exposes correct appVersionLabel`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel(versionName = "1.0", versionCode = 1)

        viewModel.state.test {
            val item = awaitItem()
            assertEquals("v1.0 · 1", item.appVersionLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state maps notifications preference`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(false)
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(RestoreResult.RequiresRestart)
        coEvery { updateWeighInReminderTimeUseCase.invoke(any()) } returns Unit
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            assertEquals(false, awaitItem().notificationsEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnNotificationsToggle updates preference`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnNotificationsToggle(false))
        advanceUntilIdle()

        coVerify(exactly = 1) { preferencesRepository.updateNotificationsEnabled(false) }
    }

    @Test
    fun `OnExportBackup invokes export use case`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnExportBackup("content://backup"))
        advanceUntilIdle()

        coVerify(exactly = 1) { exportUseCase.invoke("content://backup") }
    }

    @Test
    fun `OnImportBackup invokes restore use case`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))
        advanceUntilIdle()

        coVerify(exactly = 1) { restoreUseCase.invoke("content://backup") }
    }

    @Test
    fun `OnDefaultReminderTimeChange invokes update use case with LocalTime`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnDefaultReminderTimeChange(7, 30))
        advanceUntilIdle()

        coVerify(exactly = 1) { updateWeighInReminderTimeUseCase.invoke(LocalTime(7, 30)) }
    }

    @Test
    fun `OnWeightReminderToggleOff invokes update use case with null`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(RestoreResult.RequiresRestart)
        coEvery { updateWeighInReminderTimeUseCase.invoke(any()) } returns Unit
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnWeightReminderToggleOff)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateWeighInReminderTimeUseCase.invoke(null) }
    }

    @Test
    fun `fresh install exposes an all-unset setup and no editor`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.state.test {
            val goals = expectMostRecentItem().goals
            assertEquals(Setup(), goals.setup)
            assertEquals(null, goals.editor)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `opening a field prefills its current value`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository(Setup(monthlyOutingLimit = 3))
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.state.test {
            viewModel.onIntent(SettingsIntent.OnFieldOpen(SettingsField.MONTHLY_OUTING_LIMIT))
            advanceUntilIdle()
            assertEquals(
                SettingsEditorState(SettingsField.MONTHLY_OUTING_LIMIT, "3"),
                expectMostRecentItem().goals.editor,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `valid save reaches the repository with only that field changed`() = runTest {
        stubDefaults()
        val existing = Setup(targetWeightKg = 70f, weeklyTargets = mapOf(DailyCheckType.GYM to 4))
        val repository = FakeSetupRepository(existing)
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnFieldOpen(SettingsField.MONTHLY_OUTING_LIMIT))
        viewModel.onIntent(SettingsIntent.OnFieldInputChange("3"))
        viewModel.onIntent(SettingsIntent.OnFieldSave)
        advanceUntilIdle()

        assertEquals(listOf(existing.copy(monthlyOutingLimit = 3)), repository.saved)
    }

    @Test
    fun `weekly target of eight shows an error and saves nothing`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository()
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.state.test {
            viewModel.onIntent(SettingsIntent.OnFieldOpen(SettingsField.WEEKLY_GYM))
            viewModel.onIntent(SettingsIntent.OnFieldInputChange("8"))
            viewModel.onIntent(SettingsIntent.OnFieldSave)
            advanceUntilIdle()
            assertEquals(SettingsFieldError.OUT_OF_RANGE, expectMostRecentItem().goals.editor?.error)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(emptyList<Setup>(), repository.saved)
    }

    @Test
    fun `text that is not a number shows an error and saves nothing`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository()
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.state.test {
            viewModel.onIntent(SettingsIntent.OnFieldOpen(SettingsField.TARGET_WEIGHT))
            viewModel.onIntent(SettingsIntent.OnFieldInputChange("abc"))
            viewModel.onIntent(SettingsIntent.OnFieldSave)
            advanceUntilIdle()
            assertEquals(SettingsFieldError.INVALID_NUMBER, expectMostRecentItem().goals.editor?.error)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(emptyList<Setup>(), repository.saved)
    }

    @Test
    fun `clear sets the field back to unset`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository(
            Setup(monthlyOutingLimit = 3, weeklyTargets = mapOf(DailyCheckType.GYM to 4, DailyCheckType.DIET to 5)),
        )
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnFieldOpen(SettingsField.WEEKLY_GYM))
        viewModel.onIntent(SettingsIntent.OnFieldClear)
        advanceUntilIdle()

        assertEquals(
            listOf(Setup(monthlyOutingLimit = 3, weeklyTargets = mapOf(DailyCheckType.DIET to 5))),
            repository.saved,
        )
    }

    @Test
    fun `money input is parsed with Money parseOrNull`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository()
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnFieldOpen(SettingsField.TYPICAL_OUTING_COST))
        viewModel.onIntent(SettingsIntent.OnFieldInputChange("120,50"))
        viewModel.onIntent(SettingsIntent.OnFieldSave)
        advanceUntilIdle()

        assertEquals(listOf(Setup(typicalOutingCost = Money(12_050))), repository.saved)
    }

    @Test
    fun `a successful save closes the editor and announces it`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.OnFieldOpen(SettingsField.WEEKLY_DIET))
            viewModel.onIntent(SettingsIntent.OnFieldInputChange("5"))
            viewModel.onIntent(SettingsIntent.OnFieldSave)
            advanceUntilIdle()
            assertEquals(SettingsEffect.GoalSaved, awaitItem())
        }
        viewModel.state.test {
            assertEquals(null, expectMostRecentItem().goals.editor)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
