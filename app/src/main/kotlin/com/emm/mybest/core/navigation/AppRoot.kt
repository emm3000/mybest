package com.emm.mybest.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.features.onboarding.presentation.OnboardingGate
import com.emm.mybest.features.onboarding.presentation.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoot(
    intentAction: String?,
    onConsumeAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (state.gate) {
        OnboardingGate.LOADING -> Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        OnboardingGate.REQUIRED -> OnboardingFlow(viewModel = viewModel, modifier = modifier)
        OnboardingGate.DONE -> AppNavigation(
            modifier = modifier,
            intentAction = intentAction,
            onConsumeAction = onConsumeAction,
        )
    }
}
