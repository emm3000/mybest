package com.emm.mybest.ui.components.atelier

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import com.emm.mybest.domain.models.PhotoType

@Composable
fun photoTypeLabel(type: PhotoType): String = when (type) {
    PhotoType.TRUNK -> stringResource(R.string.photo_type_trunk)
    PhotoType.FACE -> stringResource(R.string.photo_type_face)
}
