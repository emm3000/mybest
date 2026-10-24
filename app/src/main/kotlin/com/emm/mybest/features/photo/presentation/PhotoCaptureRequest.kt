package com.emm.mybest.features.photo.presentation

import android.net.Uri
import androidx.compose.runtime.Stable

@Stable
internal data class PhotoCaptureRequest(
    val uri: Uri,
    val onPhotoCaptured: (String) -> Unit,
)
