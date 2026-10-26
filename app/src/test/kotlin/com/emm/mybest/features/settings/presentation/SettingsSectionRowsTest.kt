package com.emm.mybest.features.settings.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.repository.AppRestarter
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.usecase.capture.ObserveCaptureSummaryUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeNotificationCaptureRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsSectionRowsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(): SettingsViewModel {
        val setupRepository = FakeSetupRepository()
        return SettingsViewModel(
            dependencies = SettingsDependencies(
                userPreferencesRepository = mockk<UserPreferencesRepository>(),
                exportDatabaseBackupUseCase = mockk<ExportDatabaseBackupUseCase>(),
                restoreDatabaseBackupUseCase = mockk<RestoreDatabaseBackupUseCase>(),
                goalsEditor = SettingsGoalsEditor(
                    ObserveSetupUseCase(setupRepository),
                    UpdateSetupUseCase(setupRepository),
                ),
                remindersEditor = SettingsRemindersEditor(
                    ObserveSetupUseCase(setupRepository),
                    UpdateSetupUseCase(setupRepository),
                ),
                rowSources = SettingsRowSources(
                    ObserveDebtSummaryUseCase(FakeDebtRepository()),
                    FakePurchaseGoalRepository(),
                    ObserveCaptureSummaryUseCase(FakeNotificationCaptureRepository()),
                ),
                appRestarter = mockk<AppRestarter>(relaxUnitFun = true),
            ),
        )
    }

    @Test
    fun `each setup section gets exactly its own rows in order`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            val sectionRows = expectMostRecentItem().sectionRows
            assertEquals(
                listOf(
                    SettingsField.WEEKLY_GYM,
                    SettingsField.WEEKLY_DIET,
                    SettingsField.WEEKLY_ENGLISH,
                    SettingsField.WEEKLY_ALCOHOL,
                ),
                sectionRows.getValue(SettingsSetupSection.CHECKS).map { it.field },
            )
            assertEquals(
                listOf(SettingsField.MONTHLY_OUTING_LIMIT, SettingsField.TYPICAL_OUTING_COST),
                sectionRows.getValue(SettingsSetupSection.OUTINGS).map { it.field },
            )
            assertEquals(emptyList<SettingsGoalRow>(), sectionRows.getValue(SettingsSetupSection.MONEY))
            assertEquals(
                listOf(SettingsField.TARGET_WEIGHT),
                sectionRows.getValue(SettingsSetupSection.WEIGHT).map { it.field },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
