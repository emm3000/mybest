package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class LogbookRowState(
    val label: LogbookTextPart,
    val parts: List<LogbookTextPart>,
    val isRelapse: Boolean = false,
    val noteId: Long? = null,
)
