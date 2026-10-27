package com.emm.mybest.features.photo.presentation.viewer

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.domain.media.PhotoCaptureFiles
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.ui.components.rememberPhotoCameraHandler
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
internal fun PhotoViewer(
    initialPhotoId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PhotoViewerViewModel = koinViewModel { parametersOf(initialPhotoId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    PhotoViewerEffectsHandler(effects = viewModel.effect, snackbarHostState = snackbarHostState)
    val onReplace = rememberPhotoReplacement(viewModel, snackbarHostState)
    PhotoViewerContent(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = PhotoViewerActions(onBack = onBack, onReplace = onReplace),
        modifier = modifier,
    )
}

@Composable
private fun rememberPhotoReplacement(
    viewModel: PhotoViewerViewModel,
    snackbarHostState: SnackbarHostState,
): (ProgressPhoto) -> Unit {
    val photoCaptureFiles: PhotoCaptureFiles = koinInject()
    var replacingPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraHandler = rememberPhotoCameraHandler(
        snackbarHostState = snackbarHostState,
        photoCaptureFiles = photoCaptureFiles,
        onPhotoCaptured = { uri, _ ->
            replacingPhotoId?.let { viewModel.onIntent(PhotoViewerIntent.OnReplacementCaptured(it, uri)) }
            replacingPhotoId = null
        },
    )
    return { photo ->
        replacingPhotoId = photo.id
        cameraHandler.launch(photo.type)
    }
}

@Composable
private fun PhotoViewerEffectsHandler(
    effects: Flow<PhotoViewerEffect>,
    snackbarHostState: SnackbarHostState,
) {
    val replacedMessage = stringResource(R.string.photo_viewer_replaced)
    val failedMessage = stringResource(R.string.photo_viewer_replace_failed)
    val missingMessage = stringResource(R.string.photo_viewer_photo_missing)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            val message = when (effect) {
                PhotoViewerEffect.PhotoReplaced -> replacedMessage
                PhotoViewerEffect.ReplaceFailed -> failedMessage
                PhotoViewerEffect.PhotoNotFound -> missingMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}
