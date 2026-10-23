package com.emm.mybest.features.home.presentation

import androidx.compose.runtime.Composable
import com.emm.mybest.domain.usecase.home.HomeDestination

/**
 * The composables the host injects into Home: [routedContent] draws the screen a [HomeDestination] routes to, given a
 * callback that returns to the daily checks, and [bottomBar] is the app's bottom navigation.
 */
data class HomeSlots(
    val routedContent: @Composable (HomeDestination, () -> Unit) -> Unit,
    val bottomBar: @Composable () -> Unit,
)
