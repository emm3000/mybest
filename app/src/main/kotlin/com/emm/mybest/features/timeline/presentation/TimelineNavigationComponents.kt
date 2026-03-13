package com.emm.mybest.features.timeline.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatDdMmYy
import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.HFilterChip

internal enum class DateJumpDirection {
    PREVIOUS,
    NEXT,
}

@Composable
internal fun TimelineDateJumpRow(
    dates: List<kotlinx.datetime.LocalDate>,
    selectedDate: kotlinx.datetime.LocalDate,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Saltar por fecha",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        resolveDateJumpTarget(dates, selectedDate, DateJumpDirection.PREVIOUS)?.let(onDateSelected)
                    },
                    enabled = resolveDateJumpTarget(dates, selectedDate, DateJumpDirection.PREVIOUS) != null,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Fecha anterior",
                    )
                }
                IconButton(
                    onClick = {
                        resolveDateJumpTarget(dates, selectedDate, DateJumpDirection.NEXT)?.let(onDateSelected)
                    },
                    enabled = resolveDateJumpTarget(dates, selectedDate, DateJumpDirection.NEXT) != null,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Fecha siguiente",
                    )
                }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(dates) { date ->
                HFilterChip(
                    label = date.formatDdMmYy(),
                    selected = date == selectedDate,
                    onClick = { onDateSelected(date) },
                )
            }
        }
    }
}

internal fun resolveDateJumpTarget(
    dates: List<kotlinx.datetime.LocalDate>,
    selectedDate: kotlinx.datetime.LocalDate,
    direction: DateJumpDirection,
): kotlinx.datetime.LocalDate? {
    if (dates.isEmpty()) return null
    val selectedIndex = dates.indexOf(selectedDate).takeIf { it >= 0 } ?: return dates.firstOrNull()
    return when (direction) {
        DateJumpDirection.PREVIOUS -> dates.getOrNull(selectedIndex - 1)
        DateJumpDirection.NEXT -> dates.getOrNull(selectedIndex + 1)
    }
}

@Composable
internal fun TimelineThumbnailStrip(
    photos: List<ProgressPhoto>,
    currentPhotoId: String,
    listState: LazyListState,
    onPhotoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Navegación rápida",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(
                items = photos,
                key = { _, photo -> photo.id },
            ) { index, photo ->
                TimelineThumbnailCard(
                    photo = photo,
                    isSelected = photo.id == currentPhotoId,
                    onClick = { onPhotoSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun TimelineThumbnailCard(
    photo: ProgressPhoto,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = RoundedCornerShape(18.dp),
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                selected = isSelected
                stateDescription = if (isSelected) {
                    "Foto actual"
                } else {
                    "Miniatura disponible"
                }
                contentDescription = buildTimelineThumbnailDescription(
                    photo = photo,
                    isSelected = isSelected,
                )
            }
            .clickable { onClick() },
    ) {
        AsyncImage(
            model = photo.photoPath,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Surface(
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Text(
                text = photo.date.formatDdMmYy(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            )
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 12.dp),
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Text(
                    text = "Actual",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

private fun buildTimelineThumbnailDescription(
    photo: ProgressPhoto,
    isSelected: Boolean,
): String {
    val prefix = if (isSelected) "Foto actual. " else ""
    return prefix +
        "Miniatura de ${timelinePhotoTypeLabel(photo.type).lowercase()} del ${photo.date.formatEsLongDate()}."
}
