package com.emm.mybest.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.viewmodel.HomeState
import com.emm.mybest.viewmodel.HomeViewModel
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddWeightClick: () -> Unit,
    onAddHabitClick: () -> Unit,
    onAddPhotoClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onViewInsightsClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    HomeScreenContent(
        state = state,
        onAddWeightClick = onAddWeightClick,
        onAddHabitClick = onAddHabitClick,
        onAddPhotoClick = onAddPhotoClick,
        onViewHistoryClick = onViewHistoryClick,
        onViewInsightsClick = onViewInsightsClick
    )
}

@Composable
internal fun HomeScreenContent(
    state: HomeState,
    onAddWeightClick: () -> Unit,
    onAddHabitClick: () -> Unit,
    onAddPhotoClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onViewInsightsClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                HomeHeader()
            }
            
            item {
                SummaryCard(state, onClick = onViewInsightsClick)
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registro Diario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        TextButton(onClick = onViewInsightsClick) {
                            Text("Estadísticas")
                        }
                        TextButton(onClick = onViewHistoryClick) {
                            Text("Historial")
                        }
                    }
                }
            }
            
            item {
                QuickActionCard(
                    title = "Registrar Peso",
                    subtitle = state.lastWeight?.let { "Último: $it kg" } ?: "Sigue tu evolución física",
                    icon = Icons.Rounded.MonitorWeight,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onAddWeightClick
                )
            }
            
            item {
                QuickActionCard(
                    title = "Hábitos de Hoy",
                    subtitle = if (state.habitsCompletedToday > 0) "¡Ya has registrado hoy!" else "¿Qué tal tu alimentación?",
                    icon = Icons.Rounded.CheckCircle,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = onAddHabitClick
                )
            }
            
            item {
                QuickActionCard(
                    title = "Foto de Progreso",
                    subtitle = "Total: ${state.totalPhotos} fotos",
                    icon = Icons.Rounded.AddAPhoto,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onAddPhotoClick
                )
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Mi Mejor Versión",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Hoy es ${LocalDate.now()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Perfil",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun SummaryCard(state: HomeState, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = "Tu Progreso",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (state.isLoading) {
                        "Cargando..."
                    } else if (state.totalWeightLost > 0) {
                        "¡Has bajado ${String.format("%.1f", state.totalWeightLost)} kg!"
                    } else {
                        "¡Vas muy bien!"
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toca para ver tus estadísticas",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                Icons.Rounded.BarChart,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(24.dp)
                    .size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = iconColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(14.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyBestTheme {
        HomeScreenContent(
            state = HomeState(
                lastWeight = 75.5f,
                habitsCompletedToday = 1,
                totalPhotos = 12,
                isLoading = false
            ),
            onAddWeightClick = {},
            onAddHabitClick = {},
            onAddPhotoClick = {},
            onViewHistoryClick = {},
            onViewInsightsClick = {}
        )
    }
}
