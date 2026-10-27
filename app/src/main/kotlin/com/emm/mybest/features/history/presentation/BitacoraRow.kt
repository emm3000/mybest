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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierSansFamily
import com.emm.mybest.ui.theme.AtelierTheme

private val KIND_LABEL_WIDTH = 96.dp
private val ROW_DESCRIPTION_SIZE = 13.sp
private val ROW_MIN_HEIGHT = 48.dp
private val ROW_VERTICAL_PADDING = 12.dp
private val DESCRIPTION_START_PADDING = 14.dp

@Composable
internal fun BitacoraRow(
    row: BitacoraRowState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tone = if (row.isRelapse) MicroLabelTone.Warm else MicroLabelTone.Default
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .clickable(onClick = onClick)
            .padding(vertical = ROW_VERTICAL_PADDING, horizontal = HISTORY_GUT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(
            text = row.label.resolve(),
            style = MicroLabelStyle(tone = tone),
            modifier = Modifier.width(KIND_LABEL_WIDTH),
        )
        Text(
            text = row.parts.resolve(),
            style = TextStyle(
                fontFamily = AtelierSansFamily,
                fontSize = ROW_DESCRIPTION_SIZE,
                color = AtelierInk,
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = DESCRIPTION_START_PADDING),
        )
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun BitacoraRowPreview() {
    AtelierTheme {
        BitacoraRow(row = previewRelapseRow(), onClick = {})
    }
}
