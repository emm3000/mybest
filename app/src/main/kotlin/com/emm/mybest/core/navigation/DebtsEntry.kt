package com.emm.mybest.core.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.emm.mybest.features.debts.presentation.DebtsScreen
import com.emm.mybest.features.debts.presentation.DebtsViewModel
import org.koin.androidx.compose.koinViewModel

internal fun EntryProviderScope<NavKey>.debtsEntry(navigator: Navigator) {
    entry<Screen.Debts> {
        val viewModel: DebtsViewModel = koinViewModel()
        DebtsScreen(viewModel = viewModel, onBack = { navigator.goBack() }, modifier = Modifier)
    }
}
