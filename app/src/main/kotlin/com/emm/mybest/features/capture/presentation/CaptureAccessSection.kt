package com.emm.mybest.features.capture.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInkSecondary

private val SECTION_GAP = 12.dp

@Composable
internal fun CaptureAccessSection(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SECTION_GAP)) {
        MicroLabel(
            text = stringResource(R.string.capture_access_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Warm),
        )
        Text(
            text = stringResource(R.string.capture_access_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = AtelierInkSecondary,
        )
        HButton(
            text = stringResource(R.string.capture_access_open_settings),
            onClick = onOpenSettings,
            variant = ButtonVariant.Outline,
        )
    }
}
