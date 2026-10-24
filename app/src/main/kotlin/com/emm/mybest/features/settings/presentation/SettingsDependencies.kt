package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase

data class SettingsDependencies(
    val userPreferencesRepository: UserPreferencesRepository,
    val exportDatabaseBackupUseCase: ExportDatabaseBackupUseCase,
    val restoreDatabaseBackupUseCase: RestoreDatabaseBackupUseCase,
    val goalsEditor: SettingsGoalsEditor,
    val remindersEditor: SettingsRemindersEditor,
    val rowSources: SettingsRowSources,
)
