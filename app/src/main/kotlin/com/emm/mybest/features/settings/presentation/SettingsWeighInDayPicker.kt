package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierSerifFamily
import kotlinx.datetime.DayOfWeek

private val PICKER_GUT = 28.dp
private val PICKER_GAP = 16.dp
private val DAY_ROW_VERTICAL_PADDING = 14.dp
private val DAY_SIZE = 22.sp

@Composable
internal fun SettingsWeighInDayPicker(
    selected: DayOfWeek?,
    onIntent: (SettingsIntent.ReminderIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HBottomSheet(
        onDismissRequest = { onIntent(SettingsIntent.OnReminderDismiss) },
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = PICKER_GAP),
        ) {
            MicroLabel(
                text = stringResource(R.string.settings_reminder_weigh_in_day_label),
                modifier = Modifier.padding(start = PICKER_GUT, bottom = PICKER_GAP),
            )
            DayOfWeek.entries.forEach { day ->
                Hairline(inset = PICKER_GUT)
                WeekdayOption(
                    day = day,
                    isSelected = day == selected,
                    onClick = { onIntent(SettingsIntent.OnWeighInDayChange(day)) },
                )
            }
        }
    }
}

@Composable
private fun WeekdayOption(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = stringResource(day.weekdayRes),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = PICKER_GUT, vertical = DAY_ROW_VERTICAL_PADDING),
        style = TextStyle(
            fontFamily = AtelierSerifFamily,
            fontSize = DAY_SIZE,
            color = if (isSelected) AtelierDone else AtelierInk,
        ),
    )
}
