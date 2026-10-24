package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val SECTION_GUT = 28.dp
private val SECTION_TOP = 24.dp

@Composable
internal fun SettingsGoalsSection(
    params: SettingsGoalsSectionParams,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MicroLabel(
            text = stringResource(R.string.settings_goals_section),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier.padding(start = SECTION_GUT, top = SECTION_TOP),
        )
        params.fields.forEach { field ->
            SettingsRow(
                params = SettingsRowParams(
                    label = stringResource(field.labelRes),
                    value = goalValueText(field, params.setup),
                    onValueClick = { params.onFieldOpen(field) },
                ),
            )
            Hairline(inset = SECTION_GUT)
        }
    }
}

@Composable
private fun goalValueText(field: SettingsField, setup: Setup): String {
    val input = field.inputFor(setup)
    return when {
        input.isEmpty() -> stringResource(R.string.settings_goal_unset)
        field == SettingsField.TARGET_WEIGHT -> stringResource(R.string.settings_goal_target_weight_value, input)
        field == SettingsField.TYPICAL_OUTING_COST -> setup.typicalOutingCost?.formatPen().orEmpty()
        field.checkType != null -> pluralStringResource(
            R.plurals.settings_goal_days_per_week,
            input.toInt(),
            input.toInt(),
        )
        else -> input
    }
}
