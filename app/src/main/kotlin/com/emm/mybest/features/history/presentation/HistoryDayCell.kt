package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkMuted
import com.emm.mybest.ui.theme.AtelierSerifFamily
import kotlinx.datetime.LocalDate

private val CELL_DAY_SIZE = 17.sp
private val INDICATOR_SIZE = 4.dp

@Composable
internal fun HistoryDayCell(
    day: Int,
    date: LocalDate,
    summary: DaySummary?,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isToday) AtelierDone else Color.Transparent
    val textColor = if (isFuture) AtelierInkMuted else AtelierInk

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(width = 1.dp, color = borderColor)
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.toString(),
                style = TextStyle(
                    fontFamily = AtelierSerifFamily,
                    fontSize = CELL_DAY_SIZE,
                    color = textColor,
                    lineHeight = CELL_DAY_SIZE,
                ),
            )
            DayCellIndicatorRow(
                hasWeight = summary?.hasWeight == true,
                hasPhoto = summary?.hasPhoto == true,
            )
        }
    }
}

@Composable
private fun DayCellIndicatorRow(hasWeight: Boolean, hasPhoto: Boolean) {
    Row(
        modifier = Modifier.height(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasWeight) {
            WeightDot()
        }
        if (hasPhoto) {
            PhotoRing()
        }
    }
}

@Composable
private fun WeightDot() {
    Box(
        modifier = Modifier
            .size(INDICATOR_SIZE)
            .drawBehind { drawRect(AtelierInk) },
    )
}

@Composable
private fun PhotoRing() {
    Box(
        modifier = Modifier
            .size(INDICATOR_SIZE)
            .border(width = 1.dp, color = AtelierDone),
    )
}
