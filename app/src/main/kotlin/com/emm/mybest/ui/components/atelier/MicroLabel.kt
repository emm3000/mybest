package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierWarm

/**
 * Micro-label — Geist Mono, all caps, tracked at 0.18em.
 *
 * Callers pass already-uppercase strings (the design source treats casing
 * as part of the content, not a runtime transform — keeps accent handling
 * predictable for Spanish copy like `MIÉ · 21 MAY`).
 */
@Composable
fun MicroLabel(
    text: String,
    modifier: Modifier = Modifier,
    tone: MicroLabelTone = MicroLabelTone.Default,
    size: TextUnit = DEFAULT_SIZE_SP,
) {
    Text(
        text = text,
        modifier = modifier,
        color = when (tone) {
            MicroLabelTone.Default -> AtelierInkSecondary
            MicroLabelTone.Dim -> AtelierInkTertiary
            MicroLabelTone.Done -> AtelierDone
            MicroLabelTone.Warm -> AtelierWarm
        },
        fontFamily = AtelierMonoFamily,
        fontSize = size,
        letterSpacing = TRACKING_EM,
        fontWeight = FontWeight.Normal,
    )
}

private val DEFAULT_SIZE_SP = 10.sp
private val TRACKING_EM = 0.18.em

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun MicroLabelPreview() {
    Column(modifier = Modifier.background(AtelierBackground).padding(16.dp)) {
        MicroLabel("MIÉ · 21 MAY")
        MicroLabel("SEMANA 20", tone = MicroLabelTone.Dim)
        MicroLabel("CUMPLIDO", tone = MicroLabelTone.Done)
        MicroLabel("RECOMENDACIÓN", tone = MicroLabelTone.Warm)
    }
}
