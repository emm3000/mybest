package com.emm.mybest.features.settings.presentation

import app.cash.turbine.test
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.reminder.WeightReminderScheduler
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
import kotlinx.datetime.DayOfWeek
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
    private val scheduler = mockk<WeightReminderScheduler>()

    private fun buildViewModel(
        versionName: String = "1.0",
        versionCode: Int = 1,
        setupRepository: FakeSetupRepository = FakeSetupRepository(),
    ): SettingsViewModel = SettingsViewModel(
        userPreferencesRepository = preferencesRepository,
        exportDatabaseBackupUseCase = exportUseCase,
        restoreDatabaseBackupUseCase = restoreUseCase,
        goalsEditor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), UpdateSetupUseCase(setupRepository)),
        remindersEditor = SettingsRemindersEditor(
            ObserveSetupUseCase(setupRepository),
            UpdateSetupUseCase(setupRepository),
            UpdateWeighInReminderTimeUseCase(setupRepository, scheduler),
        ),
        appVersionName = versionName,
        appVersionCode = versionCode,
    )

    private fun stubDefaults() {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(RestoreResult.RequiresRestart)
        coEvery { scheduler.schedule(any()) } returns Unit
        coEvery { scheduler.cancel() } returns Unit
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
        coEvery { scheduler.schedule(any()) } returns Unit
        coEvery { scheduler.cancel() } returns Unit
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

    @Test
    fun `opening a reminder shows its picker and dismissing closes it`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.state.test {
            viewModel.onIntent(SettingsIntent.OnReminderOpen(ReminderField.CHECK_IN_TIME))
            advanceUntilIdle()
            assertEquals(ReminderField.CHECK_IN_TIME, expectMostRecentItem().reminderPicker)
            viewModel.onIntent(SettingsIntent.OnReminderDismiss)
            advanceUntilIdle()
            assertEquals(null, expectMostRecentItem().reminderPicker)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setting a reminder time saves it and closes the picker`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository(Setup(monthlyOutingLimit = 2))
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.state.test {
            viewModel.onIntent(SettingsIntent.OnReminderOpen(ReminderField.PRE_OUTING_TIME))
            viewModel.onIntent(SettingsIntent.OnReminderTimeChange(20, 30))
            advanceUntilIdle()
            assertEquals(null, expectMostRecentItem().reminderPicker)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(Setup(monthlyOutingLimit = 2, preOutingReminderTime = LocalTime(20, 30))), repository.saved)
        coVerify(exactly = 0) { scheduler.schedule(any()) }
    }

    @Test
    fun `clearing a reminder time leaves it unset`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository(Setup(debtReminderTime = LocalTime(9, 0)))
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnReminderClear(ReminderField.DEBT_TIME))
        advanceUntilIdle()

        assertEquals(listOf(Setup()), repository.saved)
    }

    @Test
    fun `setting the weigh-in time saves it and schedules the weight reminder`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository()
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnReminderOpen(ReminderField.WEIGH_IN_TIME))
        viewModel.onIntent(SettingsIntent.OnReminderTimeChange(7, 0))
        advanceUntilIdle()

        assertEquals(listOf(Setup(weighInReminderTime = LocalTime(7, 0))), repository.saved)
        coVerify(exactly = 1) { scheduler.schedule(LocalTime(7, 0)) }
    }

    @Test
    fun `clearing the weigh-in time cancels the weight reminder`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository(Setup(weighInReminderTime = LocalTime(7, 0)))
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnReminderClear(ReminderField.WEIGH_IN_TIME))
        advanceUntilIdle()

        assertEquals(listOf(Setup()), repository.saved)
        coVerify(exactly = 1) { scheduler.cancel() }
    }

    @Test
    fun `setting and clearing the weigh-in day`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository()
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnReminderOpen(ReminderField.WEIGH_IN_DAY))
        viewModel.onIntent(SettingsIntent.OnWeighInDayChange(DayOfWeek.MONDAY))
        advanceUntilIdle()
        viewModel.onIntent(SettingsIntent.OnReminderClear(ReminderField.WEIGH_IN_DAY))
        advanceUntilIdle()

        assertEquals(listOf(Setup(weighInDay = DayOfWeek.MONDAY), Setup()), repository.saved)
    }

    @Test
    fun `toggling the photo reminder saves it`() = runTest {
        stubDefaults()
        val repository = FakeSetupRepository()
        val viewModel = buildViewModel(setupRepository = repository)

        viewModel.onIntent(SettingsIntent.OnPhotoReminderToggle(isEnabled = true))
        advanceUntilIdle()
        viewModel.onIntent(SettingsIntent.OnPhotoReminderToggle(isEnabled = false))
        advanceUntilIdle()

        assertEquals(listOf(Setup(isPhotoReminderEnabled = true), Setup()), repository.saved)
    }
}
