package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.usecase.history.DaySummary

internal enum class DayTimelineEventType {
    WEIGHT,
    PHOTO,
}

internal data class DayTimelineEntry(
    val type: DayTimelineEventType,
    val sequence: Long,
    val photo: ProgressPhoto? = null,
)

@Composable
internal fun DayEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Sin actividad registrada",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
internal fun DayTimelineSection(
    summary: DaySummary,
    isToday: Boolean,
    onDeleteWeight: () -> Unit,
    onDeletePhoto: (ProgressPhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeline = buildDayTimelineEntries(summary)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Actividad del día",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        timeline.forEach { entry ->
            when (entry.type) {
                DayTimelineEventType.WEIGHT -> {
                    WeightTimelineItem(summary = summary, isToday = isToday, onDeleteWeight = onDeleteWeight)
                }

                DayTimelineEventType.PHOTO -> {
                    entry.photo?.let { photo ->
                        PhotoTimelineItem(
                            photo = photo,
                            isToday = isToday,
                            onDeletePhoto = onDeletePhoto,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightTimelineItem(
    summary: DaySummary,
    isToday: Boolean,
    onDeleteWeight: () -> Unit,
) {
    summary.weight?.let { weight ->
        DetailItem(
            icon = Icons.Rounded.MonitorWeight,
            color = MaterialTheme.colorScheme.primary,
            title = "Peso: ${weight.weight} kg",
            subtitle = weight.note,
            onDelete = if (isToday) onDeleteWeight else null,
        )
    }
}

@Composable
private fun PhotoTimelineItem(
    photo: ProgressPhoto,
    isToday: Boolean,
    onDeletePhoto: (ProgressPhoto) -> Unit,
) {
    DetailItem(
        icon = Icons.Rounded.Image,
        color = MaterialTheme.colorScheme.tertiary,
        title = "Foto: ${photo.type.toSpanishLabel()}",
        onDelete = if (isToday) {
            { onDeletePhoto(photo) }
        } else {
            null
        },
        content = {
            AsyncImage(
                model = photo.photoPath,
                contentDescription = "Foto de ${photo.type.toSpanishLabel().lowercase()}",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(88.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        },
    )
}

internal fun PhotoType.toSpanishLabel(): String = when (this) {
    PhotoType.TRUNK -> "Tronco"
    PhotoType.FACE -> "Cara"
}

internal fun buildDayTimelineEntries(summary: DaySummary): List<DayTimelineEntry> {
    val entries = mutableListOf<DayTimelineEntry>()
    summary.weight?.let {
        entries.add(DayTimelineEntry(type = DayTimelineEventType.WEIGHT, sequence = 1))
    }
    summary.photos.sortedBy { it.createdAt }.forEachIndexed { index, photo ->
        entries.add(
            DayTimelineEntry(
                type = DayTimelineEventType.PHOTO,
                sequence = 1000L + index,
                photo = photo,
            ),
        )
    }
    return entries.sortedBy { it.sequence }
}
