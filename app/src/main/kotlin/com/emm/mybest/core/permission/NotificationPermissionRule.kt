package com.emm.mybest.core.permission

/**
 * Decides whether the notification permission dialog is shown, so a task asks at most once.
 */
object NotificationPermissionRule {

    /**
     * Returns true only when the platform has the runtime permission, it is not granted and this task has not asked yet.
     */
    fun shouldRequest(isSupported: Boolean, isGranted: Boolean, wasRequested: Boolean): Boolean =
        isSupported && !isGranted && !wasRequested
}
