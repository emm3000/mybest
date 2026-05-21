package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

sealed interface PeriodLabel {
    object NoData : PeriodLabel
    data class SingleDay(val date: LocalDate) : PeriodLabel
    data class Range(val start: LocalDate, val end: LocalDate) : PeriodLabel
}
