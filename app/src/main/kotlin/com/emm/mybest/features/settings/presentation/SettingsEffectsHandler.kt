package com.emm.mybest.features.settings.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun SettingsEffectsHandler(
    effects: Flow<SettingsEffect>,
    snackbarHostState: SnackbarHostState,
) {
    val savedMessage = stringResource(R.string.settings_goal_saved)
    val saveFailedMessage = stringResource(R.string.settings_goal_save_failed)
    val restoredMessage = stringResource(R.string.settings_backup_restored)
    val otherVersionMessage = stringResource(R.string.settings_backup_other_version)
    val importFailedMessage = stringResource(R.string.settings_backup_import_failed)
    LaunchedEffect(Unit) {
        effects.collectLatest { effect ->
            val message = when (effect) {
                is SettingsEffect.ShowError -> effect.message
                is SettingsEffect.ShowMessage -> effect.message
                is SettingsEffect.GoalSaved -> savedMessage
                is SettingsEffect.GoalSaveFailed, is SettingsEffect.ReminderSaveFailed -> saveFailedMessage
                is SettingsEffect.BackupRestored -> restoredMessage
                is SettingsEffect.BackupFromOtherVersion -> otherVersionMessage
                is SettingsEffect.BackupImportFailed -> importFailedMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}
