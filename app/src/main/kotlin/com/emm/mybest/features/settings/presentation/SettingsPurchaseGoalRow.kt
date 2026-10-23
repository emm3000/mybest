package com.emm.mybest.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline

private val PURCHASE_GOAL_ROW_INSET = 28.dp

@Composable
internal fun SettingsPurchaseGoalRow(
    purchaseGoal: SettingsPurchaseGoalState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_purchase_goal_label),
            value = purchaseGoalValueText(purchaseGoal),
            onValueClick = onClick,
        ),
        modifier = modifier,
    )
    Hairline(inset = PURCHASE_GOAL_ROW_INSET)
}

@Composable
private fun purchaseGoalValueText(purchaseGoal: SettingsPurchaseGoalState): String {
    val name = purchaseGoal.name ?: return stringResource(R.string.settings_goal_unset)
    return stringResource(R.string.settings_purchase_goal_value, name, purchaseGoal.priceLabel.orEmpty())
}
