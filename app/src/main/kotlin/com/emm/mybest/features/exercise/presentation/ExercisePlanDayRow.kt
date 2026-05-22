package com.emm.mybest.features.exercise.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.core.datetime.longEs
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierSerifFamily
import kotlinx.datetime.DayOfWeek

private val ROW_PADDING_HORIZONTAL = 28.dp
private val ROW_PADDING_VERTICAL = 20.dp
private val ROW_GAP = 22.dp
private val ROW_MIN_HEIGHT = 64.dp
private val DAY_LETTER_WIDTH = 50.dp
private val DAY_LETTER_FONT_SIZE = 58.sp
private val DAY_LETTER_LINE_HEIGHT = 46.4.sp
private val ENTRY_NAME_FONT_SIZE = 24.sp
private val ENTRY_NAME_LINE_HEIGHT = 24.sp
private val ENTRY_NAME_MARGIN_TOP = 6.dp
private val ENTRY_DETAIL_FONT_SIZE = 12.5.sp
private val ENTRY_DETAIL_LINE_HEIGHT = 18.sp
private val ENTRY_DETAIL_MARGIN_TOP = 6.dp
private val ENTRY_VOLUME_FONT_SIZE = 9.5.sp
private val ENTRY_VOLUME_LETTER_SPACING = 0.16.em
private val ENTRY_VOLUME_MARGIN_TOP = 8.dp
private val CARET_SIZE = 16.dp
private val CARET_PADDING_TOP = 6.dp

@Composable
internal fun ExercisePlanDayRow(
    content: ExercisePlanDayContent,
    onClick: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = content.day == content.today.dayOfWeek
    val letterColor = if (isToday) AtelierDone else AtelierInk
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .clickable { onClick(content.day) }
            .padding(horizontal = ROW_PADDING_HORIZONTAL, vertical = ROW_PADDING_VERTICAL),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
    ) {
        Text(
            text = content.day.atelierLetter(),
            modifier = Modifier.width(DAY_LETTER_WIDTH),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontStyle = FontStyle.Italic,
                fontSize = DAY_LETTER_FONT_SIZE,
                lineHeight = DAY_LETTER_LINE_HEIGHT,
                color = letterColor,
            ),
        )
        EntryColumn(
            content = content,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier
                .padding(top = CARET_PADDING_TOP)
                .size(CARET_SIZE),
            tint = AtelierInkTertiary,
        )
    }
}

@Composable
private fun EntryColumn(content: ExercisePlanDayContent, modifier: Modifier = Modifier) {
    val entry = content.entry
    val isEmpty = entry.name.isBlank() && entry.detail.isBlank() && entry.volume.isBlank()
    Column(modifier = modifier) {
        MicroLabel(
            text = content.day.longEs().uppercase(),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        EntryName(entry.name)
        EntryDetail(entry.detail)
        if (!isEmpty) {
            EntryVolume(entry.volume)
        }
    }
}

@Composable
private fun EntryName(name: String) {
    val hasName = name.isNotBlank()
    Text(
        text = if (hasName) name else stringResource(R.string.exercise_plan_empty_value),
        modifier = Modifier.padding(top = ENTRY_NAME_MARGIN_TOP),
        style = TextStyle(
            fontFamily = AtelierSerifFamily,
            fontSize = ENTRY_NAME_FONT_SIZE,
            lineHeight = ENTRY_NAME_LINE_HEIGHT,
            color = if (hasName) AtelierInk else AtelierInkTertiary,
        ),
    )
}

@Composable
private fun EntryDetail(detail: String) {
    val hasDetail = detail.isNotBlank()
    Text(
        text = if (hasDetail) detail else stringResource(R.string.exercise_plan_empty_value),
        modifier = Modifier.padding(top = ENTRY_DETAIL_MARGIN_TOP),
        style = TextStyle(
            fontSize = ENTRY_DETAIL_FONT_SIZE,
            lineHeight = ENTRY_DETAIL_LINE_HEIGHT,
            color = if (hasDetail) AtelierInkSecondary else AtelierInkTertiary,
        ),
    )
}

@Composable
private fun EntryVolume(volume: String) {
    val volText = stringResource(R.string.exercise_plan_volume_format, volume.ifBlank { "—" })
    Text(
        text = volText,
        modifier = Modifier.padding(top = ENTRY_VOLUME_MARGIN_TOP),
        style = TextStyle(
            fontFamily = AtelierMonoFamily,
            fontSize = ENTRY_VOLUME_FONT_SIZE,
            letterSpacing = ENTRY_VOLUME_LETTER_SPACING,
            color = AtelierInkTertiary,
        ),
    )
}
