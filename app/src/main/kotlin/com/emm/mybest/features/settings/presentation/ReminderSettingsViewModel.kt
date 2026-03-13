package com.emm.mybest.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReminderSettingsState(
    val notificationsEnabled: Boolean = true,
)

sealed class ReminderSettingsIntent {
    data class OnNotificationsToggle(val enabled: Boolean) : ReminderSettingsIntent()
    data class OnExportBackup(val targetUri: String) : ReminderSettingsIntent()
    data class OnImportBackup(val sourceUri: String) : ReminderSettingsIntent()
}

sealed class ReminderSettingsEffect {
    data class ShowError(val message: String) : ReminderSettingsEffect()
    data class ShowMessage(val message: String) : ReminderSettingsEffect()
}

class ReminderSettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exportDatabaseBackupUseCase: ExportDatabaseBackupUseCase,
    private val restoreDatabaseBackupUseCase: RestoreDatabaseBackupUseCase,
) : ViewModel() {

    private val _effect = MutableSharedFlow<ReminderSettingsEffect>()
    val effect = _effect.asSharedFlow()

    val state: StateFlow<ReminderSettingsState> = userPreferencesRepository.notificationsEnabled
        .map { enabled ->
            ReminderSettingsState(notificationsEnabled = enabled)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReminderSettingsState(),
        )

    fun onIntent(intent: ReminderSettingsIntent) {
        when (intent) {
            is ReminderSettingsIntent.OnNotificationsToggle -> updateNotificationsPreference(intent.enabled)
            is ReminderSettingsIntent.OnExportBackup -> exportBackup(intent.targetUri)
            is ReminderSettingsIntent.OnImportBackup -> importBackup(intent.sourceUri)
        }
    }

    private fun updateNotificationsPreference(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                userPreferencesRepository.updateNotificationsEnabled(enabled)
            }.onFailure {
                _effect.emit(
                    ReminderSettingsEffect.ShowError(
                        it.message ?: "No se pudo actualizar la configuración",
                    ),
                )
            }
        }
    }

    private fun exportBackup(targetUri: String) {
        viewModelScope.launch {
            exportDatabaseBackupUseCase(targetUri)
                .onSuccess {
                    _effect.emit(ReminderSettingsEffect.ShowMessage("Backup exportado correctamente"))
                }.onFailure {
                    _effect.emit(
                        ReminderSettingsEffect.ShowError(
                            it.message ?: "No se pudo exportar el backup",
                        ),
                    )
                }
        }
    }

    private fun importBackup(sourceUri: String) {
        viewModelScope.launch {
            restoreDatabaseBackupUseCase(sourceUri)
                .onSuccess {
                    _effect.emit(
                        ReminderSettingsEffect.ShowMessage(
                            "Backup importado. Reinicia la app para aplicar cambios.",
                        ),
                    )
                }.onFailure {
                    _effect.emit(
                        ReminderSettingsEffect.ShowError(
                            it.message ?: "No se pudo importar el backup",
                        ),
                    )
                }
        }
    }
}
