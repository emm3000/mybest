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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierBg
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk3

/**
 * 18dp check indicator — printed-style.
 *
 * Done: filled sage square with a rounded-stroke check rendered in bg color.
 * Not done: 1dp hairline outline at 34% bone.
 *
 * The check geometry mirrors the original SVG path `M1 4 l2.5 2.5 L9 1` from a
 * 10×8 viewBox, centered inside the 18dp box (so the glyph sits at columns
 * 4..14 / rows 5..13 in absolute dp coordinates).
 */
@Composable
fun Check(done: Boolean, modifier: Modifier = Modifier) {
    if (done) {
        Box(
            modifier = modifier.size(BOX_DP.dp).background(AtelierDone),
        ) {
            Canvas(modifier = Modifier.size(BOX_DP.dp)) {
                val stroke = Stroke(
                    width = STROKE_DP.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
                val path = Path().apply {
                    moveTo(START_X.dp.toPx(), START_Y.dp.toPx())
                    lineTo(MID_X.dp.toPx(), MID_Y.dp.toPx())
                    lineTo(END_X.dp.toPx(), END_Y.dp.toPx())
                }
                drawPath(path = path, color = AtelierBg, style = stroke)
            }
        }
    } else {
        Box(
            modifier = modifier
                .size(BOX_DP.dp)
                .border(1.dp, AtelierInk3),
        )
    }
}

private const val BOX_DP = 18
private const val STROKE_DP = 1.6f

// Glyph anchors inside the 18dp box (centered 10×8 viewBox).
private const val START_X = 5f
private const val START_Y = 9f
private const val MID_X = 7.5f
private const val MID_Y = 11.5f
private const val END_X = 13f
private const val END_Y = 6f

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CheckPreview() {
    Row(modifier = Modifier.background(AtelierBg).padding(16.dp)) {
        Check(done = true)
        Box(modifier = Modifier.width(16.dp))
        Check(done = false)
    }
}
