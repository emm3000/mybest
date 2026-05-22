package com.emm.mybest.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.BuildConfig
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.repository.RestoreResult
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.UpdateDefaultReminderTimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exportDatabaseBackupUseCase: ExportDatabaseBackupUseCase,
    private val restoreDatabaseBackupUseCase: RestoreDatabaseBackupUseCase,
    private val updateDefaultReminderTimeUseCase: UpdateDefaultReminderTimeUseCase,
    appVersionName: String = BuildConfig.VERSION_NAME,
    appVersionCode: Int = BuildConfig.VERSION_CODE,
) : ViewModel() {

    private val appVersionLabel = "v$appVersionName · $appVersionCode"

    private val _effect = effectFlow<SettingsEffect>()
    val effect = _effect.asSharedFlow()

    private val _showDefaultTimePicker = MutableStateFlow(false)

    val state: StateFlow<SettingsState> = combine(
        userPreferencesRepository.notificationsEnabled,
        userPreferencesRepository.weightReminderTime,
        _showDefaultTimePicker,
    ) { enabled, weightTime, showPicker ->
        SettingsState(
            notificationsEnabled = enabled,
            showDefaultTimePicker = showPicker,
            weightReminderTime = weightTime,
            appVersionLabel = appVersionLabel,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = SettingsState(appVersionLabel = appVersionLabel),
    )

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.OnNotificationsToggle -> updateNotificationsPreference(intent.enabled)
            is SettingsIntent.OnExportBackup -> exportBackup(intent.targetUri)
            is SettingsIntent.OnImportBackup -> importBackup(intent.sourceUri)
            is SettingsIntent.OnDefaultTimePickerOpen -> _showDefaultTimePicker.update { true }
            is SettingsIntent.OnDefaultTimePickerDismiss -> _showDefaultTimePicker.update { false }
            is SettingsIntent.OnDefaultReminderTimeChange -> updateDefaultReminderTime(
                intent.hour,
                intent.minute,
            )
            is SettingsIntent.OnWeightReminderToggleOff -> updateWeightReminder(null)
        }
    }

    private fun updateNotificationsPreference(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                userPreferencesRepository.updateNotificationsEnabled(enabled)
            }.onFailure {
                _effect.emit(
                    SettingsEffect.ShowError(
                        it.message ?: "No se pudo actualizar la configuración",
                    ),
                )
            }
        }
    }

    private fun updateDefaultReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            runCatching {
                updateDefaultReminderTimeUseCase(LocalTime(hour, minute))
            }.onFailure {
                _effect.emit(
                    SettingsEffect.ShowError(
                        it.message ?: "No se pudo actualizar la hora por defecto",
                    ),
                )
            }
            _showDefaultTimePicker.update { false }
        }
    }

    private fun updateWeightReminder(time: LocalTime?) {
        viewModelScope.launch {
            runCatching {
                updateDefaultReminderTimeUseCase(time)
            }.onFailure {
                _effect.emit(
                    SettingsEffect.ShowError(
                        it.message ?: "No se pudo actualizar el recordatorio de peso",
                    ),
                )
            }
        }
    }

    private fun exportBackup(targetUri: String) {
        viewModelScope.launch {
            exportDatabaseBackupUseCase(targetUri)
                .onSuccess {
                    _effect.emit(SettingsEffect.ShowMessage("Backup exportado correctamente"))
                }.onFailure {
                    _effect.emit(
                        SettingsEffect.ShowError(
                            it.message ?: "No se pudo exportar el backup",
                        ),
                    )
                }
        }
    }

    private fun importBackup(sourceUri: String) {
        viewModelScope.launch {
            restoreDatabaseBackupUseCase(sourceUri)
                .onSuccess { result ->
                    when (result) {
                        RestoreResult.RequiresRestart -> _effect.emit(
                            SettingsEffect.ShowMessage(
                                "Backup restaurado. Reinicia la app para aplicar.",
                            ),
                        )
                    }
                }.onFailure {
                    _effect.emit(
                        SettingsEffect.ShowError(
                            it.message ?: "No se pudo importar el backup",
                        ),
                    )
                }
        }
    }
}
