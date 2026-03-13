package com.emm.mybest.features.habit.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.narrowEs
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.HSelect
import com.emm.mybest.ui.components.HSelectableCard
import com.emm.mybest.ui.components.HTopBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DayOfWeek

private const val ADD_HABIT_TOTAL_STEPS = 3
private const val ADD_HABIT_ICON_GRID_COLUMNS = 4
private val habitTypeDescriptions = mapOf(
    HabitType.BOOLEAN to "Sí/No: marca si lo completaste hoy. Ejemplo: Meditar.",
    HabitType.TIME to "Tiempo: define minutos diarios. Ejemplo: Leer 30 minutos.",
    HabitType.METRIC to "Métrica: registra una cantidad objetivo. Ejemplo: Beber 8 vasos.",
)
private val habitGoalExamples = mapOf(
    HabitType.TIME to "Ejemplo recomendado: 20 a 45 minutos por día.",
    HabitType.METRIC to "Ejemplo recomendado: 8 vasos o 2 litros por día.",
)

private data class HabitIconOption(
    val value: String,
    val label: String,
    val icon: ImageVector,
)

private val addHabitIconOptions = listOf(
    HabitIconOption("FitnessCenter", "Entrenamiento", Icons.Rounded.FitnessCenter),
    HabitIconOption("Restaurant", "Alimentacion", Icons.Rounded.Restaurant),
    HabitIconOption("WaterDrop", "Hidratacion", Icons.Rounded.WaterDrop),
    HabitIconOption("SelfImprovement", "Bienestar", Icons.Rounded.SelfImprovement),
)

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
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HTopBar(
                title = "Nuevo Hábito",
                navigationIcon = {
                    HIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = if (state.step > 1) {
                            { onIntent(AddHabitIntent.OnPreviousStep) }
                        } else {
                            onBackClick
                        },
                    )
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
            Text(
                text = "Paso ${state.step} de $ADD_HABIT_TOTAL_STEPS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AddHabitStepIndicator(currentStep = state.step)
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
private fun AddHabitStepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(ADD_HABIT_TOTAL_STEPS) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        color = if (index < currentStep) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.small,
                    ),
            )
        }
    }
}

@Composable
private fun StepOne(
    state: AddHabitState,
    onIntent: (AddHabitIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
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
            text = "Icono (opcional)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Elige el que mejor represente el habito. Puedes cambiarlo mas adelante.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(ADD_HABIT_ICON_GRID_COLUMNS),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(addHabitIconOptions) { option ->
                val isSelected = state.icon == option.value
                IconCard(
                    icon = option.icon,
                    label = option.label,
                    isSelected = isSelected,
                    onClick = { onIntent(AddHabitIntent.OnIconChange(option.value)) },
                )
            }
        }
    }
}

@Composable
private fun IconCard(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    HSelectableCard(
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .semantics {
                contentDescription = "Icono $label"
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
    val typeDescription = habitTypeDescriptions.getValue(state.type)
    val goalExample = habitGoalExamples[state.type]

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = "¿Cómo medirás tu progreso?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = typeDescription,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                placeholder = if (state.type == HabitType.TIME) "Ej: 30" else "Ej: 8",
                supportingText = goalExample,
                errorMessage = state.goalError,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.type == HabitType.METRIC) {
                HInput(
                    value = state.unit,
                    onValueChange = { onIntent(AddHabitIntent.OnUnitChange(it)) },
                    label = "Unidad",
                    placeholder = "Ej: vasos, km, paginas",
                    errorMessage = state.unitError,
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
    HSelectableCard(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier.height(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
        state.scheduledDaysError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Text(
            text = "Programacion semanal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Los dias seleccionados definen cuando aparecera este habito en " +
                    "tu seguimiento diario. Los recordatorios automaticos aun no estan disponibles.",
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
    HSelectableCard(
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .semantics {
                contentDescription = day.accessibilityLabelEs()
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day.narrowEs(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

private fun DayOfWeek.accessibilityLabelEs(): String = when (this) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miercoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sabado"
    DayOfWeek.SUNDAY -> "Domingo"
}
