package com.emm.mybest.features.capture.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationCaptureViewModel(
    private val useCases: NotificationCaptureUseCases,
    private val appDayClock: AppDayClock,
) : ViewModel() {

    private val isAccessGranted = MutableStateFlow(useCases.accessChecker.isGranted())
    private val isDeleteConfirmationVisible = MutableStateFlow(false)

    private val _effect = effectFlow<NotificationCaptureEffect>()
    val effect: SharedFlow<NotificationCaptureEffect> = _effect.asSharedFlow()

    val state: StateFlow<NotificationCaptureState> = combine(
        useCases.observeOverview(),
        isAccessGranted,
        isDeleteConfirmationVisible,
    ) { overview, isGranted, isConfirming ->
        NotificationCaptureState(
            isLoaded = true,
            isAccessGranted = isGranted,
            isCaptureActive = overview.summary.isActive,
            apps = overview.apps.map { it.toItemState() },
            notifications = overview.entries.map { it.toItemState(appDayClock) },
            isDeleteConfirmationVisible = isConfirming,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = NotificationCaptureState(isAccessGranted = isAccessGranted.value),
    )

    fun onIntent(intent: NotificationCaptureIntent) {
        when (intent) {
            is NotificationCaptureIntent.OnResume -> isAccessGranted.value = useCases.accessChecker.isGranted()
            is NotificationCaptureIntent.OnOpenAccessSettingsClick -> _effect.tryEmit(
                NotificationCaptureEffect.OpenAccessSettings,
            )
            is NotificationCaptureIntent.OnAppToggle -> toggleApp(intent)
            is NotificationCaptureIntent.OnTurnOffClick -> perform { useCases.repository.clearSelectedApps() }
            is NotificationCaptureIntent.OnDeleteAllClick -> isDeleteConfirmationVisible.value = true
            is NotificationCaptureIntent.OnDeleteAllConfirm -> deleteAll()
            is NotificationCaptureIntent.OnDeleteAllDismiss -> isDeleteConfirmationVisible.value = false
        }
    }

    private fun toggleApp(intent: NotificationCaptureIntent.OnAppToggle) {
        perform { useCases.repository.setAppSelected(intent.packageName, intent.isSelected) }
    }

    private fun deleteAll() {
        isDeleteConfirmationVisible.value = false
        perform { useCases.repository.deleteAllCaptured() }
    }

    private fun perform(action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }
                .onFailure {
                    if (it is CancellationException) throw it
                    _effect.emit(NotificationCaptureEffect.ActionFailed)
                }
        }
    }
}
