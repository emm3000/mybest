package com.emm.mybest.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R

@Composable
fun LimitOutcomeState.message(): String = when (this) {
    is LimitOutcomeState.Kept -> stringResource(R.string.check_in_result_kept)
    is LimitOutcomeState.MoneyExceeded -> stringResource(R.string.check_in_result_money_exceeded, amountLabel)
    is LimitOutcomeState.DrinksExceeded ->
        pluralStringResource(R.plurals.check_in_result_drinks_exceeded, count, count)
    is LimitOutcomeState.ReturnTimeExceeded -> stringResource(R.string.check_in_result_minutes_exceeded, minutes)
}
