package com.emm.mybest.core.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey

@Stable
class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.lastTransitionKind = NavTransitionKind.TabSwitch
            state.topLevelRoute = route
        } else {
            state.lastTransitionKind = NavTransitionKind.Push
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        state.lastTransitionKind = NavTransitionKind.Pop

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }

    val isStartRouteShown: Boolean
        get() = state.topLevelRoute == state.startRoute && state.backStacks[state.startRoute]?.size == 1
}
