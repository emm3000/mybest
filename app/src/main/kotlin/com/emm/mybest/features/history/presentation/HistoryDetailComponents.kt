package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.IconButtonVariant
import com.emm.mybest.ui.theme.shadcnWhite

private const val DAY_PHOTOS_GRID_COLUMNS = 3

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
internal fun HabitDetailItem(
    habit: DailyHabitSummary,
    isToday: Boolean,
    onDeleteHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailItem(
        icon = Icons.Rounded.CheckCircle,
        color = MaterialTheme.colorScheme.secondary,
        title = "Hábitos",
        onDelete = if (isToday) onDeleteHabit else null,
        modifier = modifier,
        content = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (habit.ateHealthy) HChip("Comida Sana")
                if (habit.didExercise) HChip("Ejercicio")
            }
            habit.notes?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
internal fun DayPhotosSection(
    photos: List<ProgressPhoto>,
    photoHabitNames: Map<String, String>,
    isToday: Boolean,
    onDeletePhoto: (ProgressPhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Fotos del día", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(DAY_PHOTOS_GRID_COLUMNS),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(photos) { photo ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                ) {
                    AsyncImage(
                        model = photo.photoPath,
                        contentDescription = "Foto de ${photo.type.toSpanishLabel().lowercase()} del ${photo.date.formatEsLongDate()}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = photo.type.toSpanishLabel(),
                                style = MaterialTheme.typography.labelSmall,
                                color = shadcnWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            photoHabitNames[photo.id]?.let { habitName ->
                                Text(
                                    text = habitName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = shadcnWhite.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }
                        }
                    }
                    if (isToday) {
                        HIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "Eliminar foto de ${photo.type.toSpanishLabel().lowercase()}",
                            onClick = { onDeletePhoto(photo) },
                            variant = IconButtonVariant.Destructive,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f), CircleShape),
                        )
                    }
                }
            }
        }
    }
}

private fun PhotoType.toSpanishLabel(): String = when (this) {
    PhotoType.FACE -> "Cara"
    PhotoType.ABDOMEN -> "Abdomen"
    PhotoType.BODY -> "Cuerpo"
    PhotoType.BREAKFAST -> "Desayuno"
    PhotoType.LUNCH -> "Almuerzo"
    PhotoType.DINNER -> "Cena"
    PhotoType.FOOD -> "Comida"
}
