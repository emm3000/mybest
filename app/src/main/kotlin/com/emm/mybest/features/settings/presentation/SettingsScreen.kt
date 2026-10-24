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
    SettingsEditors(state = state, onIntent = viewModel::onIntent)
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
                onPurchaseGoalClick = callbacks.onPurchaseGoalClick,
                onCaptureClick = callbacks.onCaptureClick,
                onExportClick = { backupExportLauncher.launch("mybest-backup.db") },
                onImportClick = { backupImportLauncher.launch(arrayOf("*/*")) },
                onReplayOnboardingClick = { viewModel.onIntent(SettingsIntent.OnReplayOnboarding) },
            ),
        )
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
        SettingsGoalsSection(
            params = SettingsGoalsSectionParams(rows = state.goalRows, onFieldOpen = actions.onFieldOpen),
        )
        SettingsDebtsRow(debts = state.debts, onClick = actions.onDebtsClick)
        SettingsPurchaseGoalRow(purchaseGoal = state.purchaseGoal, onClick = actions.onPurchaseGoalClick)
        SettingsCaptureRow(capture = state.capture, onClick = actions.onCaptureClick)
        SettingsRemindersSection(reminders = state.reminders, onIntent = actions.onReminderIntent)
        BackupRow(onExportClick = actions.onExportClick, onImportClick = actions.onImportClick)
        OnboardingReplayRow(onClick = actions.onReplayOnboardingClick)
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
            extra = stringResource(R.string.settings_backup_import_extra),
            onValueClick = onExportClick,
            onExtraClick = onImportClick,
        ),
    )
}

@Composable
private fun OnboardingReplayRow(onClick: () -> Unit) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_onboarding_label),
            value = stringResource(R.string.settings_onboarding_value),
            onValueClick = onClick,
        ),
    )
}
