package com.emm.mybest.ui.components.atelier

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierInk

private const val FACE_VIEWPORT_WIDTH = 100f
private const val FACE_VIEWPORT_HEIGHT = 140f

val FaceSilhouette: ImageVector by lazy {
    ImageVector.Builder(
        name = "FaceSilhouette",
        defaultWidth = FACE_VIEWPORT_WIDTH.dp,
        defaultHeight = FACE_VIEWPORT_HEIGHT.dp,
        viewportWidth = FACE_VIEWPORT_WIDTH,
        viewportHeight = FACE_VIEWPORT_HEIGHT,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(AtelierInk),
            strokeLineWidth = 0.8f,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(50f - 22f, 60f)
            arcTo(
                horizontalEllipseRadius = 22f,
                verticalEllipseRadius = 28f,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                x1 = 50f + 22f,
                y1 = 60f,
            )
            arcTo(
                horizontalEllipseRadius = 22f,
                verticalEllipseRadius = 28f,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                x1 = 50f - 22f,
                y1 = 60f,
            )
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(AtelierInk),
            strokeLineWidth = 0.8f,
        ) {
            moveTo(50f, 48f)
            lineTo(50f, 62f)
        }
        path(
            fill = SolidColor(AtelierInk),
            stroke = null,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(42f, 55f)
            arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 43.2f, 55f)
            arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 42f, 55f)
            close()
        }
        path(
            fill = SolidColor(AtelierInk),
            stroke = null,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(58f, 55f)
            arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.2f, 55f)
            arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 58f, 55f)
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(AtelierInk),
            strokeLineWidth = 0.8f,
        ) {
            moveTo(44f, 70f)
            quadTo(50f, 73f, 56f, 70f)
        }
    }.build()
}
