package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.PhotoType

internal fun PhotoType.toSpanishLabel(): String = when (this) {
    PhotoType.TRUNK -> "Tronco"
    PhotoType.FACE -> "Cara"
}
