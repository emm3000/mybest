package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkMuted
import com.emm.mybest.ui.theme.AtelierSerifFamily
import kotlinx.datetime.LocalDate

private val CELL_DAY_SIZE = 17.sp
private val CHECK_ROW_HEIGHT = 4.dp
private val MARK_ROW_HEIGHT = 5.dp
private val CHECK_GAP = 2.dp
private val MARK_GAP = 3.dp

@Composable
internal fun HistoryDayCell(
    cell: HistoryDayCellState,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (cell.isToday) AtelierDone else Color.Transparent
    val textColor = if (cell.isFuture) AtelierInkMuted else AtelierInk

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(width = 1.dp, color = borderColor)
            .clickable { onClick(cell.date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = cell.day.toString(),
                style = TextStyle(
                    fontFamily = AtelierSerifFamily,
                    fontSize = CELL_DAY_SIZE,
                    color = textColor,
                    lineHeight = CELL_DAY_SIZE,
                ),
            )
            DayMarkRows(marks = cell.marks)
        }
    }
}

@Composable
private fun DayMarkRows(marks: DayMarksState) {
    Row(
        modifier = Modifier.height(CHECK_ROW_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(CHECK_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(marks.checkCount) { HistoryMark(HistoryMarkKind.CHECK) }
    }
    Row(
        modifier = Modifier.height(MARK_ROW_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(MARK_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        marks.marks.forEach { HistoryMark(it) }
    }
}
