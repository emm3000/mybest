package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierHairline
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierMonoFamily
import java.util.Locale

private val CHART_HEIGHT = 108.dp
private val CHART_PADDING_H = 16.dp
private val CHART_PADDING_T = 16.dp
private val CHART_PADDING_B = 12.dp
private val CHART_LABEL_SIZE = 10.sp
private val CHART_LABEL_TRACKING = 0.1.em
private const val CHART_VALUE_PADDING = 0.3f
private const val DASH_ON = 1f
private const val DASH_OFF = 4f

@Composable
internal fun InsightsMiniChartContainer(weights: List<Float>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CHART_PADDING_H)
            .padding(top = CHART_PADDING_T, bottom = CHART_PADDING_B),
    ) {
        MiniChart(
            data = weights,
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT),
        )
    }
}

@Composable
fun MiniChart(data: List<Float>, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val insufficientLabel = stringResource(R.string.insights_chart_insufficient)

    if (data.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            MicroLabel(text = insufficientLabel, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        }
        return
    }

    val firstLabel = remember(data) { String.format(Locale.US, "%.1f", data.first()) }
    val lastLabel = remember(data) { String.format(Locale.US, "%.1f", data.last()) }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minVal = data.min() - CHART_VALUE_PADDING
        val maxVal = data.max() + CHART_VALUE_PADDING

        val path = Path()
        data.forEachIndexed { i, v ->
            val x = w * i / (data.size - 1)
            val y = h - (v - minVal) / (maxVal - minVal) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = AtelierInk,
            style = Stroke(
                width = 1.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
        drawLine(
            color = AtelierHairline,
            start = Offset(0f, h),
            end = Offset(w, h),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = AtelierHairline.copy(alpha = 0.05f),
            start = Offset(0f, h / 2f),
            end = Offset(w, h / 2f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF)),
        )

        val firstY = h - (data.first() - minVal) / (maxVal - minVal) * h
        drawCircle(
            color = AtelierInk,
            radius = 3.dp.toPx(),
            center = Offset(0f, firstY),
        )

        val lastY = h - (data.last() - minVal) / (maxVal - minVal) * h
        drawCircle(
            color = AtelierDone,
            radius = 4.dp.toPx(),
            center = Offset(w, lastY),
        )
        drawCircle(
            color = AtelierDone.copy(alpha = 0.3f),
            radius = 9.dp.toPx(),
            center = Offset(w, lastY),
            style = Stroke(width = 0.5.dp.toPx()),
        )

        val labelStyle = TextStyle(
            fontFamily = AtelierMonoFamily,
            fontSize = CHART_LABEL_SIZE,
            letterSpacing = CHART_LABEL_TRACKING,
        )
        val lastMeasure = textMeasurer.measure(lastLabel, labelStyle.copy(color = AtelierDone))
        val lastOffX = (w - lastMeasure.size.width - 6.dp.toPx()).coerceAtLeast(0f)
        val lastOffY = (lastY - lastMeasure.size.height - 12.dp.toPx()).coerceAtLeast(0f)
        drawText(
            textLayoutResult = lastMeasure,
            topLeft = Offset(lastOffX, lastOffY),
        )

        val dimInk = AtelierInk.copy(alpha = 0.5f)
        val firstMeasure = textMeasurer.measure(firstLabel, labelStyle.copy(color = dimInk))
        val firstOffY = (firstY - firstMeasure.size.height - 10.dp.toPx()).coerceAtLeast(0f)
        drawText(
            textLayoutResult = firstMeasure,
            topLeft = Offset(0f, firstOffY),
        )
    }
}
