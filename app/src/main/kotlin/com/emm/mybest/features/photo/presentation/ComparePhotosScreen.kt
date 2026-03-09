package com.emm.mybest.features.photo.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HTopBar
import kotlinx.coroutines.flow.collectLatest

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
            text = "Elige una foto para ANTES y otra para DESPUÉS. Toca el bloque superior para cambiar el slot activo.",
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
            text = "Slot activo: ${if (selectingForBefore) "ANTES" else "DESPUÉS"}",
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
                    state.afterPhoto?.id -> "DESPUÉS"
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
