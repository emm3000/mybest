package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class BitacoraRowState(
    val label: BitacoraTextPart,
    val parts: List<BitacoraTextPart>,
    val isRelapse: Boolean = false,
    val noteId: Long? = null,
)
