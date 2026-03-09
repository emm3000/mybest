package com.emm.mybest.features.photo.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.core.datetime.formatDdMmYy
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.HFilterChip
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HTopBar
import com.emm.mybest.ui.theme.shadcnWhite
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

private const val PHOTO_SELECTION_GRID_COLUMNS = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComparePhotosScreen(
    viewModel: ComparePhotosViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnBackClick by rememberUpdatedState(onBackClick)
    var selectingForBefore by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ComparePhotosEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HTopBar(
                title = "Comparador de Progreso",
                navigationIcon = {
                    HIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = currentOnBackClick,
                    )
                },
                actions = {
                    HIconButton(
                        icon = Icons.Rounded.SwapHoriz,
                        contentDescription = "Intercambiar",
                        onClick = { viewModel.onIntent(ComparePhotosIntent.ToggleSwap) },
                    )
                },
            )
        },
    ) { padding ->
        ComparePhotosContent(
            state = state,
            contentPadding = padding,
            selectingForBefore = selectingForBefore,
            onActivateBefore = { selectingForBefore = true },
            onActivateAfter = { selectingForBefore = false },
            onIntent = viewModel::onIntent,
        )
    }
}

@Composable
private fun ComparePhotosContent(
    state: ComparePhotosState,
    contentPadding: PaddingValues,
    selectingForBefore: Boolean,
    onActivateBefore: () -> Unit,
    onActivateAfter: () -> Unit,
    onIntent: (ComparePhotosIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
    ) {
        Text(
            text = "Elige una foto para ANTES y otra para DESPUES. Toca el bloque superior para cambiar el slot activo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ComparisonSlot(
                    modifier = Modifier.weight(1f),
                    label = "ANTES",
                    photo = state.beforePhoto,
                    isSelected = selectingForBefore,
                    onClick = onActivateBefore,
                )
                ComparisonSlot(
                    modifier = Modifier.weight(1f),
                    label = "DESPUÉS",
                    photo = state.afterPhoto,
                    isSelected = !selectingForBefore,
                    onClick = onActivateAfter,
                )
            }
        }

        PhotoTypeSelector(
            selectedType = state.selectedType,
            onTypeChange = { onIntent(ComparePhotosIntent.OnTypeSelected(it)) },
        )

        Text(
            text = "Slot activo: ${if (selectingForBefore) "ANTES" else "DESPUES"}",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary,
        )

        ComparePhotosSelectionSection(
            state = state,
            selectingForBefore = selectingForBefore,
            onIntent = onIntent,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun ComparePhotosSelectionSection(
    state: ComparePhotosState,
    selectingForBefore: Boolean,
    onIntent: (ComparePhotosIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Box(
                modifier = modifier.padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Cargando fotos...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.photos.isEmpty() -> ComparePhotosEmptyState(
            hasActiveFilter = state.selectedType != null,
            modifier = modifier,
        )
        else -> ComparePhotosGrid(
            state = state,
            selectingForBefore = selectingForBefore,
            onIntent = onIntent,
            modifier = modifier,
        )
    }
}

@Composable
private fun ComparePhotosGrid(
    state: ComparePhotosState,
    selectingForBefore: Boolean,
    onIntent: (ComparePhotosIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(PHOTO_SELECTION_GRID_COLUMNS),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(state.photos) { photo ->
            PhotoSelectionCard(
                photo = photo,
                selectionLabel = when (photo.id) {
                    state.beforePhoto?.id -> "ANTES"
                    state.afterPhoto?.id -> "DESPUES"
                    else -> null
                },
                isSelected = photo == state.beforePhoto || photo == state.afterPhoto,
                onSelect = {
                    if (selectingForBefore) {
                        onIntent(ComparePhotosIntent.OnBeforePhotoSelected(photo))
                    } else {
                        onIntent(ComparePhotosIntent.OnAfterPhotoSelected(photo))
                    }
                },
            )
        }
    }
}

@Composable
fun ComparisonSlot(
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
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        "Sin foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
        Surface(
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
fun PhotoTypeSelector(
    selectedType: PhotoType?,
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
                label = "Todas",
            )
        }
        items(PhotoType.entries) { type ->
            val isSelected = type == selectedType
            HFilterChip(
                selected = isSelected,
                onClick = { onTypeChange(if (isSelected) null else type) },
                label = type.name.lowercase().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                },
            )
        }
    }
}

@Composable
fun PhotoSelectionCard(
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
            .clickable { onSelect() },
    ) {
        AsyncImage(
            model = photo.photoPath,
            contentDescription = photoSelectionDescription(photo),
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
private fun ComparePhotosEmptyState(
    hasActiveFilter: Boolean,
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
                text = if (hasActiveFilter) {
                    "No hay fotos del tipo seleccionado."
                } else {
                    "Aun no tienes fotos para comparar."
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (hasActiveFilter) {
                    "Prueba otro filtro o agrega nuevas fotos."
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

private fun photoSelectionDescription(photo: ProgressPhoto): String {
    return "Foto de ${getLabelForType(photo.type).lowercase(Locale.getDefault())}, ${photo.date.formatDdMmYy()}"
}
