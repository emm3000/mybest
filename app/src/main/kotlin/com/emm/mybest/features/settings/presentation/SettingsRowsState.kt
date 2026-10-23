package com.emm.mybest.features.settings.presentation

data class SettingsRowsState(
    val debts: SettingsDebtsState = SettingsDebtsState(),
    val purchaseGoal: SettingsPurchaseGoalState = SettingsPurchaseGoalState(),
)
