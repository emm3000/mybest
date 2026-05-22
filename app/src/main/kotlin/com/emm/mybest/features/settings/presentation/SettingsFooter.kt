package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val SETTINGS_GUT = 28.dp
private val FOOTER_TOP = 18.dp
private val FOOTER_BOTTOM = 22.dp

@Composable
internal fun SettingsFooter(
    versionLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = SETTINGS_GUT,
                end = SETTINGS_GUT,
                top = FOOTER_TOP,
                bottom = FOOTER_BOTTOM,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        MicroLabel(
            text = versionLabel,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        MicroLabel(
            text = stringResource(R.string.settings_footer_tagline),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}
