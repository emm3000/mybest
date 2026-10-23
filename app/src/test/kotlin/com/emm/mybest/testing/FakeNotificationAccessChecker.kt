package com.emm.mybest.testing

import com.emm.mybest.domain.repository.NotificationAccessChecker

class FakeNotificationAccessChecker(
    var isAccessGranted: Boolean = false,
) : NotificationAccessChecker {

    override fun isGranted(): Boolean = isAccessGranted
}
