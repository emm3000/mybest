package com.emm.mybest.features.history.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

data class DayDetailState(
    val date: LocalDate,
    val outing: OutingDetailState? = null,
    val wonNight: WonNightDetailState? = null,
    val checks: ImmutableList<CheckDetailState> = persistentListOf(),
    val weight: WeightRowState? = null,
    val photos: ImmutableList<PhotoRowState> = persistentListOf(),
    val isEmpty: Boolean = true,
)
