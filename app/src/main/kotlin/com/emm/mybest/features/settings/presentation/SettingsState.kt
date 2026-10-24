package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.Setup

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val reminderPicker: ReminderField? = null,
    val appVersionLabel: String = "",
    val goals: SettingsGoals = SettingsGoals(),
    val goalRows: List<SettingsGoalRow> = Setup().toGoalRows(),
    val reminders: SettingsReminders = Setup().toReminders(),
    val debts: SettingsDebtsState = SettingsDebtsState(),
    val purchaseGoal: SettingsPurchaseGoalState = SettingsPurchaseGoalState(),
    val capture: SettingsCaptureState = SettingsCaptureState(),
)
