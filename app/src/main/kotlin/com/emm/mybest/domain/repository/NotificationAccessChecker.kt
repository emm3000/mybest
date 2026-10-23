package com.emm.mybest.domain.repository

/**
 * Tells whether the user granted MyBest notification access in the system settings.
 */
interface NotificationAccessChecker {
    fun isGranted(): Boolean
}
