package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.domain.models.Outing
import kotlinx.datetime.LocalDate

/**
 * What the next-day check-in screen has to deal with right now.
 */
sealed interface CheckInTask {
    /**
     * An outing waiting for its check-in.
     */
    data class PendingOuting(val outing: Outing) : CheckInTask

    /**
     * No outing was recorded on [eveningAppDay] and the user has not said they stayed in: ask if they went out.
     */
    data class AskUnplanned(val eveningAppDay: LocalDate) : CheckInTask

    data object NoTask : CheckInTask
}
