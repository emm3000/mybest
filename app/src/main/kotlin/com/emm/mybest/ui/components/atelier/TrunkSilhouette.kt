package com.emm.mybest.ui.components.atelier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierInk

private const val VIEWPORT_WIDTH = 100f
private const val VIEWPORT_HEIGHT = 140f

val TrunkSilhouette: ImageVector by lazy {
    ImageVector.Builder(
        name = "TrunkSilhouette",
        defaultWidth = VIEWPORT_WIDTH.dp,
        defaultHeight = VIEWPORT_HEIGHT.dp,
        viewportWidth = VIEWPORT_WIDTH,
        viewportHeight = VIEWPORT_HEIGHT,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(AtelierInk),
            strokeLineWidth = 0.8f,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(50f, 20f)
            quadTo(42f, 30f, 38f, 50f)
            lineTo(34f, 90f)
            quadTo(40f, 100f, 50f, 100f)
            quadTo(60f, 100f, 66f, 90f)
            lineTo(62f, 50f)
            quadTo(58f, 30f, 50f, 20f)
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(AtelierInk),
            strokeLineWidth = 0.8f,
        ) {
            moveTo(38f, 50f)
            lineTo(62f, 50f)
        }
        path(
            fill = null,
            stroke = SolidColor(AtelierInk),
            strokeLineWidth = 0.8f,
        ) {
            moveTo(50f, 20f)
            lineTo(50f, 100f)
        }
    }.build()
}

@Suppress("UnusedPrivateMember")
private val unused: Color = AtelierInk
