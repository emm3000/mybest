package com.emm.mybest.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            HSnackbar(data = data)
        },
    )
}

@Composable
fun HSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        snackbarData = data,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actionColor = MaterialTheme.colorScheme.primary,
        dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
