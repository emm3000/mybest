package com.emm.mybest.features.history.presentation

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed interface BitacoraTextPart {
    data class Res(@StringRes val resId: Int, val args: List<Any> = emptyList()) : BitacoraTextPart

    data class Plural(@PluralsRes val resId: Int, val count: Int) : BitacoraTextPart

    data class Text(val value: String) : BitacoraTextPart
}
