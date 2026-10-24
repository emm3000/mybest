package com.emm.mybest.core.navigation

/**
 * Decides which intent action is still pending when an activity is created, so a launch action runs at most once.
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
}
