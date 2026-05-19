package com.emm.mybest.features.timeline.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.features.timeline.presentation.components.MonthHeader
import com.emm.mybest.features.timeline.presentation.components.PhotoGridItem
import com.emm.mybest.features.timeline.presentation.components.SelectionActionBar
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.HTopBar

private const val PHOTO_GRID_COLUMNS = 3
private const val GRID_ITEM_SPACING = 1
private const val SECTION_SPACING = 24

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    onAddPhotoClick: () -> Unit,
    onCompareClick: () -> Unit,
    onSuppressBottomBar: (Boolean) -> Unit,
    onOpenViewer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TimelineEffect.NavigateToCompare -> onCompareClick()
                TimelineEffect.NavigateBack -> Unit
            }
        }
    }

    // Suppress the global bottom nav while selection mode is active; restore on exit or dispose.
    LaunchedEffect(state.selectionMode) {
        onSuppressBottomBar(state.selectionMode)
    }
    DisposableEffect(Unit) {
        onDispose { onSuppressBottomBar(false) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (state.selectionMode) {
                HTopBar(
                    title = "${state.selectedIds.size} seleccionadas",
                    navigationIcon = {
                        HIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Salir del modo selección",
                            onClick = { viewModel.onIntent(TimelineIntent.ExitSelection) },
                        )
                    },
                )
            } else {
                HTopBar(
                    title = "Fotos",
                    actions = {
                        HIconButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Agregar foto",
                            onClick = onAddPhotoClick,
                        )
                    },
                )
            }
        },
        bottomBar = {
            if (state.selectionMode) {
                SelectionActionBar(
                    selectedCount = state.selectedIds.size,
                    onCompare = { viewModel.onIntent(TimelineIntent.CompareSelected) },
                    onDelete = { viewModel.onIntent(TimelineIntent.DeleteSelected) },
                )
            }
        },
    ) { paddingValues ->
        TimelineContent(
            photosByMonth = state.photosByMonth,
            selectionMode = state.selectionMode,
            selectedIds = state.selectedIds,
            isLoading = state.isLoading,
            onPhotoTap = { photo ->
                if (state.selectionMode) {
                    viewModel.onIntent(TimelineIntent.ToggleSelection(photo.id))
                } else {
                    onOpenViewer(photo.id)
                }
            },
            onPhotoLongPress = { photo ->
                if (state.selectionMode) {
                    viewModel.onIntent(TimelineIntent.ToggleSelection(photo.id))
                } else {
                    viewModel.onIntent(TimelineIntent.EnterSelection(photo.id))
                }
            },
            onAddPhotoClick = onAddPhotoClick,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun TimelineContent(
    photosByMonth: Map<YearMonthValue, List<ProgressPhoto>>,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    isLoading: Boolean,
    onPhotoTap: (ProgressPhoto) -> Unit,
    onPhotoLongPress: (ProgressPhoto) -> Unit,
    onAddPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentModifier = modifier.fillMaxSize()

    if (isLoading) {
        HSkeleton(
            modifier = contentModifier.padding(16.dp),
            cornerRadius = 4.dp,
        )
        return
    }

    if (photosByMonth.isEmpty()) {
        HEmptyState(
            title = "Aún no tenés fotos",
            description = "Tus fotos de progreso aparecerán acá.",
            icon = Icons.Rounded.PhotoCamera,
            modifier = contentModifier,
            action = {
                HButton(
                    text = "Agregar primera foto",
                    onClick = onAddPhotoClick,
                    variant = ButtonVariant.Default,
                )
            },
        )
        return
    }

    PhotoGrid(
        photosByMonth = photosByMonth,
        selectionMode = selectionMode,
        selectedIds = selectedIds,
        onPhotoTap = onPhotoTap,
        onPhotoLongPress = onPhotoLongPress,
        modifier = contentModifier,
    )
}

@Composable
private fun PhotoGrid(
    photosByMonth: Map<YearMonthValue, List<ProgressPhoto>>,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    onPhotoTap: (ProgressPhoto) -> Unit,
    onPhotoLongPress: (ProgressPhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        photosByMonth.entries.forEachIndexed { sectionIndex, (yearMonth, photos) ->
            item(key = "header_${yearMonth.year}_${yearMonth.month}") {
                MonthHeader(
                    yearMonth = yearMonth,
                    photoCount = photos.size,
                    modifier = if (sectionIndex > 0) Modifier.padding(top = SECTION_SPACING.dp) else Modifier,
                )
            }

            // Emit rows of PHOTO_GRID_COLUMNS photos each.
            val rows = photos.chunked(PHOTO_GRID_COLUMNS)
            items(
                items = rows,
                key = { row -> "row_${row.first().id}" },
            ) { row ->
                PhotoGridRow(
                    photos = row,
                    selectionMode = selectionMode,
                    selectedIds = selectedIds,
                    onPhotoTap = onPhotoTap,
                    onPhotoLongPress = onPhotoLongPress,
                )
            }
        }
    }
}

@Composable
private fun PhotoGridRow(
    photos: List<ProgressPhoto>,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    onPhotoTap: (ProgressPhoto) -> Unit,
    onPhotoLongPress: (ProgressPhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = GRID_ITEM_SPACING.dp),
        horizontalArrangement = Arrangement.spacedBy(GRID_ITEM_SPACING.dp),
    ) {
        photos.forEach { photo ->
            val isSelected = photo.id in selectedIds
            PhotoGridItem(
                photo = photo,
                selectionMode = selectionMode,
                isSelected = isSelected,
                onTap = { onPhotoTap(photo) },
                onLongPress = { onPhotoLongPress(photo) },
                modifier = Modifier.weight(1f),
            )
        }
        // Fill remaining cells in the row with empty space to keep a flush left-aligned grid.
        val remaining = PHOTO_GRID_COLUMNS - photos.size
        repeat(remaining) {
            Box(modifier = Modifier.weight(1f))
        }
    }
}

internal fun timelinePhotoTypeLabel(type: PhotoType): String = when (type) {
    PhotoType.TRUNK -> "Tronco"
    PhotoType.FACE -> "Cara"
}
