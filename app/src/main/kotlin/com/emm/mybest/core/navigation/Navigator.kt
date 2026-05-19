package com.emm.mybest.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // Top-level tab switch — no horizontal motion.
            state.lastTransitionKind = NavTransitionKind.TabSwitch
            state.topLevelRoute = route
        } else {
            state.lastTransitionKind = NavTransitionKind.Push
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun setSuppressBottomBar(suppress: Boolean) {
        state.suppressBottomBar = suppress
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        state.lastTransitionKind = NavTransitionKind.Pop

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
