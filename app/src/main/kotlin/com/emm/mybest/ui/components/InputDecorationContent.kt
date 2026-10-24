package com.emm.mybest.ui.components

import androidx.compose.runtime.Composable

internal data class InputDecorationContent(
    val value: String,
    val placeholder: String?,
    val leadingIcon: (@Composable () -> Unit)?,
    val trailingIcon: (@Composable () -> Unit)?,
)
