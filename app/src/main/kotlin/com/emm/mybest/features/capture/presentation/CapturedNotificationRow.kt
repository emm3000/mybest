package com.emm.mybest.features.capture.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInkSecondary

private val ROW_VERTICAL = 14.dp
private val ROW_GAP = 4.dp

@Composable
internal fun CapturedNotificationRow(
    item: CapturedItemState,
    modifier: Modifier = Modifier,
) {
    val postedAt = stringResource(R.string.capture_posted_at, item.postedAtDateLabel, item.postedAtTimeLabel)
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = ROW_VERTICAL),
        verticalArrangement = Arrangement.spacedBy(ROW_GAP),
    ) {
        MicroLabel(text = item.appLabel)
        item.title?.let { Text(text = it, style = MaterialTheme.typography.titleMedium) }
        item.text?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = AtelierInkSecondary)
        }
        MicroLabel(text = postedAt, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
    }
}
