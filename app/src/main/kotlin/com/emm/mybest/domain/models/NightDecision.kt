package com.emm.mybest.domain.models

/**
 * What the user decided for one app day: nothing yet, to go out, or to stay in.
 */
sealed interface NightDecision {
    data object Undecided : NightDecision
    data class WentOut(val outing: Outing) : NightDecision
    data class StayedIn(val wonNight: WonNight) : NightDecision
}
