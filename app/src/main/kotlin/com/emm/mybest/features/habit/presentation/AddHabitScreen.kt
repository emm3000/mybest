package com.emm.mybest.features.habit.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.narrowEs
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.HSelect
import com.emm.mybest.ui.components.HTopBar
import com.emm.mybest.ui.theme.MyBestTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DayOfWeek

private const val ADD_HABIT_TOTAL_STEPS = 3
private const val ADD_HABIT_ICON_GRID_COLUMNS = 4

@Composable
fun AddHabitScreen(
    viewModel: AddHabitViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnBackClick by rememberUpdatedState(onBackClick)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AddHabitEffect.NavigateBack -> currentOnBackClick()
                is AddHabitEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    AddHabitContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun AddHabitContent(
    state: AddHabitState,
    onIntent: (AddHabitIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HTopBar(
                title = "Nuevo Hábito",
                navigationIcon = {
                    IconButton(
                        onClick = if (state.step > 1) {
                            { onIntent(AddHabitIntent.OnPreviousStep) }
                        } else {
                            onBackClick
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
        ) {
            // Step indicator
            Text(
                text = "Paso ${state.step} de $ADD_HABIT_TOTAL_STEPS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.step,
                label = "WizardStep",
                modifier = Modifier.weight(1f),
            ) { step ->
                when (step) {
                    1 -> StepOne(state, onIntent)
                    2 -> StepTwo(state, onIntent)
                    ADD_HABIT_TOTAL_STEPS -> StepThree(state, onIntent)
                }
            }

            HButton(
                text = if (state.step < ADD_HABIT_TOTAL_STEPS) "Continuar" else "Crear Hábito",
                onClick = {
                    if (state.step < ADD_HABIT_TOTAL_STEPS) {
                        onIntent(AddHabitIntent.OnNextStep)
                    } else {
                        onIntent(AddHabitIntent.OnSaveClick)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                isLoading = state.isLoading,
            )
        }
    }
}

@Composable
private fun StepOne(
    state: AddHabitState,
    onIntent: (AddHabitIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        HInput(
            value = state.name,
            onValueChange = { onIntent(AddHabitIntent.OnNameChange(it)) },
            label = "Nombre del hábito",
            placeholder = "Ej: Beber agua",
            errorMessage = state.nameError,
            modifier = Modifier.fillMaxWidth(),
        )

        HSelect(
            items = listOf("Salud", "Deporte", "Mente", "Productividad"),
            selectedItem = state.category,
            onItemSelect = { onIntent(AddHabitIntent.OnCategoryChange(it)) },
            label = "Categoría",
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Elige un icono",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )

        val icons = listOf(
            Icons.Rounded.FitnessCenter,
            Icons.Rounded.Restaurant,
            Icons.Rounded.WaterDrop,
            Icons.Rounded.SelfImprovement,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(ADD_HABIT_ICON_GRID_COLUMNS),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(icons) { icon ->
                val isSelected = state.icon == icon.name // Simplified for demonstration
                IconCard(
                    icon = icon,
                    isSelected = isSelected,
                    onClick = { onIntent(AddHabitIntent.OnIconChange(icon.name)) },
                )
            }
        }
    }
}

@Composable
private fun IconCard(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(64.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isSelected) {
                Icon(
                    Icons.Rounded.Check,
                    null,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StepTwo(
    state: AddHabitState,
    onIntent: (AddHabitIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = "¿Cómo medirás tu progreso?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val types = listOf(
                "Si/No" to HabitType.BOOLEAN,
                "Tiempo" to HabitType.TIME,
                "Métrica" to HabitType.METRIC,
            )
            types.forEach { (label, type) ->
                TypeCard(
                    label = label,
                    isSelected = state.type == type,
                    onClick = { onIntent(AddHabitIntent.OnTypeChange(type)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (state.type != HabitType.BOOLEAN) {
            HInput(
                value = state.goalValue,
                onValueChange = { onIntent(AddHabitIntent.OnGoalValueChange(it)) },
                label = if (state.type == HabitType.TIME) "Minutos al día" else "Valor objetivo",
                placeholder = "Ej: 30",
                errorMessage = state.goalError,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.type == HabitType.METRIC) {
                HInput(
                    value = state.unit,
                    onValueChange = { onIntent(AddHabitIntent.OnUnitChange(it)) },
                    label = "Unidad",
                    placeholder = "Ej: Vasos, Litros",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TypeCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.height(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun StepThree(
    state: AddHabitState,
    onIntent: (AddHabitIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = "¿Qué días realizarás este hábito?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        val days = DayOfWeek.entries
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            days.forEach { day ->
                val isSelected = state.scheduledDays.contains(day)
                DayChip(
                    day = day,
                    isSelected = isSelected,
                    onClick = { onIntent(AddHabitIntent.OnDayToggle(day)) },
                )
            }
        }

        // Reminder section (placeholder)
        Text(
            text = "Recordatorios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Se te notificará para completar el hábito en los días seleccionados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun DayChip(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day.narrowEs(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddHabitScreenPreview() {
    MyBestTheme {
        AddHabitContent(
            state = AddHabitState(),
            onIntent = {},
            onBackClick = {},
        )
    }
}
