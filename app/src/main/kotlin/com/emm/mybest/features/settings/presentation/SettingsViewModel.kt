package com.emm.mybest.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.BuildConfig
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.repository.RestoreResult
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exportDatabaseBackupUseCase: ExportDatabaseBackupUseCase,
    private val restoreDatabaseBackupUseCase: RestoreDatabaseBackupUseCase,
    private val goalsEditor: SettingsGoalsEditor,
    private val remindersEditor: SettingsRemindersEditor,
    moneySources: SettingsMoneySources,
    appVersionName: String = BuildConfig.VERSION_NAME,
    appVersionCode: Int = BuildConfig.VERSION_CODE,
) : ViewModel() {

    private val appVersionLabel = "v$appVersionName · $appVersionCode"

    private val _effect = effectFlow<SettingsEffect>()
    val effect = _effect.asSharedFlow()

    val state: StateFlow<SettingsState> = combine(
        userPreferencesRepository.notificationsEnabled,
        goalsEditor.goals,
        remindersEditor.picker,
        moneySources.observeDebtSummary(),
        moneySources.purchaseGoalRepository.observeGoal(),
    ) { enabled, goals, picker, debtSummary, purchaseGoal ->
        SettingsState(
            notificationsEnabled = enabled,
            reminderPicker = picker,
            appVersionLabel = appVersionLabel,
            goals = goals,
            debts = SettingsDebtsState(
                count = debtSummary.count,
                totalLabel = debtSummary.totalBalance.formatPen().takeIf { debtSummary.count > 0 },
            ),
            purchaseGoal = SettingsPurchaseGoalState(
                name = purchaseGoal?.name,
                priceLabel = purchaseGoal?.price?.formatPen(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = SettingsState(appVersionLabel = appVersionLabel),
    )

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.OnNotificationsToggle -> updateNotificationsPreference(intent.enabled)
            is SettingsIntent.BackupIntent -> handleBackupIntent(intent)
            is SettingsIntent.FieldIntent -> handleFieldIntent(intent)
            is SettingsIntent.ReminderIntent -> handleReminderIntent(intent)
        }
    }

    private fun handleBackupIntent(intent: SettingsIntent.BackupIntent) {
        when (intent) {
            is SettingsIntent.OnExportBackup -> exportBackup(intent.targetUri)
            is SettingsIntent.OnImportBackup -> importBackup(intent.sourceUri)
        }
    }

    private fun handleFieldIntent(intent: SettingsIntent.FieldIntent) {
        viewModelScope.launch { goalsEditor.handle(intent)?.let { _effect.emit(it) } }
    }

    private fun handleReminderIntent(intent: SettingsIntent.ReminderIntent) {
        viewModelScope.launch { remindersEditor.handle(intent)?.let { _effect.emit(it) } }
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
