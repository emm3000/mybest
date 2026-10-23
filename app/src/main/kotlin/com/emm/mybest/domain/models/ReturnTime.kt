package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

/**
 * Days from the relapse on [relapseAppDay] to its return on [returnAppDay]. Both [returnAppDay] and [days] are null
 * while the return has not happened yet.
 */
data class ReturnTime(
    val relapseAppDay: LocalDate,
    val returnAppDay: LocalDate?,
    val days: Int?,
)
