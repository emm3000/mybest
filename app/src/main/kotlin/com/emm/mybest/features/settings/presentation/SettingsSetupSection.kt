package com.emm.mybest.features.settings.presentation

enum class SettingsSetupSection(
    val fields: List<SettingsField> = emptyList(),
    val hasMoneyRows: Boolean = false,
    val hasReminders: Boolean = false,
) {
    CHECKS(
        fields = listOf(
            SettingsField.WEEKLY_GYM,
            SettingsField.WEEKLY_DIET,
            SettingsField.WEEKLY_ENGLISH,
            SettingsField.WEEKLY_ALCOHOL,
        ),
    ),
    OUTINGS(fields = listOf(SettingsField.MONTHLY_OUTING_LIMIT, SettingsField.TYPICAL_OUTING_COST)),
    MONEY(hasMoneyRows = true),
    WEIGHT(fields = listOf(SettingsField.TARGET_WEIGHT), hasReminders = true),
}
