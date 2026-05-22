package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk

private val DOT_SIZE = 5.dp
private val RING_SIZE = 6.dp

@Composable
internal fun HistoryLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT)
            .padding(top = 12.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(isRing = false, label = stringResource(R.string.history_legend_weight))
        LegendItem(isRing = true, label = stringResource(R.string.history_legend_photo))
    }
}

@Composable
private fun LegendItem(isRing: Boolean, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isRing) {
            Box(
                modifier = Modifier
                    .size(RING_SIZE)
                    .border(width = 1.dp, color = AtelierDone),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(DOT_SIZE)
                    .drawBehind { drawRect(AtelierInk) },
            )
        }
        MicroLabel(
            text = label,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}
