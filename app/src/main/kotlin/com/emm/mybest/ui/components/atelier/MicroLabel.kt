package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierWarm

/**
 * Micro-label — Geist Mono, all caps, tracked at 0.18em.
 *
 * Callers pass already-uppercase strings: the design treats casing as part of
 * the content (preserves Spanish accents like "MIÉ · 21 MAY" predictably).
 */
@Composable
fun MicroLabel(
    text: String,
    modifier: Modifier = Modifier,
    style: MicroLabelStyle = MicroLabelStyle(),
) {
    Text(
        text = text,
        modifier = modifier,
        color = style.tone.toInkColor(),
        fontFamily = AtelierMonoFamily,
        fontSize = style.size,
        letterSpacing = TRACKING_EM,
        fontWeight = FontWeight.Normal,
    )
}

private val TRACKING_EM = 0.18.em

private fun MicroLabelTone.toInkColor(): Color = when (this) {
    MicroLabelTone.Default -> AtelierInkSecondary
    MicroLabelTone.Dim -> AtelierInkTertiary
    MicroLabelTone.Done -> AtelierDone
    MicroLabelTone.Warm -> AtelierWarm
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun MicroLabelPreview() {
    Column(modifier = Modifier.background(AtelierBackground).padding(16.dp)) {
        MicroLabel("MIÉ · 21 MAY")
        MicroLabel("SEMANA 20", style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        MicroLabel("CUMPLIDO", style = MicroLabelStyle(tone = MicroLabelTone.Done))
        MicroLabel("RECOMENDACIÓN", style = MicroLabelStyle(tone = MicroLabelTone.Warm))
    }
}
