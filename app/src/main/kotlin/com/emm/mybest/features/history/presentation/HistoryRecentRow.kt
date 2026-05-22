package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.components.atelier.photoTypeLabel
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierSansFamily
import kotlinx.datetime.LocalDate

private val DATE_LABEL_WIDTH = 96.dp
private val ROW_DESCRIPTION_SIZE = 13.sp
private val ROW_MIN_HEIGHT = 48.dp
private val ROW_VERTICAL_PADDING = 12.dp

@Composable
internal fun HistoryRecentRow(
    entry: HistoryRecentEntry,
    today: LocalDate,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateLabel = formatRecentDate(entry.date)
    val photoLabels = entry.photoTypes.map { photoTypeLabel(it) }
    val description = buildDescription(entry, photoLabels)
    val isToday = entry.date == today

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .clickable { onClick(entry.date) }
            .padding(vertical = ROW_VERTICAL_PADDING, horizontal = HISTORY_GUT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(
            text = dateLabel,
            modifier = Modifier.width(DATE_LABEL_WIDTH),
        )
        Text(
            text = description,
            style = TextStyle(
                fontFamily = AtelierSansFamily,
                fontSize = ROW_DESCRIPTION_SIZE,
                color = AtelierInk,
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
        )
        if (isToday) {
            MicroLabel(
                text = stringResource(R.string.history_today_chip),
                style = MicroLabelStyle(tone = MicroLabelTone.Done),
            )
        }
    }
}

private fun buildDescription(entry: HistoryRecentEntry, photoLabels: List<String>): String {
    val parts = mutableListOf<String>()
    entry.weight?.let { parts.add("Peso ${"%.1f".format(it)}") }
    if (photoLabels.isNotEmpty()) {
        parts.add("Foto ${photoLabels.joinToString(" · ")}")
    }
    return parts.joinToString(" · ")
}
