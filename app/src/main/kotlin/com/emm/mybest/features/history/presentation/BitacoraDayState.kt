package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
data class BitacoraDayState(
    val appDay: LocalDate,
    val dateLabel: String,
    val isToday: Boolean,
    val rows: ImmutableList<BitacoraRowState>,
)
