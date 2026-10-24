package com.emm.mybest.core.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.emm.mybest.features.capture.presentation.NotificationCaptureScreen
import com.emm.mybest.features.capture.presentation.NotificationCaptureViewModel
import org.koin.androidx.compose.koinViewModel

internal fun EntryProviderScope<NavKey>.notificationCaptureEntry(navigator: Navigator) {
    entry<Screen.NotificationCapture> {
        val viewModel: NotificationCaptureViewModel = koinViewModel()
        NotificationCaptureScreen(viewModel = viewModel, onBack = { navigator.goBack() }, modifier = Modifier)
    }
}
