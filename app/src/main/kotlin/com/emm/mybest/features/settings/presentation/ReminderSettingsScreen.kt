package com.emm.mybest.features.settings.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HTopBar
import kotlinx.coroutines.flow.collectLatest

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Filled,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Recordatorios preventivos",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Activa notificaciones para recibir recordatorios de hábitos pendientes en días programados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = {
                            viewModel.onIntent(ReminderSettingsIntent.OnNotificationsToggle(it))
                        },
                    )
                }
            }
            HCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Filled,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Backup y restore",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Exporta tu base de datos o importa un backup válido en formato SQLite.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
