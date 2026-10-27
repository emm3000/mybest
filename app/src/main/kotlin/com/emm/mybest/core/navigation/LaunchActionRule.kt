package com.emm.mybest.core.navigation

import com.emm.mybest.domain.usecase.home.HomeDestination

private val ACTION_SCREENS: Map<String, Screen> = mapOf(
    ACTION_ADD_WEIGHT to Screen.AddWeight,
    ACTION_ADD_PHOTO to Screen.Photos,
    ACTION_PRE_OUTING to Screen.PreOuting,
    ACTION_DEBTS to Screen.Debts,
    ACTION_CHECK_IN to Screen.CheckIn,
    ACTION_WEEKLY_SUMMARY to Screen.WeeklySummary,
)

private val EMBEDDED_SCREENS: Map<HomeDestination, Screen> = mapOf(
    HomeDestination.PRE_OUTING to Screen.PreOuting,
    HomeDestination.CHECK_IN to Screen.CheckIn,
    HomeDestination.WEEKLY_SUMMARY to Screen.WeeklySummary,
)

/**
 * Decides which intent action is still pending when an activity is created, so a launch action runs at most once,
 * and which screen it opens.
 */
object LaunchActionRule {

    /**
     * The pending action an earlier instance of the activity saved before it was destroyed.
     */
    data class Saved(val pendingAction: String?)

    /**
     * Returns the action to run: the saved one on recreation, the intent action on first creation unless the task
     * was relaunched from history.
     */
    fun resolve(saved: Saved?, intentAction: String?, isFromHistory: Boolean): String? = when {
        saved != null -> saved.pendingAction
        isFromHistory -> null
        else -> intentAction
    }

    /**
     * Returns the screen a launch action opens, or null for an action that opens nothing. When Home is on screen
     * and already shows that screen embedded, returns [Screen.Home] instead, so the screen is never open twice.
     * An unknown Home destination never holds the action back.
     */
    fun screenFor(action: String, homeDestination: HomeDestination?, isHomeVisible: Boolean): Screen? {
        val screen = ACTION_SCREENS[action] ?: return null
        val isEmbeddedOnScreen = isHomeVisible && EMBEDDED_SCREENS[homeDestination] == screen
        return if (isEmbeddedOnScreen) Screen.Home else screen
    }
}
