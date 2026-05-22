package com.emm.mybest.features.photo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.R
import com.emm.mybest.core.datetime.formatDdMmYy
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val HERO_HEIGHT = 280.dp
private val HERO_HORIZONTAL_PADDING = 28.dp
private val HERO_SLOT_SPACING = 12.dp
private val OVERLAY_PADDING = 12.dp

@Composable
internal fun CompareHero(
    beforePhoto: ProgressPhoto?,
    afterPhoto: ProgressPhoto?,
    silhouette: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(HERO_HEIGHT)
            .padding(horizontal = HERO_HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(HERO_SLOT_SPACING),
    ) {
        HeroSlot(
            label = stringResource(R.string.photos_label_before),
            photo = beforePhoto,
            silhouette = silhouette,
            modifier = Modifier.weight(1f),
        )
        HeroSlot(
            label = stringResource(R.string.photos_label_after),
            photo = afterPhoto,
            silhouette = silhouette,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HeroSlot(
    label: String,
    photo: ProgressPhoto?,
    silhouette: ImageVector,
    modifier: Modifier = Modifier,
) {
    if (photo != null) {
        FilledHeroSlot(
            label = label,
            photo = photo,
            modifier = modifier,
        )
    } else {
        EmptyPhotoPlaceholder(
            label = label,
            silhouette = silhouette,
            modifier = modifier,
        )
    }
}

@Composable
private fun FilledHeroSlot(
    label: String,
    photo: ProgressPhoto,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = photo.photoPath,
            contentDescription = label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        MicroLabel(
            text = label,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(OVERLAY_PADDING),
        )
        MicroLabel(
            text = photo.date.formatDdMmYy(),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(OVERLAY_PADDING),
        )
    }
}
