package com.emm.mybest.ui.components.atelier

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Visual variant a [MicroLabel] is rendered with. */
data class MicroLabelStyle(
    val tone: MicroLabelTone = MicroLabelTone.Default,
    val size: TextUnit = 10.sp,
)
