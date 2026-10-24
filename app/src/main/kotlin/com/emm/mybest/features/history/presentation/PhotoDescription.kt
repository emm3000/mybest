package com.emm.mybest.features.history.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.domain.models.PhotoType

@StringRes
internal fun PhotoType.toDescriptionRes(): Int = when (this) {
    PhotoType.TRUNK -> R.string.history_photo_trunk_description
    PhotoType.FACE -> R.string.history_photo_face_description
}
