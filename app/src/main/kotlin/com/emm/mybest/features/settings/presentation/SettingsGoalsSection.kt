package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
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
        params.rows.forEach { row ->
            SettingsRow(
                params = SettingsRowParams(
                    label = stringResource(row.labelRes),
                    value = row.value.resolve(),
                    onValueClick = { params.onFieldOpen(row.field) },
                ),
            )
            Hairline(inset = SECTION_GUT)
        }
    }
}
