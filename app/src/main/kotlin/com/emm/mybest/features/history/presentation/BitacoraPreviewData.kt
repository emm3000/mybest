package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

private val PREVIEW_DAY = LocalDate(2026, 11, 6)
private val PREVIEW_TODAY = LocalDate(2026, 11, 10)

internal fun previewRelapseRow(): BitacoraRowState = BitacoraRowState(
    label = textRes(R.string.bitacora_kind_relapse),
    parts = listOf(
        textRes(R.string.history_detail_outing_planned),
        textRes(R.string.bitacora_spent, "S/ 150.00"),
        textRes(R.string.history_detail_reason_broke_limit),
        textRes(R.string.bitacora_mood, 2),
    ),
    isRelapse = true,
)

private fun previewReturnRow(): BitacoraRowState = BitacoraRowState(
    label = textRes(R.string.bitacora_kind_return),
    parts = listOf(BitacoraTextPart.Plural(R.plurals.bitacora_return_days, 1)),
)

private fun previewStayedInRow(): BitacoraRowState = BitacoraRowState(
    label = textRes(R.string.bitacora_kind_stayed_in),
    parts = listOf(textRes(R.string.bitacora_stayed_in)),
)

private fun previewNoteRow(): BitacoraRowState = BitacoraRowState(
    label = BitacoraTextPart.Text("02:30"),
    parts = listOf(BitacoraTextPart.Text("Volví a las 2 y dormí bien")),
    noteId = 1,
)

internal fun previewBitacoraDays(): ImmutableList<BitacoraDayState> = persistentListOf(
    BitacoraDayState(
        appDay = PREVIEW_TODAY,
        dateLabel = "10 NOV · MAR",
        isToday = true,
        rows = persistentListOf(previewStayedInRow()),
    ),
    BitacoraDayState(
        appDay = PREVIEW_DAY,
        dateLabel = "06 NOV · VIE",
        isToday = false,
        rows = persistentListOf(previewRelapseRow(), previewReturnRow(), previewNoteRow()),
    ),
)
