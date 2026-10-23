package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierSansFamily

private val LINE_VERTICAL_PADDING = 10.dp
private val LINE_GAP = 16.dp
private val LINE_TEXT_SIZE = 13.sp
private val LABEL_STYLE = TextStyle(
    fontFamily = AtelierSansFamily,
    fontSize = LINE_TEXT_SIZE,
    color = AtelierInkSecondary,
)
private val VALUE_STYLE = TextStyle(fontFamily = AtelierSansFamily, fontSize = LINE_TEXT_SIZE)

@Composable
internal fun HistoryDetailLine(line: DetailLineState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LINE_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(LINE_GAP),
    ) {
        Text(text = line.label, style = LABEL_STYLE)
        Text(
            text = line.value,
            style = VALUE_STYLE,
            color = line.tone.color(),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
    Hairline()
}

@Composable
private fun DetailLineTone.color(): Color = when (this) {
    DetailLineTone.DEFAULT -> AtelierInk
    DetailLineTone.KEPT -> AtelierDone
    DetailLineTone.ALERT -> MaterialTheme.colorScheme.error
}
