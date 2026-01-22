package com.emm.mybest.screens

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.PhotoType
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.viewmodel.DaySummary
import com.emm.mybest.viewmodel.HistoryState
import com.emm.mybest.viewmodel.HistoryViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    HistoryContent(
        state = state,
        onBackClick = onBackClick,
        onMonthChange = viewModel::onMonthChange,
        onDateSelected = viewModel::onDateSelected,
        onDateDismiss = viewModel::onDateDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    state: HistoryState,
    onBackClick: () -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDateDismiss: () -> Unit
) {
    val selectedDate = state.selectedDate
    if (selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = { onDateDismiss() },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            DayDetailContent(
                date = selectedDate,
                summary = state.monthlyData[selectedDate],
                onClose = { onDateDismiss() }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Progreso") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthSelector(
                currentMonth = state.selectedMonth,
                onMonthChange = onMonthChange
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                DayOfWeek.entries.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-ES")).uppercase(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CalendarGrid(
                yearMonth = state.selectedMonth,
                onDateClick = onDateSelected,
                dayData = state.monthlyData
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
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }
        
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES"))).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    dayData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value
    val startOffset = firstDayOfMonth - 1
    
    val totalCells = daysInMonth + startOffset
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    onClick = { onDateClick(date) }
                )
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    summary: DaySummary?,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    
    Column(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(6.dp)
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
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.forLanguageTag("es-ES"))).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
            }
        }

        if (summary == null || (!summary.hasWeight && !summary.hasHabit && !summary.hasPhoto)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin actividad registrada",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            summary.weight?.let { weight ->
                DetailItem(
                    icon = Icons.Rounded.MonitorWeight,
                    color = MaterialTheme.colorScheme.primary,
                    title = "Peso: ${weight.weight} kg",
                    subtitle = weight.note
                )
            }

            summary.habit?.let { habit ->
                DetailItem(
                    icon = Icons.Rounded.CheckCircle,
                    color = MaterialTheme.colorScheme.secondary,
                    title = "Hábitos",
                    content = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (habit.ateHealthy) Chip("Comida Sana")
                            if (habit.didExercise) Chip("Ejercicio")
                        }
                        habit.notes?.let { 
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) 
                        }
                    }
                )
            }

            if (summary.photos.isNotEmpty()) {
                Text("Fotos del día", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(summary.photos) { photo ->
                        AsyncImage(
                            model = photo.photoPath,
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    subtitle: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content?.invoke()
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    
    val sampleMonthlyData = mapOf(
        today to DaySummary(
            date = today,
            weight = DailyWeightEntity(date = today, weight = 75.5f, note = "Post entrenamiento"),
            habit = DailyHabitEntity(date = today, ateHealthy = true, didExercise = true, notes = "Buen día"),
            photos = listOf(
                ProgressPhotoEntity(date = today, type = PhotoType.BODY, photoPath = ""),
                ProgressPhotoEntity(date = today, type = PhotoType.ABDOMEN, photoPath = "")
            )
        ),
        today.minusDays(1) to DaySummary(
            date = today.minusDays(1),
            weight = DailyWeightEntity(date = today.minusDays(1), weight = 76.0f),
            habit = DailyHabitEntity(date = today.minusDays(1), ateHealthy = false, didExercise = true)
        )
    )
    
    val state = HistoryState(
        selectedMonth = currentMonth,
        monthlyData = sampleMonthlyData
    )
    
    MyBestTheme {
        HistoryContent(
            state = state,
            onBackClick = {},
            onMonthChange = {},
            onDateSelected = {},
            onDateDismiss = {}
        )
    }
}
