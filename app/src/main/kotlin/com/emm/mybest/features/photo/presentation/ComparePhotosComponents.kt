package com.emm.mybest.features.photo.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatDdMmYy
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.HFilterChip
import com.emm.mybest.ui.theme.shadcnWhite
import java.util.Locale

@Composable
internal fun ComparisonSlot(
    label: String,
    photo: ProgressPhoto?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {
                role = Role.Button
                selected = isSelected
                stateDescription = if (isSelected) {
                    "Slot activo"
                } else {
                    "Slot inactivo"
                }
                contentDescription = comparisonSlotStateDescription(
                    label = label,
                    photo = photo,
                    isSelected = isSelected,
                )
            }
            .clickable { onClick() },
    ) {
        ComparisonSlotPhotoContent(
            label = label,
            photo = photo,
            isSelected = isSelected,
        )
        ComparisonSlotFooter(
            label = label,
            isSelected = isSelected,
        )
    }
}

@Composable
private fun ColumnScope.ComparisonSlotPhotoContent(
    label: String,
    photo: ProgressPhoto?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo.photoPath,
                contentDescription = comparisonSlotDescription(label, photo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Surface(
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                shape = RoundedCornerShape(bottomEnd = 12.dp),
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Text(
                    text = photo.date.formatDdMmYy(),
                    color = shadcnWhite,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            if (isSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text(
                        text = "Activo",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        } else {
            EmptyComparisonSlot()
        }
    }
}

@Composable
private fun EmptyComparisonSlot(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "Sin foto",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun ComparisonSlotFooter(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (isSelected) "Activo" else "Toca para activar",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun PhotoTypeSelector(
    selectedType: PhotoType?,
    totalPhotosCount: Int,
    photoCountByType: Map<PhotoType, Int>,
    onTypeChange: (PhotoType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            HFilterChip(
                selected = selectedType == null,
                onClick = { onTypeChange(null) },
                label = "Todas ($totalPhotosCount)",
            )
        }
        items(PhotoType.entries) { type ->
            val isSelected = type == selectedType
            val count = photoCountByType[type] ?: 0
            HFilterChip(
                selected = isSelected,
                onClick = { onTypeChange(if (isSelected) null else type) },
                label = "${getLabelForType(type)} ($count)",
            )
        }
    }
}

@Composable
internal fun PhotoSelectionCard(
    photo: ProgressPhoto,
    selectionLabel: String?,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                selected = isSelected
                stateDescription = when (selectionLabel) {
                    "ANTES" -> "Seleccionada para ANTES"
                    "DESPUÉS" -> "Seleccionada para DESPUÉS"
                    else -> "Disponible para seleccionar"
                }
                contentDescription = photoSelectionDescription(
                    photo = photo,
                    selectionLabel = selectionLabel,
                )
            }
            .clickable { onSelect() },
    ) {
        AsyncImage(
            model = photo.photoPath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Surface(
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
            shape = RoundedCornerShape(topEnd = 12.dp),
            modifier = Modifier.align(Alignment.BottomStart),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = photo.date.formatDdMmYy(),
                    color = shadcnWhite,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = getLabelForType(photo.type),
                    color = shadcnWhite,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            )
        }
        selectionLabel?.let { label ->
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 12.dp),
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
internal fun ComparePhotosEmptyState(
    selectedType: PhotoType?,
    totalPhotosCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = if (selectedType != null) {
                    "No hay fotos de ${getLabelForType(selectedType).lowercase(Locale.getDefault())}."
                } else {
                    "Aún no tienes fotos para comparar."
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (selectedType != null && totalPhotosCount > 0) {
                    "Cambia el filtro o agrega fotos de este tipo para poder compararlas."
                } else if (selectedType != null) {
                    "Aún no tienes fotos cargadas. Agrega fotos para habilitar comparación."
                } else {
                    "Agrega fotos de progreso para empezar a comparar cambios."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun comparisonSlotDescription(label: String, photo: ProgressPhoto): String {
    return "$label, ${getLabelForType(photo.type)}, ${photo.date.formatDdMmYy()}"
}

private fun comparisonSlotStateDescription(
    label: String,
    photo: ProgressPhoto?,
    isSelected: Boolean,
): String {
    val activityState = if (isSelected) "Activo" else "Inactivo"
    return if (photo != null) {
        "$activityState. ${comparisonSlotDescription(label, photo)}"
    } else {
        "$activityState. Slot $label sin foto."
    }
}

private fun photoSelectionDescription(
    photo: ProgressPhoto,
    selectionLabel: String?,
): String {
    val selectedContext = when (selectionLabel) {
        "ANTES" -> "Seleccionada para ANTES. "
        "DESPUÉS" -> "Seleccionada para DESPUÉS. "
        else -> ""
    }
    return selectedContext +
        "Foto de ${getLabelForType(photo.type).lowercase(Locale.getDefault())}, ${photo.date.formatDdMmYy()}"
}
