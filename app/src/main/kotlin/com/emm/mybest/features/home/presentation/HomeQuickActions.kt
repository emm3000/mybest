package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.DisplayNumber
import com.emm.mybest.ui.components.atelier.DisplayNumberStyle
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val DIVIDER_VERTICAL_PADDING = 14.dp
private val QUICK_ACTION_NUMBER_FONT_SIZE = 28.sp

@Composable
internal fun QuickActionDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = DIVIDER_VERTICAL_PADDING),
    ) { Hairline() }
}

@Composable
internal fun QuickActionNumberRow(number: String, unit: String?, isPlaceholder: Boolean = false) {
    if (isPlaceholder) {
        MicroLabel(text = number, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        return
    }
    Row(verticalAlignment = Alignment.Bottom) {
        DisplayNumber(
            text = number,
            style = DisplayNumberStyle(fontSize = QUICK_ACTION_NUMBER_FONT_SIZE),
        )
        if (unit != null) {
            MicroLabel(text = unit, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        }
    }
}

@Composable
internal fun weightCellContent(state: HomeState): QuickActionCellContent {
    val kg = state.lastWeightKg
    val caption = state.previousWeightKg?.let { prev ->
        stringResource(R.string.home_previous_weight_format, formatHomeWeight(prev))
    }
    return QuickActionCellContent(
        label = stringResource(R.string.home_register_weight).uppercase(),
        number = if (kg != null) formatHomeWeight(kg) else stringResource(R.string.home_quick_action_placeholder),
        unit = if (kg != null) stringResource(R.string.home_weight_unit_kg).uppercase() else null,
        caption = caption,
        isPlaceholder = kg == null,
    )
}

@Composable
internal fun photoCellContent(state: HomeState): QuickActionCellContent {
    val type = state.lastPhotoType
    val daysAgo = state.lastPhotoDaysAgo
    val caption = if (type != null && daysAgo != null) {
        val typeLabel = type.labelEs()
        when {
            daysAgo == 0 -> stringResource(R.string.home_photo_caption_today_format, typeLabel)
            daysAgo == 1 -> stringResource(R.string.home_photo_caption_yesterday_format, typeLabel)
            else -> stringResource(R.string.home_photo_caption_with_days_format, typeLabel, daysAgo)
        }
    } else {
        null
    }
    return QuickActionCellContent(
        label = stringResource(R.string.home_photo).uppercase(),
        number = state.photoCount.toString(),
        caption = caption,
        isPlaceholder = state.photoCount == 0,
    )
}
