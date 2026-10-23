package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalTime

/**
 * What the user entered for a check-in; a null value means the input was empty or not valid.
 */
data class CheckInDraft(
    val totalSpent: Money?,
    val drinks: Int?,
    val returnTime: LocalTime?,
    val mood: Int?,
)
