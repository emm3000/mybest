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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.ReminderTimePickerDialog
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import kotlinx.coroutines.flow.collectLatest

private val SETTINGS_GUT = 28.dp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    onMealPlanClick: () -> Unit = {},
    onExercisePlanClick: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri?.let { viewModel.onIntent(SettingsIntent.OnExportBackup(it.toString())) }
    }
    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.onIntent(SettingsIntent.OnImportBackup(it.toString())) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is SettingsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (state.showDefaultTimePicker) {
        ReminderTimePickerDialog(
            initialHour = state.weightReminderTime?.hour ?: 8,
            initialMinute = state.weightReminderTime?.minute ?: 0,
            onConfirm = { hour, minute ->
                viewModel.onIntent(SettingsIntent.OnDefaultReminderTimeChange(hour, minute))
            },
            onDismiss = { viewModel.onIntent(SettingsIntent.OnDefaultTimePickerDismiss) },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
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
        },
        snackbarHost = { HSnackbarHost(snackbarHostState) },
        bottomBar = bottomBar,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsHero()
            Hairline()
            ReminderRow(state = state, onOpenPicker = { viewModel.onIntent(SettingsIntent.OnDefaultTimePickerOpen) })
            Hairline(inset = SETTINGS_GUT)
            DietRow(onMealPlanClick = onMealPlanClick)
            Hairline(inset = SETTINGS_GUT)
            ExerciseRow(onExercisePlanClick = onExercisePlanClick)
            Hairline(inset = SETTINGS_GUT)
            BackupRow(
                onExportClick = { backupExportLauncher.launch("mybest-backup.db") },
                onImportClick = { backupImportLauncher.launch(arrayOf("*/*")) },
            )
            Hairline()
            SettingsFooter(versionLabel = state.appVersionLabel)
        }
    }
}

@Composable
private fun ReminderRow(
    state: SettingsState,
    onOpenPicker: () -> Unit,
) {
    val labelActive = state.notificationsEnabled
    val value = if (state.notificationsEnabled) {
        formatReminderTime(state.weightReminderTime)
    } else {
        stringResource(R.string.settings_reminder_off_value)
    }
    val caption = if (state.notificationsEnabled) {
        stringResource(R.string.settings_reminder_caption_on)
    } else {
        stringResource(R.string.settings_reminder_caption_off)
    }
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_reminder_label),
            value = value,
            caption = caption,
            labelActive = labelActive,
            onValueClick = onOpenPicker,
        ),
    )
}

@Composable
private fun DietRow(onMealPlanClick: () -> Unit) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_diet_label),
            value = stringResource(R.string.settings_diet_value),
            caption = stringResource(R.string.settings_diet_caption),
            onValueClick = onMealPlanClick,
        ),
    )
}

@Composable
private fun ExerciseRow(onExercisePlanClick: () -> Unit) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_exercise_label),
            value = stringResource(R.string.settings_exercise_value),
            caption = stringResource(R.string.settings_exercise_caption),
            onValueClick = onExercisePlanClick,
        ),
    )
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
