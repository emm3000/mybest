package com.emm.mybest.features.photo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val FOOTER_HORIZONTAL_PADDING = 28.dp
private val FOOTER_BOTTOM_PADDING = 16.dp

@Composable
internal fun CompareFooter(
    selection: CompareSelection,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summaryText = summaryText(selection)
    val showCompareCta = selection.beforePhoto != null && selection.afterPhoto != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FOOTER_HORIZONTAL_PADDING, vertical = FOOTER_BOTTOM_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MicroLabel(
            text = summaryText,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        if (showCompareCta) {
            MicroLabel(
                text = stringResource(R.string.photos_compare_cta),
                style = MicroLabelStyle(tone = MicroLabelTone.Done),
                modifier = Modifier.clickable { onCompareClick() },
            )
        }
    }
}

@Composable
private fun summaryText(selection: CompareSelection): String {
    val beforePhoto = selection.beforePhoto
    if (beforePhoto == null) return stringResource(R.string.photos_compare_summary_empty)
    val referenceDate = selection.afterPhoto?.date ?: selection.today
    val days = (referenceDate.toEpochDays() - beforePhoto.date.toEpochDays()).toInt()
    val daysLabel = pluralStringResource(R.plurals.photos_compare_days_part, days, days)
    val deltaText = deltaText(
        beforeWeightKg = selection.beforeWeightKg,
        afterWeightKg = selection.afterWeightKg,
    )
    return stringResource(R.string.photos_compare_summary_format, daysLabel, deltaText)
}

@Composable
private fun deltaText(beforeWeightKg: Float?, afterWeightKg: Float?): String {
    if (beforeWeightKg == null || afterWeightKg == null) return "—"
    return stringResource(R.string.photos_compare_delta_format, afterWeightKg - beforeWeightKg)
}
