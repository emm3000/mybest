package com.emm.mybest.features.exercise.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.emm.mybest.features.exercise.presentation.components.ExerciseDayRow
import com.emm.mybest.features.exercise.presentation.components.ExerciseEditorSheet
import com.emm.mybest.ui.components.HSeparator
import com.emm.mybest.ui.components.HTopBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DayOfWeek

@Composable
fun ExercisePlanScreen(
    viewModel: ExercisePlanViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                ExercisePlanEffect.DismissSheet -> Unit
            }
        }
    }

    ExercisePlanContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun ExercisePlanContent(
    state: ExercisePlanState,
    onIntent: (ExercisePlanIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        topBar = {
            HTopBar(title = "MY ROUTINE")
        },
    ) { padding ->
        val days = DayOfWeek.entries

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(padding),
            contentPadding = padding,
        ) {
            itemsIndexed(
                items = days,
                key = { _, day -> day.name },
            ) { index, day ->
                val routine = state.routines[day].orEmpty()
                ExerciseDayRow(
                    day = day,
                    routine = routine,
                    onClick = { onIntent(ExercisePlanIntent.StartEdit(day)) },
                )
                if (index < days.lastIndex) {
                    HSeparator()
                }
            }
        }
    }

    state.editing?.let { editing ->
        ExerciseEditorSheet(
            editing = editing,
            onIntent = onIntent,
        )
    }
}
