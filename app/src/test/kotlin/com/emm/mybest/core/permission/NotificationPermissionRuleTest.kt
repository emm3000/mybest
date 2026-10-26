package com.emm.mybest.core.permission

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPermissionRuleTest {

    @Test
    fun `does not request when the platform has no runtime permission`() {
        val shouldRequest = NotificationPermissionRule.shouldRequest(
            isSupported = false,
            isGranted = false,
            wasRequested = false,
        )

        assertFalse(shouldRequest)
    }

    @Test
    fun `does not request when already granted`() {
        val shouldRequest = NotificationPermissionRule.shouldRequest(
            isSupported = true,
            isGranted = true,
            wasRequested = false,
        )

        assertFalse(shouldRequest)
    }

    @Test
    fun `does not request again when the task already requested`() {
        val shouldRequest = NotificationPermissionRule.shouldRequest(
            isSupported = true,
            isGranted = false,
            wasRequested = true,
        )

        assertFalse(shouldRequest)
    }

    @Test
    fun `requests when supported, not granted and not yet requested`() {
        val shouldRequest = NotificationPermissionRule.shouldRequest(
            isSupported = true,
            isGranted = false,
            wasRequested = false,
        )

        assertTrue(shouldRequest)
    }
}
