package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.core.datetime.formatEsMonthYear
import com.emm.mybest.core.datetime.formatEsWeekdayDayMonth
import com.emm.mybest.core.datetime.minusDays
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.ui.theme.MyBestTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private const val CALENDAR_COLUMNS = 7
private const val DAY_CELL_ASPECT_RATIO = 0.8f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    HistoryContent(
        modifier = modifier,
        state = state,
        onBackClick = onBackClick,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    state: HistoryState,
    onBackClick: () -> Unit,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDate = state.selectedDate
    if (selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(HistoryIntent.OnDateDismiss) },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            DayDetailContent(
                date = selectedDate,
                summary = state.monthlyData[selectedDate],
                onClose = { onIntent(HistoryIntent.OnDateDismiss) },
                onDeleteWeight = { onIntent(HistoryIntent.OnDeleteWeight(selectedDate)) },
                onDeleteHabit = { onIntent(HistoryIntent.OnDeleteHabit(selectedDate)) },
                onDeletePhoto = { onIntent(HistoryIntent.OnDeletePhoto(it)) },
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mi Progreso") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MonthSelector(
                currentMonth = state.selectedMonth,
                onMonthChange = { onIntent(HistoryIntent.OnMonthChange(it)) },
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                DayOfWeek.entries.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.shortEs(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            CalendarGrid(
                yearMonth = state.selectedMonth,
                onDateClick = { onIntent(HistoryIntent.OnDateSelected(it)) },
                dayData = state.monthlyData,
            )

            Spacer(modifier = Modifier.weight(1f))
            LegendItem(color = MaterialTheme.colorScheme.primary, text = "Peso registrado")
            LegendItem(color = MaterialTheme.colorScheme.secondary, text = "Hábitos completados")
            LegendItem(color = MaterialTheme.colorScheme.tertiary, text = "Fotos tomadas")
        }
    }
}

@Composable
fun MonthSelector(
    currentMonth: YearMonthValue,
    onMonthChange: (YearMonthValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }

        Text(
            text = currentMonth.formatEsMonthYear(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonthValue,
    dayData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.ordinal + 1
    val startOffset = firstDayOfMonth - 1

    val totalCells = daysInMonth + startOffset

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(CALENDAR_COLUMNS),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(count = totalCells) { index ->
            if (index < startOffset) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                val dayOfMonth = index - startOffset + 1
                val date = yearMonth.atDay(dayOfMonth)
                val summary = dayData[date]

                DayCell(
                    date = date,
                    summary = summary,
                    onClick = { onDateClick(date) },
                )
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    summary: DaySummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == currentDate()

    Column(
        modifier = modifier
            .aspectRatio(DAY_CELL_ASPECT_RATIO)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isToday) {
                    MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.3f,
                    )
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(6.dp),
        ) {
            if (summary?.hasWeight == true) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
            if (summary?.hasHabit == true) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
            }
            if (summary?.hasPhoto == true) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun DayDetailContent(
    date: LocalDate,
    summary: DaySummary?,
    onClose: () -> Unit,
    onDeleteWeight: () -> Unit,
    onDeleteHabit: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == currentDate()
    var photoToDelete by remember { mutableStateOf<ProgressPhoto?>(null) }

    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("¿Eliminar foto?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    photoToDelete?.let { onDeletePhoto(it.id) }
                    photoToDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Cancelar")
                }
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
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
            }
        }

        if (summary?.hasActivity != true) {
            DayEmptyState()
        } else {
            summary.weight?.let { weight ->
                DetailItem(
                    icon = Icons.Rounded.MonitorWeight,
                    color = MaterialTheme.colorScheme.primary,
                    title = "Peso: ${weight.weight} kg",
                    subtitle = weight.note,
                    onDelete = if (isToday) onDeleteWeight else null,
                )
            }

            summary.habit?.let { habit ->
                HabitDetailItem(
                    habit = habit,
                    isToday = isToday,
                    onDeleteHabit = onDeleteHabit,
                )
            }

            if (summary.photos.isNotEmpty()) {
                DayPhotosSection(
                    photos = summary.photos,
                    isToday = isToday,
                    onDeletePhoto = { photoToDelete = it },
                )
            }
        }
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
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
fun HChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
fun LegendItem(
    color: Color,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    val today = currentDate()
    val currentMonth = YearMonthValue.now()

    val sampleMonthlyData = mapOf(
        today to DaySummary(
            date = today,
            weight = WeightEntry(id = "w1", date = today, weight = 75.5f, note = "Post entrenamiento"),
            habit = DailyHabitSummary(date = today, ateHealthy = true, didExercise = true, notes = "Buen día"),
            photos = listOf(
                ProgressPhoto(id = "p1", date = today, type = PhotoType.BODY, photoPath = "", createdAt = 0L),
                ProgressPhoto(id = "p2", date = today, type = PhotoType.ABDOMEN, photoPath = "", createdAt = 0L),
            ),
        ),
        today.minusDays(1) to DaySummary(
            date = today.minusDays(1),
            weight = WeightEntry(id = "w2", date = today.minusDays(1), weight = 76.0f),
            habit = DailyHabitSummary(date = today.minusDays(1), ateHealthy = false, didExercise = true, notes = null),
        ),
    )

    val state = HistoryState(
        selectedMonth = currentMonth,
        monthlyData = sampleMonthlyData,
    )

    MyBestTheme {
        HistoryContent(
            state = state,
            onBackClick = {},
            onIntent = {},
        )
    }
}
