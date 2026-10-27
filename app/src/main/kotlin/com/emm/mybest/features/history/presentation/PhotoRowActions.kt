package com.emm.mybest.features.history.presentation

internal data class PhotoRowActions(
    val onReplace: () -> Unit,
    val onDelete: () -> Unit,
)
