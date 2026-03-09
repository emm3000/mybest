package com.emm.mybest.features.photo.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.emm.mybest.ui.components.HButton
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Añadir Fotos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Rounded.AddAPhoto, contentDescription = "Añadir")
                    }
                },
            )
        },
    ) { padding ->
        AddPhotoBody(
            state = state,
            contentPadding = padding,
            onIntent = onIntent,
            onAddPhotoClick = { showBottomSheet = true },
        )
    }

    if (showBottomSheet) {
        PhotoSourceBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            onCameraClick = {
                showBottomSheet = false
                handleCameraAction(
                    context = context,
                    permissionLauncher = permissionLauncher,
                    onPermissionGranted = {
                        val uri = mediaManager.generatePhotoUri()
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    },
                )
            },
            onGalleryClick = {
                showBottomSheet = false
                galleryLauncher.launch("image/*")
            },
        )
    }
}

@Composable
private fun AddPhotoBody(
    state: AddPhotoState,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onIntent: (AddPhotoIntent) -> Unit,
    onAddPhotoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.selectedPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(onClick = onAddPhotoClick),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Toca para añadir fotos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    ModalBottomSheet(
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
                "Seleccionar origen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            HSourceOption(
                icon = Icons.Rounded.PhotoCamera,
                label = "Usar Cámara",
                onClick = onCameraClick,
                modifier = Modifier.fillMaxWidth(),
            )

            HSourceOption(
                icon = Icons.Rounded.PhotoLibrary,
                label = "Elegir de Galería",
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
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f), CircleShape)
                        .size(32.dp),
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Borrar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            var showTypeSelector by remember { mutableStateOf(false) }

            Text(
                text = getLabelForType(selectedType),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTypeSelector = !showTypeSelector }
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            if (showTypeSelector) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(PhotoType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                onTypeClick(type)
                                showTypeSelector = false
                            },
                            label = { Text(getLabelForType(type), style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }
        }
    }
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
