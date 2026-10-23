package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.lazy.LazyListScope

internal fun LazyListScope.checksItem(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    item(contentType = HomeContentType.Checks) {
        HomeChecksSection(
            model = HomeChecksModel(state.checks, state.keptChecks, state.totalChecks),
            actions = HomeCheckActions(
                onToggle = { onIntent(HomeIntent.ToggleDailyCheck(it)) },
                onAnswerAlcohol = { onIntent(HomeIntent.AnswerAlcohol(it)) },
            ),
        )
    }
}

internal fun LazyListScope.weekItem(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    item(contentType = HomeContentType.Week) {
        HomeWeekSection(
            checks = state.checks,
            onDefineTarget = { onIntent(HomeIntent.DefineWeeklyTarget) },
        )
    }
}

internal fun LazyListScope.installmentItem(state: HomeState, callbacks: HomeCallbacks) {
    val installment = state.nextInstallment ?: return
    item(contentType = HomeContentType.Installment) {
        HomeInstallmentRow(installment = installment, onClick = callbacks.onInstallmentClick)
    }
}

internal fun LazyListScope.shortcutsItem(state: HomeState, callbacks: HomeCallbacks) {
    item(contentType = HomeContentType.Shortcuts) {
        HomeShortcutsSection(shortcuts = state.shortcuts, onShortcutClick = callbacks.onShortcutClick)
    }
}
