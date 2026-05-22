package com.emm.mybest.features.photo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInk

private val PLACEHOLDER_PADDING = 12.dp
private const val GRADIENT_DIM_STOP_0 = 0.07f
private const val GRADIENT_DIM_STOP_1 = 0.05f
private const val SILHOUETTE_ALPHA = 0.07f

@Composable
internal fun EmptyPhotoPlaceholder(
    label: String,
    silhouette: ImageVector,
    modifier: Modifier = Modifier,
    footerText: String? = null,
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to AtelierInk.copy(alpha = GRADIENT_DIM_STOP_0),
                        1f to AtelierInk.copy(alpha = GRADIENT_DIM_STOP_1),
                    ),
                ),
            ),
    ) {
        Image(
            imageVector = silhouette,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            alpha = SILHOUETTE_ALPHA,
            contentScale = ContentScale.Fit,
        )
        MicroLabel(
            text = label,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(PLACEHOLDER_PADDING),
        )
        if (footerText != null) {
            MicroLabel(
                text = footerText,
                style = MicroLabelStyle(tone = MicroLabelTone.Dim),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(PLACEHOLDER_PADDING),
            )
        }
    }
}
