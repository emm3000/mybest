package com.emm.mybest.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme
import kotlin.math.min

private const val RING_ANIMATION_MS = 300

@Composable
fun HProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 8.dp,
    showLabel: Boolean = false,
    label: String? = null,
) {
    val cs = MaterialTheme.colorScheme
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = RING_ANIMATION_MS),
        label = "ring_progress",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val canvasMinSide = min(this.size.width, this.size.height)
            val radius = (canvasMinSide - strokeWidth.toPx()) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            drawCircle(
                color = cs.secondary.copy(alpha = 0.16f),
                radius = radius,
                center = center,
                style = stroke,
            )

            drawArc(
                color = cs.secondary,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(
                    x = center.x - radius,
                    y = center.y - radius,
                ),
                size = Size(radius * 2f, radius * 2f),
                style = stroke,
            )
        }

        if (showLabel) {
            Text(
                text = label ?: "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun HProgressRing(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 8.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
        strokeWidth = strokeWidth,
    )
}

@PreviewLightDark
@Composable
private fun HProgressRingPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HProgressRing(
                    progress = 0.72f,
                    showLabel = true,
                )
                HProgressRing(
                    progress = 0.35f,
                    showLabel = true,
                    label = "7/20",
                    modifier = Modifier.padding(top = 16.dp),
                )
                HProgressRing(
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
