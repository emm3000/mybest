package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.ui.components.atelier.AtelierCaret
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierSerifFamily

private val SETTINGS_GUT = 28.dp
private val ROW_VERTICAL_PADDING = 18.dp
private val VALUE_ROW_TOP = 8.dp
private val CAPTION_TOP = 6.dp
private val VALUE_EXTRA_GAP = 14.dp
private val ROW_INNER_GAP = 18.dp
private val VALUE_SIZE = 26.sp
private val EXTRA_SIZE = 10.sp
private val EXTRA_LETTER_SPACING = 0.16.em
private val CAPTION_LETTER_SPACING = 0.10.em
private val CAPTION_SIZE = 10.sp

internal data class SettingsRowParams(
    val label: String,
    val value: String,
    val caption: String? = null,
    val labelActive: Boolean = false,
    val extra: String? = null,
    val onValueClick: () -> Unit = {},
    val onExtraClick: (() -> Unit)? = null,
)

@Composable
internal fun SettingsRow(
    params: SettingsRowParams,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = params.onValueClick)
            .padding(horizontal = SETTINGS_GUT, vertical = ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_INNER_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsRowContent(
            params = params,
            modifier = Modifier.weight(1f),
        )
        AtelierCaret()
    }
}

@Composable
private fun SettingsRowContent(
    params: SettingsRowParams,
    modifier: Modifier = Modifier,
) {
    val labelTone = if (params.labelActive) MicroLabelTone.Done else MicroLabelTone.Default
    Column(modifier = modifier) {
        MicroLabel(
            text = params.label,
            style = MicroLabelStyle(tone = labelTone),
        )
        ValueRow(params = params)
        if (params.caption != null) {
            Text(
                text = params.caption,
                modifier = Modifier.padding(top = CAPTION_TOP),
                style = TextStyle(
                    fontFamily = AtelierMonoFamily,
                    fontSize = CAPTION_SIZE,
                    letterSpacing = CAPTION_LETTER_SPACING,
                    color = AtelierInkTertiary,
                ),
            )
        }
    }
}

@Composable
private fun ValueRow(params: SettingsRowParams) {
    Row(
        modifier = Modifier.padding(top = VALUE_ROW_TOP),
        horizontalArrangement = Arrangement.spacedBy(VALUE_EXTRA_GAP),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = params.value,
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = VALUE_SIZE,
                color = AtelierInk,
            ),
        )
        if (params.extra != null) {
            ExtraLabel(
                extra = params.extra,
                onExtraClick = params.onExtraClick,
            )
        }
    }
}

@Composable
private fun ExtraLabel(
    extra: String,
    onExtraClick: (() -> Unit)?,
) {
    val extraModifier = if (onExtraClick != null) Modifier.clickable(onClick = onExtraClick) else Modifier
    Text(
        text = "+ $extra",
        modifier = extraModifier,
        style = TextStyle(
            fontFamily = AtelierMonoFamily,
            fontSize = EXTRA_SIZE,
            letterSpacing = EXTRA_LETTER_SPACING,
            color = AtelierInkSecondary,
        ),
    )
}
