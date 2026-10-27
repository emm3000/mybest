package com.emm.mybest.features.history.presentation

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed interface LogbookTextPart {
    data class Res(@StringRes val resId: Int, val args: List<Any> = emptyList()) : LogbookTextPart

    data class Plural(@PluralsRes val resId: Int, val count: Int) : LogbookTextPart

    data class Text(val value: String) : LogbookTextPart
}
