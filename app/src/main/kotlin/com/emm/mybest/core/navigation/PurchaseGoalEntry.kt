package com.emm.mybest.core.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.emm.mybest.features.goal.presentation.PurchaseGoalScreen
import com.emm.mybest.features.goal.presentation.PurchaseGoalViewModel
import org.koin.androidx.compose.koinViewModel

internal fun EntryProviderScope<NavKey>.purchaseGoalEntry(navigator: Navigator) {
    entry<Screen.PurchaseGoal> {
        val viewModel: PurchaseGoalViewModel = koinViewModel()
        PurchaseGoalScreen(viewModel = viewModel, onBack = { navigator.goBack() }, modifier = Modifier)
    }
}
