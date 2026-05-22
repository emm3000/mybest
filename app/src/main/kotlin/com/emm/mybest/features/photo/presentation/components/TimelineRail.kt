package com.emm.mybest.features.photo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val RAIL_START_PADDING = 28.dp
private val RAIL_TOP_PADDING = 16.dp
private val RAIL_BOTTOM_PADDING = 18.dp
private val TILE_SPACING = 8.dp
private val HEADER_BOTTOM_PADDING = 12.dp

@Composable
internal fun TimelineRail(
    photos: List<ProgressPhoto>,
    onTileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(
            start = RAIL_START_PADDING,
            top = RAIL_TOP_PADDING,
            bottom = RAIL_BOTTOM_PADDING,
        ),
    ) {
        MicroLabel(
            text = stringResource(R.string.photos_timeline_header_format, photos.size),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier.padding(bottom = HEADER_BOTTOM_PADDING),
        )
        LazyRow(
            contentPadding = PaddingValues(end = RAIL_START_PADDING),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
        ) {
            itemsIndexed(photos, key = { _, photo -> photo.id }) { index, photo ->
                PhotoTile(
                    photoPath = photo.photoPath,
                    dateLabel = formatTileDate(photo),
                    isNewest = index == photos.lastIndex,
                    onClick = { onTileClick(photo.id) },
                )
            }
        }
    }
}

private fun formatTileDate(photo: ProgressPhoto): String {
    val day = "%02d".format(photo.date.day)
    val month = "%02d".format(photo.date.month.ordinal + 1)
    return "$day/$month"
}
