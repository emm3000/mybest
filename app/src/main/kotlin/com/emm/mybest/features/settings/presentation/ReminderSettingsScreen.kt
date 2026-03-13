package com.emm.mybest.features.settings.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.HSwitch
import com.emm.mybest.ui.components.HTopBar
import kotlinx.coroutines.flow.collectLatest

private const val SETTINGS_SCREEN_PADDING = 16
private const val SETTINGS_SECTION_SPACING = 12
private const val SETTINGS_CARD_PADDING = 16
private const val SETTINGS_CARD_CONTENT_SPACING = 8

@Composable
fun ReminderSettingsScreen(
    viewModel: ReminderSettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri?.let { viewModel.onIntent(ReminderSettingsIntent.OnExportBackup(it.toString())) }
    }
    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.onIntent(ReminderSettingsIntent.OnImportBackup(it.toString())) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ReminderSettingsEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is ReminderSettingsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            HTopBar(
                title = "Recordatorios",
                navigationIcon = {
                    HIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = onBackClick,
                    )
                },
            )
        },
        snackbarHost = { HSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(SETTINGS_SCREEN_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(SETTINGS_SECTION_SPACING.dp),
        ) {
            SettingsSectionCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Recordatorios preventivos",
                description = "Activa notificaciones para recibir recordatorios de hábitos pendientes en días programados.",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (state.notificationsEnabled) "Activos" else "Pausados",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HSwitch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = {
                            viewModel.onIntent(ReminderSettingsIntent.OnNotificationsToggle(it))
                        },
                        contentDescription = "Alternar recordatorios",
                        modifier = Modifier.semantics {
                            role = Role.Switch
                            stateDescription = if (state.notificationsEnabled) "Recordatorios activos" else "Recordatorios pausados"
                        },
                    )
                }
            }
            SettingsSectionCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Backup y restore",
                description = "Exporta tu base de datos o importa un backup válido en formato SQLite.",
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(SETTINGS_CARD_CONTENT_SPACING.dp),
                ) {
                    HButton(
                        text = "Exportar backup",
                        onClick = {
                            backupExportLauncher.launch("mybest-backup.db")
                        },
                        leadingIcon = Icons.Rounded.Download,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HButton(
                        text = "Importar backup",
                        onClick = {
                            backupImportLauncher.launch(arrayOf("*/*"))
                        },
                        leadingIcon = Icons.Rounded.Upload,
                        variant = ButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    HCard(
        modifier = modifier,
        variant = CardVariant.Filled,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SETTINGS_CARD_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(SETTINGS_CARD_CONTENT_SPACING.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}
