package com.emm.mybest.features.history.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class BitacoraRowState(
    @StringRes val labelRes: Int,
    val parts: List<BitacoraTextPart>,
    val isRelapse: Boolean = false,
)
