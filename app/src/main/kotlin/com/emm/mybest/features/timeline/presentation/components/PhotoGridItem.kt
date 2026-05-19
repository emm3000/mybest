package com.emm.mybest.features.timeline.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.features.timeline.presentation.timelinePhotoTypeLabel

private val GRID_CORNER_RADIUS = 4.dp
private val SELECTION_BADGE_SIZE = 20.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PhotoGridItem(
    photo: ProgressPhoto,
    selectionMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(GRID_CORNER_RADIUS)
    val desc = "${timelinePhotoTypeLabel(photo.type)} del ${photo.date.formatEsLongDate()}"

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier.border(width = 2.dp, color = cs.primary, shape = shape)
                } else {
                    Modifier.border(width = 1.dp, color = cs.outlineVariant, shape = shape)
                },
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                selected = isSelected
                contentDescription = desc
            }
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress,
            ),
    ) {
        AsyncImage(
            model = photo.photoPath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (selectionMode && isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primary.copy(alpha = 0.18f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(SELECTION_BADGE_SIZE)
                    .background(color = cs.primary, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = cs.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
