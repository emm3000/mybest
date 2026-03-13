package com.emm.mybest.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.repository.UserPreferencesRepository
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
}

sealed class ReminderSettingsEffect {
    data class ShowError(val message: String) : ReminderSettingsEffect()
}

class ReminderSettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
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
            is ReminderSettingsIntent.OnNotificationsToggle -> {
                viewModelScope.launch {
                    runCatching {
                        userPreferencesRepository.updateNotificationsEnabled(intent.enabled)
                    }.onFailure {
                        _effect.emit(
                            ReminderSettingsEffect.ShowError(
                                it.message ?: "No se pudo actualizar la configuración",
                            ),
                        )
                    }
                }
            }
        }
    }
}
