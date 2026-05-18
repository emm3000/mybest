package com.emm.mybest.features.diet.presentation

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
import com.emm.mybest.features.diet.presentation.components.DaySection
import com.emm.mybest.features.diet.presentation.components.MealEditorSheet
import com.emm.mybest.ui.components.HSeparator
import com.emm.mybest.ui.components.HTopBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DayOfWeek

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                MealPlanEffect.DismissSheet -> Unit
            }
        }
    }

    MealPlanContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun MealPlanContent(
    state: MealPlanState,
    onIntent: (MealPlanIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        topBar = {
            HTopBar(title = "My Diet")
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
                val meals = state.entries[day] ?: emptyMap()
                DaySection(
                    day = day,
                    meals = meals,
                    onMealClick = { type ->
                        onIntent(MealPlanIntent.StartEdit(day, type))
                    },
                )
                if (index < days.lastIndex) {
                    HSeparator()
                }
            }
        }
    }

    state.editing?.let { editing ->
        MealEditorSheet(
            editing = editing,
            onIntent = onIntent,
        )
    }
}
