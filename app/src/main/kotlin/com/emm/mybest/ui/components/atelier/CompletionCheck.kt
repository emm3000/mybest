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
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInkTertiary

/**
 * 18dp completion check indicator — printed style.
 *
 * Done: filled sage square with a rounded-stroke check rendered in the page
 * background colour. Not done: 1dp hairline outline at 34% bone.
 *
 * The check glyph mirrors the original SVG path `M1 4 l2.5 2.5 L9 1` from a
 * 10×8 viewBox, centred inside the 18dp box (columns 4..14, rows 5..13 in
 * absolute dp coordinates).
 */
@Composable
fun CompletionCheck(done: Boolean, modifier: Modifier = Modifier) {
    if (done) {
        Box(modifier = modifier.size(BOX_SIZE_DP).background(AtelierDone)) {
            Canvas(modifier = Modifier.size(BOX_SIZE_DP)) {
                val stroke = Stroke(
                    width = CHECK_STROKE_WIDTH_DP.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
                val path = Path().apply {
                    moveTo(CHECK_START_X_DP.dp.toPx(), CHECK_START_Y_DP.dp.toPx())
                    lineTo(CHECK_MID_X_DP.dp.toPx(), CHECK_MID_Y_DP.dp.toPx())
                    lineTo(CHECK_END_X_DP.dp.toPx(), CHECK_END_Y_DP.dp.toPx())
                }
                drawPath(path = path, color = AtelierBackground, style = stroke)
            }
        }
    } else {
        Box(
            modifier = modifier
                .size(BOX_SIZE_DP)
                .border(width = 1.dp, color = AtelierInkTertiary),
        )
    }
}

private val BOX_SIZE_DP = 18.dp
private const val CHECK_STROKE_WIDTH_DP = 1.6f

// Glyph anchors inside the 18dp box (centred 10×8 viewBox).
private const val CHECK_START_X_DP = 5f
private const val CHECK_START_Y_DP = 9f
private const val CHECK_MID_X_DP = 7.5f
private const val CHECK_MID_Y_DP = 11.5f
private const val CHECK_END_X_DP = 13f
private const val CHECK_END_Y_DP = 6f

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CompletionCheckPreview() {
    Row(modifier = Modifier.background(AtelierBackground).padding(16.dp)) {
        CompletionCheck(done = true)
        Box(modifier = Modifier.width(16.dp))
        CompletionCheck(done = false)
    }
}
