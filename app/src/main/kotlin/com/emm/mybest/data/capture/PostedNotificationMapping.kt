package com.emm.mybest.data.capture

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.emm.mybest.domain.models.PostedNotification
import kotlin.time.Instant

internal fun StatusBarNotification.toPostedNotification(): PostedNotification {
    val extras = notification.extras
    return PostedNotification(
        packageName = packageName,
        key = key,
        title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
        text = bodyOf(
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT),
            text = extras.getCharSequence(Notification.EXTRA_TEXT),
        ),
        postedAt = Instant.fromEpochMilliseconds(postTime),
        isGroupSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
    )
}
