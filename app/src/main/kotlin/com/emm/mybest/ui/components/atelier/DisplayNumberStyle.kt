package com.emm.mybest.ui.components.atelier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import com.emm.mybest.ui.theme.AtelierInk

/** Visual variant a [DisplayNumber] is rendered with. */
data class DisplayNumberStyle(
    val fontSize: TextUnit,
    val italic: Boolean = false,
    val color: Color = AtelierInk,
)
