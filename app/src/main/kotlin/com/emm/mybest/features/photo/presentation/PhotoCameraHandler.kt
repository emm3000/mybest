package com.emm.mybest.features.photo.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emm.mybest.R
import com.emm.mybest.domain.media.MediaManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
internal data class PhotoCameraHandler(
    val launch: () -> Unit,
)

@Composable
internal fun rememberPhotoCameraHandler(
    snackbarHostState: SnackbarHostState,
    mediaManager: MediaManager,
    onPhotoCaptured: (String) -> Unit,
): PhotoCameraHandler {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tempPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = tempPhotoUri ?: return@rememberLauncherForActivityResult
        if (success) persistAndNotify(context, scope, uri, onPhotoCaptured)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            val uri = mediaManager.generatePhotoUri()
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            handlePermissionDenied(context, scope, snackbarHostState)
        }
    }

    return remember(permissionLauncher, cameraLauncher) {
        val launchAction: () -> Unit = {
            checkAndLaunch(context, permissionLauncher) {
                val uri = mediaManager.generatePhotoUri()
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
        PhotoCameraHandler(launch = launchAction)
    }
}

private fun checkAndLaunch(
    context: Context,
    permissionLauncher: ActivityResultLauncher<String>,
    onPermissionGranted: () -> Unit,
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
    if (hasPermission) {
        onPermissionGranted()
    } else {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}

private fun persistAndNotify(
    context: Context,
    scope: CoroutineScope,
    uri: Uri,
    onPhotoCaptured: (String) -> Unit,
) {
    scope.launch(Dispatchers.IO) {
        val persisted = copyUriToInternalStorageSafe(context, uri)
        withContext(Dispatchers.Main) {
            persisted?.let { onPhotoCaptured(it.toString()) }
        }
    }
}

private fun handlePermissionDenied(
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
) {
    val isPermanentlyDenied = (context as? Activity)?.let {
        !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
    } ?: false
    if (isPermanentlyDenied) {
        showPermanentlyDeniedSnackbar(context, scope, snackbarHostState)
    } else {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.photos_camera_permission_required),
                duration = SnackbarDuration.Short,
            )
        }
    }
}

private fun showPermanentlyDeniedSnackbar(
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
) {
    scope.launch {
        val result = snackbarHostState.showSnackbar(
            message = context.getString(R.string.photos_camera_permission_denied),
            actionLabel = context.getString(R.string.photos_camera_permission_settings),
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) openAppSettings(context)
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
