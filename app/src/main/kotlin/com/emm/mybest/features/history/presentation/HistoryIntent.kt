package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.usecase.history.HistoryRange
import kotlinx.datetime.LocalDate

sealed class HistoryIntent {
    data class OnMonthChange(val newMonth: YearMonthValue) : HistoryIntent()
    data class OnRangeChange(val range: HistoryRange) : HistoryIntent()
    data class OnDateSelected(val date: LocalDate) : HistoryIntent()
    object OnDateDismiss : HistoryIntent()
    data class OnDeleteWeight(val date: LocalDate) : HistoryIntent()
    data class OnDeletePhoto(val photoId: String) : HistoryIntent()
}
