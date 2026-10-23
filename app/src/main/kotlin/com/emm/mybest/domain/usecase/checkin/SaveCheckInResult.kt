package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.domain.models.Outing

/**
 * What happened when the user tried to save a check-in.
 */
sealed interface SaveCheckInResult {
    /**
     * The check-in was stored; [outing] is the outing with it attached.
     */
    data class Saved(val outing: Outing) : SaveCheckInResult
    data object InvalidMood : SaveCheckInResult
    data object MissingSpend : SaveCheckInResult
    data object NegativeSpend : SaveCheckInResult
    data object MissingDrinks : SaveCheckInResult
    data object MissingReturnTime : SaveCheckInResult
}
