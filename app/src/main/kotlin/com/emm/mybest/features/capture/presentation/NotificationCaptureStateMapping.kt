package com.emm.mybest.features.capture.presentation

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.formatEsWeekdayDayMonth
import com.emm.mybest.core.datetime.formatHhMm
import com.emm.mybest.domain.models.CaptureAppOption
import com.emm.mybest.domain.models.CapturedNotificationEntry

internal fun CaptureAppOption.toItemState(): CaptureAppItemState = CaptureAppItemState(
    packageName = app.packageName,
    label = app.label,
    isSelected = isSelected,
)

internal fun CapturedNotificationEntry.toItemState(appDayClock: AppDayClock): CapturedItemState {
    val postedAt = appDayClock.localDateTimeOf(notification.postedAt)
    return CapturedItemState(
        id = notification.id,
        appLabel = appLabel.uppercase(),
        title = notification.title,
        text = notification.text,
        postedAtDateLabel = postedAt.date.formatEsWeekdayDayMonth().uppercase(),
        postedAtTimeLabel = postedAt.time.formatHhMm(),
    )
}
