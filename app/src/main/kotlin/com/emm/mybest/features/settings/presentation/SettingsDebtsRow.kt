package com.emm.mybest.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline

private val DEBTS_ROW_INSET = 28.dp

@Composable
internal fun SettingsDebtsRow(
    debts: SettingsDebtsState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_debts_label),
            value = debtsValueText(debts),
            onValueClick = onClick,
        ),
        modifier = modifier,
    )
    Hairline(inset = DEBTS_ROW_INSET)
}

@Composable
private fun debtsValueText(debts: SettingsDebtsState): String {
    val totalLabel = debts.totalLabel ?: return stringResource(R.string.settings_goal_unset)
    return pluralStringResource(R.plurals.settings_debts_value, debts.count, debts.count, totalLabel)
}
