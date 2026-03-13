package com.emm.mybest.features.photo.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.domain.media.MediaManager
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HFilterChip
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HMediaCard
import com.emm.mybest.ui.components.HMediaOverlayLabel
import com.emm.mybest.ui.components.HSelect
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.HTopBar
import com.emm.mybest.ui.components.IconButtonVariant
import com.emm.mybest.ui.theme.MyBestTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun AddPhotoScreen(
    viewModel: AddPhotoViewModel,
    mediaManager: MediaManager,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val effect = viewModel.effect

    AddPhotoContent(
        state = state,
        onIntent = viewModel::onIntent,
        mediaManager = mediaManager,
        onBackClick = onBackClick,
        effect = effect,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoContent(
    state: AddPhotoState,
    onIntent: (AddPhotoIntent) -> Unit,
    mediaManager: MediaManager,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    effect: Flow<AddPhotoEffect> = emptyFlow(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentOnBackClick by rememberUpdatedState(onBackClick)

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris: List<Uri> ->
        handleGallerySelection(
            uris = uris,
            context = context,
            scope = scope,
            onIntent = onIntent,
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        handleCameraCaptureResult(success = success, tempPhotoUri = tempPhotoUri, onIntent = onIntent)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        handleCameraPermissionResult(
            isGranted = isGranted,
            context = context,
            scope = scope,
            snackbarHostState = snackbarHostState,
            mediaManager = mediaManager,
            onLaunchCamera = { uri ->
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            },
        )
    }

    val launchCamera = {
        handleCameraAction(
            context = context,
            permissionLauncher = permissionLauncher,
            onPermissionGranted = {
                val uri = mediaManager.generatePhotoUri()
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            },
        )
    }

    val launchGallery = {
        galleryLauncher.launch("image/*")
    }

    LaunchedEffect(Unit) {
        effect.collectLatest { effect ->
            when (effect) {
                AddPhotoEffect.NavigateBack -> currentOnBackClick()
                is AddPhotoEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { HSnackbarHost(snackbarHostState) },
        topBar = {
            HTopBar(
                title = "Añadir Fotos",
                navigationIcon = {
                    HIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = onBackClick,
                    )
                },
                actions = {
                    HIconButton(
                        icon = Icons.Rounded.AddAPhoto,
                        contentDescription = "Añadir fotos",
                        onClick = { showBottomSheet = true },
                    )
                },
            )
        },
    ) { padding ->
        AddPhotoBody(
            state = state,
            contentPadding = padding,
            onIntent = onIntent,
            onUseCameraClick = launchCamera,
            onPickFromGalleryClick = launchGallery,
        )
    }

    if (showBottomSheet) {
        PhotoSourceBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            onCameraClick = {
                showBottomSheet = false
                launchCamera()
            },
            onGalleryClick = {
                showBottomSheet = false
                launchGallery()
            },
        )
    }
}

@Composable
private fun AddPhotoBody(
    state: AddPhotoState,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onIntent: (AddPhotoIntent) -> Unit,
    onUseCameraClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HSelect(
            items = state.availableHabits,
            selectedItem = state.availableHabits.firstOrNull { it.id == state.selectedHabitId },
            onItemSelect = { onIntent(AddPhotoIntent.OnHabitSelected(it.id)) },
            label = "Hábito relacionado (opcional)",
            itemLabel = { it.name },
            placeholder = "Sin hábito específico",
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.selectedPhotos.isEmpty()) {
            HCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                variant = com.emm.mybest.ui.components.CardVariant.Filled,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Rounded.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Registra una foto de progreso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Puedes tomar una foto ahora o elegir una existente de tu galería.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    HButton(
                        text = "Usar cámara",
                        onClick = onUseCameraClick,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.PhotoCamera,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HButton(
                        text = "Elegir de galería",
                        onClick = onPickFromGalleryClick,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Outline,
                        leadingIcon = Icons.Rounded.PhotoLibrary,
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(state.selectedPhotos) { index, photo ->
                    PhotoCard(
                        uri = photo.uri,
                        selectedType = photo.type,
                        onTypeClick = { onIntent(AddPhotoIntent.OnTypeSelected(index, it)) },
                        onRemove = { onIntent(AddPhotoIntent.OnRemovePhoto(index)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        HButton(
            text = "Guardar ${state.selectedPhotos.size} ${if (state.selectedPhotos.size == 1) "Foto" else "Fotos"}",
            onClick = { onIntent(AddPhotoIntent.OnSaveClick) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state.selectedPhotos.isNotEmpty() && !state.isLoading,
            isLoading = state.isLoading,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoSourceBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    HBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Añadir fotos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            HSourceOption(
                icon = Icons.Rounded.PhotoCamera,
                label = "Usar cámara",
                onClick = onCameraClick,
                modifier = Modifier.fillMaxWidth(),
            )

            HSourceOption(
                icon = Icons.Rounded.PhotoLibrary,
                label = "Elegir de galería",
                onClick = onGalleryClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun PhotoCard(
    uri: String,
    selectedType: PhotoType,
    onTypeClick: (PhotoType) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HMediaCard(
        modifier = modifier,
        aspectRatio = 1f,
        media = {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = uri,
                    contentDescription = selectedPhotoContentDescription(selectedType),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                HMediaOverlayLabel(
                    text = getLabelForType(selectedType),
                    align = Alignment.BottomStart,
                )
                HIconButton(
                    icon = Icons.Rounded.Delete,
                    contentDescription = "Eliminar foto seleccionada",
                    onClick = onRemove,
                    variant = IconButtonVariant.Destructive,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp),
                )
            }
        },
        footer = {
            Text(
                text = "Tipo de foto",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(PhotoType.entries) { type ->
                    HFilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeClick(type) },
                        label = getLabelForType(type),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        },
    )
}

private fun selectedPhotoContentDescription(type: PhotoType): String {
    return "Foto seleccionada de ${getLabelForType(type).lowercase()}"
}

@Preview(showBackground = true)
@Composable
private fun AddPhotoScreenPreview() {
    MyBestTheme {
        AddPhotoContent(
            state = AddPhotoState(),
            onIntent = {},
            mediaManager = MediaManager(androidx.compose.ui.platform.LocalContext.current),
            onBackClick = {},
        )
    }
}
