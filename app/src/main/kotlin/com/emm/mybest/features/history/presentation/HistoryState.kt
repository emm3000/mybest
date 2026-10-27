package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.core.datetime.YearMonthValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate

@Stable
data class HistoryState(
    val today: LocalDate,
    val currentMonth: YearMonthValue,
    val selectedMonth: YearMonthValue,
    val monthNavigationLabel: String = "",
    val monthWeightCount: Int = 0,
    val monthPhotoCount: Int = 0,
    val monthOutingCount: Int = 0,
    val monthWonNightCount: Int = 0,
    val dayMarks: ImmutableMap<LocalDate, DayMarksState> = persistentMapOf(),
    val dayDetail: DayDetailState? = null,
    val selectedView: HistoryView = HistoryView.CALENDAR,
    val bitacoraDays: ImmutableList<BitacoraDayState> = persistentListOf(),
    val selectedDate: LocalDate? = null,
    val noteEditor: NoteEditorState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val canWriteNote: Boolean get() = selectedMonth == currentMonth
}
