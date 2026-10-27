package com.emm.mybest.features.settings.presentation

import com.emm.mybest.core.coroutines.attempt
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class SettingsRemindersEditor(
    private val observeSetupUseCase: ObserveSetupUseCase,
    private val updateSetupUseCase: UpdateSetupUseCase,
) {
    private val openPicker = MutableStateFlow<ReminderField?>(null)
    private val saveLock = Mutex()

    val picker: StateFlow<ReminderField?> = openPicker.asStateFlow()

    /**
     * Applies [intent] and returns the one-shot effect it caused, if any.
     */
    suspend fun handle(intent: SettingsIntent.ReminderIntent): SettingsEffect? = when (intent) {
        is SettingsIntent.OnReminderOpen -> showPicker(intent.field)
        is SettingsIntent.OnReminderDismiss -> showPicker(null)
        is SettingsIntent.OnReminderTimeChange -> saveTime(LocalTime(intent.hour, intent.minute))
        is SettingsIntent.OnWeighInDayChange -> saveDay(intent.day)
        is SettingsIntent.OnReminderClear -> clear(intent.field)
        is SettingsIntent.OnPhotoReminderToggle -> persist { it.copy(isPhotoReminderEnabled = intent.isEnabled) }
    }

    private fun showPicker(field: ReminderField?): SettingsEffect? {
        openPicker.value = field
        return null
    }

    private suspend fun saveTime(time: LocalTime): SettingsEffect? {
        val field = openPicker.value ?: return null
        openPicker.value = null
        return save(field, time)
    }

    private suspend fun saveDay(day: DayOfWeek): SettingsEffect? {
        openPicker.value = null
        return persist { it.copy(weighInDay = day) }
    }

    private suspend fun clear(field: ReminderField): SettingsEffect? =
        if (field == ReminderField.WEIGH_IN_DAY) persist { it.copy(weighInDay = null) } else save(field, null)

    private suspend fun save(field: ReminderField, time: LocalTime?): SettingsEffect? =
        persist { field.withTime(it, time) }

    private suspend fun persist(change: (Setup) -> Setup): SettingsEffect? = saveLock.withLock {
        attempt { updateSetupUseCase(change(observeSetupUseCase().first())) }.failureEffect()
    }

    private fun Result<*>.failureEffect(): SettingsEffect? = exceptionOrNull()?.let { SettingsEffect.ReminderSaveFailed }
}
