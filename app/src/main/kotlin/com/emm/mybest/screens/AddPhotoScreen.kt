package com.emm.mybest.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import coil3.compose.AsyncImage
import com.emm.mybest.data.entities.PhotoType
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.viewmodel.AddPhotoEffect
import com.emm.mybest.viewmodel.AddPhotoIntent
import com.emm.mybest.viewmodel.AddPhotoState
import com.emm.mybest.viewmodel.AddPhotoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AddPhotoScreen(
    viewModel: AddPhotoViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    AddPhotoContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        effect = viewModel.effect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoContent(
    state: AddPhotoState,
    onIntent: (AddPhotoIntent) -> Unit,
    onBackClick: () -> Unit,
    effect: Flow<AddPhotoEffect> = emptyFlow()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val copiedUris = uris.mapNotNull { copyUriToInternalStorage(context, it) }
                withContext(Dispatchers.Main) {
                    onIntent(AddPhotoIntent.OnPhotosSelected(copiedUris.map { it.toString() }))
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            onIntent(AddPhotoIntent.OnPhotosSelected(listOf(tempPhotoUri.toString())))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createPhotoUri(context)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            val isPermanentlyDenied = (context as? Activity)?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: false
            
            if (isPermanentlyDenied) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "El acceso a la cámara está desactivado",
                        actionLabel = "AJUSTES",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        openAppSettings(context)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        effect.collectLatest { effect ->
            when (effect) {
                AddPhotoEffect.NavigateBack -> onBackClick()
                is AddPhotoEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.selectedPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { showBottomSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Toca para añadir fotos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.selectedPhotos) { index, photo ->
                        PhotoCard(
                            uri = photo.uri,
                            selectedType = photo.type,
                            onTypeClick = { onIntent(AddPhotoIntent.OnTypeSelected(index, it)) },
                            onRemove = { onIntent(AddPhotoIntent.OnRemovePhoto(index)) }
                        )
                    }
                }
            }

            Button(
                onClick = { onIntent(AddPhotoIntent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = state.selectedPhotos.isNotEmpty() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Guardar ${state.selectedPhotos.size} ${if (state.selectedPhotos.size == 1) "Foto" else "Fotos"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Seleccionar origen",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    SourceOption(
                        icon = Icons.Rounded.PhotoCamera,
                        label = "Usar Cámara",
                        onClick = {
                            showBottomSheet = false
                            handleCameraAction(
                                context = context,
                                permissionLauncher = permissionLauncher,
                                onPermissionGranted = {
                                    val uri = createPhotoUri(context)
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            )
                        }
                    )
                    
                    SourceOption(
                        icon = Icons.Rounded.PhotoLibrary,
                        label = "Elegir de Galería",
                        onClick = {
                            showBottomSheet = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoCard(
    uri: String,
    selectedType: PhotoType,
    onTypeClick: (PhotoType) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Borrar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            if (showTypeSelector) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(PhotoType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { 
                                onTypeClick(type)
                                showTypeSelector = false
                            },
                            label = { Text(getLabelForType(type), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

private fun getLabelForType(type: PhotoType): String = when (type) {
    PhotoType.FACE -> "Cara"
    PhotoType.ABDOMEN -> "Abdomen"
    PhotoType.BODY -> "Cuerpo"
    PhotoType.BREAKFAST -> "Desayuno"
    PhotoType.LUNCH -> "Almuerzo"
    PhotoType.DINNER -> "Cena"
    PhotoType.FOOD -> "Comida"
}

private fun handleCameraAction(
    context: android.content.Context,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    onPermissionGranted: () -> Unit
) {
    when {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            onPermissionGranted()
        }

        (context as? Activity)?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } == true -> {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        else -> {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun createPhotoUri(context: android.content.Context): Uri {
    val directory = File(context.filesDir, "photos")
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "IMG_${System.currentTimeMillis()}.jpg")
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

private fun copyUriToInternalStorage(context: android.content.Context, uri: Uri): Uri? {
    return try {
        val directory = File(context.filesDir, "photos")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "IMG_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeStream(input, null, options) ?: return null
            
            // Handle Orientation
            val orientation = context.contentResolver.openInputStream(uri)?.use { orientationInput ->
                val exif = ExifInterface(orientationInput)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            } ?: ExifInterface.ORIENTATION_UNDEFINED

            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            file.outputStream().use { output ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            }
            
            if (rotatedBitmap != bitmap) {
                rotatedBitmap.recycle()
            }
            bitmap.recycle()
        }
        val authority = "${context.packageName}.fileprovider"
        FileProvider.getUriForFile(context, authority, file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@Composable
fun SourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
            onBackClick = {}
        )
    }
}
