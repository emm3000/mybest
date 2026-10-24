package com.emm.mybest.features.settings.presentation

import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.Setup

internal fun Setup.toGoalRows(): List<SettingsGoalRow> =
    SettingsField.entries.map { SettingsGoalRow(it, it.labelRes, it.valueIn(this)) }

internal fun List<SettingsGoalRow>.toSectionRows(): Map<SettingsSetupSection, List<SettingsGoalRow>> {
    val rowsByField = associateBy { it.field }
    return SettingsSetupSection.entries.associateWith { section -> section.fields.mapNotNull(rowsByField::get) }
}

private fun SettingsField.valueIn(setup: Setup): SettingsRowValue {
    val input = inputFor(setup)
    return when {
        input.isEmpty() -> SettingsRowValue.Resource(R.string.settings_goal_unset)
        this == SettingsField.TARGET_WEIGHT -> SettingsRowValue.Resource(
            R.string.settings_goal_target_weight_value,
            input,
        )
        this == SettingsField.TYPICAL_OUTING_COST -> SettingsRowValue.Text(
            setup.typicalOutingCost?.formatPen().orEmpty(),
        )
        checkType != null -> SettingsRowValue.Plural(R.plurals.settings_goal_days_per_week, input.toInt())
        else -> SettingsRowValue.Text(input)
    }
}
