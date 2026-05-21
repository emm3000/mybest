package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierSerifFamily

/**
 * Editorial numeric display — Instrument Serif, tabular-nums, tight line.
 *
 * Regular weight (the serif's elegance is in the shapes), `-0.02em` tracking,
 * `0.88` line-height, `tnum` feature so digits stack when the value changes.
 */
@Composable
fun DisplayNumber(
    text: String,
    style: DisplayNumberStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = style.color,
        style = TextStyle(
            fontFamily = AtelierSerifFamily,
            fontSize = style.fontSize,
            fontStyle = style.italic.toFontStyle(),
            fontWeight = FontWeight.Normal,
            letterSpacing = LETTER_SPACING_EM,
            lineHeight = style.fontSize * LINE_HEIGHT_RATIO,
            fontFeatureSettings = TABULAR_NUMS_FEATURE,
        ),
    )
}

private val LETTER_SPACING_EM = (-0.02).em
private const val LINE_HEIGHT_RATIO = 0.88f
private const val TABULAR_NUMS_FEATURE = "tnum"

private fun Boolean.toFontStyle(): FontStyle = if (this) FontStyle.Italic else FontStyle.Normal

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun DisplayNumberPreview() {
    Column(modifier = Modifier.background(AtelierBackground).padding(16.dp)) {
        DisplayNumber("3", DisplayNumberStyle(fontSize = 148.sp, italic = true))
        DisplayNumber("/5", DisplayNumberStyle(fontSize = 56.sp, color = AtelierInkTertiary))
        DisplayNumber("−4.2", DisplayNumberStyle(fontSize = 96.sp, italic = true))
        DisplayNumber("78.2", DisplayNumberStyle(fontSize = 28.sp))
    }
}
