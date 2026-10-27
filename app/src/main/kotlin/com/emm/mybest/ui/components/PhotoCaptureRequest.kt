package com.emm.mybest.ui.components

import android.net.Uri
import androidx.compose.runtime.Stable
import com.emm.mybest.domain.models.PhotoType

@Stable
internal data class PhotoCaptureRequest(
    val uri: Uri,
    val type: PhotoType,
    val onPhotoCaptured: (String, PhotoType) -> Unit,
)
