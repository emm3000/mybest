package com.emm.mybest.features.photo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierHairline
import com.emm.mybest.ui.theme.AtelierInk

private val TILE_WIDTH = 60.dp
private val TILE_HEIGHT = 80.dp
private val TILE_LABEL_PADDING = 4.dp
private val NEWEST_TILE_OUTLINE_WIDTH = 1.dp
private const val TILE_DATE_FONT_SIZE = 8
private const val GRADIENT_ALPHA_START = 0.06f
private const val GRADIENT_ALPHA_END = 0.02f

@Composable
internal fun PhotoTile(
    photoPath: String?,
    dateLabel: String,
    isNewest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outlineModifier = if (isNewest) {
        Modifier.border(width = NEWEST_TILE_OUTLINE_WIDTH, color = AtelierDone)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(width = TILE_WIDTH, height = TILE_HEIGHT)
            .then(outlineModifier)
            .background(AtelierHairline)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to AtelierInk.copy(alpha = GRADIENT_ALPHA_START),
                        1f to AtelierInk.copy(alpha = GRADIENT_ALPHA_END),
                    ),
                ),
            )
            .clickable { onClick() },
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = photoPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        MicroLabel(
            text = dateLabel,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim, size = TILE_DATE_FONT_SIZE.sp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(TILE_LABEL_PADDING),
        )
    }
}
