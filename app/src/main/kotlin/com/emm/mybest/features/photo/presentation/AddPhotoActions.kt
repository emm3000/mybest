package com.emm.mybest.features.photo.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emm.mybest.domain.media.MediaManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun handleCameraAction(
    context: android.content.Context,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    onPermissionGranted: () -> Unit,
) {
    when {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
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

internal fun copyUriToInternalStorage(context: android.content.Context, uri: Uri): Uri? {
    return copyUriToInternalStorageSafe(context = context, uri = uri)
}

internal fun handleGallerySelection(
    uris: List<Uri>,
    context: android.content.Context,
    scope: CoroutineScope,
    onIntent: (AddPhotoIntent) -> Unit,
) {
    if (uris.isEmpty()) return
    scope.launch(Dispatchers.IO) {
        val copiedUris = uris.mapNotNull { copyUriToInternalStorage(context, it) }
        withContext(Dispatchers.Main) {
            onIntent(AddPhotoIntent.OnPhotosSelected(copiedUris.map { it.toString() }))
        }
    }
}

internal fun handleCameraCaptureResult(
    success: Boolean,
    tempPhotoUri: Uri?,
    onIntent: (AddPhotoIntent) -> Unit,
) {
    if (success && tempPhotoUri != null) {
        onIntent(AddPhotoIntent.OnPhotosSelected(listOf(tempPhotoUri.toString())))
    }
}

internal fun handleCameraPermissionResult(
    isGranted: Boolean,
    context: android.content.Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    mediaManager: MediaManager,
    onLaunchCamera: (Uri) -> Unit,
) {
    if (isGranted) {
        onLaunchCamera(mediaManager.generatePhotoUri())
        return
    }
    val isPermanentlyDenied = (context as? Activity)?.let {
        !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
    } ?: false

    if (isPermanentlyDenied) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Habilita el permiso de camara en Ajustes para tomar fotos.",
                actionLabel = "Ajustes",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                openAppSettings(context)
            }
        }
        return
    }

    scope.launch {
        snackbarHostState.showSnackbar(
            message = "Se necesita acceso a la camara para tomar fotos.",
            duration = SnackbarDuration.Short,
        )
    }
}
