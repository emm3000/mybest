package com.emm.mybest.features.settings.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

private val SETTINGS_GUT = 28.dp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    callbacks: SettingsCallbacks,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val backupExportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            uri?.let { viewModel.onIntent(SettingsIntent.OnExportBackup(it.toString())) }
        }
    val backupImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onIntent(SettingsIntent.OnImportBackup(it.toString())) }
    }
    SettingsEffectsHandler(effects = viewModel.effect, snackbarHostState = snackbarHostState)
    state.reminderPicker?.let { field ->
        SettingsReminderPicker(field = field, setup = state.goals.setup, onIntent = viewModel::onIntent)
    }
    state.goals.editor?.let { editor ->
        SettingsFieldEditor(
            editor = editor,
            actions = SettingsFieldEditorActions(
                onInputChange = { viewModel.onIntent(SettingsIntent.OnFieldInputChange(it)) },
                onSave = { viewModel.onIntent(SettingsIntent.OnFieldSave) },
                onClear = { viewModel.onIntent(SettingsIntent.OnFieldClear) },
                onDismiss = { viewModel.onIntent(SettingsIntent.OnFieldDismiss) },
            ),
        )
    }
    Scaffold(
        modifier = modifier,
        topBar = { SettingsAppBar() },
        snackbarHost = { HSnackbarHost(snackbarHostState) },
        bottomBar = callbacks.bottomBar,
    ) { paddingValues ->
        SettingsBody(
            state = state,
            paddingValues = paddingValues,
            actions = SettingsBodyActions(
                onReminderIntent = viewModel::onIntent,
                onFieldOpen = { viewModel.onIntent(SettingsIntent.OnFieldOpen(it)) },
                onDebtsClick = callbacks.onDebtsClick,
                onExportClick = { backupExportLauncher.launch("mybest-backup.db") },
                onImportClick = { backupImportLauncher.launch(arrayOf("*/*")) },
            ),
        )
    }
}

@Composable
private fun SettingsEffectsHandler(
    effects: Flow<SettingsEffect>,
    snackbarHostState: SnackbarHostState,
) {
    val savedMessage = stringResource(R.string.settings_goal_saved)
    val saveFailedMessage = stringResource(R.string.settings_goal_save_failed)
    LaunchedEffect(Unit) {
        effects.collectLatest { effect ->
            val message = when (effect) {
                is SettingsEffect.ShowError -> effect.message
                is SettingsEffect.ShowMessage -> effect.message
                is SettingsEffect.GoalSaved -> savedMessage
                is SettingsEffect.GoalSaveFailed -> saveFailedMessage
                is SettingsEffect.ReminderSaveFailed -> saveFailedMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
private fun SettingsAppBar() {
    AtelierAppBar(
        title = stringResource(R.string.settings_title),
        actions = {
            MicroLabel(
                text = stringResource(R.string.settings_app_label),
                style = MicroLabelStyle(tone = MicroLabelTone.Dim),
                modifier = Modifier.padding(end = SETTINGS_GUT),
            )
        },
    )
}

@Composable
private fun SettingsBody(
    state: SettingsState,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    actions: SettingsBodyActions,
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsHero()
        Hairline()
        SettingsGoalsSection(setup = state.goals.setup, onFieldOpen = actions.onFieldOpen)
        SettingsDebtsRow(debts = state.debts, onClick = actions.onDebtsClick)
        SettingsRemindersSection(setup = state.goals.setup, onIntent = actions.onReminderIntent)
        BackupRow(onExportClick = actions.onExportClick, onImportClick = actions.onImportClick)
        Hairline()
        SettingsFooter(versionLabel = state.appVersionLabel)
    }
}

@Composable
private fun BackupRow(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_backup_label),
            value = stringResource(R.string.settings_backup_export_value),
            caption = stringResource(R.string.settings_backup_caption_empty),
            extra = stringResource(R.string.settings_backup_import_extra),
            onValueClick = onExportClick,
            onExtraClick = onImportClick,
        ),
    )
}
