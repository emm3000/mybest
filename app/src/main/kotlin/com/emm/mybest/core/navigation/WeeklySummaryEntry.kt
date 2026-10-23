package com.emm.mybest.core.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.emm.mybest.features.summary.presentation.SummaryScreen
import com.emm.mybest.features.summary.presentation.SummaryViewModel
import org.koin.androidx.compose.koinViewModel

internal fun EntryProviderScope<NavKey>.weeklySummaryEntry(navigator: Navigator) {
    entry<Screen.WeeklySummary> {
        val viewModel: SummaryViewModel = koinViewModel()
        SummaryScreen(
            viewModel = viewModel,
            onOpenSettings = { navigator.navigate(Screen.Settings) },
            onBack = { navigator.goBack() },
            modifier = Modifier,
        )
    }
}
