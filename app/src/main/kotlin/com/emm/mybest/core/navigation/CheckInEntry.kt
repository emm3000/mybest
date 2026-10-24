package com.emm.mybest.core.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.emm.mybest.features.checkin.presentation.CheckInScreen
import com.emm.mybest.features.checkin.presentation.CheckInViewModel
import org.koin.androidx.compose.koinViewModel

internal fun EntryProviderScope<NavKey>.checkInEntry(navigator: Navigator) {
    entry<Screen.CheckIn> {
        val viewModel: CheckInViewModel = koinViewModel()
        CheckInScreen(viewModel = viewModel, onClose = { navigator.goBack() }, modifier = Modifier)
    }
}
