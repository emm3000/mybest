package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.ui.components.atelier.CompletionCheck
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.components.checkLabelRes
import com.emm.mybest.ui.theme.AtelierInkMuted
import com.emm.mybest.ui.theme.AtelierTheme

private val SECTION_PADDING_VERTICAL = 14.dp
private val ALCOHOL_SEGMENT_BORDER_WIDTH = 1.dp
private val SEGMENT_PADDING_HORIZONTAL = 14.dp
private val SEGMENT_PADDING_VERTICAL = 10.dp

@Composable
internal fun HomeChecksSection(
    model: HomeChecksModel,
    actions: HomeCheckActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = HOME_SECTION_PADDING_HORIZONTAL)) {
        MicroLabel(
            text = stringResource(R.string.home_checks_title_format, model.keptChecks, model.totalChecks),
            modifier = Modifier.padding(vertical = SECTION_PADDING_VERTICAL),
        )
        model.items.forEach { item ->
            Hairline()
            DailyCheckRow(item = item, actions = actions)
        }
    }
}

@Composable
private fun DailyCheckRow(item: DailyCheckItem, actions: HomeCheckActions) {
    val label = stringResource(checkLabelRes(item.type))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HOME_ROW_HEIGHT_MIN)
            .then(rowToggleModifier(item, actions.onToggle)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        if (item.isLimit) {
            AlcoholSegment(item = item, onAnswer = actions.onAnswerAlcohol)
        } else {
            CompletionCheck(done = item.isDone)
        }
    }
}

private fun rowToggleModifier(item: DailyCheckItem, onToggle: (DailyCheckType) -> Unit): Modifier =
    if (item.isLimit) {
        Modifier
    } else {
        Modifier.toggleable(value = item.isDone, role = Role.Checkbox, onValueChange = { onToggle(item.type) })
    }

@Composable
private fun AlcoholSegment(item: DailyCheckItem, onAnswer: (Boolean) -> Unit) {
    Row(modifier = Modifier.border(width = ALCOHOL_SEGMENT_BORDER_WIDTH, color = AtelierInkMuted)) {
        Segment(
            text = stringResource(R.string.home_alcohol_no),
            isSelected = item.isAnsweredNo,
            onSelect = { onAnswer(false) },
        )
        Segment(
            text = stringResource(R.string.home_alcohol_yes),
            isSelected = item.isDone,
            onSelect = { onAnswer(true) },
        )
    }
}

@Composable
private fun Segment(text: String, isSelected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = SEGMENT_PADDING_HORIZONTAL, vertical = SEGMENT_PADDING_VERTICAL),
    ) {
        val tone = if (isSelected) MicroLabelTone.Done else MicroLabelTone.Dim
        MicroLabel(text = text, style = MicroLabelStyle(tone = tone))
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun HomeChecksSectionPreview() {
    AtelierTheme {
        HomeChecksSection(
            model = HomeChecksModel(previewDailyChecks(), PREVIEW_KEPT_CHECKS, PREVIEW_TOTAL_CHECKS),
            actions = HomeCheckActions(onToggle = {}, onAnswerAlcohol = {}),
        )
    }
}
