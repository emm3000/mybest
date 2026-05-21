package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInkTertiary

/**
 * 18dp completion check — filled sage square with a stroked tick when done,
 * 1dp hairline outline when not.
 *
 * The tick glyph mirrors the original SVG path `M1 4 l2.5 2.5 L9 1` from a
 * 10×8 viewBox, centred inside the 18dp box.
 */
@Composable
fun CompletionCheck(done: Boolean, modifier: Modifier = Modifier) {
    if (done) FilledCheckBox(modifier) else OutlinedCheckBox(modifier)
}

@Composable
private fun FilledCheckBox(modifier: Modifier) {
    Box(modifier = modifier.size(BOX_SIZE).background(AtelierDone)) {
        Canvas(modifier = Modifier.size(BOX_SIZE)) { drawCheckGlyph() }
    }
}

@Composable
private fun OutlinedCheckBox(modifier: Modifier) {
    Box(modifier = modifier.size(BOX_SIZE).border(width = 1.dp, color = AtelierInkTertiary))
}

private fun DrawScope.drawCheckGlyph() {
    val stroke = Stroke(
        width = STROKE_WIDTH.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    val glyph = Path().apply {
        moveTo(GLYPH_START_X.dp.toPx(), GLYPH_START_Y.dp.toPx())
        lineTo(GLYPH_MID_X.dp.toPx(), GLYPH_MID_Y.dp.toPx())
        lineTo(GLYPH_END_X.dp.toPx(), GLYPH_END_Y.dp.toPx())
    }
    drawPath(path = glyph, color = AtelierBackground, style = stroke)
}

private val BOX_SIZE = 18.dp
private const val STROKE_WIDTH = 1.6f
private const val GLYPH_START_X = 5f
private const val GLYPH_START_Y = 9f
private const val GLYPH_MID_X = 7.5f
private const val GLYPH_MID_Y = 11.5f
private const val GLYPH_END_X = 13f
private const val GLYPH_END_Y = 6f

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CompletionCheckPreview() {
    Row(modifier = Modifier.background(AtelierBackground).padding(16.dp)) {
        CompletionCheck(done = true)
        Box(modifier = Modifier.width(16.dp))
        CompletionCheck(done = false)
    }
}
