package com.emm.mybest.features.history.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInkMuted
import com.emm.mybest.ui.theme.AtelierTheme

private val SWITCH_BORDER_WIDTH = 1.dp
private val SEGMENT_PADDING_HORIZONTAL = 14.dp
private val SEGMENT_PADDING_VERTICAL = 10.dp
private val SWITCH_VERTICAL_PADDING = 12.dp

@Composable
internal fun HistoryViewSwitch(
    selected: HistoryView,
    onSelect: (HistoryView) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = HISTORY_GUT, vertical = SWITCH_VERTICAL_PADDING)
            .border(width = SWITCH_BORDER_WIDTH, color = AtelierInkMuted),
    ) {
        HistoryView.entries.forEach { view ->
            Segment(
                text = stringResource(view.labelRes()),
                isSelected = view == selected,
                onSelect = { onSelect(view) },
            )
        }
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

@StringRes
private fun HistoryView.labelRes(): Int = when (this) {
    HistoryView.CALENDAR -> R.string.history_view_calendar
    HistoryView.LOGBOOK -> R.string.history_view_logbook
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun HistoryViewSwitchPreview() {
    AtelierTheme {
        HistoryViewSwitch(selected = HistoryView.LOGBOOK, onSelect = {})
    }
}
