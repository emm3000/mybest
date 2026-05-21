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
import com.emm.mybest.ui.theme.AtelierBg
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk2
import com.emm.mybest.ui.theme.AtelierInk3
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierWarm

enum class MLabelTone { Default, Dim, Done, Warm }

/**
 * Micro-label — Geist Mono, all caps, tracked at 0.18em.
 *
 * Callers pass already-uppercase strings (the design source treats casing
 * as part of the content, not a runtime transform — keeps accent handling
 * predictable for Spanish copy like `MIÉ · 21 MAY`).
 */
@Composable
fun MLabel(
    text: String,
    modifier: Modifier = Modifier,
    tone: MLabelTone = MLabelTone.Default,
    size: TextUnit = 10.sp,
) {
    Text(
        text = text,
        modifier = modifier,
        color = when (tone) {
            MLabelTone.Default -> AtelierInk2
            MLabelTone.Dim -> AtelierInk3
            MLabelTone.Done -> AtelierDone
            MLabelTone.Warm -> AtelierWarm
        },
        fontFamily = AtelierMonoFamily,
        fontSize = size,
        letterSpacing = 0.18.em,
        fontWeight = FontWeight.Normal,
    )
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun MLabelPreview() {
    Column(modifier = Modifier.background(AtelierBg).padding(16.dp)) {
        MLabel("MIÉ · 21 MAY")
        MLabel("SEMANA 20", tone = MLabelTone.Dim)
        MLabel("CUMPLIDO", tone = MLabelTone.Done)
        MLabel("RECOMENDACIÓN", tone = MLabelTone.Warm)
    }
}
