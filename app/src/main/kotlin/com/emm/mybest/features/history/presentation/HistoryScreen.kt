package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.core.datetime.formatEsWeekdayDayMonth
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.HistoryRange
import com.emm.mybest.domain.usecase.history.WeightTrendPoint
import com.emm.mybest.features.history.presentation.components.HistoryRangeSelector
import com.emm.mybest.features.history.presentation.components.WeightTrendChart
import com.emm.mybest.ui.components.AlertVariant
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HAlert
import com.emm.mybest.ui.components.HAlertDialog
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.HTopBar
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val HISTORY_SECTION_CORNER = 16
private const val HISTORY_LOADING_CARD_HEIGHT = 56
private const val HISTORY_LOADING_GRID_HEIGHT = 320
private const val DAYS_IN_WEEK = 7

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onSeePhotosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    HistoryContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
        onSeePhotosClick = onSeePhotosClick,
    )
}

@Composable
fun HistoryContent(
    state: HistoryState,
    onIntent: (HistoryIntent) -> Unit,
    onSeePhotosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDate = state.selectedDate
    if (selectedDate != null) {
        HBottomSheet(
            onDismissRequest = { onIntent(HistoryIntent.OnDateDismiss) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            DayDetailContent(
                date = selectedDate,
                summary = state.monthlyData[selectedDate],
                onClose = { onIntent(HistoryIntent.OnDateDismiss) },
                onDeleteWeight = { onIntent(HistoryIntent.OnDeleteWeight(selectedDate)) },
                onDeletePhoto = { onIntent(HistoryIntent.OnDeletePhoto(it)) },
                onSeePhotosClick = onSeePhotosClick,
            )
        }
    }

    Scaffold(
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        topBar = {
            HTopBar(title = "Historial")
        },
    ) { padding ->
        val contentModifier = Modifier
            .padding(padding)
            .fillMaxSize()

        when {
            state.isLoading -> HistoryLoadingState(modifier = contentModifier.padding(16.dp))
            state.errorMessage != null -> {
                HAlert(
                    title = "No se pudo cargar el historial",
                    description = state.errorMessage,
                    variant = AlertVariant.Destructive,
                    modifier = contentModifier.padding(16.dp),
                )
            }
            else -> {
                HistoryScrollContent(
                    state = state,
                    onIntent = onIntent,
                    modifier = contentModifier,
                )
            }
        }
    }
}

@Composable
private fun HistoryScrollContent(
    state: HistoryState,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HistoryRangeSelector(
                selectedRange = state.selectedRange,
                onRangeChange = { onIntent(HistoryIntent.OnRangeChange(it)) },
            )
        }

        item {
            RangeNavigationRow(
                selectedMonth = state.selectedMonth,
                selectedRange = state.selectedRange,
                onMonthChange = { onIntent(HistoryIntent.OnMonthChange(it)) },
            )
        }

        item {
            RangeSummaryLine(
                range = state.selectedRange,
                activeDays = state.activeDays,
                streak = state.streak,
                monthlyData = state.monthlyData,
                selectedMonth = state.selectedMonth,
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            HeatmapCard(
                selectedRange = state.selectedRange,
                selectedMonth = state.selectedMonth,
                monthlyData = state.monthlyData,
                onDateClick = { onIntent(HistoryIntent.OnDateSelected(it)) },
            )
        }

        item {
            WeightTrendSection(weightTrend = state.weightTrend)
        }
    }
}

@Composable
private fun RangeNavigationRow(
    selectedMonth: YearMonthValue,
    selectedRange: HistoryRange,
    onMonthChange: (YearMonthValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HIconButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = "Período anterior",
            onClick = { onMonthChange(selectedMonth.minusMonths(1)) },
        )

        Text(
            text = rangeLabel(selectedMonth, selectedRange),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        HIconButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Período siguiente",
            onClick = { onMonthChange(selectedMonth.plusMonths(1)) },
        )
    }
}

@Composable
private fun RangeSummaryLine(
    range: HistoryRange,
    activeDays: Int,
    streak: Int,
    monthlyData: Map<LocalDate, DaySummary>,
    selectedMonth: YearMonthValue,
    modifier: Modifier = Modifier,
) {
    val text = when (range) {
        HistoryRange.WEEK -> {
            val weightDays = monthlyData.values.count { it.hasWeight }
            val photoDays = monthlyData.values.count { it.hasPhoto }
            "$activeDays/7 días · $weightDays con peso · $photoDays con foto"
        }
        HistoryRange.MONTH -> "$activeDays días activos · racha $streak"
        HistoryRange.YEAR -> {
            val bestMonth = findBestMonthName(monthlyData, selectedMonth.year)
            "$activeDays/365 días activos · $bestMonth fue tu mejor mes"
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun HeatmapCard(
    selectedRange: HistoryRange,
    selectedMonth: YearMonthValue,
    monthlyData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Outlined,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (selectedRange) {
                HistoryRange.WEEK -> {
                    val anchorDay = selectedMonth.atDay(1)
                    val weekStart = anchorDay.plus(DatePeriod(days = -anchorDay.dayOfWeek.ordinal))
                    val weekDates = (0 until DAYS_IN_WEEK).map { weekStart.plus(DatePeriod(days = it)) }
                    WeekdayHeaderRow()
                    Spacer(modifier = Modifier.height(8.dp))
                    WeekHeatmapRow(
                        dates = weekDates,
                        dayData = monthlyData,
                        onDateClick = onDateClick,
                    )
                }
                HistoryRange.MONTH -> {
                    val hasActivity = hasActivityInSelectedMonth(
                        selectedMonth = selectedMonth,
                        monthlyData = monthlyData,
                    )
                    if (hasActivity) {
                        WeekdayHeaderRow()
                        Spacer(modifier = Modifier.height(8.dp))
                        MonthCalendarGrid(
                            yearMonth = selectedMonth,
                            dayData = monthlyData,
                            onDateClick = onDateClick,
                        )
                    } else {
                        HEmptyState(
                            title = "Sin actividad",
                            description = "Registra peso o fotos para ver actividad diaria.",
                            icon = Icons.Rounded.History,
                        )
                    }
                }
                HistoryRange.YEAR -> {
                    YearHeatmapRow(
                        yearMonth = selectedMonth,
                        dayData = monthlyData,
                        onDateClick = onDateClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightTrendSection(
    weightTrend: List<WeightTrendPoint>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "EVOLUCIÓN DEL PESO",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
        )
        Spacer(modifier = Modifier.height(8.dp))
        HCard(
            modifier = Modifier.fillMaxWidth(),
            variant = CardVariant.Outlined,
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                WeightTrendChart(points = weightTrend)
            }
        }
    }
}

@Composable
private fun HistoryLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(HISTORY_LOADING_CARD_HEIGHT.dp),
            cornerRadius = HISTORY_SECTION_CORNER.dp,
        )
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(HISTORY_LOADING_GRID_HEIGHT.dp),
            cornerRadius = HISTORY_SECTION_CORNER.dp,
        )
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(HISTORY_LOADING_CARD_HEIGHT.dp),
            cornerRadius = HISTORY_SECTION_CORNER.dp,
        )
    }
}

@Composable
fun DayDetailContent(
    date: LocalDate,
    summary: DaySummary?,
    onClose: () -> Unit,
    onDeleteWeight: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    onSeePhotosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { currentDate() }
    val isToday = date == today
    var photoToDelete by remember { mutableStateOf<ProgressPhoto?>(null) }

    if (photoToDelete != null) {
        HAlertDialog(
            title = "¿Eliminar foto?",
            description = "Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            isDangerous = true,
            onDismiss = { photoToDelete = null },
            onConfirm = {
                photoToDelete?.let { onDeletePhoto(it.id) }
                photoToDelete = null
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = date.formatEsWeekdayDayMonth(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            HIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = "Cerrar",
                onClick = onClose,
            )
        }

        if (summary?.hasActivity != true) {
            DayEmptyState()
        } else {
            DayTimelineSection(
                summary = summary,
                isToday = isToday,
                onDeleteWeight = onDeleteWeight,
                onDeletePhoto = { photoToDelete = it },
            )
        }

        HButton(
            text = "Ver fotos del día",
            onClick = onSeePhotosClick,
            variant = ButtonVariant.Outline,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onDelete: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content?.invoke()
        }
        if (onDelete != null) {
            Spacer(modifier = Modifier.weight(1f))
            HIconButton(
                icon = Icons.Rounded.Delete,
                contentDescription = "Eliminar",
                onClick = onDelete,
                variant = com.emm.mybest.ui.components.IconButtonVariant.Destructive,
            )
        }
    }
}
