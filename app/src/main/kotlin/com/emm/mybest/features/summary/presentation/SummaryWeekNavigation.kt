package com.emm.mybest.features.summary.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton

@Composable
internal fun SummaryWeekNavigation(
    canGoNext: Boolean,
    onIntent: (SummaryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        HButton(
            text = stringResource(R.string.summary_previous_week),
            onClick = { onIntent(SummaryIntent.OnPreviousWeekClick) },
            variant = ButtonVariant.Ghost,
        )
        HButton(
            text = stringResource(R.string.summary_next_week),
            onClick = { onIntent(SummaryIntent.OnNextWeekClick) },
            variant = ButtonVariant.Ghost,
            enabled = canGoNext,
        )
    }
}
