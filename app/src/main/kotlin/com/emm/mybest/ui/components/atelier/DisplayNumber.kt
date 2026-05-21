package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.ui.theme.AtelierBg
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInk3
import com.emm.mybest.ui.theme.AtelierSerifFamily

/**
 * Editorial numeric display — Instrument Serif, tabular-nums, tight line.
 *
 * Mirrors the `.disp` / `.disp--italic` token from the design: regular weight
 * (the serif's elegance is in the shapes, not the weight), `-0.02em` track,
 * `0.88` line-height, `tnum` feature on so digits stack perfectly when the
 * value changes (e.g. ratio counter, peso, delta).
 */
@Composable
fun DisplayNumber(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    italic: Boolean = false,
    color: Color = AtelierInk,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = TextStyle(
            fontFamily = AtelierSerifFamily,
            fontSize = fontSize,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.02).em,
            lineHeight = fontSize * 0.88f,
            fontFeatureSettings = "tnum",
        ),
    )
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun DisplayNumberPreview() {
    Column(modifier = Modifier.background(AtelierBg).padding(16.dp)) {
        DisplayNumber(text = "3", fontSize = 148.sp, italic = true)
        DisplayNumber(text = "/5", fontSize = 56.sp, color = AtelierInk3)
        DisplayNumber(text = "−4.2", fontSize = 96.sp, italic = true)
        DisplayNumber(text = "78.2", fontSize = 28.sp)
    }
}
