package com.emm.mybest.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.emm.mybest.features.debts.presentation.DebtsScreen
import com.emm.mybest.features.goal.presentation.PurchaseGoalScreen
import com.emm.mybest.features.onboarding.presentation.OnboardingScreen
import com.emm.mybest.features.onboarding.presentation.OnboardingStepHost
import com.emm.mybest.features.onboarding.presentation.OnboardingViewModel
import com.emm.mybest.features.settings.presentation.SettingsSetupContent
import com.emm.mybest.features.settings.presentation.SettingsSetupHost
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingFlow(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    var destination by rememberSaveable { mutableStateOf(OnboardingDestination.STEPS) }
    BackHandler(enabled = destination != OnboardingDestination.STEPS) { destination = OnboardingDestination.STEPS }
    when (destination) {
        OnboardingDestination.STEPS -> OnboardingScreen(modifier = modifier, viewModel = viewModel) { host ->
            OnboardingSetupBody(host = host, onOpen = { destination = it })
        }
        OnboardingDestination.DEBTS -> DebtsScreen(
            viewModel = koinViewModel(),
            onBack = { destination = OnboardingDestination.STEPS },
            modifier = modifier,
        )
        OnboardingDestination.PURCHASE_GOAL -> PurchaseGoalScreen(
            viewModel = koinViewModel(),
            onBack = { destination = OnboardingDestination.STEPS },
            modifier = modifier,
        )
    }
}

@Composable
private fun OnboardingSetupBody(
    host: OnboardingStepHost,
    onOpen: (OnboardingDestination) -> Unit,
) {
    val section = host.step.toSetupSection() ?: return
    SettingsSetupContent(
        section = section,
        host = SettingsSetupHost(
            snackbarHostState = host.snackbarHostState,
            onDebtsClick = { onOpen(OnboardingDestination.DEBTS) },
            onPurchaseGoalClick = { onOpen(OnboardingDestination.PURCHASE_GOAL) },
        ),
    )
}
